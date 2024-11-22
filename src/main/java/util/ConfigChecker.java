package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import model.ConfigCheckEnum.ConfigCheckEnum;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static config.BotConfig.properties;

@Slf4j
public class ConfigChecker {

    public Map<ConfigCheckEnum, Boolean> checkConfigFile() {
        // HashMap to store the results of the configuration checks
        Map<ConfigCheckEnum, Boolean> configResults = new LinkedHashMap<>();

        // Perform all checks and add results to the map
        checkDownloadPath(configResults);
        checkUserData(configResults);
        checkHashtags(configResults);
        checkMessagesIgnored(configResults);
        checkWebDriver(configResults);
        checkImageUploadWaitTime(configResults);
        checkItemUploadWaitTime(configResults);
        checkCleanUpWaitTime(configResults);
        checkBooleanConfigs(configResults);
        //checkCookies(configResults);

        return configResults;
    }

    // Check cookies
    private void checkCookies(Map<ConfigCheckEnum, Boolean> configResults) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file
            File file = new File("src/main/resources/cookies.json");

            // Parse json file
            JsonNode jsonNode = objectMapper.readTree(file);

            // Check if cookies are null
            if (jsonNode.isEmpty()) {
                configResults.put(ConfigCheckEnum.INVALID_COOKIES, false);
            } else {
                configResults.put(ConfigCheckEnum.VALID_COOKIES, true);
            }
        } catch (IOException e) {
            log.error("No se ha encontrado el archivo de cookies: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // Check download path and add result to the map
    private void checkDownloadPath(Map<ConfigCheckEnum, Boolean> configResults) {
        File downloadDirectory = new File(properties.getProperty("DownloadPath"));
        if (downloadDirectory.exists()) {
            if (downloadDirectory.isDirectory()) {
                File[] items = downloadDirectory.listFiles();
                if (items != null) {
                    for (File file : items) {
                        File[] downloadedFiles = file.listFiles();
                        if (downloadedFiles != null) {
                            for (File fl : downloadedFiles) {
                                if (!fl.getPath().endsWith(".txt") && !fl.getPath().endsWith(".jpg")) {
                                    configResults.put(ConfigCheckEnum.WRONG_DOWNLOAD_PATH_STRUCTURE, false);
                                    return;
                                }
                            }
                        } else {
                            configResults.put(ConfigCheckEnum.WRONG_DOWNLOAD_PATH_STRUCTURE, false);
                            return;
                        }
                    }
                }
                configResults.put(ConfigCheckEnum.DOWNLOAD_PATH_OK, true);
            } else {
                configResults.put(ConfigCheckEnum.NOT_A_DIRECTORY, false);
            }
        } else {
            configResults.put(ConfigCheckEnum.WRONG_DOWNLOAD_PATH, false);
        }
    }

    // Check user data path and add result to the map
    private void checkUserData(Map<ConfigCheckEnum, Boolean> configResults) {
        if (properties.getProperty("UserData").equals(System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\User Data")) {
            configResults.put(ConfigCheckEnum.USERDATA_PATH_OK, true);
        } else {
            configResults.put(ConfigCheckEnum.USERDATA_PATH_WRONG, false);
        }
    }

    // Check hashtags and add result to the map
    private void checkHashtags(Map<ConfigCheckEnum, Boolean> configResults) {
        int lineCounter = 0;

        // Try to load the file from the classpath (for JAR execution)
        InputStream in = getClass().getClassLoader().getResourceAsStream("hashtags.txt");

        try (BufferedReader reader = in != null ?
                // If the file is found in the classpath (inside JAR), use an InputStreamReader
                new BufferedReader(new InputStreamReader(in)) :
                // Otherwise, fallback to reading from the file system (for development)
                new BufferedReader(new FileReader("src/main/resources/hashtags.txt"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line contains a hashtag character
                if (line.contains("#")) {
                    configResults.put(ConfigCheckEnum.NO_HASH_CHAR_ALLOWED, false);
                    return; // Exit early if a hashtag is found
                }
                lineCounter++;
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading hashtags.txt", e);
        }

        // Determine the result based on the number of lines
        if (lineCounter <= 5) {
            configResults.put(ConfigCheckEnum.HASHTAGS_OK, true);
        } else {
            configResults.put(ConfigCheckEnum.HASHTAGS_EXCEED_MAX, false);
        }
    }


    // Check MessagesIgnored (must be a valid integer) and add result to the map
    private void checkMessagesIgnored(Map<ConfigCheckEnum, Boolean> configResults) {
        try {
            int messagesIgnored = Integer.parseInt(properties.getProperty("MessagesIgnored"));
            if (messagesIgnored >= 0) {
                configResults.put(ConfigCheckEnum.MESSAGES_IGNORED_OK, true);
            } else {
                configResults.put(ConfigCheckEnum.INVALID_MESSAGES_IGNORED_NEGATIVE, false);
            }
        } catch (NumberFormatException e) {
            configResults.put(ConfigCheckEnum.MESSAGES_IGNORED_MUST_BE_INTEGER, false);
        }
    }

    // Check WebDriver and add result to the map
    private void checkWebDriver(Map<ConfigCheckEnum, Boolean> configResults) {
        String webDriver = properties.getProperty("WebDriver");
        if ("Chrome".equalsIgnoreCase(webDriver) || "Edge".equalsIgnoreCase(webDriver) || "Firefox".equalsIgnoreCase(webDriver)) {
            configResults.put(ConfigCheckEnum.WEBDRIVER_OK, true);
        } else {
            configResults.put(ConfigCheckEnum.INVALID_WEBDRIVER, false);
        }
    }

    // Check ImageUploadWaitTime (must be a valid integer) and add result to the map
    private void checkImageUploadWaitTime(Map<ConfigCheckEnum, Boolean> configResults) {
        try {
            int imageUploadWaitTime = Integer.parseInt(properties.getProperty("ImageUploadWaitTime"));
            if (imageUploadWaitTime >= 0) {
                configResults.put(ConfigCheckEnum.IMAGE_UPLOAD_WAIT_TIME_OK, true);
            } else {
                configResults.put(ConfigCheckEnum.INVALID_IMAGE_NEGATIVE, false);
            }
        } catch (NumberFormatException e) {
            configResults.put(ConfigCheckEnum.IMAGE_UPLOAD_WAIT_TIME_MUST_BE_INTEGER, false);
        }
    }

    // Check ItemUploadWaitTime (must be a valid integer) and add result to the map
    private void checkItemUploadWaitTime(Map<ConfigCheckEnum, Boolean> configResults) {
        try {
            int itemUploadWaitTime = Integer.parseInt(properties.getProperty("ItemUploadWaitTime"));
            if (itemUploadWaitTime >= 0) {
                configResults.put(ConfigCheckEnum.ITEM_UPLOAD_WAIT_TIME_OK, true);
            } else {
                configResults.put(ConfigCheckEnum.INVALID_ITEM_UPLOAD_NEGATIVE, false);
            }
        } catch (NumberFormatException e) {
            configResults.put(ConfigCheckEnum.ITEM_UPLOAD_WAIT_TIME_MUST_BE_INTEGER, false);
        }
    }

    // Check CleanUpWaitTime (must be a valid integer) and add result to the map
    private void checkCleanUpWaitTime(Map<ConfigCheckEnum, Boolean> configResults) {
        try {
            int cleanUpWaitTime = Integer.parseInt(properties.getProperty("CleanUpWaitTime"));
            if (cleanUpWaitTime >= 0) {
                configResults.put(ConfigCheckEnum.CLEANUP_WAIT_TIME_OK, true);
            } else {
                configResults.put(ConfigCheckEnum.CLEANUP_WAIT_TIME_NEGATIVE, false);
            }
        } catch (NumberFormatException e) {
            configResults.put(ConfigCheckEnum.CLEANUP_WAIT_TIME_MUST_BE_INTEGER, false);
        }
    }

    // Check CleanUp and KillChrome (must be boolean) and add result to the map
    private void checkBooleanConfigs(Map<ConfigCheckEnum, Boolean> configResults) {
        if (!"true".equalsIgnoreCase(properties.getProperty("CleanUp")) && !"false".equalsIgnoreCase(properties.getProperty("CleanUp"))) {
            configResults.put(ConfigCheckEnum.INVALID_CLEANUP_BOOLEAN, false);
        } else {
            configResults.put(ConfigCheckEnum.CLEANUP_BOOLEAN_OK, true);
        }

        if (!"true".equalsIgnoreCase(properties.getProperty("KillChrome")) && !"false".equalsIgnoreCase(properties.getProperty("KillChrome"))) {
            configResults.put(ConfigCheckEnum.INVALID_KILL_CHROME_BOOLEAN, false);
        } else {
            configResults.put(ConfigCheckEnum.KILL_CHROME_BOOLEAN_OK, true);
        }
    }
}
