import Config.BotConfig;
import Controller.TelegramController;
import Model.ConfigCheckEnum.ConfigCheckEnum;
import Model.Page.WallapopUploadPage;
import Util.ConfigChecker;
import View.BadConfigView;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        BotConfig.initializeProperty();
        ConfigCheckEnum downloadPathCheck = ConfigChecker.checkDownloadPath();
        ConfigCheckEnum userDataCheck = ConfigChecker.checkUserData();
        ConfigCheckEnum hashtagCheck = ConfigChecker.checkHashtags();
        if (downloadPathCheck.equals(ConfigCheckEnum.DOWNLOAD_PATH_OK) && userDataCheck.equals(ConfigCheckEnum.USERDATA_PATH_OK) && hashtagCheck.equals(ConfigCheckEnum.HASTAGS_OK)){
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramController());
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        } else {
            BadConfigView badConfigView = new BadConfigView(downloadPathCheck, userDataCheck, hashtagCheck);
        }
    }
}
