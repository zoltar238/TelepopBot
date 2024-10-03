package Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {

    public static Properties properties;
    private static final String configPath = "src/main/resources/BotConfig.properties";

    public static void initializeProperty() {
        properties = new Properties();
        try {
            InputStream in = new FileInputStream(configPath);
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
