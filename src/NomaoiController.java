package nomaoi;

import java.awt.Dimension;
import javax.swing.*;

public class NomaoiController implements Runnable {
    public NomaoiController() {}

    public void createAndShowGui() {
        JFrame frame = createFrame();
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("NomaoiKeyboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        return frame;
    }

    private JPanel createPane() {
        JPanel pane = new JPanel();
        pane.setPreferredSize(new Dimension(320, 240));
        return pane;
    }

    public void run() {
        createAndShowGui();
    }

    public static void main(String[] args) {
        NomaoiController app = new NomaoiController();
        SwingUtilities.invokeLater(app);
    }
}
