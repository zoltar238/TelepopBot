package Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = https://es.wallapop.com/app/catalog/upload
public class WallapopUploadPage {

    WebDriver driver;
    WebDriverWait wait;

    By productTypeButton = By.xpath("\"//span[text()='Algo que ya no necesito']\"");
    By titleTextBox = By.id("title");
    By categorySelectorButton = By.xpath("\"//label[text()='Categoría y subcategoría']\"");

    public WallapopUploadPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    //select product type
    public void clickProductType() {
        clickButton(productTypeButton, "Tipo de producto seleccionado correctamente", "No se ha entontrado el boton de tipo de producto");
    }

    //enter title
    public void enterTitle(String title) {
        enterText(titleTextBox, title, "Titulo insertado correctamente", "No se ha encontrado la caja de texto del titulo");
    }

    //open category item box
    public void clickCategorySelector(){
        clickButton(categorySelectorButton, "Caja de categorías abierta correctamente", "No se ha encontrado la caja de categorías");
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