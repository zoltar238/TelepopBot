package Service;

import DAO.ItemDAOImp;
import Model.Item;
import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Config.BotConfig.properties;

public class TelegramService {
    public Map<String, BufferedImage> downloadedImages = new ConcurrentHashMap<>();
    private final WallapopService wallaService = new WallapopService();
    private final ItemDAOImp itemImp = new ItemDAOImp();
    private final List<Item> items = Collections.synchronizedList(new ArrayList<>());
    private volatile List<Item> uploadedItems = Collections.synchronizedList(new ArrayList<>());
    private volatile List<Item> nonUploadedItems = Collections.synchronizedList(new ArrayList<>());
    private final java.io.File downloadPath = new java.io.File(properties.getProperty("DownloadPath"));
    private String title;
    private volatile String imagePath;
    private int imageCounter = 1;
    private final TelegramLongPollingBot bot;
    private final String descriptionSuffix;
    // implement executor service for background image comparing
    private final ExecutorService executor = Executors.newFixedThreadPool(6);


    // Constructor
    public TelegramService(TelegramLongPollingBot bot) {
        //load files
        loadFiles();

        //load images
        loadImages();

        this.bot = bot;

        // extract description suffix from file
        try {
            // Read all lines from file
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/description.txt"));

            // Write all lines to string
            descriptionSuffix = String.join("\n", lines);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //sale start
    public String startSale(String type) {
        if (type.equals("full")) {
            return """
                    Escribe título y descripción siguiendo el siguiente formato:
                     Titulo:
                     Descripcion:""";
        } else if (type.equals("title")) {
            return "Escribe el titulo";
        } else return "Opción incorrecta";
    }

    //search for non uploaded items and upload them if they are found
    public String scanNonUploadedItems() {
        //if there are non uploaded items, upload them
        if (nonUploadedItems.isEmpty()) {
            return "Archivos sin subir no detectados";
        } else {
            Thread thread = new Thread(() -> publishItems(nonUploadedItems));
            thread.start();
            return "Archivos sin subir detectados, procediendo a subirlos";
        }
    }

    //publish items
    public void publishItems(List<Item> items){
        wallaService.publishItems(items);
        items.clear();
    }

    //verify article info
    public String processInfo(String message, String saleType) {
        if (saleType.equals("full")) {
            if (message.contains("Titulo:") && message.contains("Descripcion:")) {
                //extract title and description from message
                title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
                String description = message.substring(message.indexOf("Descripcion:") + 12).trim() + descriptionSuffix;
                //create new directory for the item
                return createFile(description);
            } else {
                return "Formato incorrecto";
            }
        } else if (saleType.equals("title")) {
            //extract title and description from title only
            title = message.trim();
            String description = title + descriptionSuffix;
            //create new directory for the item
            return createFile(description);
        }
        return null;
    }

    //method to create new item folder and info file
    //todo:move to DAO
    private String createFile(String description) {
        java.io.File directory = new java.io.File(downloadPath.getAbsolutePath() + "\\" + title);
        if (!directory.exists()) {
            //verify if description size is correct
            if (description.length() > 640) {
                int lengthDiff = description.length() - 640;
                return "La descripcion supera en " + lengthDiff + " los caracteres permitidos";
            } else {
                if (directory.mkdir()) {
                    java.io.File file = new java.io.File(directory.getPath() + "\\" + title + ".txt");
                    itemImp.writeInfoFile(file, title, description);
                    items.add(new Item(file));
                    return "Información recibida, envía imagen/es";
                } else return "Error al crear la carpeta";
            }
        } else {
            return "Ya tienes un articulo con el mismo titulo";
        }
    }

    // processImagnes
    public String processImage(Update update, String botToken, String status) {
        if (status.equals("correctInfo")) {
            var photos = update.getMessage().getPhoto();
            var photo = photos.getLast();
            var fileId = photo.getFileId();
            try {
                if (imageCounter <= 10) {
                    // get image URL
                    GetFile getFileMethod = new GetFile(fileId);
                    File telegramFile = bot.execute(getFileMethod);
                    String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + telegramFile.getFilePath();

                    // download image
                    imagePath = downloadPath + "\\" + title + "\\" + title + imageCounter + ".jpg";
                    // add image to
                    items.getLast().addPath(imagePath);
                    imageCounter++;
                    return itemImp.downloadImage(fileUrl, imagePath, imageCounter - 1);
                } else {
                    return "Imagen no descargada, se ha superado el máximo de 10";
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return "Hubo un problema al descargar la imagen. Inténtalo nuevamente";
            }
        } else if (status.equals("incorrectInfo")) {
            return "Todavia no has añadido informacion";
        } else {
            return "Añade imagenes";
        }
    }

    // Compare images
    public CompletableFuture<String> compareImage() {
        // launch thread to compare image
        return CompletableFuture.supplyAsync(() -> itemImp.compareImage(downloadedImages, imagePath), executor)
                .thenApply(result -> {
                    if (!result.equals("noCoincidence")) {
                        return result;
                    }
                    return null; // Return null if there is no coincidence
                });
    }

    // process next item
    public String nextItem(String status, String saleType) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            if (saleType.equals("full")) {
                return """
                        Añadre otro articulo:
                        Titulo:
                        Descripcion:""";
            } else if (saleType.equals("title")) {
                return "Escribe el titulo:";
            } else return "Comando incorrecto";
        } else if (status.equals("imagesNotUploaded")) {
            return "No se han enviado imagenes";
        } else return "Comando no correcto";
    }

    //cancel sale
    public String cancelSale() {
        if (items.isEmpty()) {
            return "Se ha cancelado el proceso de venta";
        } else {
            java.io.File file;
            for (Item item : items) {
                file = new java.io.File(downloadPath + "\\" + itemImp.readInfoFile(item.getInfoFile())[0]);
                if (!itemImp.deleteDirectory(file)) return "Error al borrar los articulos";
            }
            int size = items.size();
            items.clear();
            return "Se han eliminado " + size + " articulos, vuelve a iniciar proceso de venta";
        }
    }

    //finish processing items
    public String finishSale(String status) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            uploadedItems.addAll(items);
            int size = items.size();
            Thread thread = new Thread(() -> publishItems(items));
            thread.start();
            return "Correcto, se van a subir " + size + " articulos";
        } else if (status.equals("imagesNotUploaded")) {
            return "No se han enviado imagenes";
        } else return "Comando incorrecto";
    }

    //
    public String saveItems() {
        items.clear();
        return "Articulos guardados, se subiran cuando reinicies el programa";
    }

    // send to client all uploaded items
    public String showUploadedItems() {
        int itemCounter = 1;
        StringBuilder response = new StringBuilder(uploadedItems.size() + " articulos para resubir:\n");
        for (Item item : uploadedItems) {
            response.append(itemCounter).append(" ").append(itemImp.readInfoFile(item.getInfoFile())[0]).append("\n");
            itemCounter++;
        }
        response.append("Escribe el numero de los archivos que quieres que se envien dejando un espacio entre ellos");
        return response.toString().trim();
    }

    //method to upload again items
    public String reuploadItems(String itemNumbers) {
        String[] itemPositions = itemNumbers.split(" ");
        //get selected items
        for (String itemPosition : itemPositions) {
            try {
                items.add(uploadedItems.get(Integer.parseInt(itemPosition) - 1));
            } catch (NumberFormatException e) {
                return "Error de formato al escribir los numeros";
            }
        }
        //upload selected items
        Thread thread = new Thread(() -> publishItems(items));
        thread.start();
        return "Se van a resubir " + itemPositions.length + " articulos";
    }

    //method for ignored messages
    public String ignoreMessage(int messageCount) {
        return "Se ha ingnorado el menssaje numero: " + messageCount;
    }

    //preload previously upload files
    public synchronized void loadFiles() {
        Thread thread = new Thread(() -> {
            Pair<ArrayList<Item>, ArrayList<Item>> loadedItems = itemImp.loadFiles(downloadPath);
            uploadedItems = loadedItems.getLeft();
            nonUploadedItems = loadedItems.getRight();
        });
        thread.start();
    }

    //preload all images
    private synchronized void loadImages() {
        Thread thread = new Thread(() -> downloadedImages = itemImp.loadImages());
        thread.start();
    }

}
