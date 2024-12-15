package model.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static config.BotConfig.properties;

@Slf4j
public class WallapopUploadPage {

    private final WebDriver driver;
    private WebDriverWait wait;
    private final JavascriptExecutor js;

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
                clickButton(acceptCookies, "Las cookies ya estaban aceptadas");
            }
        } catch (TimeoutException e) {
            // Si no se encuentra el botón dentro del tiempo, se captura la excepción y se omite el clic
        }
    }

    //select product type
    public void selectProductType() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        clickButton(productTypeOption, "No se ha encontrado el botón de tipo de producto");
    }

    //enter title
    public void enterTitle(String title) {
        enterText(titleInputField, title + Keys.ENTER);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //open category item box
    public void selectCategory() {
        clickButton(categorySelectorLabel, "No se ha encontrado la caja de categorías");
        clickButton(firstSubcategoryOption, "No se pudo seleccionar la categoria");
        clickButton(secondSubcategoryOption, "No se pudo seleccionar la subcategoria");
    }

    //enter price
    public void enterPrice() {
        enterText(priceInputField, "1");
    }

    //enter description
    public void enterDescription(String description) {
        enterText(descriptionInputField, description);
    }

    //select condition
    public void selectCondition() {
        clickButton(conditionSelector, "No se pudo abrir el menu de condicion");
        clickButton(conditionSelectorOption, "No se pudo selecionar la condicion");
    }

    //enter hashtags
    public void enterHashTags(String[] hashTags) {
        for (String hashTag : hashTags) {
            enterText(hashTagsInputField, hashTag);
            clickButton(hastTagsCheckBox, "No se pudo seleccionar el check");
            clickButton(body, "No se pudo añadir el hashtag");
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
                System.out.println("Error during image upload wait time");
            }
        }
    }

    //submit item
    public void submit(String name) {
        clickButton(submitButtonSelector, "No se pudo añadir el item");
        log.info("{} añadido correctamente", name);
    }

    // method for entering text inside a text box
    private void enterText(By locator, String text) {
        try {
            WebElement textField = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            if (textField.isDisplayed() && textField.isEnabled()) {
                textField.clear();
                textField.sendKeys(text);
            } else {
                log.warn("El campo ubicado por {} no está visible o habilitado.", locator);
                success.set(false);
            }
        } catch (TimeoutException e) {
            log.warn("TimeoutException: No se encontró el elemento '{}' a tiempo. Detalles: {}", locator, e.getMessage());
            success.set(false);
        } catch (Exception e) {
            log.warn("Error inesperado al interactuar con el campo ubicado por {}. Detalles: {}", locator, e.getMessage(), e);
            success.set(false);
        }
    }


    // method for clicking buttons
    private void clickButton(By locator, String errorMessage) {
        int maxAttempts = 2;
        success.set(true); // reset success before attempting to click
        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            try {
                // wait until the element is clickable
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
                button.click();
                return; //exit if successful
            } catch (TimeoutException | NoSuchElementException | ElementClickInterceptedException e) {
                log.warn("Error de interacción con el elemento: {} - Excepción: {}", errorMessage, e.getClass().getSimpleName(), e);
                success.set(false);
            } catch (ElementNotInteractableException e) {
                log.warn("ElementNotInteractableException: El elemento no es interactuable. {} - Detalles: {}", errorMessage, e.getMessage());
                success.set(false);
            } catch (InvalidElementStateException e) {
                log.warn("InvalidElementStateException: Estado inválido para interactuar con el elemento. {} - Detalles: {}", errorMessage, e.getMessage());
                success.set(false);
            } catch (StaleElementReferenceException e) {
                log.warn("StaleElementReferenceException: El elemento ya no es válido (posiblemente recargado). {} - Detalles: {}", errorMessage, e.getMessage());
                success.set(false);
            } catch (WebDriverException e) {
                log.warn("WebDriverException: Un error genérico de WebDriver ocurrió. {} - Detalles: {}", errorMessage, e.getMessage(), e);
                success.set(false);
            } catch (Exception e) {
                log.warn("Exception inesperada: {} - Detalles: {}", errorMessage, e.getMessage(), e);
                success.set(false);
            }

            // Set success status as false
            success.set(false);
        }

        // Set success status as false
        success.set(false);
    }

}
