package Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static Config.BotConfig.properties;

public class SeleniumController {

    private Process seleniumProcess;

    // Start selenium grid server with standalone mode
    public void startSelenium() {
        try {
            String seleniumPath = "src/main/resources/selenium-server-4.25.0.jar";
            if (!new File(seleniumPath).exists()) {
                //seleniumPath = Objects.requireNonNull(getClass().getClassLoader().getResource("selenium-server-4.25.0.jar")).getPath();
                File seleniumJar = extractFileFromJar("selenium-server-4.25.0.jar");
                seleniumPath = seleniumJar.getAbsolutePath();
            }

            seleniumProcess = Runtime.getRuntime().exec("java -jar " + seleniumPath + " standalone --max-sessions " + properties.getProperty("BrowserInstances"));

            // wait until the server has started
            waitForSeleniumGridToStart();

        } catch (IOException e) {
            throw new RuntimeException("Error al iniciar Selenium Grid", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error al esperar la inicialización de Selenium Grid", e);
        }
    }

    private File extractFileFromJar(String fileName) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("No se encuentra el recurso " + fileName);
        }

        // Extraemos el archivo desde el recurso
        File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
        try (InputStream in = resource.openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private void waitForSeleniumGridToStart() throws InterruptedException {
        // Check if the Selenium Grid is up and running
        int retries = 10; // Try up to 10 times
        while (retries > 0) {
            try {
                // Try connecting to the Selenium Grid at localhost:4444
                HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:4444").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                int status = connection.getResponseCode();

                if (status == 200) {
                    // If the server is up, break the loop
                    return;
                }
            } catch (IOException e) {
                // If an exception occurs, it means the Grid is not ready yet
            }

            retries--;
            // wait half a second before trying again
            Thread.sleep(500);
        }

        throw new RuntimeException("Selenium Grid didn't start within the expected time.");
    }

    // Shut down selenium grid
    public void shutDownSelenium() {
        if (seleniumProcess != null) {
            seleniumProcess.destroyForcibly();
        }
    }
}
