package Controller;

import Service.TelegramService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import static Config.BotConfig.properties;

public class TelegramController extends TelegramLongPollingBot {
    private final TelegramService telegramService;
    private final String botUsername = properties.getProperty("Username");
    private final String botToken = properties.getProperty("Token");
    private static boolean saleProcess = false;
    public static boolean correctInfo = false;
    public static boolean imageUploaded = false;
    public static boolean nonUploadedItems = true;

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
            //check if message contains image
            if (update.getMessage().hasPhoto()) {
                if (correctInfo) {
                    telegramService.processImages(update, "correctInfo");
                    imageUploaded = true;
                } else {
                    telegramService.processImages(update, "incorrectInfo");
                }
                //check if message contains text
            } else if (update.getMessage().hasText()) {
                //get message
                String message = update.getMessage().getText().trim();
                // end the app
                if (!saleProcess && !correctInfo && !imageUploaded && message.equals("/apagar")){
                    telegramService.endProgram(update);
                    // if sales process hasn't started, check for command /venta to start procces
                } else if (!saleProcess && message.equals("/venta")) {
                    //when starting the bot, check for non uploaded items, if they exist, upload them
                    if (nonUploadedItems){
                        nonUploadedItems = telegramService.scanNonUploadedItems(update);
                    //after uploading all non uploaded item, or no non uploaded items detected, start sales process
                    } if (!nonUploadedItems){
                        telegramService.startSale(update);
                        saleProcess = true;
                    }
                //if sales process has started, and command /venta is imputed, send warning message
                } else if (saleProcess && message.equals("/venta")) {
                    telegramService.saleAlreadyStarted(update);
                //if sales process has started, and info is not correct, check if info is correct
                } else if (saleProcess && !correctInfo) {
                    correctInfo = telegramService.processInfo(update, message);
                } else if (correctInfo) {
                    // add new item with /siguiente
                    if (message.equals("/siguiente")) {
                        //check if images where uploaded successfully
                        if (!imageUploaded){
                            telegramService.nextItem(update, "imagesNotUploaded");
                        } else {
                            correctInfo = false;
                            imageUploaded = false;
                            telegramService.nextItem(update, "imagesUploaded");
                        }
                    //end sales process with /finventa
                    } else if (message.equals("/finventa")) {
                        //check if images where uploaded successfully
                        if (!imageUploaded){
                            telegramService.finishSale(update, "imagesNotUploaded");
                        } else {
                            correctInfo = false;
                            saleProcess = false;
                            imageUploaded = false;
                            telegramService.finishSale(update, "imagesUploaded");
                        }
                    } else {
                        telegramService.processImages(update, "sdfsdf");
                    }
                }
            }
        }
    }
}

