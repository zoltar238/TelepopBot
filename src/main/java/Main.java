import Config.BotConfig;
import Controller.TelegramController;
import Util.ConfigurationChecker;
import View.BadConfigView;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args){
        //BadConfigView badConfigView = new BadConfigView();
        BotConfig.initializeProperty();
        ConfigurationChecker configurationChecker = new ConfigurationChecker();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramController());
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
