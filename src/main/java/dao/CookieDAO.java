package dao;


import org.openqa.selenium.Cookie;

import java.util.Set;

public interface CookieDAO {
    void writeCookies(String path, Set<Cookie> cookies);

    Set<Cookie> getCookies(String path);

    boolean validCookies(String path);
}
