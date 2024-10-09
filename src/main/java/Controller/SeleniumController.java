package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static Config.BotConfig.properties;

public class SeleniumController {
    private Process seleniumProcess;

    //start selenium grid server with standalone mode
    public void startSelenium() {
        try {
            String seleniumPath = "src/main/resources/selenium-server-4.25.0.jar";
            seleniumProcess = Runtime.getRuntime().exec("java -jar " + seleniumPath + " standalone --max-sessions " + properties.getProperty("BrowserInstances"));
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(seleniumProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Selenium: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // wait until the server has started
            Thread.sleep(5000);
        } catch (IOException e) {
            throw new RuntimeException("Error al iniciar Selenium Grid", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error al esperar la inicialización de Selenium Grid", e);
        }
    }


    //shut down selenium grid
    public void shutDownSelenium() {
        if (seleniumProcess != null) {
            seleniumProcess.destroyForcibly();
        }
    }
}
