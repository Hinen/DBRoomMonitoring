import Manager.*;

public class Main implements Runnable {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DBManager.get().closeConnection();
        }));

        // Manager Init
        SMTPManager.get();
        DBManager.get();
        SerialManager.get();
        MonitoringManager.get();

        System.out.println("1 : DB에 박수빈 Student를 Insert합니다.");
        System.out.println("2 : DB에 박수빈 Student를 Delete합니다.");

        // SMTP Thread
        new Thread(() -> {
            while (true) {
                SMTPManager.get().start();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }).start();

        // Main Thread
        Thread thread = new Thread(new Main());
        thread.start();

        // Input Thread
        new Thread(() -> {
            while (true) {
                InputManager.get().update();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }).start();
    }

    @Override
    public void run() {
        while (true) {
            MonitoringManager.get().start();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}