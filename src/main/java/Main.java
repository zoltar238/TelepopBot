import Config.BotConfig;
import Controller.SeleniumController;
import Controller.TelegramController;
import Model.ConfigCheckEnum.ConfigCheckEnum;
import Util.ConfigChecker;
import Util.ImageProcessor;
import View.BadConfigView;
import View.SystemTrayIcon;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) {
        //initialize configuration file
        BotConfig.initializeProperty();
        SeleniumController seleniumController = new SeleniumController();
        ConfigChecker configChecker = new ConfigChecker();
        Map<ConfigCheckEnum, Boolean> configSatusMap = configChecker.checkConfigFile();
        boolean badConfig = false;
        //check if configurations are ok
        for (Map.Entry<ConfigCheckEnum, Boolean> entry : configSatusMap.entrySet()) {
            if (!entry.getValue()) {
                badConfig = true;
                break;
            }
        }
        //if there is a bad config, launch warning, else, begin program
        if (badConfig) {
            BadConfigView badConfigView = new BadConfigView(configSatusMap);
        } else {
            try {
                //launch selenium server
                seleniumController.startSelenium();
                //start telegram bot
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramController());
                //load downloaded images
                ImageProcessor processor = new ImageProcessor();
                processor.loadImages();
                //create tray icon
                SystemTrayIcon trayIcon = new SystemTrayIcon();
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        }

        //shutdown selenium grid on exit
        Runtime.getRuntime().addShutdownHook(new Thread(seleniumController::shutDownSelenium));
    }
}

