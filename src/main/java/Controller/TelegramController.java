package Controller;

import Service.TelegramService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

import static Config.BotConfig.properties;

public class TelegramController extends TelegramLongPollingBot {
    private final TelegramService telegramService;
    private final String botUsername = properties.getProperty("Username");
    private final String botToken = properties.getProperty("Token");
    private static boolean saleProcess = false;
    public static boolean correctInfo = false;
    public static boolean imageUploaded = false;
    public static boolean nonUploadedItems = true;
    public int messageCount = 1;
    public String saleType = "";

    public TelegramController() {
        telegramService = new TelegramService(this);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            //ignore first messages
            if (messageCount <= Integer.parseInt(properties.getProperty("MessagesIgnored"))) {
                sendResponse(update, telegramService.ignoreMessage(messageCount));
                messageCount++;
            }
            //check if message contains image
            else if (update.getMessage().hasPhoto()) {
                if (correctInfo) {
                    sendResponse(update, telegramService.processImage(update, botToken, "correctInfo"));
                    imageComparer(update);
                    imageUploaded = true;
                } else {
                    sendResponse(update, telegramService.processImage(update, botToken, "incorrectInfo"));
                }
                //check if message contains text
            } else if (update.getMessage().hasText()) {
                //get message
                String message = update.getMessage().getText().trim();
                // end the app
                if (!saleProcess && !correctInfo && !imageUploaded && message.equals("/apagar")) {
                    sendResponse(update, "Cerrando el programa");
                    System.exit(0);
                } else if (message.equals("/cancelartodo")) {
                    sendResponse(update, telegramService.cancelSale());
                    correctInfo = false;
                    saleProcess = false;
                    imageUploaded = false;
                }
                // if sales process hasn't started, check for command /venta to start procces
                else if (!saleProcess && (message.equals("/ventafull") || message.equals("/ventatitulo") || message.equals("/resubir"))) {
                    //when starting the bot, check for non uploaded items, if they exist, upload them
                    if (nonUploadedItems) {
                        String response = telegramService.scanNonUploadedItems();
                        nonUploadedItems = response.equals("Archivos sin subir detectados, procediendo a subirlos");
                        sendResponse(update, response);
                        //after uploading all non uploaded item, or no non uploaded items detected, start sales process
                    }
                    if (!nonUploadedItems) {
                        //determine sale type
                        if (message.equals("/ventafull")) {
                            saleType = "full";
                        } else if (message.equals("/ventatitulo")) {
                            saleType = "title";
                        } else {
                            saleType = "reupload";
                            sendResponse(update, telegramService.showUploadedItems());
                        }
                        sendResponse(update, telegramService.startSale(saleType));
                        saleProcess = true;
                    }
                    //if sales process has started, and command /venta is imputed, send warning message
                } else if (saleProcess && (message.equals("/ventafull") || message.equals("/ventatitulo"))) {
                    sendResponse(update, "Proceso de venta ya en curso");
                    //reupload items
                } else if (saleProcess && saleType.equals("reupload")) {
                    sendResponse(update, telegramService.reuploadItems(message));
                    saleType = "";
                    saleProcess = false;
                }//if sales process has started, and info is not correct, check if info is correct
                else if (saleProcess && !correctInfo) {
                    String response = telegramService.processInfo(message, saleType);
                    sendResponse(update, response);
                    correctInfo = response.equals("Información recibida, envía imagen/es");
                } else if (correctInfo) {
                    // add new item with /siguiente
                    switch (message) {
                        case "/siguiente" -> {
                            //check if images where uploaded successfully
                            if (!imageUploaded) {
                                sendResponse(update, telegramService.nextItem("imagesNotUploaded", saleType));
                            } else {
                                correctInfo = false;
                                imageUploaded = false;
                                sendResponse(update, telegramService.nextItem("imagesUploaded", saleType));
                            }
                        } //end sales process with /finventa
                        case "/finventa" -> {
                            //check if images where uploaded successfully
                            if (!imageUploaded) {
                                sendResponse(update, telegramService.finishSale("imagesNotUploaded"));
                            } else {
                                correctInfo = false;
                                saleProcess = false;
                                imageUploaded = false;
                                saleType = "";
                                sendResponse(update, telegramService.finishSale("imagesUploaded"));
                            }
                        }//save items to upload them later
                        case "/guardar" -> {
                            correctInfo = false;
                            saleProcess = false;
                            imageUploaded = false;
                            saleType = "";
                            sendResponse(update, telegramService.saveItems());
                        }//send warning message if image is expected
                        default -> sendResponse(update, telegramService.processImage(update, botToken, "noImage"));
                    }
                }
            }
        }
    }

    // get result from image comparison
    public void imageComparer(Update update) {
        // call comparer
        telegramService.compareImage().thenAccept(result -> sendResponse(update, Objects.requireNonNullElse(result, "No se encontraron coincidencias"))).exceptionally(ex -> {
            sendResponse(update, "Ocurrió un error al comparar imágenes: " + ex.getMessage());
            return null;
        });
    }


    // respond to client
    public void sendResponse(Update update, String message) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText(message);
        try {
            this.execute(response);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}

