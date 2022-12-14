package Model;

import Data.Constants;
import Manager.DBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentModel {
    public int id;
    public String name;
    public int age;

    public static List<StudentModel> getStudentList() {
        List<StudentModel> list = new ArrayList<>();

        ArrayList<Map<String, String>> students = DBManager.get().query(Constants.Query.SELECT_STUDENT);
        if (students == null)
            return list;

        for (Map<String, String> map : students) {
            if (map == null)
                continue;

            StudentModel student = new StudentModel();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry == null)
                    continue;

                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("id"))
                    student.id = Integer.parseInt(value);
                else if (key.equals("name"))
                    student.name = value;
                else if (key.equals("age"))
                    student.age = Integer.parseInt(value);
            }

            list.add(student);
        }

        return list;
    }
}