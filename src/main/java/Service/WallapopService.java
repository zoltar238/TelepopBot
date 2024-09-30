package Service;

import DAO.ItemDAOImplementation;
import Model.Item;
import Model.Page.WallapopUploadPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static Config.BotConfig.properties;

public class WallapopService {
    private WebDriver driver;
    private final ItemDAOImplementation itemImp = new ItemDAOImplementation();
    private WallapopUploadPage wallaUpload;

    public WallapopService(){
        //element wait time
    }

    //start sales process
    public void startSale(ArrayList<Item> items){
        String[] hashTags = extractHashTags();
        //detect if chrome is open and kill it
        if (properties.getProperty("KillChrome").equals("true")) {
            detectedChromeInstance();
        }
        //initialize web driver
        initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wallaUpload = new WallapopUploadPage(driver, wait);
        //maximize browser window
        driver.manage().window().maximize();
        for (Item item : items) {
            //load upload page
            driver.get("https://es.wallapop.com/app/catalog/upload");
            //read info file
            String[] info = itemImp.readInfoFile(item.getInfoFile()); //{title, description}
            wallaUpload.selectProductType();
            wallaUpload.enterTitle(info[0]);
            wallaUpload.selectCategory();
            wallaUpload.enterPrice();
            wallaUpload.enterDescription(info[1]);
            wallaUpload.selectCondition();
            wallaUpload.enterHashTags(hashTags);
            wallaUpload.uploadImages(item.getPaths());
            wallaUpload.submit();
            try {
                Thread.sleep(Long.parseLong(properties.getProperty("ItemUploadWaitTime")));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //set file status to submitted
            itemImp.modifyInfoFile(item);
        }
        //close chrome after all items have been submitted
        if (properties.getProperty("CleanUp").equals("true")) {
            cleanup();
        }
    }

    //initialize web driver
    private void initializeWebDriver() {
        String browser = properties.getProperty("WebDriver");
        switch (browser) {
            case "Firefox":
                System.setProperty("webdriver.firefox.driver", "src/main/resources/geckodriver.exe");
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setProfile(new FirefoxProfile(new File(properties.getProperty("UserData"))));
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "Chrome":
                System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("user-data-dir=" + properties.getProperty("UserData"));
                chromeOptions.addArguments("profile-directory=" + properties.getProperty("Profile"));
                driver = new ChromeDriver(chromeOptions);
                break;
            case "Edge":
                System.setProperty("webdriver.edge.driver", "src/main/resources/msedgedriver.exe");
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("user-data-dir=" + properties.getProperty("UserData"));
                driver = new EdgeDriver(edgeOptions);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + browser);
        }
    }

    //extract hashtags from file
    private String[] extractHashTags(){
        try {
            // read entire file and return array of hashtags.txt
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/hashtags.txt"));
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //detect chrome instance and kill it
    private void detectedChromeInstance(){
        String processName = "chrome.exe";
        try {
            // execute task list in search for Chrome
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // kill chrome if found
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase())) {
                    Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //terminate Chrome browser after finishing
    private void cleanup() {
        try {
            Thread.sleep(Long.parseLong(properties.getProperty("CleanUpWaitTime")));
            driver.quit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
