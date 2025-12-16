public class TestApp {
    public static void main(String[] args) {
        System.out.println("Timesheet Application Running!");
        System.out.println("Java version: " + System.getProperty("java.version"));
        // Keep the app running
        while(true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
