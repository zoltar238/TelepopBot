package Util;

import Model.ConfigCheckEnum.ConfigCheckEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static Config.BotConfig.properties;

public class ConfigChecker {

    //check configuration path
    public static ConfigCheckEnum checkDownloadPath() {
        File downloadDirectory = new File(properties.getProperty("DownloadPath"));
        // check if it exists
        if (downloadDirectory.exists()) {
            if (downloadDirectory.isDirectory()) {
                //get all files inside the directory
                File[] items = downloadDirectory.listFiles();
                //if download directory is empty -> ok, else, check file structure
                if (items != null) {
                    for (File file : items) {
                        File[] downloadedFiles = file.listFiles();
                        //check file structure
                        if (downloadedFiles != null) {
                            for (File fl : downloadedFiles) {
                                if (!fl.getPath().endsWith(".txt") && !fl.getPath().endsWith(".jpg")) {
                                    return ConfigCheckEnum.WRONG_STRUCTURE;
                                }
                            }
                        } else {
                            return ConfigCheckEnum.WRONG_STRUCTURE;
                        }
                    }
                }
                return ConfigCheckEnum.DOWNLOAD_PATH_OK;
            } else {
                return ConfigCheckEnum.NOT_A_DIRECTORY;
            }
        } else {
            return ConfigCheckEnum.FILE_DOESNT_EXIST;
        }
    }

    //check user data
    public static ConfigCheckEnum checkUserData() {
        if (properties.getProperty("UserData").equals(System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\User Data")) {
            return ConfigCheckEnum.USERDATA_PATH_OK;
        } else {
            return ConfigCheckEnum.USERDATA_PATH_WRONG;
        }
    }

    //check hashtags are 5 or less and don't contain the # symbol
    public static ConfigCheckEnum checkHashtags() {
        int lineCounter = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/hashtags.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("#")) {
                    return ConfigCheckEnum.NO_HASH_CHAR_ALLOWED;
                }
                lineCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lineCounter <= 5) {
            return ConfigCheckEnum.HASTAGS_OK;
        } else {
            return ConfigCheckEnum.HASTAGS_EXCEED_MAX;
        }
    }

    //todo: write check for the rest of the elements
}
