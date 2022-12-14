package Manager;

import Data.Constants;
import Model.StudentModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitoringManager {
    private static MonitoringManager singleton = new MonitoringManager();
    public static MonitoringManager get() { return singleton; }

    private static final int STANDARD_MAX_CONNECTION = 4030;

    private Map<String, String> statusMap = new HashMap<>();
    private List<StudentModel> studentModelList;

    private MonitoringManager() {
        System.out.println("Initializing Manager.MonitoringManager...");

        // MAX_CONNECTIONS은 4030을 기준으로
        statusMap.put(Constants.StatusKey.MAX_CONNECTIONS, Integer.toString(STANDARD_MAX_CONNECTION));

        // 기존 학생들 추가
        studentModelList = StudentModel.getStudentList();
    }

    public void start() {
        System.out.println("----------\nCHECK TIME : " + DateManager.get().getNowTime());

        checkVariables();
        checkStudents();
    }

    private void checkVariables() {
        ArrayList<Map<String, String>> variables = DBManager.get().query(Constants.Query.SHOW_VARIABLES);
        if (variables == null)
            return;

        for (Map<String, String> map : variables) {
            if (map.get("VARIABLE_NAME").equals(Constants.StatusKey.MAX_CONNECTIONS))
                checkMaxConnections(map);
        }
    }

    private void checkMaxConnections(Map<String, String> map) {
        String valueStr = map.get("VARIABLE_VALUE");
        Integer value = Integer.parseInt(valueStr);
        if (value == null)
            return;

        int beforeValue = Integer.parseInt(statusMap.get(Constants.StatusKey.MAX_CONNECTIONS));
        if (beforeValue >= STANDARD_MAX_CONNECTION && value < STANDARD_MAX_CONNECTION) {
            SMTPManager.get().addMail(
                    "DB Max Connection 문제 발생_" + DateManager.get().getNowTime(),
                    "Monitoring DB Host : " + Constants.DBConfig.DB_HOST + "\n" +
                            "Check Time : " + DateManager.get().getNowTime() + "\n\n" +
                            "Before MaxConnection : " + beforeValue + "\n" +
                            "Now MaxConnection : " + value + "\n\n" +
                            "위와 같이 MaxConnection 값에 문제가 발생했으므로 모니터링 결과를 공유합니다.",
                    Constants.MonitoringType.MAX_CONNECTION_ERROR
            );
        }
        else if (beforeValue < STANDARD_MAX_CONNECTION && value >= STANDARD_MAX_CONNECTION) {
            SMTPManager.get().addMail(
                    "DB Max Connection 정상 복구_" + DateManager.get().getNowTime(),
                    "Monitoring DB Host : " + Constants.DBConfig.DB_HOST + "\n" +
                            "Check Time : " + DateManager.get().getNowTime() + "\n\n" +
                            "Before MaxConnection : " + beforeValue + "\n" +
                            "Now MaxConnection : " + value + "\n\n" +
                            "위와 같이 MaxConnection 값이 정상 복구 했으므로 모니터링 결과를 공유합니다.",
                    Constants.MonitoringType.MAX_CONNECTION_NORMAL
            );
        }

        statusMap.put(Constants.StatusKey.MAX_CONNECTIONS, valueStr);
    }

    private void checkStudents() {
        List<StudentModel> newStudentModelList = StudentModel.getStudentList();

        // 새로 추가된 학생 확인
        for (StudentModel studentModel : newStudentModelList) {
            if (studentModel == null)
                continue;

            // 기존 리스트에 없는 학생의 경우엔 새로 추가된 학생이다
            if (!isExistStudent(studentModel.id, studentModelList)) {
                SMTPManager.get().addMail(
                        "학생(" + studentModel.name + ":" + studentModel.id + ") 추가_" + DateManager.get().getNowTime(),
                        "Monitoring DB Host : " + Constants.DBConfig.DB_HOST + "\n" +
                                "Check Time : " + DateManager.get().getNowTime() + "\n\n" +
                                "New Student ID : " + studentModel.id + "\n" +
                                "New Student Name : " + studentModel.name + "\n" +
                                "New Student Age : " + studentModel.age + "\n\n" +
                                "위와 같이 학생이 추가되었으므로 모니터링 결과를 공유합니다.",
                        Constants.MonitoringType.NEW_STUDENT
                );
            }
        }

        // 제거된 학생 확인
        for (StudentModel studentModel : studentModelList) {
            if (studentModel == null)
                continue;

            // 새 리스트에 없는 학생의 경우엔 제거된 학생이다
            if (!isExistStudent(studentModel.id, newStudentModelList)) {
                SMTPManager.get().addMail(
                        "학생(" + studentModel.name + ":" + studentModel.id + ") 제거_" + DateManager.get().getNowTime(),
                        "Monitoring DB Host : " + Constants.DBConfig.DB_HOST + "\n" +
                                "Check Time : " + DateManager.get().getNowTime() + "\n\n" +
                                "New Student ID : " + studentModel.id + "\n" +
                                "New Student Name : " + studentModel.name + "\n" +
                                "New Student Age : " + studentModel.age + "\n\n" +
                                "위와 같이 학생이 제거되었으므로 모니터링 결과를 공유합니다.",
                        Constants.MonitoringType.REMOVE_STUDENT
                );
            }
        }

        studentModelList = newStudentModelList;
    }

    private boolean isExistStudent(int studentID, List<StudentModel> list) {
        for (StudentModel studentModel : list) {
            if (studentModel == null)
               continue;

            if (studentModel.id == studentID)
                return true;
        }

        return false;
    }
}
