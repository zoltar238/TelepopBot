import Pages.WallapopUploadPage;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        /*
        BotConfig.initializeProperty();
        ConfigCheckEnum downloadPathCheck = ConfigChecker.checkDownloadPath();
        ConfigCheckEnum userDataCheck = ConfigChecker.checkUserData();
        ConfigCheckEnum hashtagCheck = ConfigChecker.checkHashtags();
        if (downloadPathCheck.equals(ConfigCheckEnum.DOWNLOAD_PATH_OK) && userDataCheck.equals(ConfigCheckEnum.USERDATA_PATH_OK) && hashtagCheck.equals(ConfigCheckEnum.HASTAGS_OK)){
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramController());
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        } else {
            BadConfigView badConfigView = new BadConfigView(downloadPathCheck, userDataCheck, hashtagCheck);
        }
         */
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\dabac\\Proton Drive\\Protoandrei\\My files\\TelepopBot\\src\\main\\resources\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=" + "C:\\Users\\dabac\\AppData\\Local\\Google\\Chrome\\User Data");
        chromeOptions.addArguments("profile-directory=" + "Default");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        driver.manage().window().maximize();

        WallapopUploadPage walla = new WallapopUploadPage(driver, wait);

        driver.get("https://es.wallapop.com/app/catalog/upload");


        walla.clickProductType();
        walla.enterTitle("hola");
        walla.selectCategory();
        walla.enterPrice();
        walla.enterDescription();
        walla.selectCondition();
        walla.enterHashTags(new String[]{"HOla", "Walla"});
        ArrayList<String> imagePaths = new ArrayList<>(Arrays.asList("C:\\Users\\dabac\\Proton Drive\\Protoandrei\\My files\\TelepopBot\\downloads\\xxasdxx\\xxasdxx1.jpg"));

        walla.uploadImages(imagePaths);
        walla.submit();
    }
}
