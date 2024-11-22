package controller;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import dao.CookieDAO;
import dao.CookieDAOImp;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class CookiesController {

    // todo: review logging

    private volatile Page page;

    // todo: corregir error al cerrar el navegador
    // Launch Chromium browser to get cookies
    @Synchronized
    @SneakyThrows
    public void launchChromiumBrowser() {
        // Create a Playwright instance
        Thread thread = new Thread(() -> {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)
                         .setArgs(List.of("--disable-blink-features=AutomationControlled")));
            ) {

                // Open a new page
                page = browser.newPage();

                // Navigate to Wallapop login page (or any desired URL)
                page.navigate("https://es.wallapop.com/app/catalog/upload");

                // Wait for user interaction (e.g., login)
                log.info("Navegador lanzado correctamente");

                // Wait until the user closes the browser
                page.waitForTimeout(200000);

            } catch (Exception e) {
                // todo: imporove error handling here
                log.error("Error en el navegador: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });

        thread.start();
    }

    @Synchronized
    // Method to capture and print cookies from the page
    public void setCookies() {
        CookieDAO cookieDAO = new CookieDAOImp();
        try {
            // Retrieve cookies from the page
            java.util.List<Cookie> cookies = page.context().cookies();  // Get cookies from the browser context

            // Print cookies to the console
            if (cookies.isEmpty()) {
                log.error("No se han encontrado cookies");
            } else {
                String cookieJsonPath = "src/main/resources/cookies.json";
                if (!new File(cookieJsonPath).exists()) {
                    cookieJsonPath = "cookies.json";
                }

                // Save cookies
                log.info("Cookies guardadas correctamente");
                cookieDAO.writeCookies(cookieJsonPath, cookies);
            }
        } catch (Exception e) {
            log.error("Error intentando capturar cookies: {}", e.getMessage());
        }
    }

    @Synchronized
    // Method to close the browser
    public void closeBrowser() {
        try {
            page.close();
            log.info("Navegador cerrado correctamente");
        } catch (Exception e) {
            log.error("Error intentando cerrar el browser: {}", e.getMessage());
        }
    }
}
