import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ScenarioView {
    private final JPanel mainPanel;
    private final JTable scenariosTable;
    private final ScenariosTableModel tableModel;
    private  JTextField durationField;

    public ScenarioView(ScenarioController controller) {
        mainPanel = new JPanel(new BorderLayout());
        tableModel = controller.getTableModel();
        scenariosTable = new JTable(tableModel);

        initControlPanel(controller);
        initTable();
        initButtonPanel(controller);
    }

    private void initControlPanel(ScenarioController controller) {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationField = new JTextField(5);
        durationField.setText(String.valueOf(controller.getTestDurationMinutes()));
        durationField.addActionListener(e -> controller.updateDuration(durationField.getText()));

        durationField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                controller.updateDuration(durationField.getText());
            }
        });

        controlPanel.add(new JLabel("Продолжительность теста, мин:"));
        controlPanel.add(durationField);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        // Настройка столбца Active
        TableColumn activeColumn = scenariosTable.getColumnModel().getColumn(6);
        activeColumn.setCellRenderer(new BooleanCellRenderer());
        activeColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));

        // Настройка столбца процентов
        TableColumn percentColumn = scenariosTable.getColumnModel().getColumn(5);
        percentColumn.setCellRenderer(new PercentageCellRenderer());

        mainPanel.add(new JScrollPane(scenariosTable), BorderLayout.CENTER);
    }

    private void initButtonPanel(ScenarioController controller) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createButton("Add Scenario", e -> controller.addScenario()));
        buttonPanel.add(createButton("Remove Selected",
                e -> controller.removeSelectedScenario(scenariosTable.getSelectedRow())));
        buttonPanel.add(createButton("Save", e -> controller.saveData()));
        buttonPanel.add(createButton("Exit", e -> controller.exitApplication()));
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    // Custom Cell Renderers
    private static class BooleanCellRenderer extends DefaultTableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();

        public BooleanCellRenderer() {
            checkBox.setHorizontalAlignment(JLabel.CENTER);
            checkBox.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Boolean) {
                checkBox.setSelected((Boolean) value);
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
    }

    private static class PercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setText(value != null ? value + "%" : "0%");
            label.setHorizontalAlignment(JLabel.RIGHT);
            return label;
        }
    }
}
