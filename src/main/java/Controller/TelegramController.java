package Controller;

import Service.ItemService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import static Config.BotConfig.properties;

public class TelegramController extends TelegramLongPollingBot {
    private final ItemService itemService;
    private final String botUsername = properties.getProperty("Username");
    private final String botToken = properties.getProperty("Token");
    private static boolean saleProcess = false;
    public static boolean correctInfo = false;

    public TelegramController() {
        itemService = new ItemService(this);
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
            if (update.getMessage().hasPhoto()) {
                if (correctInfo) {
                    itemService.processImages(update, true);
                }
            } else if (update.getMessage().hasText()) {
                String message = update.getMessage().getText();
                // Start sales process with /venta
                if (!saleProcess && message.equals("/venta")) {
                    //check for unuploaded items
                    if (!itemService.scanNonUploadedItems(update)) {
                        itemService.startSale(update);
                        saleProcess = true;
                    }
                } else if (saleProcess && message.equals("/venta")) {
                    itemService.saleAlreadyStarted(update);
                    //verify info
                } else if (saleProcess && !correctInfo) {
                    correctInfo = itemService.processInfo(update, message);
                } else if (correctInfo) {
                    System.out.println("correct info");
                    // add new item with /siguiente
                    if (message.equals("/siguiente")) {
                        System.out.println("se ha dado a siguiente");
                        correctInfo = false;
                        System.out.println(correctInfo + " next item");
                        itemService.nextItem(update);
                        //end sales process with /finventa
                    } else if (message.equals("/finventa")) {
                        itemService.finishSale(update);
                        saleProcess = false;
                        correctInfo = false;
                    } else {
                        itemService.processImages(update, false);
                    }
                }
            }
        }
    }
}

