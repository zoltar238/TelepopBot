package Service;

import DAO.ItemDAOImplementation;
import entity.Item;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static Config.BotConfig.properties;

public class WallapopService {
    private final ItemDAOImplementation itemImp = new ItemDAOImplementation();
    private WebDriver driver;
    private final JavascriptExecutor js;

    public WallapopService(ArrayList<Item> items) {
        //detect if chrome is open and kill it
        if (properties.getProperty("KillChrome").equals("true")) {
            detectedChromeInstance();
        }
        //initialize web driver
        initializeWebDriver();
        js = (JavascriptExecutor) driver;
        //maximize browser window
        driver.manage().window().maximize();

        for (Item item : items) {
            addItemToSale(item.getInfoFile(), item.getPaths());
            //set file status to submitted
            itemImp.modifyInfoFile(item);
        }
        //close chrome after all items have been submitted
        if (properties.getProperty("CleanUp").equals("true")) {
            cleanup();
        }
    }

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

    private void addItemToSale(File file, ArrayList<String> paths) {
        //read info file
        String[] info = itemImp.readInfoFile(file); //{title, description}

        driver.get("https://es.wallapop.com/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        clickButton(wait, "//a[text()='Vender']", "Vender encontrado", "Vender no encontrado");
        clickButton(wait, "//span[text()='Algo que ya no necesito']", "Súbelo encontrado", "Súbelo no encontrado");

        fillForm(wait, info[0], info[1], paths);
    }

    private void fillForm(WebDriverWait wait, String title, String description, ArrayList<String> paths) {
        enterText(wait, By.id("title"), title, "Label de texto no encontrado");
        selectCategory(wait);
        enterPrice(wait);
        enterDescription(wait, description);
        selectCondition(wait);
        selectHashtags(wait);
        uploadImages(wait, paths);
        submitProduct(wait);
    }

    private void enterText(WebDriverWait wait, By locator, String text, String errorMessage) {
        try {
            WebElement textField = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            textField.sendKeys(text);
            textField.sendKeys(Keys.RETURN);
        } catch (TimeoutException e) {
            System.out.println(errorMessage);
        }
    }

    private void clickButton(WebDriverWait wait, String xpath, String successMessage, String errorMessage) {
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            button.click();
            System.out.println(successMessage);
        } catch (TimeoutException e) {
            System.out.println(errorMessage);
        }
    }

    private void selectCategory(WebDriverWait wait) {
        clickButton(wait, "//label[text()='Categoría y subcategoría']", "Súbelo encontrado", "Seleccion de categoria no encontrado");

        // Selecting category
        try {
            WebElement panel1 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(11)\");");
            panel1.click();
            WebElement panel2 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(14)\");");
            panel2.click();
        } catch (Exception e) {
            System.out.println("Error selecting category or subcategory");
        }
    }

    private void enterPrice(WebDriverWait wait) {
        clickButton(wait, "//label[text()='Precio']", "Súbelo encontrado", "Súbelo no encontrado");
        enterText(wait, By.xpath("//input[@id='sale_price']"), "1", "Precio no encontrado");
    }

    private void enterDescription(WebDriverWait wait, String description) {
        clickButton(wait, "//label[text()='Descripción']", "Súbelo encontrado", "Súbelo no encontrado");
        enterText(wait, By.xpath("//textarea[@id='description']"), description, "Descripción no encontrada");
    }

    private void selectCondition(WebDriverWait wait) {
        try {
            WebElement button = (WebElement) js.executeScript("return document.querySelectorAll('.walla-text-input__label.sc-walla-text-input')[8]");
            button.click();
            button = (WebElement) js.executeScript("return document.querySelectorAll('.sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated')[19]");
            button.click();
            System.out.println("Súbelo encontrado");
        } catch (Exception e) {
            System.out.println("Súbelo no encontrado");
        }
    }

    private void selectHashtags(WebDriverWait wait) {
        String[] hashTags = extractHashTags();
        try {
            WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tsl-multi-select-form[@class='HashtagField__suggested__multiselect ng-untouched ng-pristine ng-valid']")));
            WebElement placeHolder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Buscar o crear hashtag']")));

            for (String hashTag : hashTags) {
                placeHolder.sendKeys(hashTag);
                WebElement checkBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Checkbox__mark.d-block.position-relative.m-0")));
                checkBox.click();
                body.click();
            }
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
    }

    private String[] extractHashTags(){
        try {
            // read entire file and return array of hashtags.txt
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/hashtags.txt"));
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadImages(WebDriverWait wait, ArrayList<String> paths) {
        try {
            System.out.println(paths.size());
            for (String path : paths) {
                WebElement fileInput = (WebElement) js.executeScript("return document.querySelector('.DropArea__wrapper input')");

                System.out.println("Uploading: " + path);
                fileInput.sendKeys(path);
                //delay between image uploaded
                Thread.sleep(Long.parseLong(properties.getProperty("ImageUploadWaitTime")));
            }
        } catch (Exception e) {
            System.out.println("Error uploading images");
        }
    }

    private void submitProduct(WebDriverWait wait) {
        try {
            WebElement submitButton = (WebElement) js.executeScript("return document.querySelectorAll('.col-12.col-md-6')[4]");
            submitButton.click();
            Thread.sleep(Long.parseLong(properties.getProperty("ItemUploadWaitTime")));
        } catch (Exception e) {
            System.out.println("Error submitting product");
        }
    }

    private void cleanup() {
        try {
            Thread.sleep(Long.parseLong(properties.getProperty("CleanUpWaitTime")));
            driver.quit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //detect chrome instance and kill it
    private void detectedChromeInstance(){
        String processName = "chrome.exe";
        try {
            // execute tasklist in search for Chrome
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
}
