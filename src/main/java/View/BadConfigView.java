package View;

import Model.ConfigCheckEnum.ConfigCheckEnum;

import javax.swing.*;
import java.awt.*;

import static Config.BotConfig.properties;

public class BadConfigView extends JFrame {

    public BadConfigView(ConfigCheckEnum downloadPathStatus, ConfigCheckEnum userDataStatus, ConfigCheckEnum hashtagsStatus) {
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

        //create components
        JLabel downloadPathCheck = new JLabel(downloadPathStatus.toString());
        JTextField downloadPath = new JTextField(properties.getProperty("DownloadPath"));

        JLabel userDataCheck = new JLabel(userDataStatus.toString());
        JTextField userData = new JTextField(properties.getProperty("UserData"));

        JLabel hashtagsCheck = new JLabel(hashtagsStatus.toString());

        // add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        listPane.add(downloadPathCheck, gbc);

        gbc.gridx = 1;
        listPane.add(downloadPath, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        listPane.add(userDataCheck, gbc);

        gbc.gridx = 1;
        listPane.add(userData, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        listPane.add(hashtagsCheck, gbc);

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
