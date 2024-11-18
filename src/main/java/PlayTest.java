import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.Arrays;

import static Config.BotConfig.properties;

public class PlayTest {
    PlayTest () {
        /*
         Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList("--user-data-dir=" + properties.getProperty("UserData"),
                        "--profile-directory=Default")));
        Page page = browser.newPage();
        page.navigate("https://es.wallapop.com/app/catalog/upload");
         */

        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();
            BrowserContext context = chromium.launchPersistentContext(Paths.get(properties.getProperty("UserData")), new BrowserType.LaunchPersistentContextOptions().setHeadless(false));
            Page page = context.newPage();
            page.navigate("https://es.wallapop.com/app/catalog/upload");
            // Perform your actions here
            try {
                Thread.sleep(100000);  // Espera de 5 segundos para que puedas ver la página antes de cerrar
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //context.close();
        }
    }
}
