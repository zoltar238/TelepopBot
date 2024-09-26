package Util;

import Model.ConfigCheckEnum.ConfigCheckEnum;

import java.io.*;

import static Config.BotConfig.properties;

public class ConfigurationChecker {

    public ConfigurationChecker() {
        ConfigCheckEnum ConfigCheckEnum = checkDownloadPath();
        System.out.println(checkDownloadPath().toString());
        System.out.println(checkUserData().toString());
        System.out.println(checkHashtags().toString());
    }

    //check configuration path
    private ConfigCheckEnum checkDownloadPath() {
        String downloadPath = properties.getProperty("DownloadPath");
        File downloadDirectory = new File(downloadPath);
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
    private ConfigCheckEnum checkUserData(){
        if (properties.getProperty("UserData").equals(System.getProperty("user.home")+ "\\AppData\\Local\\Google\\Chrome\\User Data")){
            return ConfigCheckEnum.USERDATA_PATH_OK;
        } else {
            return ConfigCheckEnum.USERDATA_PATH_WRONG;
        }
    }

    //check hashtags are 5 or less and dont contain the # symnol
    private ConfigCheckEnum checkHashtags(){
        int lineCounter = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/hashtags.txt"))) {
            String line;
            while ((line = reader.readLine()) != null){
                if (line.contains("#")){
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
}
