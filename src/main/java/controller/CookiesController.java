package controller;

import dao.CookieDAO;
import dao.CookieDAOImp;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;

import java.util.Set;

@Slf4j
public class CookiesController {

    private final CookieDAO cookieDAO = new CookieDAOImp();

    public void writeCookies(String path, Set<Cookie> cookies) {
        cookieDAO.writeCookies(path, cookies);
    }

    public Set<Cookie> getCookies(String path) {
        return cookieDAO.getCookies(path);
    }

    public boolean validCookies(String path) {
        return cookieDAO.validCookies(path);
    }
}
