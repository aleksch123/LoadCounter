public class Scenario {
    private String name;
    private String className;
    private int operationsPerHour;
    private int pacingTime;
    private int usersCount;
    private boolean active;

    public Scenario(String name, String className, int operationsPerHour, int pacingTime, int usersCount, boolean active) {
        this.name = name;
        this.className = className;
        this.operationsPerHour = operationsPerHour;
        this.pacingTime = pacingTime;
        this.usersCount = usersCount;
        this.active = active;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getOperationsPerHour() { return operationsPerHour; }
    public void setOperationsPerHour(int operationsPerHour) { this.operationsPerHour = operationsPerHour; }
    public int getPacingTime() { return pacingTime; }
    public void setPacingTime(int pacingTime) { this.pacingTime = pacingTime; }
    public int getUsersCount() { return usersCount; }
    public void setUsersCount(int usersCount) { this.usersCount = usersCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}