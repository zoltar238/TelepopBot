package Controller;

import java.io.IOException;

import static Config.BotConfig.properties;

public class SeleniumController {
    private final String seleniumPath = "src/main/resources/selenium-server-4.25.0.jar";
    private Process seleniumProcess;

    //start selenium grid server with standalone mode
    public void startSelenium() {
        try {
            //start selenium grid
            seleniumProcess = Runtime.getRuntime().exec("java -jar " + seleniumPath + " standalone --max-sessions " + properties.getProperty("BrowserInstances"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //shut down selenium grid
    public void shutDownSelenium() {
        if (seleniumProcess!= null) {
            seleniumProcess.destroyForcibly();
        }
    }
}
