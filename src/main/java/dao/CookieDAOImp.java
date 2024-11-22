package dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import model.CookieModel;
import model.CookieModel2;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CookieDAOImp implements CookieDAO {
    @Override
    public void writeCookies(String path, List<Cookie> cookies) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(path), cookies);
    }

    @Override
    public List<Cookie> getCookies(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Reading cookies from file: " + path);

        try {
            // Read the JSON file into a list of CookieModel
            List<CookieModel> customCookies = objectMapper.readValue(new File(path), new TypeReference<>() {
            });

            // Map CookieModel to Playwright Cookie objects
            return customCookies.stream()
                    .map(customCookie -> {
                        // Create a new Playwright Cookie
                        Cookie cookie = new Cookie(customCookie.name, customCookie.value);

                        // Set optional fields if present in CookieModel
                        if (customCookie.url != null) {
                            cookie.setUrl(customCookie.url);
                        }
                        if (customCookie.domain != null) {
                            cookie.setDomain(customCookie.domain);
                        }
                        if (customCookie.path != null) {
                            cookie.setPath(customCookie.path);
                        }
                        if (customCookie.expires != null) {
                            cookie.setExpires(customCookie.expires);
                        }
                        if (customCookie.httpOnly != null) {
                            cookie.setHttpOnly(customCookie.httpOnly);
                        }
                        if (customCookie.secure != null) {
                            cookie.setSecure(customCookie.secure);
                        }
                        if (customCookie.sameSite != null) {
                            if (customCookie.sameSite.equals("LAX")) {
                                cookie.setSameSite(SameSiteAttribute.LAX);
                            }
                            if (customCookie.sameSite.equals("NONE")) {
                                cookie.setSameSite(SameSiteAttribute.NONE);
                            }
                            if (customCookie.sameSite.equals("STRICT")) {
                                cookie.setSameSite(SameSiteAttribute.STRICT);
                            }
                        }

                        // Return the constructed Cookie
                        return cookie;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading cookies from file: " + path, e);
        }
    }

    @Override
    public List<Cookie> getCookies2(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Reading cookies from file: " + path);

        try {
            // Leer el archivo JSON en una lista de CookieModel2
            List<CookieModel2> customCookies = objectMapper.readValue(new File(path), new TypeReference<>() {
            });

            // Mapear CookieModel2 a objetos Cookie de Playwright
            return customCookies.stream()
                    .map(customCookie -> {
                        // Crear un nuevo objeto Cookie de Playwright
                        Cookie cookie = new Cookie(customCookie.getName(), customCookie.getValue());

                        // Establecer los campos opcionales si están presentes en CookieModel2
                        if (customCookie.getDomain() != null) {
                            cookie.setDomain(customCookie.getDomain());
                        }
                        if (customCookie.getPath() != null) {
                            cookie.setPath(customCookie.getPath());
                        }
                        if (customCookie.getStoreId() != null) {
                            // No hay un equivalente directo en Playwright para storeId, así que puedes ignorarlo
                        }
                        if (customCookie.getExpirationDate() > 0) {
                            cookie.setExpires(customCookie.getExpirationDate());
                        }
                        if (customCookie.isHttpOnly()) {
                            cookie.setHttpOnly(true);
                        }
                        if (customCookie.isSecure()) {
                            cookie.setSecure(true);
                        }
                        if (customCookie.getSameSite() != null) {
                            switch (customCookie.getSameSite()) {
                                case "LAX":
                                    cookie.setSameSite(SameSiteAttribute.LAX);
                                    break;
                                case "NONE":
                                    cookie.setSameSite(SameSiteAttribute.NONE);
                                    break;
                                case "STRICT":
                                    cookie.setSameSite(SameSiteAttribute.STRICT);
                                    break;
                                default:
                                    // Si el valor no coincide, no se asigna el atributo SameSite
                                    break;
                            }
                        }

                        // Devolver la cookie construida
                        return cookie;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading cookies from file: " + path, e);
        }
    }


}
