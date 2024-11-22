package dao;

import com.microsoft.playwright.options.Cookie;

import java.io.IOException;
import java.util.List;

public interface CookieDAO {
    void writeCookies(String path, List<Cookie> cookies) throws IOException;

    List<Cookie> getCookies(String path);

    List<Cookie> getCookies2(String path);
}
