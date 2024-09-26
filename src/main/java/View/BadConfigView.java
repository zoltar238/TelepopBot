package View;

import javax.swing.*;
import java.awt.*;

public class BadConfigView extends JFrame {

    public BadConfigView(){
        setTitle("ConfigError");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        //boxLayout: every element is placed bellow the previous one
        JPanel listPane = new JPanel();
        this.add(listPane);
        listPane.setLayout(new GridLayout(3, 2));
        //add textBoxes to the layout
        JLabel label = new JLabel("ConfigError");
        JLabel label2 = new JLabel("ConfigError");

        JTextField textArea1 = new JTextField("sdf");
        JTextField textArea2 = new JTextField("C:\\Users\\dabac\\Proton Drive\\Protoandrei\\My files\\TelepopBot\\downloads");
        listPane.add(label);
        listPane.add(label2);
        listPane.add(textArea1);
        listPane.add(textArea2);
        this.pack();
        setVisible(true);
    }
}
