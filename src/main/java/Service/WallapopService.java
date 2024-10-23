package Service;

import Config.BotConfig;
import DAO.ItemDAOImp;
import Model.Item;
import Model.Page.WallapopUploadPage;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static Config.BotConfig.properties;

public class WallapopService {
    //private WebDriver driver;
    private final ItemDAOImp itemImp = new ItemDAOImp();
    private final ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("BrowserInstances")));


    public WallapopService() {
    }

    //start sales process
    public void publishItems(List<Item> items) {
        String[] hashTags = extractHashTags();
        //detect if chrome is open and kill it
        if (properties.getProperty("KillChrome").equals("true")) {
            detectedChromeInstance();
        }

        //get cookies
        Set<Cookie> savedCookies = getCookie();

        for (Item item : items) {
            executor.submit(() -> {
                WebDriver driver = initializeWebDriver();
                WallapopUploadPage wallaUpload = new WallapopUploadPage(driver);
                driver.manage().window().maximize();
                //load upload page
                driver.get("https://es.wallapop.com");
                //load cookies to driver
                savedCookies.forEach(cookie -> driver.manage().addCookie(cookie));
                driver.navigate().refresh();

                //read info file
                String[] info = itemImp.readInfoFile(item.getInfoFile()); //{title, description}

                do {
                    wallaUpload.success.set(true);
                    //refresh page
                    driver.get("https://es.wallapop.com/app/catalog/upload");
                    driver.navigate().refresh();
                    wallaUpload.acceptCookies();
                    //run until upload succeeds
                    if (wallaUpload.success.get()) wallaUpload.selectProductType();
                    if (wallaUpload.success.get()) wallaUpload.enterTitle(info[0]);
                    if (wallaUpload.success.get()) wallaUpload.selectCategory();
                    if (wallaUpload.success.get()) wallaUpload.enterPrice();
                    if (wallaUpload.success.get()) wallaUpload.enterDescription(info[1]);
                    if (wallaUpload.success.get()) wallaUpload.selectCondition();
                    if (wallaUpload.success.get()) wallaUpload.enterHashTags(hashTags);
                    if (wallaUpload.success.get()) wallaUpload.uploadImages(item.getPaths());
                    if (wallaUpload.success.get()) wallaUpload.submit();
                } while (!wallaUpload.success.get());

                try {
                    Thread.sleep(Long.parseLong(properties.getProperty("ItemUploadWaitTime")));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //set file status to submitted
                itemImp.modifyInfoFile(item);
                cleanup(driver);
            });
        }
    }

    private Set<Cookie> getCookie() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=" + properties.getProperty("UserData"));
        chromeOptions.addArguments("profile-directory=" + properties.getProperty("Profile"));
        WebDriver driver = new ChromeDriver(chromeOptions);

        driver.get("https://es.wallapop.com");
        //capture profile cookies
        Set<Cookie> cookies = driver.manage().getCookies();
        driver.quit();
        return cookies;
    }

    //initialize web driver
    private WebDriver initializeWebDriver() {
        String browser = properties.getProperty("WebDriver");
        switch (browser) {
            case "Firefox":
                System.setProperty("webdriver.firefox.driver", "src/main/resources/geckodriver.exe");
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setProfile(new FirefoxProfile(new File(properties.getProperty("UserData"))));
                return new FirefoxDriver(firefoxOptions);
            case "Chrome":
                System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
                ChromeOptions options = new ChromeOptions();
                try {
                    return new RemoteWebDriver(new URL("http://localhost:4444"), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            case "Edge":
                System.setProperty("webdriver.edge.driver", "src/main/resources/msedgedriver.exe");
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("user-data-dir=" + properties.getProperty("UserData"));
                return new EdgeDriver(edgeOptions);
            default:
                throw new IllegalStateException("Unexpected value: " + browser);
        }
    }

    //extract hashtags from file
    private String[] extractHashTags() {
        try {
            // try to read file from jar
            InputStream in = BotConfig.class.getClassLoader().getResourceAsStream("hashtags.txt");
            List<String> lines;
            if (in != null) {
                //read all lines from jar
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    lines = reader.lines().collect(Collectors.toList());
                }
            } else {
                // read all lines
                lines = Files.readAllLines(Paths.get("src/main/resources/hashtags.txt"));
            }
            // convert to array
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //detect chrome instance and kill it
    private void detectedChromeInstance() {
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
    private void cleanup(WebDriver driver) {
        try {
            Thread.sleep(Long.parseLong(properties.getProperty("CleanUpWaitTime")));
            driver.quit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
