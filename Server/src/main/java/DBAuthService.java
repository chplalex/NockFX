import java.sql.*;

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
    private PreparedStatement ps;

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

        ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS rowcount FROM clients");
        int count = rs.getInt("rowcount");
        rs.close();

        if (count == 0) {
            // начальное заполнение таблицы клиентов для отладки
            ps = connection.prepareStatement("INSERT INTO clients (log, pass, nick) VALUES (?, ?, ?)");
            for (int i = 1; i <= AUTH_LIST_SIZE; i++) {
                ps.setString(1, "log" + i);
                ps.setString(2, "pass" + i);
                ps.setString(3, "nick" + i);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
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
                // такой логин уже имеется в БД
                String nickInDB = rs.getString("nick");
                String passInDB = rs.getString("pass");
                if (passInDB.equals(pass)) {
                    // пароль совпал. если изменён ник -> перезапишем новый ник в БД
                     if (!nickInDB.equalsIgnoreCase(nick)) {
                         statement.executeUpdate(
                                 String.format("UPDATE clients SET nick = \"%s\" WHERE log = \"%s\"",
                                         nick, log));
                     }
                    return nick;
                } else {
                    return null; // пароль не совпал (возможна попытка подбора пароля) -> отказ в авторизации
                }
            } else {
                // такого логина нет в БД -> регистрируем нового пользователя
                statement.executeUpdate(
                        String.format("INSERT INTO clients (log, pass, nick) VALUES (\"%s\", \"%s\", \"%s\")",
                                log, pass, nick));
                return nick;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
