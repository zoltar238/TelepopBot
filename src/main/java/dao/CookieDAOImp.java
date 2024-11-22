package dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.Cookie;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class CookieDAOImp implements CookieDAO {
    private final Gson gson = new Gson();

    @Override
    public void writeCookies(String path, Set<Cookie> cookies) {
        try (Writer writer = new FileWriter(path)) {
            // Convertir cookies a JSON y escribir al archivo
            gson.toJson(cookies, writer);
            System.out.println("Cookies guardadas correctamente en: " + path);
        } catch (IOException e) {
            System.out.println("Error al guardar cookies: " + e.getMessage());
        }
    }

    @Override
    public Set<Cookie> getCookies(String path) {
        try (Reader reader = new FileReader(path)) {
            // Leer el JSON del archivo y convertirlo a Set de cookies
            Type setType = new TypeToken<HashSet<Cookie>>() {
            }.getType();
            Set<Cookie> cookies = gson.fromJson(reader, setType);
            System.out.println("Cookies cargadas correctamente desde: " + path);
            return cookies;
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de cookies no encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error al leer cookies: " + e.getMessage());
        }
        return new HashSet<>();
    }

    @Override
    public boolean validCookies(String path) {
        File cookieFile = new File(path);
        // Verificar si el archivo existe y no está vacío
        return cookieFile.exists() && cookieFile.length() > 0;
    }
}
