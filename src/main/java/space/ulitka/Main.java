package space.ulitka;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.load(Main.class.getClassLoader().getResourceAsStream("app.properties"));
        String version = props.getProperty("app.version");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(I18n.get("title.main") + version);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new HotbarPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}