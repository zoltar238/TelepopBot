package Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;

import static Config.BotConfig.properties;

// page_url = https://es.wallapop.com/app/catalog/upload
public class WallapopUploadPage {

    WebDriver driver;
    WebDriverWait wait;

    By productTypeOption = By.xpath("//span[text()='Algo que ya no necesito']");
    By titleInputField = By.id("title");
    By categorySelectorLabel = By.xpath("//label[text()='Categoría y subcategoría']");
    //original
    //WebElement panel1 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(11)\");");
    By firstSubcategoryOption = By.cssSelector(".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(11)");
    //original
    //WebElement panel2 = (WebElement) js.executeScript("return document.querySelector(\".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(14)\");");
    By secondSubcategoryOption = By.cssSelector(".sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated:nth-child(14)");
    By priceInputField = By.xpath("//input[@id='sale_price']");
    By descriptionInputField = By.xpath("//textarea[@id='description']");
    //original
    //WebElement button = (WebElement) js.executeScript("return document.querySelectorAll('.walla-text-input__label.sc-walla-text-input')[8]");
    By conditionSelector =  By.xpath("(//label[contains(@class, 'walla-text-input__label') and contains(@class, 'sc-walla-text-input')])[9]");
    //original
    //button = (WebElement) js.executeScript("return document.querySelectorAll('.sc-walla-dropdown-item-h.sc-walla-dropdown-item-s.hydrated')[19]");
    By conditionSelectorOption = By.xpath("(//walla-dropdown-item[contains(@class, 'sc-walla-dropdown-item-h') and contains(@class, 'sc-walla-dropdown-item-s')])[20]");
    By body = By.xpath("//tsl-multi-select-form[@class='HashtagField__suggested__multiselect ng-untouched ng-pristine ng-valid']");
    By hashTagsInputField = By.cssSelector("input[placeholder='Buscar o crear hashtag']");
    By hastTagsCheckBox = By.cssSelector(".Checkbox__mark.d-block.position-relative.m-0");
    By submitButtonSelector =By.xpath("(//div[contains(@class, 'col-12 col-md-6')])[4]");


    public WallapopUploadPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    //select product type
    public void clickProductType() {
        clickButton(productTypeOption, "Tipo de producto seleccionado correctamente", "No se ha entontrado el boton de tipo de producto");
    }

    //enter title
    public void enterTitle(String title) {
        enterText(titleInputField, title, "Titulo insertado correctamente", "No se ha encontrado la caja de texto del titulo");
    }

    //open category item box
    public void selectCategory() {
        clickButton(categorySelectorLabel, "Caja de categorías abierta correctamente", "No se ha encontrado la caja de categorías");
        clickButton(firstSubcategoryOption, "Categoria seleccionada correctamente", "La categoría no pudo ser seleccionada");
        clickButton(secondSubcategoryOption, "Subcategoria seleccionada correctamente", "La subcategoría no pudo ser seleccionada");
    }

    //enter price
    public void enterPrice() {
        enterText(priceInputField, "1", "Precio añadido correctamente", "No se pudo añadir el precio");
    }

    //enter description
    public void enterDescription() {
        enterText(descriptionInputField, "Description", "Descripción añadida correctamente", "No se pudo añadir la descripción");
    }

    //select condition
    public void selectCondition() {
        clickButton(conditionSelector, "Menu de condición abierto correctamente", "No se pudo abrir el menu de condición");
        clickButton(conditionSelectorOption, "Categoria seleccionada correctamente", "No se pudo seleccionar la categoria");
    }

    //enter hashTags
    public void enterHashTags(String[] hashTags) {
        for (String hashTag : hashTags) {
            enterText(hashTagsInputField, hashTag, "Hashtag escrito correctamente", "Hashtag no se pudo escribir");
            clickButton(hastTagsCheckBox, "Check seleccionado correctamente", "No se pudo selecionar el check");
            clickButton(body, "Se ha añadido un hashtag correctamente", "No se pudo añadir el hastag");
        }
    }

    //upload images
    public void uploadImages(ArrayList<String> paths) {
        for (String path : paths) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement fileInput = (WebElement) js.executeScript("return document.querySelector('.DropArea__wrapper input')");

            fileInput.sendKeys(path);
            //delay between image uploaded
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
            System.out.println(successMessage);
        } catch (TimeoutException e) {
            System.out.println(errorMessage);
        }
    }

    // method for clicking buttons
    private void clickButton(By locator, String successMessage, String errorMessage) {
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            System.out.println(successMessage);
        } catch (TimeoutException e) {
            System.out.println(errorMessage);
        }
    }

}