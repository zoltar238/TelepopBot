import Config.BotConfig;
import Controller.TelegramController;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TelepotBot {
    public static void main(String[] args){
        BotConfig.initializeProperty();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramController());
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
