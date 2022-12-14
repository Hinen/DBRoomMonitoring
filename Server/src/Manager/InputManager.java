package Manager;

import Data.Constants;

import java.util.Scanner;

public class InputManager {
    private static InputManager singleton = new InputManager();
    public static InputManager get() { return singleton; }

    private Scanner scanner = new Scanner(System.in);

    public void update() {
        try {
            int input = scanner.nextInt();

            switch (input) {
                case 1:
                    // Max Connection 140 으로
                    System.out.println(Constants.Query.SET_MAX_CONNECTIONS_140);
                    DBManager.get().query(Constants.Query.SET_MAX_CONNECTIONS_140);
                    System.out.println("Success Set Max Connections to 140");
                    break;
                case 2:
                    // Max Connection 150 으로
                    System.out.println(Constants.Query.SET_MAX_CONNECTIONS_150);
                    DBManager.get().query(Constants.Query.SET_MAX_CONNECTIONS_150);
                    System.out.println("Success Set Max Connections to 150");
                    break;
                case 3:
                    // 20192762 박수빈 학생 추가
                    System.out.println(Constants.Query.INSERT_STUDENT);
                    if (DBManager.get().queryAndIsSuccess(Constants.Query.INSERT_STUDENT))
                        System.out.println("Success Insert Student");
                    else
                         System.out.println("Fail Insert Student");
                    break;
                case 4:
                    // 20192762 박수빈 학생 삭제
                    System.out.println(Constants.Query.REMOVE_STUDENT);
                    if (DBManager.get().queryAndIsSuccess(Constants.Query.REMOVE_STUDENT))
                        System.out.println("Success Remove Student");
                    else
                        System.out.println("Fail Remove Student");
                    break;
            }
        } catch (Exception e) {
            // DO NOTHING
        }
    }
}