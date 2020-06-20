public class Const {

    public final static int SERVER_PORT = 8189;
    public final static String SERVER_ADDR = "localhost";
    // внутрение команды программы
    public final static String CMD_STOP_CLIENT = "/SC";
    public final static String CMD_STOP_SERVER = "/SS";
    public final static String CMD_SING_UP = "/SU";
    public final static String CMD_AUTH = "/AU";
    public final static String CMD_AUTH_OK = "/AO";
    public final static String CMD_AUTH_NO = "/AN";
    public final static String CMD_DE_AUTH = "/DA";
    public final static String CMD_BROADCAST_MSG = "/BM";
    public final static String CMD_PRIVATE_MSG = "/PM";
    public final static String CMD_CLIENTS_LIST = "/CL";
    public final static String CMD_REGEX = "\\s*(\\s)\\s*";
    // внешние (пользовательские) команды
    public final static String USER_PRIVATE_MSG = "/w"; // отправить приватное сообщение
    public final static String USER_DE_AUTH = "/end";     // деавторизоваться

    public final static String TITLE_CLIENT_AUTH_NO = "NockFX Client :: нет авторизации";
    public final static String TITLE_CLIENT_AUTH_OK = "NockFX Client :: ";
    public final static String TITLE_SERVER = "NockFX Server";

    public final static int TIMEOUT_NO_AUTH = 20 * 1000; // таймаут отключения от сервера неавторизованного клиента

}
