package View;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SystemTrayIcon {
    public SystemTrayIcon() {
        if (SystemTray.isSupported()) {
            // Get system tray object
            SystemTray tray = SystemTray.getSystemTray();

            try {
                // Load the image from file
                Image image = ImageIO.read(new File("src/main/resources/cow.png"));

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
        MenuItem aboutItem = new MenuItem("About");
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        Menu displayMenu = new Menu("Display");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);
        popup.add(exitItem);
        return popup;
    }
}
