import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class ScenariosTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "Scenario Name", "Class Name", "Operations/Hour", "Pacing (sec)", "Users Count","%", "Active"
    };

    private final Class<?>[] columnTypes = {
            String.class, String.class, Integer.class, Integer.class, Integer.class,Integer.class, Boolean.class
    };

    private List<Scenario> scenarios = new ArrayList<>();
    private Scenario totalRow = new Scenario("TOTAL", "", 0, 0, 0, false);
    private boolean updating = false;

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = new ArrayList<>(scenarios);
        this.scenarios.add(totalRow);
        updateTotalRow();
        fireTableDataChanged();
    }

    public List<Scenario> getScenarios() {
        return new ArrayList<>(scenarios.subList(0, scenarios.size()-1));
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenarios.size()-1, scenario);
        updateTotalRow();
        fireTableDataChanged();
    }

    public void removeScenario(int rowIndex) {
        if (rowIndex < scenarios.size()-1) {
            scenarios.remove(rowIndex);
            updateTotalRow();
            fireTableDataChanged();
        }
    }

    @Override
    public int getRowCount() {
        return scenarios.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex == scenarios.size()-1) { // Для строки Total
            switch (columnIndex) {
                case 0: return totalRow.getName();
                case 2: return totalRow.getOperationsPerHour();
                case 4: return totalRow.getUsersCount();
                case 5: return calculateTotalPercentage(); // Новый метод для расчета суммы %
                default: return "";
            }
        }

        Scenario scenario = scenarios.get(rowIndex);
        switch (columnIndex) {
            case 0: return scenario.getName();
            case 1: return scenario.getClassName();
            case 2: return scenario.getOperationsPerHour();
            case 3: return scenario.getPacingTime();
            case 4: return scenario.getUsersCount();
            case 5: return calculatePercentage(scenario);
            case 6: return scenario.isActive();
            default: return null;
        }
    }
    private int calculatePercentage(Scenario scenario) {
        // Если сценарий неактивен или нет пользователей, возвращаем 0
        if (!scenario.isActive() || totalRow.getUsersCount() == 0) {
            return 0;
        }

        double percentage = (double)scenario.getUsersCount() / totalRow.getUsersCount() * 100;
        //return (int) Math.ceil(percentage);
        return (int) Math.round(percentage);
    }
    private int calculateTotalPercentage() {
        int sum = 0;
        for (int i = 0; i < scenarios.size()-1; i++) {
            sum += calculatePercentage(scenarios.get(i));
        }
        return sum;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex == scenarios.size()-1) return;

        Scenario scenario = scenarios.get(rowIndex);
        try {
            switch (columnIndex) {
                case 0:
                    scenario.setName((String)aValue);
                    break;
                case 1:
                    scenario.setClassName((String)aValue);
                    break;
                case 2:
                    int newOps = Integer.parseInt(aValue.toString());
                    scenario.setOperationsPerHour(newOps);
                    if (scenario.getPacingTime() > 0) {
                        int newUsers = (int) Math.round(newOps / (60.0 / scenario.getPacingTime() * 60));
                        scenario.setUsersCount(newUsers);
                        // Обновляем все зависимые значения
                        updateAllDependentValues(rowIndex);
                    }
                    break;
                case 3:
                    int newPacing = Integer.parseInt(aValue.toString());
                    scenario.setPacingTime(newPacing);
                    if (newPacing > 0) {
                        newOps = (int) Math.round((60.0 / newPacing * 60) * scenario.getUsersCount());
                        scenario.setOperationsPerHour(newOps);
                        // Обновляем все зависимые значения
                        updateAllDependentValues(rowIndex);
                    }
                    break;
                case 4:
                    int newUsers = Integer.parseInt(aValue.toString());
                    scenario.setUsersCount(newUsers);
                    if (scenario.getPacingTime() > 0) {
                        newOps = (int) Math.round((60.0 / scenario.getPacingTime() * 60) * newUsers);
                        scenario.setOperationsPerHour(newOps);
                    }
                    // Обновляем все зависимые значения
                    updateAllDependentValues(rowIndex);
                    break;
                case 6:
                    boolean newActive = (Boolean)aValue;
                    scenario.setActive(newActive);
                    // При деактивации обнуляем процент
                    if (!newActive) {
                        fireTableCellUpdated(rowIndex, 5);
                    }
                    updateTotalRow();
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAllDependentValues(int changedRow) {
        // Обновляем процент для измененной строки
        fireTableCellUpdated(changedRow, 5);

        // Обновляем Total
        updateTotalRow();

        // Обновляем проценты для всех строк
        for (int i = 0; i < scenarios.size()-1; i++) {
            if (i != changedRow) { // Уже обновили текущую строку
                fireTableCellUpdated(i, 5);
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (rowIndex == scenarios.size()-1) return false;
        return columnIndex != 5; // Столбец "%" не редактируемый
    }

    public void updateTotalRow() {
        if (updating) return;
        updating = true;

        try {
            int totalOps = 0;
            int totalUsers = 0;

            for (int i = 0; i < scenarios.size()-1; i++) {
                Scenario s = scenarios.get(i);
                if (s.isActive()) {
                    totalOps += s.getOperationsPerHour();
                    totalUsers += s.getUsersCount();
                }
            }

            totalRow.setOperationsPerHour(totalOps);
            totalRow.setUsersCount(totalUsers);

            // Обновляем отображение строки Total
            fireTableRowsUpdated(scenarios.size()-1, scenarios.size()-1);
        } finally {
            updating = false;
        }
    }
}