package Service;

import entity.Item;
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

import static Config.BotConfig.properties;

public class ItemService {
    private final ArrayList<Item> items = new ArrayList<>();
    private String title;
    private final java.io.File downloadPath = new java.io.File(properties.getProperty("DownloadPath"));
    private int imageCounter = 1;
    private final TelegramLongPollingBot bot;

    // Constructor
    public ItemService(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    //sale start
    public void startSale(Update update) {
        sendResponse(update, """
                Escribe título y descripción siguiendo el siguiente formato:
                 Titulo: xxxx
                 Descripcion: xxxx""");
    }

    public Boolean scanNonUploadedItems(Update update) {
        sendResponse(update, "Escaneando articulos sin subir");
        String[] items = downloadPath.list();
        ArrayList<Item> nonUploadedItems = new ArrayList<>();
        //if item list is not empty check for non uploaded items
        if (items != null) {
            for (String item : items) {
                try (BufferedReader br = new BufferedReader(new FileReader(downloadPath + "\\" + item + "\\" + item))){
                    br.readLine();
                    br.readLine();
                    String status = br.readLine();
                    if (status.equals("sinSubir")) {
                        // get all files inside directory except for the first
                        String[] files = Objects.requireNonNull(new java.io.File(downloadPath + "\\" + item).list());
                        System.out.println(Arrays.toString(files));
                        String[] nonUploadedItem = Arrays.copyOfRange(files, 1, files.length);
                        // add absolute path
                        for (int i = 0; i < nonUploadedItem.length; i++) {
                            nonUploadedItem[i] = downloadPath + "\\" + item + "\\" + nonUploadedItem[i];
                        }
                        System.out.println(Arrays.toString(nonUploadedItem));
                        nonUploadedItems.add(new Item(new java.io.File(downloadPath + "\\" + item + "\\" + item), new ArrayList<>(Arrays.asList(Arrays.copyOfRange(nonUploadedItem, 1, nonUploadedItem.length)))));
                        System.out.println(nonUploadedItems.size());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (nonUploadedItems.isEmpty()) {
            sendResponse(update, "Archivos sin subir no detectados");
            return false;
        } else {
            sendResponse(update, "Archivos sin subir detectados, procediendo a subirlos");
            WallapopService wallapopService = new WallapopService(nonUploadedItems);
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
            title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
            String description = message.substring(message.indexOf("Descripcion:") + 12).trim();

            //create new directory for the item
            java.io.File directory = new java.io.File(downloadPath.getAbsolutePath() + "\\" + title);
            if (directory.mkdir()) {
                java.io.File file = new java.io.File(directory.getPath() + "\\" + title);
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(title + "\n" + description + "\nsinSubir");
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                items.add(new Item(file));
                sendResponse(update, "Información recibida, envía imagen/es.");
                return true;
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
    public void processImages(Update update, Boolean status) {
        if (status) {
            var photos = update.getMessage().getPhoto();
            var photo = photos.getLast();  // Última imagen de mayor tamaño
            var fileId = photo.getFileId();

            try {
                // get image URL
                GetFile getFileMethod = new GetFile(fileId);
                File telegramFile = bot.execute(getFileMethod);
                String fileUrl = "https://api.telegram.org/file/bot" + bot.getBotToken() + "/" + telegramFile.getFilePath();

                // download image
                String path = downloadPath + "\\" + title + "\\" + title + imageCounter + ".jpg";
                items.getLast().addPath(path);
                downloadImage(fileUrl, path);
                sendResponse(update, "Imagen " + imageCounter + " descargada correctamente");
                imageCounter++;
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(update, "Hubo un problema al descargar la imagen. Inténtalo nuevamente.");
            }
        } else {
            sendResponse(update, "Añade imagenes");
        }
    }

    // process next item
    public void nextItem(Update update) {
        imageCounter = 1;
        sendResponse(update, """
                Añadre otro articulo:
                Titulo: xxxx
                Descripcion: xxxx""");
    }

    //finish processing items
    public void finishSale(Update update) {
        imageCounter = 1;
        sendResponse(update, "Correcto, se van a subir " + items.size() + " articulos");
        WallapopService wallapopService = new WallapopService(items);
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


}
