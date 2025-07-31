import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class LoadCalcApp extends JFrame {
    private final ScenarioController controller;
    private final ScenarioView view;

    private static String initialDirectoryPath;

    public LoadCalcApp() {
        setTitle("Load Testing Scenarios Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        this.controller = new ScenarioController(this, initialDirectoryPath);
        this.view = new ScenarioView(controller);
        add(view.getMainPanel());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.exitApplication();
            }
        });
    }

    public static void main(String[] args) {
        if (args.length != 0) initialDirectoryPath = args[0];
        SwingUtilities.invokeLater(() -> new LoadCalcApp().setVisible(true));
    }
}