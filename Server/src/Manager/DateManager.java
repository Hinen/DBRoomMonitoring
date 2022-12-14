package Manager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateManager {
    private static DateManager singleton = new DateManager();
    public static DateManager get() { return singleton; }

    private SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date date = new Date();

    public String getNowTime() {
        date.setTime(System.currentTimeMillis());
        return dayTime.format(date);
    }
}
