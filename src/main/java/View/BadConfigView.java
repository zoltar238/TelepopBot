package View;

import Model.ConfigCheckEnum.ConfigCheckEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static Config.BotConfig.properties;

public class BadConfigView extends JFrame {

    public BadConfigView(Map<ConfigCheckEnum, Boolean> configSatusMap) {
        setTitle("ConfigError");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon("src/main/resources/cow.png").getImage());


        // gridbaglayout config
        JPanel listPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        this.add(listPane);

        // GridBagConstraints config
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 0, 10); // Espacio entre los componentes

        // create and add components
        JTextField downloadPath = new JTextField(properties.getProperty("DownloadPath"));
        JTextField userData = new JTextField(properties.getProperty("UserData"));
        downloadPath.setForeground(Color.green);
        userData.setForeground(Color.green);
        JLabel label;
        int positionX = 0;
        int positionY = 0;
        for (Map.Entry<ConfigCheckEnum, Boolean> entry : configSatusMap.entrySet()) {
            label = new JLabel(entry.getKey().toString());
            gbc.gridx = positionX;
            gbc.gridy = positionY;
            if (entry.getValue()) {
                label.setForeground(Color.green);
            } else {
                label.setForeground(Color.red);
                if (positionY == 0) {
                    downloadPath.setForeground(Color.red);
                } else if (positionY == 1) {
                    userData.setForeground(Color.red);
                }
            }
            listPane.add(label, gbc);
            if (positionY > 1 && !(positionX % 2 == 0)) {
                positionY++;
                positionX = 0;
            } else if (positionY > 1) {
                positionX++;
            } else {
                positionY++;
            }
        }

        gbc.gridx = 1;
        gbc.gridy = 0;
        listPane.add(downloadPath, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        listPane.add(userData, gbc);

        // window non-resizable
        this.setResizable(false);
        // adjust size to fit components
        this.pack();
        // force first plane
        this.toFront();
        this.setAlwaysOnTop(true);
        setVisible(true);
    }
}
