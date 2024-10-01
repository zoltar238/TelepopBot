import Config.BotConfig;
import Controller.TelegramController;
import Model.ConfigCheckEnum.ConfigCheckEnum;
import Util.ConfigChecker;
import Util.ImageProcessor;
import View.BadConfigView;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        BotConfig.initializeProperty();
        ConfigCheckEnum downloadPathCheck = ConfigChecker.checkDownloadPath();
        ConfigCheckEnum userDataCheck = ConfigChecker.checkUserData();
        ConfigCheckEnum hashtagCheck = ConfigChecker.checkHashtags();
        if (downloadPathCheck.equals(ConfigCheckEnum.DOWNLOAD_PATH_OK) && userDataCheck.equals(ConfigCheckEnum.USERDATA_PATH_OK) && hashtagCheck.equals(ConfigCheckEnum.HASTAGS_OK)) {
            try {
                //start telegram bot
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramController());
                //load downloaded images
                ImageProcessor processor = new ImageProcessor();
                processor.loadImages();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            BadConfigView badConfigView = new BadConfigView(downloadPathCheck, userDataCheck, hashtagCheck);
        }
    }
}
