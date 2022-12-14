package Manager;

import Data.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DBManager {
    private static DBManager singleton = new DBManager();
    public static DBManager get() { return singleton; }

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://";

    private Object queryLock = new Object();

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private Map<String, Boolean> sqlStatusMap = new HashMap<>();

    private DBManager() {
        System.out.println("Initializing Manager.DBManager...");

        try{
            Class.forName(JDBC_DRIVER);
            connectToDB();
        } catch (Exception e) {
            SMTPManager.get().addMail(e);
        }
    }

    private boolean isConnectionClosed() throws SQLException {
        return connection == null ||
                connection.isClosed() ||
                statement == null ||
                statement.isClosed();
    }

    private void connectToDB() throws SQLException {
        String pass = new String(Base64.getDecoder().decode(Constants.DBConfig.DB_USER_PASSWORD));

        connection = DriverManager.getConnection(
                DB_URL +
                        Constants.DBConfig.DB_HOST +
                        ":" +
                        Constants.DBConfig.DB_PORT +
                        "?" +
                        Constants.DBConfig.DB_CONNECTION_OPTION,
                Constants.DBConfig.DB_USER_NAME,
                pass);

        statement = connection.createStatement();
    }

    public void closeConnection() {
        if (resultSet == null)
            System.out.println("DB resultSet is already null");
        else {
            try {
                if (resultSet.isClosed())
                    System.out.println("DB resultSet is already closed");
                else
                    resultSet.close();
            } catch (SQLException e) {
                SMTPManager.get().addMail(e);
            }

            resultSet = null;
        }

        if (statement == null)
            System.out.println("DB statement is already null");
        else {
            try {
                if (statement.isClosed())
                    System.out.println("DB statement is already closed");
                else
                    statement.close();
            } catch (SQLException e) {
                SMTPManager.get().addMail(e);
            }

            statement = null;
        }

        if (connection == null)
            System.out.println("DB connection is already null");
        else {
            try {
                if (connection.isClosed())
                    System.out.println("DB connection is already closed");
                else
                    connection.close();
            } catch (SQLException e) {
                SMTPManager.get().addMail(e);
            }

            connection = null;
        }

        System.out.println("DB connection all closed");
    }

    public boolean queryAndIsSuccess(String sql) {
        synchronized (queryLock) {
            try {
                if (isConnectionClosed())
                    connectToDB();

                // ???
                if (isConnectionClosed())
                    return false;

                statement.execute(sql);
                sqlStatusMap.put(sql, true);
            } catch (SQLException e) {
                closeConnection();

                // exception이 나면 쿼리문에 문제가 있을 확률이 높다.
                if (!sqlStatusMap.containsKey(sql) || sqlStatusMap.get(sql)) {
                    SMTPManager.get().addMail(e);

                    // 중복으로 계속 메일을 보내지 않기 위해 실패한 status를 저장한다.
                    sqlStatusMap.put(sql, false);
                }

                return false;
            }

            return true;
        }
    }

    public ArrayList<Map<String, String>> query(String sql) {
        synchronized (queryLock) {
            ArrayList<Map<String, String>> resultList = new ArrayList<>();

            try {
                if (isConnectionClosed())
                    connectToDB();

                // ???
                if (isConnectionClosed())
                    return resultList;

                if (statement.execute(sql)) {
                    resultSet = statement.executeQuery(sql);

                    if (resultSet != null) {
                        while (resultSet.next()) {
                            Map<String, String> resultMap = new HashMap<>();
                            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                                resultMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));

                            resultList.add(resultMap);
                        }
                    }

                    resultSet.close();
                }

                sqlStatusMap.put(sql, true);
            } catch (SQLException e) {
                closeConnection();

                // exception이 나면 쿼리문에 문제가 있을 확률이 높다.
                if (!sqlStatusMap.containsKey(sql) || sqlStatusMap.get(sql)) {
                    SMTPManager.get().addMail(e);

                    // 중복으로 계속 메일을 보내지 않기 위해 실패한 status를 저장한다.
                    sqlStatusMap.put(sql, false);
                }
            }

            return resultList;
        }
    }

    public ArrayList<String> query(String sql, String column) {
        ArrayList<Map<String, String>> resultList = query(sql);
        if (resultList == null)
            return null;

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++)
            result.add(resultList.get(i).get(column));

        return result;
    }

    public String queryGetValue(String sql, String column) {
        ArrayList<String> resultList = query(sql, column);
        if (resultList == null || resultList.isEmpty())
            return null;

        return resultList.get(0);
    }
}
