import config.BotConfig;
import controller.ConfigController;
import controller.TelegramController;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import view.SystemTrayIcon;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Iniciando el bot");

        // Set chrome driver
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        // Initialize configuration file
        BotConfig.initializeProperty();


        // Check configs
        log.info("Comprobando configuraciones");
        ConfigController configController = new ConfigController();
        boolean badConfig = configController.isConfigBad();
        //if there is a bad config, launch warning, else, begin program
        if (badConfig) {
            configController.getConfigSatusMap().forEach((key, value) -> {
                if (!value) {
                    log.error(String.valueOf(key));
                }
            });
        } else {
            configController.getConfigSatusMap().forEach((key, value) -> log.info(String.valueOf(key)));
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramController());
                //create tray icon
                SystemTrayIcon trayIcon = new SystemTrayIcon();
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        }

        //shutdown selenium grid on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Bot apagado");
        }));
    }

}

