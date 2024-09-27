package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = https://es.wallapop.com/
public class WallapopHomepage {

    WebDriver driver;
    WebDriverWait wait;

    By saleButton = new By.ByCssSelector(".anchor-button_AnchorButton__gJxoN.anchor-button_AnchorButton--upload__CMTDX.align-items-center");

    public WallapopHomepage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void clickSale(){
        //driver.findElement(saleButton).click();
        WebElement saleButtonElement = wait.until(ExpectedConditions.elementToBeClickable(saleButton));
        saleButtonElement.click();
    }
}