package Pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = https://es.wallapop.com/
public class WallapopHomepage {
    public WallapopHomepage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}