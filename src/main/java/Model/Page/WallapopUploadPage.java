package Model.Page;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static Config.BotConfig.properties;

public class WallapopUploadPage {

     WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    private static final Logger logger = LoggerFactory.getLogger(WallapopUploadPage.class);

    private final By acceptCookies = new By.ByCssSelector("#onetrust-accept-btn-handler");
    private final By productTypeOption = By.xpath("//span[text()='Algo que ya no necesito']");
    private final By titleInputField = By.id("title");
    private final By categorySelectorLabel = By.xpath("//label[text()='Categoría y subcategoría']");
    private final By firstSubcategoryOption = By.cssSelector(".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-last-child(4)");
    private final By secondSubcategoryOption = By.cssSelector(".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(14)");
    private final By priceInputField = By.xpath("//input[@id='sale_price']");
    private final By descriptionInputField = By.xpath("//textarea[@id='description']");
    private final By conditionSelector = By.xpath("//div[@class='inputWrapper inputWrapper--constricted sc-walla-text-input']");
    private final By conditionSelectorOption = By.xpath("(//walla-dropdown-item[contains(@class, 'sc-walla-dropdown-item-h') and contains(@class, 'sc-walla-dropdown-item-s')])[20]");
    private final By body = By.xpath("//tsl-multi-select-form[@class='HashtagField__suggested__multiselect ng-untouched ng-pristine ng-valid']");
    private final By hashTagsInputField = By.cssSelector("input[placeholder='Buscar o crear hashtag']");
    private final By hastTagsCheckBox = By.cssSelector(".Checkbox__mark.d-block.position-relative.m-0");
    private final By submitButtonSelector = By.xpath("(//div[contains(@class, 'col-12 col-md-6')])[4]");
    public AtomicReference<Boolean> success = new AtomicReference<>(true);

    public WallapopUploadPage(WebDriver driver) {
        this.driver = driver;
        js = (JavascriptExecutor) driver;
    }

    //accept cookies
    public void acceptCookies() {
        try {
            // Esperar hasta que el elemento esté presente por un máximo de 1 segundo
            wait = new WebDriverWait(driver, Duration.ofSeconds(1));

            // Verificar si el botón de aceptar cookies está presente y es clicable
            WebElement cookieButton = wait.until(ExpectedConditions.presenceOfElementLocated(acceptCookies));
            if (cookieButton != null && cookieButton.isDisplayed()) {
                // Si el botón está presente y visible, intentar hacer clic
                clickButton(acceptCookies, "Se han aceptado las cookies correctamente", "Las cookies ya estaban aceptadas");
            }
        } catch (TimeoutException e) {
            // Si no se encuentra el botón dentro del tiempo, se captura la excepción y se omite el clic
        }
    }

    //select product type
    public void selectProductType() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        clickButton(productTypeOption, "Tipo de producto seleccionado correctamente", "No se ha encontrado el botón de tipo de producto");
    }

    //enter title
    public void enterTitle(String title) {
        enterText(titleInputField, title, "Título insertado correctamente", "No se ha encontrado la caja de texto del título");
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //open category item box
    public void selectCategory() {
        clickButton(categorySelectorLabel, "Caja de categorías abierta correctamente", "No se ha encontrado la caja de categorías");
        clickButton(firstSubcategoryOption, "d", ",");
        clickButton(secondSubcategoryOption, "d", ",");
    }

    //enter price
    public void enterPrice() {
        enterText(priceInputField, "1", "Precio añadido correctamente", "No se pudo añadir el precio");
    }

    //enter description
    public void enterDescription(String description) {
        enterText(descriptionInputField, description, "Descripción añadida correctamente", "No se pudo añadir la descripción");
    }

    //select condition
    public void selectCondition() {
        clickButton(conditionSelector, "bien", "mal");
        clickButton(conditionSelectorOption, "bien", "mal");
    }

    //enter hashtags
    public void enterHashTags(String[] hashTags) {
        for (String hashTag : hashTags) {
            enterText(hashTagsInputField, hashTag, "Hashtag escrito correctamente", "No se pudo escribir el hashtag");
            clickButton(hastTagsCheckBox, "Check seleccionado correctamente", "No se pudo seleccionar el check");
            clickButton(body, "Se ha añadido un hashtag correctamente", "No se pudo añadir el hashtag");
        }
    }

    //upload images
    public void uploadImages(ArrayList<String> paths) {
        for (String path : paths) {
            WebElement fileInput = (WebElement) js.executeScript("return document.querySelector('.DropArea__wrapper input')");
            if (fileInput != null) {
                fileInput.sendKeys(path);
            }
            try {
                Thread.sleep(Long.parseLong(properties.getProperty("ImageUploadWaitTime")));
            } catch (InterruptedException e) {
                logger.error("Error during image upload wait time");
            }
        }
    }

    //submit item
    public void submit() {
        clickButton(submitButtonSelector, "Item añadido correctamente", "No se pudo añadir el item");
    }

    // method for entering text inside a text box
    private void enterText(By locator, String text, String successMessage, String errorMessage) {
        try {
            WebElement textField = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            textField.sendKeys(text);
            textField.sendKeys(Keys.RETURN);
            logger.info(successMessage);
        } catch (TimeoutException e) {
            success.set(false);
            logger.error(errorMessage);
        }
    }

    // method for clicking buttons
    private void clickButton(By locator, String successMessage, String errorMessage) {
        int maxAttempts = 2;
        success.set(true); // Reiniciar success en true antes del intento de clic
        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            try {
                // Esperar hasta que el elemento sea clicable
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
                button.click();
                logger.info(successMessage);
                return;  // Si tiene éxito, salir del método
            } catch (TimeoutException e) {
                System.out.println("TimeoutException: No se ha podido clickar en el intento " + (attempts + 1));
                logger.warn("{} Intento: {}", errorMessage, attempts + 1);
            } catch (NoSuchElementException e) {
                System.out.println("NoSuchElementException: El elemento no se encontró en el DOM.");
                logger.warn("No se encontró el elemento: {}", errorMessage);
            } catch (ElementClickInterceptedException e) {
                System.out.println("ElementClickInterceptedException: El clic fue interceptado por otro elemento.");
                logger.warn("El clic fue interceptado: {}", errorMessage);
                success.set(false);
            } catch (ElementNotInteractableException e) {
                System.out.println("ElementNotInteractableException: El elemento no es interactuable.");
                logger.warn("El elemento no es interactuable: {}", errorMessage);
                success.set(false);
            } catch (InvalidElementStateException e) {
                System.out.println("InvalidElementStateException: El estado del elemento no permite la interacción.");
                logger.warn("El estado del elemento no permite la interacción: {}", errorMessage);
                success.set(false);
            } catch (StaleElementReferenceException e) {
                System.out.println("StaleElementReferenceException: El elemento ya no es válido (puede haber sido recargado).");
                logger.warn("El elemento ya no es válido o ha sido recargado: {}", errorMessage);
                success.set(false);
            } catch (WebDriverException e) {
                System.out.println("WebDriverException: Un error genérico de WebDriver ha ocurrido.");
                logger.warn("Un error de WebDriver ha ocurrido: {}", errorMessage);
                success.set(false);
            } catch (Exception e) {
                System.out.println("Exception: Se produjo una excepción no esperada: " + e.getMessage());
                logger.warn("Excepción no esperada: {}", e.getMessage());
                success.set(false);
            }

            // Si no tuvo éxito en este intento, marcar success como false
            success.set(false);
        }

        // Si después de todos los intentos no tiene éxito, asegurarse de que success es false
        success.set(false);
    }

}
