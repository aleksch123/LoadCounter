import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LoadProfileCalc extends JFrame {
    private JTable scenariosTable;
    private ScenariosTableModel tableModel;
    private JButton saveButton;
    private JButton exitButton;
    private JButton addButton;
    private JButton removeButton;
    private File currentFile;
    private boolean isModified = false;
    static String classPath;

    public LoadProfileCalc() {
        // classPath= classPath == null ?"src/main/resources/":classPath;
        setTitle("Load Testing Scenarios Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        initUI();
        loadInitialData(classPath);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }


    private void initUI() {
        tableModel = new ScenariosTableModel();
        scenariosTable = new JTable(tableModel);

        // Настройка столбца Active (индекс 6)
        TableColumn activeColumn = scenariosTable.getColumnModel().getColumn(6);
        activeColumn.setCellRenderer(new DefaultTableCellRenderer() {
            private final JCheckBox checkBox = new JCheckBox();
            {
                checkBox.setHorizontalAlignment(JLabel.CENTER);
                checkBox.setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Boolean) {
                    checkBox.setSelected((Boolean)value);
                }
                if (isSelected) {
                    checkBox.setBackground(table.getSelectionBackground());
                    checkBox.setForeground(table.getSelectionForeground());
                } else {
                    checkBox.setBackground(table.getBackground());
                    checkBox.setForeground(table.getForeground());
                }
                return checkBox;
            }
        });

        activeColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));

        // Настройка столбца процентов (индекс 5)
        TableColumn percentColumn = scenariosTable.getColumnModel().getColumn(5);
        percentColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setText(value != null ? value + "%" : "0%");
                label.setHorizontalAlignment(JLabel.RIGHT);
                return label;
            }
        });
        JScrollPane scrollPane = new JScrollPane(scenariosTable);

        saveButton = new JButton("Save");
        exitButton = new JButton("Exit");
        addButton = new JButton("Add Scenario");
        removeButton = new JButton("Remove Selected");

        saveButton.addActionListener(e -> saveData());
        exitButton.addActionListener(e -> exitApplication());
        addButton.addActionListener(e -> addScenario());
        removeButton.addActionListener(e -> removeSelectedScenario());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Убрали слушатель, который вызывал рекурсию
        tableModel.addTableModelListener(e -> isModified = true);
    }




    private void loadInitialData(String path) {

        if (path==null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open Scenarios File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                loadDataFromFile(currentFile);
            } else {
                tableModel.setScenarios(new ArrayList<>());
            }
        }else {
            currentFile = new File(path+ "profile.txt");
            loadDataFromFile(currentFile);
        }
    }

    private void loadDataFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
            isModified = false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveData() {
        if (currentFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Scenarios File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                if (!currentFile.getName().toLowerCase().endsWith(".txt")) {
                    currentFile = new File(currentFile.getAbsolutePath() + ".txt");
                }
            } else {
                return;
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentFile))) {
            for (Scenario scenario : tableModel.getScenarios()) {
                if (!scenario.getName().equals("TOTAL")) {
                    writer.println(scenario.getName() + " | " +
                            scenario.getClassName() + " | " +
                            scenario.getOperationsPerHour() + " | " +
                            scenario.getPacingTime() + " | " +
                            scenario.getUsersCount() + " | " +
                            scenario.isActive());
                }
            }
            isModified = false;
            JOptionPane.showMessageDialog(this, "Data saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitApplication() {
        if (isModified) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Do you want to save changes before exiting?",
                    "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                saveData();
                dispose();
            } else if (option == JOptionPane.NO_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }

    private void addScenario() {
        Scenario newScenario = new Scenario("New Scenario", "", 0, 60, 0, true);
        tableModel.addScenario(newScenario);
        isModified = true;
        tableModel.updateTotalRow();
    }

    private void removeSelectedScenario() {
        int selectedRow = scenariosTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow != tableModel.getRowCount()-1) {
            tableModel.removeScenario(selectedRow);
            isModified = true;
            tableModel.updateTotalRow();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a scenario to remove (Total row cannot be removed)",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        if (args.length!=0) classPath=args[0];

        SwingUtilities.invokeLater(() -> {
            LoadProfileCalc app = new LoadProfileCalc();
            app.setVisible(true);
        });
    }
}