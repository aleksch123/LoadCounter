

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScenarioController {
    private final ScenariosTableModel tableModel = new ScenariosTableModel();
    private int testDurationMinutes = 60;
    private File currentFile;
    private boolean isModified = false;
    private final JFrame parentFrame;

    public ScenarioController(JFrame parentFrame, String initialDirectoryPath) {
        this.parentFrame = parentFrame;
        tableModel.addTableModelListener(e -> isModified = true);
        loadInitialData(initialDirectoryPath==null?System.getProperty("user.dir"):initialDirectoryPath);
    }

    public ScenariosTableModel getTableModel() {
        return tableModel;
    }

    public int getTestDurationMinutes() {
        return testDurationMinutes;
    }

    public void updateDuration(String text) {
        try {
            int newDuration = Integer.parseInt(text);
            if (newDuration > 0) {
                testDurationMinutes = newDuration;
            } else {
                showError("Duration must be positive");
            }
        } catch (NumberFormatException ex) {
            showError("Invalid number format");
        }
    }

    public void loadInitialData(String path) {
        JFileChooser fileChooser = new JFileChooser();
        File initialDirectory = new File(path);
        fileChooser.setCurrentDirectory(initialDirectory.exists() ? initialDirectory :
                new File(System.getProperty("user.home")));

        fileChooser.setDialogTitle("Open Scenarios File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            loadDataFromFile(currentFile);
        } else {
            tableModel.setScenarios(new ArrayList<>());
        }
    }

    private void loadDataFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine.startsWith("Duration|")) {
                testDurationMinutes = Integer.parseInt(firstLine.split("\\|")[1]);
            }
            List<Scenario> scenarios = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    scenarios.add(new Scenario(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim()),
                            Integer.parseInt(parts[4].trim()),
                            Boolean.parseBoolean(parts[5].trim())
                    ));
                }
            }
            tableModel.setScenarios(scenarios);
            updateWindowTitle(file);
            isModified = false;
        } catch (IOException e) {
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void saveData() {
        if (currentFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Scenarios File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

            if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                if (!currentFile.getName().toLowerCase().endsWith(".txt")) {
                    currentFile = new File(currentFile.getAbsolutePath() + ".txt");
                }
            } else {
                return;
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentFile))) {
            writer.println("Duration|" + testDurationMinutes);
            for (Scenario scenario : tableModel.getScenarios()) {
                if (!scenario.getName().equals("TOTAL")) {
                    writer.println(String.join("|",
                            scenario.getName(),
                            scenario.getClassName(),
                            String.valueOf(scenario.getOperationsPerHour()),
                            String.valueOf(scenario.getPacingTime()),
                            String.valueOf(scenario.getUsersCount()),
                            String.valueOf(scenario.isActive())));
                }
            }
            updateWindowTitle(currentFile);
            isModified = false;
            JOptionPane.showMessageDialog(parentFrame, "Data saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
        }
    }

    public void exitApplication() {
        if (isModified) {
            int option = JOptionPane.showConfirmDialog(parentFrame,
                    "Do you want to save changes before exiting?",
                    "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                saveData();
                parentFrame.dispose();
            } else if (option == JOptionPane.NO_OPTION) {
                parentFrame.dispose();
            }
        } else {
            parentFrame.dispose();
        }
    }

    public void addScenario() {
        tableModel.addScenario(new Scenario("New Scenario", "", 0, 60, 0, true));
        isModified = true;
        tableModel.updateTotalRow();
    }

    public void removeSelectedScenario(int selectedRow) {
        if (selectedRow != -1 && selectedRow != tableModel.getRowCount() - 1) {
            tableModel.removeScenario(selectedRow);
            isModified = true;
            tableModel.updateTotalRow();
        } else {
            JOptionPane.showMessageDialog(parentFrame,
                    "Please select a scenario to remove (Total row cannot be removed)",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateWindowTitle(File file) {
        String baseTitle = "Load Testing Scenarios Calculator";
        if (file != null) {
            String fileName = file.getName().replaceFirst("[.][^.]+$", "");
            parentFrame.setTitle(baseTitle + " - Текущий профиль: " + fileName);
        } else {
            parentFrame.setTitle(baseTitle);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(parentFrame, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static List<Scenario> loadProfileFromFile(File file) {
        List<Scenario> scenarios = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    scenarios.add(new Scenario(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim()),
                            Integer.parseInt(parts[4].trim()),
                            Boolean.parseBoolean(parts[5].trim())
                    ));
                }
            }

        } catch (IOException e) {


        }
        return scenarios;
    }
}
