package Service;

import DAO.DescriptionDAOImp;
import DAO.ItemDAOImp;
import Model.ItemModel;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static Config.BotConfig.properties;

public class TelegramService {
    public Map<String, BufferedImage> downloadedImages = new ConcurrentHashMap<>();
    private final WallapopService wallaService = new WallapopService();
    private final ItemDAOImp itemImp = new ItemDAOImp();
    @Getter
    private final List<ItemModel> itemModels = Collections.synchronizedList(new ArrayList<>());
    private volatile List<ItemModel> uploadedItemModels = new CopyOnWriteArrayList<>();
    private volatile List<ItemModel> nonUploadedItemModels = new CopyOnWriteArrayList<>();
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

        CompletableFuture<Void> loadFilesFuture = loadFiles();
        CompletableFuture<Void> loadImagesFuture = loadImages();

        // print loading progress
        Thread loadingIndicator = loadingIndicator();

        // wait until results are loaded
        CompletableFuture.allOf(loadFilesFuture, loadImagesFuture).thenRun(() -> {

            loadingIndicator.interrupt();

            // when both methods are finished print results
            System.out.println("\nArchivos cargados:");
            System.out.println("Items subidos: " + uploadedItemModels.size());
            System.out.println("Items no subidos: " + nonUploadedItemModels.size());
            System.out.println("Imagenes descargadas: " + downloadedImages.size());
        });

        this.bot = bot;

        // check description file
        java.io.File file = new java.io.File("src/main/resources/description.txt");
        String descriptionPath;
        if (file.exists()) {
            descriptionPath = "src/main/resources/description.txt";
        } else {
            descriptionPath = "description.txt";
        }


        // extract description suffix from file
        DescriptionDAOImp descriptionDAOImp = new DescriptionDAOImp();
        descriptionSuffix = "\n" + descriptionDAOImp.getDescription(descriptionPath);
    }

    // print . every 0.5 seconds
    private static Thread loadingIndicator() {
        Thread loadingIndicator = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.print(".");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // start loading indicator
        loadingIndicator.start();
        return loadingIndicator;
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
        } else return "Escribe el numero de los archivos que quieres que se envien dejando un espacio entre ellos";
    }

    //search for non uploaded items and upload them if they are found
    public String scanNonUploadedItems() {
        //if there are non uploaded items, upload them
        if (nonUploadedItemModels.isEmpty()) {
            return "Archivos sin subir no detectados";
        } else {
            //Thread thread = new Thread(() -> publishItems(nonUploadedItemModels));
            //thread.start();
            publishItems(nonUploadedItemModels);
            return "Archivos sin subir detectados, procediendo a subirlos";
        }
    }

    //publish items
    public void publishItems(List<ItemModel> itemModels) {
        wallaService.publishItems(itemModels);
        itemModels.clear();
    }

    //verify article info
    public String processInfo(String message, String saleType) {
        if (saleType.equals("full")) {
            if (message.contains("Titulo:") && message.contains("Descripcion:")) {
                //extract title and description from message
                title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
                String description = message.substring(message.indexOf("Descripcion:") + 12).trim() + System.lineSeparator() + descriptionSuffix;
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
                    itemModels.add(new ItemModel(file));
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
                    itemModels.getLast().addPath(imagePath);
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
                .thenApply(result -> result);
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
        if (itemModels.isEmpty()) {
            return "Se ha cancelado el proceso de venta";
        } else {
            java.io.File file;
            for (ItemModel itemModel : itemModels) {
                file = new java.io.File(downloadPath + "\\" + itemImp.readInfoFile(itemModel.getInfoFile())[0]);
                if (!itemImp.deleteDirectory(file)) return "Error al borrar los articulos";
            }
            int size = itemModels.size();
            itemModels.clear();
            return "Se han eliminado " + size + " articulos, vuelve a iniciar proceso de venta";
        }
    }

    //finish processing items
    public String finishSale(String status) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            uploadedItemModels.addAll(itemModels);
            int size = itemModels.size();
            //Thread thread = new Thread(() -> publishItems(itemModels));
            //thread.start();
            publishItems(itemModels);
            return "Se van a subir " + size + " items";
        } else if (status.equals("imagesNotUploaded")) {
            return "No se han enviado imagenes";
        } else return "Comando incorrecto";
    }

    //
    public String saveItems() {
        itemModels.clear();
        return "Articulos guardados, se subiran cuando reinicies el programa";
    }

    // send to client all uploaded items
    public String showUploadedItems() {
        int itemCounter = 1;
        StringBuilder response = new StringBuilder(uploadedItemModels.size() + " articulos para resubir:\n");
        for (ItemModel itemModel : uploadedItemModels) {
            response.append(itemCounter).append(" ").append(itemImp.readInfoFile(itemModel.getInfoFile())[0]).append("\n");
            itemCounter++;
        }
        return response.toString().trim();
    }

    //method to upload again items
    public String reuploadItems(String itemNumbers) {
        String[] itemPositions = itemNumbers.split(" ");
        //get selected items
        for (String itemPosition : itemPositions) {
            try {
                itemModels.add(uploadedItemModels.get(Integer.parseInt(itemPosition) - 1));
            } catch (NumberFormatException e) {
                return "Error de formato al escribir los numeros";
            }
        }
        //upload selected items
        //Thread thread = new Thread(() -> publishItems(itemModels));
        //thread.start();
        publishItems(itemModels);
        return "Se van a resubir " + itemPositions.length + " articulos";
    }

    //method for ignored messages
    public String ignoreMessage(int messageCount) {
        return "Se ha ingnorado el menssaje numero: " + messageCount;
    }


    // preload files
    public CompletableFuture<Void> loadFiles() {
        return CompletableFuture.runAsync(() -> {
            ImmutablePair<List<ItemModel>, List<ItemModel>> loadedItems = itemImp.loadFiles(downloadPath);
            uploadedItemModels = loadedItems.getLeft();
            nonUploadedItemModels = loadedItems.getRight();
        });
    }

    // preload images
    public CompletableFuture<Void> loadImages() {
        return CompletableFuture.runAsync(() -> downloadedImages = itemImp.loadImages());
    }


}

