public interface AuthService {
    public abstract AuthData getDataByLogAndPass(String log, String pass);
    public abstract AuthData getDataBySingUp(String nick, String log, String pass);
    public abstract AuthData changeNick(String newNick, String log, String pass);
    public abstract AuthData changePass(String newPass, String log, String pass);
    public default void close() { };
}
