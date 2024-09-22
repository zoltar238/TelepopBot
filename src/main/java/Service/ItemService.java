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

import static Config.BotConfig.properties;

public class ItemService {
    private final ArrayList<Item> items = new ArrayList<>();
    private String title;
    private final String downloadPath = properties.getProperty("DownloadPath");
    private int imageCounter = 1;
    private final TelegramLongPollingBot bot; // Agregar bot aquí

    // Constructor
    public ItemService(TelegramLongPollingBot bot) {
        this.bot = bot;
        System.out.println(downloadPath);
    }

    //sale start
    public void startSale(Update update){
        sendResponse(update, """
                            Escribe título y descripción siguiendo el siguiente formato:
                             Titulo: xxxx
                             Descripcion: xxxx""");
    }

    //sale already started
    public void saleAlreadyStarted(Update update){
        sendResponse(update, "Proceso de venta ya en curso");
    }

    //verify article info
    public Boolean processInfo(Update update, String message) {
        if (message.contains("Titulo:") && message.contains("Descripcion:")) {
            title = message.substring(message.indexOf("Titulo:") + 7, message.indexOf("Descripcion:")).trim();
            String description = message.substring(message.indexOf("Descripcion:") + 12).trim();

            //create new directory for the item
            java.io.File directory = new java.io.File(downloadPath + "\\" + title);
            if (directory.mkdir()) {
                java.io.File file = new java.io.File(directory.getPath() + "\\" + title);
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(title + "\n" + description);
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
    public void processImages(Update update, Boolean status){
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
        sendResponse(update, "Añadre otro articulo");
    }

    //finish processing items
    public void finishSale(Update update) {
        imageCounter = 1;
        sendResponse( update, "Correcto, se van a subir " + items.size() + " articulos");
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
