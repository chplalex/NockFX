import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBAuthService implements AuthService {

    private class AuthEntry {
        String nick;
        String log;
        String pass;

        public AuthEntry(String nick, String log, String pass) {
            this.nick = nick;
            this.log = log;
            this.pass = pass;
        }

    }

    private final int AUTH_LIST_SIZE = 5;
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    public DBAuthService() {
        try {
            openDB();
            initDB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void openDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:Nock.db");
        statement = connection.createStatement();
    }

    private void initDB() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (" +
                "log text PRIMARY KEY, " +
                "pass text, " +
                "nick text" +
                ");");

        statement.executeUpdate("DELETE FROM clients");

        preparedStatement = connection.prepareStatement("INSERT INTO clients (log, pass, nick) VALUES (?, ?, ?)");
        for (int i = 1; i <= AUTH_LIST_SIZE; i++) {
            preparedStatement.setString(1, "log" + i);
            preparedStatement.setString(2, "pass" + i);
            preparedStatement.setString(3, "nick" + i);
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    @Override
    public void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getNickByLogAndPass(String log, String pass) {
        try {
            ResultSet rs = statement.executeQuery(
                    String.format("SELECT nick FROM clients WHERE log = \"%s\" AND pass = \"%s\"",
                            log, pass));
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public String getNickBySingUp(String nick, String log, String pass) {
        try {
            ResultSet rs = statement.executeQuery(
                    String.format("SELECT nick, pass FROM clients WHERE log = \"%s\"",
                            log, pass));
            if (rs.next()) {
                String nickInDB = rs.getString("nick");
                String passInDB = rs.getString("pass");
                if (nickInDB.equalsIgnoreCase(nick) && passInDB.equals(pass)) {
                    return nick;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
