package Model;

import Data.Constants;
import Manager.DBManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MonitoringConfigModel {
    public String key;
    public int value;

    public static Map<String, MonitoringConfigModel> getConfigModelMap() {
        Map<String, MonitoringConfigModel> m = new HashMap<>();

        ArrayList<Map<String, String>> configs = DBManager.get().query(Constants.Query.SELECT_MONITORING_CONFIG);
        if (configs == null)
            return m;

        for (Map<String, String> map : configs) {
            if (map == null)
                continue;

            MonitoringConfigModel configModel = new MonitoringConfigModel();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry == null)
                    continue;

                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("key"))
                    configModel.key = value;
                else if (key.equals("value"))
                    configModel.value = Integer.parseInt(value);
            }

            m.put(configModel.key, configModel);
        }

        return m;
    }
}
