package controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.ConfigCheckEnum.ConfigCheckEnum;
import util.ConfigChecker;

import java.util.Map;

@Slf4j
@Getter
public class ConfigController {

    private final Map<ConfigCheckEnum, Boolean> configSatusMap;

    // Constructor, load configuration check
    public ConfigController() {
        ConfigChecker configChecker = new ConfigChecker();
        configSatusMap = configChecker.checkConfigFile();
    }

    // Check configuration
    public boolean isConfigBad() {
        // Return true if configs is bad
        for (Map.Entry<ConfigCheckEnum, Boolean> entry : configSatusMap.entrySet()) {
            if (!entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    // Checks validity of cookies
    public boolean areCookiesValid() {
        return configSatusMap.containsKey(ConfigCheckEnum.VALID_COOKIES);
    }
}
