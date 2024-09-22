package Service;

import entity.Item;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

import static Config.BotConfig.properties;

public class WallapopService {
    public WallapopService(ArrayList<Item> items) {
        // Create webDriver
        WebDriver driver;
        switch (properties.getProperty("WebDriver")){
            case "Firefox": {
                System.setProperty("webdriver.firefox.driver", "src/main/resources/geckodriver.exe");
                FirefoxOptions options = new FirefoxOptions();
                options.setProfile(new FirefoxProfile(new File(properties.getProperty("UserData"))));
                //options.addArguments("user-data-dir=" + properties.getProperty("UserData"));
                options.addArguments("user-agent=" + properties.getProperty("UserAgent"));
                driver = new FirefoxDriver(options);
                break;
            }
            case "Chrome": {
                System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
                ChromeOptions options = new ChromeOptions();
                options.addArguments("user-data-dir=" + properties.getProperty("UserData"));
                options.addArguments("user-agent=" + properties.getProperty("UserAgent"));
                driver = new ChromeDriver(options);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + properties.getProperty("WebDriver"));
        }

        //maximize window
        driver.manage().window().maximize();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // publish all the items
        for (Item item: items) {
            addItemToSale(driver, js, item.getFile(), item.getPaths());
        }
        try {
            JOptionPane.showMessageDialog(null, "Proceso terminado", "Información", JOptionPane.INFORMATION_MESSAGE);
            Thread.sleep(3500);
            driver.quit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void addItemToSale(WebDriver driver, JavascriptExecutor js, File file, ArrayList<String> paths) {
        String title = "";
        String description = "";
        StringBuilder concatPaths = new StringBuilder();
        try {
            //read title and description
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {
                if (count == 1) {
                    title = line;
                }
                if (count == 2) {
                    description = line;
                }
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        driver.get("https://es.wallapop.com/");
        //espera para que se carguen todos los elementos de la pagina
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        //intenta pulsar el boton vender
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Vender']")));
            button.click();
            System.out.println("Vender encontrado");
        } catch (TimeoutException e) {
            System.out.println("Vender no encontrado");
        }
        //avanzando a la siguiente pagina
        //selección tipo producto
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Algo que ya no necesito']")));
            button.click();
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
        //inserción del titulo
        try {
            WebElement textField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
            textField.sendKeys(title);
            textField.sendKeys(Keys.RETURN);
        } catch (TimeoutException e) {
            System.out.println("Label de texto no encontrado");
        }
        //seleccion de categoria
        try {
            WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Categoría y subcategoría']")));
            js.executeScript("arguments[0].scrollIntoView(true);", button);
            button.click();
            //seleccion categoria collecionismo
            WebElement panel1 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown.walla-floating-area__wrapper--hidden.walla-floating-area__wrapper--full-size.hydrated > .walla-dropdown__floating-area.sc-walla-dropdown.sc-walla-dropdown-s > .sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(11)\");");
            panel1.click();
            //seleccion subcategoría coleccionismo
            WebElement panel2 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown.walla-floating-area__wrapper--hidden.walla-floating-area__wrapper--full-size.hydrated > .walla-dropdown__floating-area.sc-walla-dropdown.sc-walla-dropdown-s > .sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(14)\");");
            panel2.click();
        } catch (TimeoutException e) {
            e.printStackTrace();
            System.out.println("Seleccion de categoria no encontrado");
        }
        //selección del precio
        try {
            WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Precio']")));
            button.click();
            WebElement textField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='sale_price']")));
            textField.sendKeys("1");
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
        //selección de la descripcion
        try {
            WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Descripción']")));
            button.click();
            WebElement textField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//textarea[@id='description']")));
            textField.sendKeys(description);
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
        //seleccion de estado
        try {
            WebElement button = (WebElement) js.executeScript("return document.querySelectorAll('.walla-text-input__label.sc-walla-text-input')[8]");
            button.click();
            button = (WebElement) js.executeScript("return document.querySelectorAll('.sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated')[19]");
            button.click();
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
        //selección de hashtags
        try {
            WebElement marcoHashtags = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".HashtagField__search__box.HashtagField__search__box--design-system.p-2")));
            // Ejecutar JavaScript para hacer scroll y centrar el botón en la pantalla
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", marcoHashtags);
            //selección de hashtags
            String[] hashTags = {"FigurasPersonalizadas", "Impresion3D", "Modelos3D", "Resina", "Arte3D"};
            seleccionHashtags(wait, hashTags);
            System.out.println("Súbelo encontrado");
        } catch (TimeoutException e) {
            System.out.println("Súbelo no encontrado");
        }
        //seleccion imagenes
        try {
            WebElement fileInput = (WebElement) js.executeScript("return document.querySelector('.DropArea__wrapper input')");
            for (String path : paths) {
                concatPaths.append(new File(path).getAbsolutePath()).append("\n");
            }
            String resultPaths =  concatPaths.toString().replaceFirst("\\n$", "");
            fileInput.sendKeys(resultPaths);
        } catch (TimeoutException e){
            e.printStackTrace();
        }
        //scroll all the way down
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        //selección subir producto
        try {
            WebElement botonSubir = (WebElement) js.executeScript("return document.querySelectorAll('.col-12.col-md-6')[4]");
            botonSubir.click();
        } catch (TimeoutException e){
            e.printStackTrace();
        }
    }

    private void seleccionHashtags(WebDriverWait wait, String[] hashTags){
        WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tsl-multi-select-form[@class='HashtagField__suggested__multiselect ng-untouched ng-pristine ng-valid']")));
        WebElement placeHolder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Buscar o crear hashtag']")));
        WebElement checkBox;

        for (String hashTag : hashTags){
            placeHolder.sendKeys(hashTag);
            checkBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Checkbox__mark.d-block.position-relative.m-0")));
            checkBox.click();
            body.click();
        }
    }

}
