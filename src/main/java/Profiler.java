import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class Profiler {
    List<Scenario> profile;
    String path;

    public Profiler(String path){
        this.path=path;
        this.profile = ScenarioController.loadProfileFromFile(new File(path));
    }
    public Integer getUserCount(String scenarioName){

        Optional<Integer> result =
                profile.stream()
                        .filter(p -> p.getClassName().equals(scenarioName))
                        .map(Scenario::getUsersCount)
                        .findFirst();

        return result.orElse(-1); // если не найдено возвращаем -1
    }
    public int getScenarioPace(String scenarioName){
        Optional<Integer> result =
                profile.stream()
                        .filter(p -> p.getClassName().equals(scenarioName))
                        .map(Scenario::getPacingTime)
                        .findFirst();

        return result.orElse(-1); // если не найдено возвращаем -1
    }

    public Integer getTestDurationInMinutes(){
        return loadTestDurationTimeFromFile(path);
    }
    public void printScenariosAsTable() {

        final int columnWidth = 18; // ширина каждого столбца

        // Подсчет общих показателей
        long totalOps = profile.stream().filter(Scenario::isActive).mapToInt(Scenario::getOperationsPerHour).sum(); // общее число операций
        long totalUsers = profile.stream().filter(Scenario::isActive).mapToInt(Scenario::getUsersCount).sum();       // общее число пользователей

        // Шапка таблицы
        System.out.printf("%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "s%n",
                "Имя", "Класс", "Операций/ч.", "Пейсинг", "Пользователи", "Активность");
        drawSeparator((columnWidth * 6) + 5); // разделительная линия под шапкой

        for (Scenario s : profile) {
            String activitySymbol = s.isActive() ? "✔️" : "-"; // отображение активности

            System.out.printf("%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "d|%-" + columnWidth + "d|%-" + columnWidth + "d|%-" + columnWidth + "s%n",
                    s.getName(), s.getClassName(), s.getOperationsPerHour(), s.getPacingTime(), s.getUsersCount(), activitySymbol);
        }
        drawSeparator((columnWidth * 6) + 5); // разделительная линия под таблицей

        // Итоговая строка Total
        System.out.printf("%-" + columnWidth + "s|%-" + columnWidth + "s|%-" + columnWidth + "d|%-" + columnWidth + "s|%-" + columnWidth + "d|%-" + columnWidth + "s%n",
                "Total", "", totalOps, "", totalUsers, "");
    }

        private  void drawSeparator(int width) {
            char[] separator = new char[width];
            java.util.Arrays.fill(separator, '-');
            System.out.println(new String(separator));
        }

        public List<Scenario> getProfile(){
        return this.profile;
        }
    private Integer loadTestDurationTimeFromFile(String path){
        File file=new File(path);
        Integer testDurationMinutes = 60;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine.startsWith("Duration|")) {
                testDurationMinutes = Integer.parseInt(firstLine.split("\\|")[1]);

            }
        }
        catch (IOException e) {  }

        return testDurationMinutes;

    }
}
