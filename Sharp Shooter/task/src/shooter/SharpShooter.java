package shooter;

import javax.swing.*;
import java.awt.*;

public class SharpShooter extends JFrame {
    private static JLabel statusbar;

    public SharpShooter() {
        initUI();
    }

    public static void setStatusBarText(String text) {
        statusbar.setText(text);
    }

    private void initUI() {
        statusbar = new JLabel("Ready to shoot");
        statusbar.setName("Statusbar");
        add(statusbar, BorderLayout.SOUTH);
        Canvas canvas = new Canvas();
        add(canvas);
        canvas.requestFocusInWindow();
        pack();
        setTitle("Shooting Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}