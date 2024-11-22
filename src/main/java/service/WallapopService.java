package service;

import dao.HashTagDAOImp;
import dao.ItemDAOImp;
import model.HashTagFileModel;
import model.ItemModel;
import model.page.WallapopUploadPage;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static config.BotConfig.properties;

public class WallapopService {
    //private WebDriver driver;
    private final ItemDAOImp itemImp = new ItemDAOImp();
    private final ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("BrowserInstances")));
    private final BlockingQueue<ItemModel> itemsQueue = new LinkedBlockingQueue<>();

    public WallapopService() {
    }

    //start sales process
    public void publishItems(List<ItemModel> itemModels) {
        //Extract hashtags
        HashTagFileModel hashTagFileModel = new HashTagFileModel();
        HashTagDAOImp hashTagDAOImp = new HashTagDAOImp();
        hashTagFileModel.setHashTags(hashTagDAOImp.extractHashTags());
        //detect if chrome is open and kill it
        if (properties.getProperty("KillChrome").equals("true")) {
            detectedChromeInstance();
        }

        // Add all items to the queue
        itemsQueue.addAll(itemModels);

        //get cookies
        Set<Cookie> savedCookies = getCookie();

        // Initialize necessary web drivers
        int drives = Integer.parseInt(properties.getProperty("BrowserInstances"));
        for (int i = 0; i < drives; i++) {
            executor.submit(() -> {
                // Initialize ChromeDriver
                WebDriver driver = initializeWebDriver();

                // Initialize WallapopUploadPage
                WallapopUploadPage wallaUpload = new WallapopUploadPage(driver);

                // Maximize window
                driver.manage().window().maximize();
                //load upload page
                driver.get("https://es.wallapop.com");
                //load cookies to driver
                savedCookies.forEach(cookie -> driver.manage().addCookie(cookie));
                driver.navigate().refresh();

                // Run until there are no items left in the queue
                while (true) {
                    // Get item from queue
                    ItemModel itemModel = itemsQueue.poll();
                    // Break loop if item is empty
                    if (itemModel == null) {
                        cleanup(driver);
                        break;
                    } else {
                        //read info file
                        String[] info = itemImp.readInfoFile(Objects.requireNonNull(itemsQueue.poll()).getInfoFile()); //{title, description}
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
                            if (wallaUpload.success.get()) wallaUpload.enterHashTags(hashTagFileModel.getHashTags());
                            if (wallaUpload.success.get()) wallaUpload.uploadImages(itemModel.getPaths());
                            if (wallaUpload.success.get()) wallaUpload.submit(info[0]);
                        } while (!wallaUpload.success.get());

                        try {
                            Thread.sleep(Long.parseLong(properties.getProperty("ItemUploadWaitTime")));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        //set file status to submitted
                        itemImp.modifyInfoFile(itemModel);
                    }
                }
            });
        }
    }

    private Set<Cookie> getCookie() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=" + properties.getProperty("UserData"));
        chromeOptions.addArguments("profile-directory=" + properties.getProperty("Profile"));
        chromeOptions.addArguments("--headless"); // headless mode
        chromeOptions.addArguments("--disable-gpu"); //disable gpu for faster loading

        WebDriver driver = new ChromeDriver(chromeOptions);

        driver.get("https://es.wallapop.com");
        //capture profile cookies
        Set<Cookie> cookies = driver.manage().getCookies();
        driver.quit();
        return cookies;
    }

    //initialize web driver
    private WebDriver initializeWebDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        //chromeOptions.addArguments("--no-sandbox");
        // get web driver
        //chromeOptions.addArguments("--headless"); // headless mode

        return new ChromeDriver(chromeOptions);
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
            System.out.println(e.getMessage());
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
