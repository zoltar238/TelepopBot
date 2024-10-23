package View;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class SystemTrayIcon {
    public SystemTrayIcon() {
        if (SystemTray.isSupported()) {
            // Get system tray object
            SystemTray tray = SystemTray.getSystemTray();

            try {
                // Load the image from the classpath
                InputStream imageStream = getClass().getClassLoader().getResourceAsStream("cow.png");
                if (imageStream == null) {
                    System.err.println("Image file not found in classpath.");
                    return;
                }
                Image image = ImageIO.read(imageStream);
                // Create TrayIcon instance
                TrayIcon trayIcon = new TrayIcon(image);

                // Auto-size the icon
                trayIcon.setImageAutoSize(true);

                // Attach TrayIcon to SystemTray
                tray.add(trayIcon);

                //create pop-up menu
                PopupMenu popup = getPopupMenu();

                trayIcon.setPopupMenu(popup);

            } catch (IOException e) {
                System.err.println("Error loading the image: " + e.getMessage());
            } catch (AWTException e) {
                System.err.println("Error adding the tray icon: " + e.getMessage());
            }
        } else {
            System.err.println("System tray is not supported on this platform.");
        }
    }

    private static PopupMenu getPopupMenu() {
        PopupMenu popup = new PopupMenu();

        // Create a pop-up menu components
        MenuItem exitItem = new MenuItem("Apagar");
        exitItem.addActionListener(e -> System.exit(0));

        //add components to pop-up menu
        popup.add(exitItem);
        return popup;
    }
}
