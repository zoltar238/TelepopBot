package Service;

import DAO.ItemDAOImp;
import Model.Item;
import Util.ImageProcessor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Config.BotConfig.properties;

public class TelegramService {
    private final WallapopService wallaService = new WallapopService();
    private final ItemDAOImp itemImp = new ItemDAOImp();
    private final ArrayList<Item> items = new ArrayList<>();
    private final ArrayList<Item> uploadedItems = new ArrayList<>();
    private final ArrayList<Item> nonUploadedItems = new ArrayList<>();
    private final java.io.File downloadPath = new java.io.File(properties.getProperty("DownloadPath"));
    private String title;
    private int imageCounter = 1;
    private final TelegramLongPollingBot bot;
    private String descriptionSuffix = "";
    // implement executor service for background image comparing
    private final ExecutorService executor = Executors.newFixedThreadPool(6);


    // Constructor
    public TelegramService(TelegramLongPollingBot bot) {
        loadFiles();
        this.bot = bot;

        // Extract description suffix from file
        InputStream in = getClass().getClassLoader().getResourceAsStream("description.txt");

        try {
            // Use BufferedReader based on where the file is located
            BufferedReader br = in != null ?
                    new BufferedReader(new InputStreamReader(in)) :
                    new BufferedReader(new FileReader("src/main/resources/description.txt"));

            String line;
            while ((line = br.readLine()) != null) {
                descriptionSuffix += "\n" + line.trim();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading description.txt", e);
        }
    }


    //sale start
    public void startSale(Update update, String type) {
        if (type.equals("full")) {
            sendResponse(update, """
                    Escribe título y descripción siguiendo el siguiente formato:
                     Titulo:
                     Descripcion:""");
        } else if (type.equals("title")) {
            sendResponse(update, "Escribe el título");
        }
    }

    //search for non uploaded items and upload them if they are found
    public Boolean scanNonUploadedItems(Update update) {
        sendResponse(update, "Escaneando articulos sin subir");
        //if there are non uploaded items, upload them
        if (nonUploadedItems.isEmpty()) {
            sendResponse(update, "Archivos sin subir no detectados");
            return false;
        } else {
            sendResponse(update, "Archivos sin subir detectados, procediendo a subirlos");
            wallaService.startSale(nonUploadedItems);
            nonUploadedItems.clear();
            return true;
        }
    }

    //sale already started
    public void saleAlreadyStarted(Update update) {
        sendResponse(update, "Proceso de venta ya en curso");
    }

    //verify article info
    public Boolean processInfo(Update update, String message, String saleType) {
        if (saleType.equals("full")) {
            if (message.contains("Titulo:") && message.contains("Descripcion:")) {
                //extract title and description from message
                //todo: change spaces for _ in file name
                title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
                String description = message.substring(message.indexOf("Descripcion:") + 12).trim() + descriptionSuffix;
                //create new directory for the item
                return createFile(update, description);
            } else {
                sendResponse(update, "Formato incorrecto");
                return false;
            }
        } else if (saleType.equals("title")) {
            //extract title and description from title only
            title = message.trim();
            String description = title + descriptionSuffix;
            //create new directory for the item
            return createFile(update, description);
        }
        return null;
    }

    //method to create new item folder and info file
    private Boolean createFile(Update update, String description) {
        java.io.File directory = new java.io.File(downloadPath.getAbsolutePath() + "\\" + title);
        if (!directory.exists()) {
            //verify if description size is correct
            if (description.length() > 640) {
                int lengthDiff = description.length() - 640;
                sendResponse(update, "La descripcion supera en " + lengthDiff + " los caracteres permitidos");
                return false;
            } else {
                directory.mkdir();
                java.io.File file = new java.io.File(directory.getPath() + "\\" + title + ".txt");
                itemImp.writeInfoFile(file, title, description);
                items.add(new Item(file));
                sendResponse(update, "Información recibida, envía imagen/es.");
                return true;
            }
        } else {
            sendResponse(update, "Ya tienes un articulo con el mismo titulo");
            return false;
        }
    }

    // verify images
    public void processImages(Update update, String status) {
        if (status.equals("correctInfo")) {
            var photos = update.getMessage().getPhoto();
            var photo = photos.getLast();
            var fileId = photo.getFileId();
            try {
                if (imageCounter <= 10) {
                    // get image URL
                    GetFile getFileMethod = new GetFile(fileId);
                    File telegramFile = bot.execute(getFileMethod);
                    String fileUrl = "https://api.telegram.org/file/bot" + bot.getBotToken() + "/" + telegramFile.getFilePath();

                    // download image
                    String path = downloadPath + "\\" + title + "\\" + title + imageCounter + ".jpg";
                    // add image to
                    items.getLast().addPath(path);
                    downloadImage(fileUrl, path);
                    sendResponse(update, "Imagen " + imageCounter + " descargada correctamente");
                    imageCounter++;
                    //compare image
                    ImageProcessor comp = new ImageProcessor();

                    // launch a thread to compare images
                    CompletableFuture.supplyAsync(() -> comp.compare(path), executor).thenAccept(result -> {
                        if (!result.equals("noCoincidence")) {
                            sendResponse(update, result);
                        }
                    });
                } else {
                    sendResponse(update, "Imagen no descargada, se ha superado el máximo de 10");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(update, "Hubo un problema al descargar la imagen. Inténtalo nuevamente.");
            }
        } else if (status.equals("incorrectInfo")) {
            sendResponse(update, "Todavia no has añadido informacion");
        } else {
            sendResponse(update, "Añade imagenes");
        }
    }

    // process next item
    public void nextItem(Update update, String status, String saleType) {
        if (status.equals("imagesUploaded")) {
            if (saleType.equals("full")) {
                sendResponse(update, """
                        Añadre otro articulo:
                        Titulo:
                        Descripcion:""");
            } else if (saleType.equals("title")) {
                sendResponse(update, "Escribe el titulo:");
            }
            imageCounter = 1;
        } else if (status.equals("imagesNotUploaded")) {
            sendResponse(update, "No se han enviado imagenes");
        }
    }

    //cancel sale
    public void cancelSale(Update update) {
        if (items.isEmpty()) {
            sendResponse(update, "Se ha cancelado el proceso de venta");
        } else {
            java.io.File file;
            for (Item item : items) {
                file = new java.io.File(downloadPath + "\\" + itemImp.readInfoFile(item.getInfoFile())[0]);
                deleteDirectory(file);
            }
            sendResponse(update, "Se han eliminado " + items.size() + " articulos, vuelve a iniciar proceso de venta");
            items.clear();
        }
    }

    //finish processing items
    public void finishSale(Update update, String status) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            sendResponse(update, "Correcto, se van a subir " + items.size() + " articulos");
            wallaService.startSale(items);
            //add all new items to the uploaded list
            uploadedItems.addAll(items);
            items.clear();
        } else if (status.equals("imagesNotUploaded")) {
            sendResponse(update, "No se han enviado imagenes");
        }
    }

    //
    public void saveItems(Update update) {
        sendResponse(update, "Articulos guardados, se subiran cuando reinicies el programa");
        items.clear();
    }

    // send to client all uploaded items
    public void showUploadedItems(Update update) {
        int itemCounter = 1;
        StringBuilder response = new StringBuilder(uploadedItems.size() + " articulos para resubir:\n");
        for (Item item : uploadedItems) {
            response.append(itemCounter).append(" ").append(itemImp.readInfoFile(item.getInfoFile())[0]).append("\n");
            itemCounter++;
        }
        response.append("Escribe el numero de los archivos que quieres que se envien dejando un espacio entre ellos");
        sendResponse(update, response.toString().trim());
    }

    //method to upload again items
    public void reuploadItems(Update update, String itemNumbers) {
        String[] itemPositions = itemNumbers.split(" ");
        //get selected items
        for (String itemPosition : itemPositions) {
            try {
                items.add(uploadedItems.get(Integer.parseInt(itemPosition) - 1));
            } catch (NumberFormatException e) {
                sendResponse(update, "Error de formato al escribir los numeros");
                return;
            }
        }
        sendResponse(update, "Se van a resubir " + itemPositions.length + " articulos");
        //upload selected items
        wallaService.startSale(items);
        items.clear();
    }

    // respond to client
    public void sendResponse(Update update, String message) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText(message);
        try {
            bot.execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //download images
    private void downloadImage(String fileUrl, String localPath) throws IOException {
        URL url = new URL(fileUrl);
        InputStream in = url.openStream();
        OutputStream out = new FileOutputStream(localPath);

        byte[] buffer = new byte[2048];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }

    //method for closing the program
    public void endProgram(Update update) {
        sendResponse(update, "Cerrando el programa");
        System.exit(0);
    }

    //method for ignored messages
    public void ignoreMessage(Update update, int messageCount) {
        sendResponse(update, "Se ha ingnorado el menssaje numero: " + messageCount);
    }

    //preload previously upload files
    public void loadFiles() {
        String[] items = downloadPath.list();
        //if item list is not empty check for uploaded items
        if (items != null) {
            for (String item : items) {
                String pathnameInfoFile = downloadPath + "\\" + item + "\\" + item + ".txt";
                String status = itemImp.readStatus(new java.io.File(pathnameInfoFile));
                String[] files = Objects.requireNonNull(new java.io.File(downloadPath + "\\" + item).list());
                String[] uploadedImages = Arrays.copyOfRange(files, 1, files.length);
                //categorize into uploaded and nonUploaded
                if (status.equals("subido")) {
                    // get all files inside directory except for the first
                    // add absolute path
                    for (int i = 0; i < uploadedImages.length; i++) {
                        uploadedImages[i] = downloadPath + "\\" + item + "\\" + uploadedImages[i];
                    }
                    uploadedItems.add(new Item(new java.io.File(pathnameInfoFile), new ArrayList<>(Arrays.asList(uploadedImages))));
                } else {
                    // get all files inside directory except for the first
                    // add absolute path
                    for (int i = 0; i < uploadedImages.length; i++) {
                        uploadedImages[i] = downloadPath + "\\" + item + "\\" + uploadedImages[i];
                    }
                    nonUploadedItems.add(new Item(new java.io.File(pathnameInfoFile), new ArrayList<>(Arrays.asList(uploadedImages))));
                }
            }
        }
    }

    //delete all files inside directory
    public void deleteDirectory(java.io.File directory) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
