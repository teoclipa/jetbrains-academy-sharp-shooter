package shooter;

import javax.swing.*;

public class ApplicationRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new SharpShooter();
            frame.setVisible(true);
        });
    }
}
