public interface AuthService {
    public abstract String getNickByLogAndPass(String log, String pass);
    public abstract String getNickBySingUp(String nick, String log, String pass);
    public default void close() { };
}
