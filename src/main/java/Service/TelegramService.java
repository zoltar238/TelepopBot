package Service;

import DAO.ItemDAOImplementation;
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
import java.util.concurrent.*;

import static Config.BotConfig.properties;

public class TelegramService {
    private final WallapopService wallaService = new WallapopService();
    private final ItemDAOImplementation itemImp = new ItemDAOImplementation();
    private final ArrayList<Item> items = new ArrayList<>();
    private final java.io.File downloadPath = new java.io.File(properties.getProperty("DownloadPath"));
    private String title;
    private int imageCounter = 1;
    private final TelegramLongPollingBot bot;
    private String descriptionSuffix = "";
    // implement executor service for background image comparing
    private final ExecutorService executor = Executors.newFixedThreadPool(6);


    // Constructor
    public TelegramService(TelegramLongPollingBot bot) {
        this.bot = bot;
        // extract description suffix from file
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/description.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                descriptionSuffix += "\n" + line.trim();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //sale start
    public void startSale(Update update) {
        sendResponse(update, """
                Escribe título y descripción siguiendo el siguiente formato:
                 Titulo: xxxx
                 Descripcion: xxxx""");
    }

    //search for non uploaded items and upload them if they are found
    public Boolean scanNonUploadedItems(Update update) {
        sendResponse(update, "Escaneando articulos sin subir");
        String[] items = downloadPath.list();
        ArrayList<Item> nonUploadedItems = new ArrayList<>();
        //if item list is not empty check for non uploaded items
        if (items != null) {
            for (String item : items) {
                String pathnameInfoFile = downloadPath + "\\" + item + "\\" + item + ".txt";
                String status = itemImp.readStatus(new java.io.File(pathnameInfoFile));
                if (status.equals("sinSubir")) {
                    // get all files inside directory except for the first
                    String[] files = Objects.requireNonNull(new java.io.File(downloadPath + "\\" + item).list());
                    String[] nonUploadedImages = Arrays.copyOfRange(files, 1, files.length);
                    // add absolute path
                    for (int i = 0; i < nonUploadedImages.length; i++) {
                        nonUploadedImages[i] = downloadPath + "\\" + item + "\\" + nonUploadedImages[i];
                    }
                    nonUploadedItems.add(new Item(new java.io.File(pathnameInfoFile), new ArrayList<>(Arrays.asList(nonUploadedImages))));
                }
            }
        }
        if (nonUploadedItems.isEmpty()) {
            sendResponse(update, "Archivos sin subir no detectados");
            return false;
        } else {
            sendResponse(update, "Archivos sin subir detectados, procediendo a subirlos");
            wallaService.startSale(nonUploadedItems);
            return true;
        }
    }

    //sale already started
    public void saleAlreadyStarted(Update update) {
        sendResponse(update, "Proceso de venta ya en curso");
    }

    //verify article info
    public Boolean processInfo(Update update, String message) {
        if (message.contains("Titulo:") && message.contains("Descripcion:")) {
            //extract title and description from message
            //todo: change spaces for _ in file name
            title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
            String description = message.substring(message.indexOf("Descripcion:") + 12).trim() + descriptionSuffix;
            //create new directory for the item
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
        } else {
            sendResponse(update, "Formato incorrecto");
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
                    if (!result.equals("noCoincidence")){
                        sendResponse(update, result);
                    }
                });

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
    public void nextItem(Update update, String status) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            sendResponse(update, """
                    Añadre otro articulo:
                    Titulo: xxxx
                    Descripcion: xxxx""");
        } else if (status.equals("imagesNotUploaded")) {
            sendResponse(update, "No se han enviado imagenes");
        }
    }

    //finish processing items
    public void finishSale(Update update, String status) {
        if (status.equals("imagesUploaded")) {
            imageCounter = 1;
            sendResponse(update, "Correcto, se van a subir " + items.size() + " articulos");
            wallaService.startSale(items);
            items.clear();
        } else if (status.equals("imagesNotUploaded")) {
            sendResponse(update, "No se han enviado imagenes");
        }
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
}
