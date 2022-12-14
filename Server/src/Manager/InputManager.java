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
                    // 20192762 박수빈 학생 추가
                    System.out.println(Constants.Query.INSERT_STUDENT);
                    if (DBManager.get().queryAndIsSuccess(Constants.Query.INSERT_STUDENT))
                        System.out.println("Success Insert Student");
                    else
                         System.out.println("Fail Insert Student");
                    break;
                case 2:
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