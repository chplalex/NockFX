import java.util.ArrayList;
import java.util.List;

public class TestAuthService implements AuthService {

    List<AuthData> authList;
    final int AUTH_LIST_SIZE = 5;

    public TestAuthService() {
        authList = new ArrayList<>(AUTH_LIST_SIZE);
        for (int i = 1; i <= AUTH_LIST_SIZE; i++) {
            authList.add(new AuthData("nick" + i, "log" + i, "pass" + i));
        }
    }

    @Override
    public AuthData getDataByLogAndPass(String log, String pass) {
        for (AuthData authauthData: authList) {
            if (authauthData.log.equalsIgnoreCase(log) && authauthData.pass.equals(pass)) {
                return authauthData;
            }
        }
        return null;
    }

    @Override
    public AuthData getDataBySingUp(String nick, String log, String pass) {
        AuthData dataInList = getDataByLogAndPass(log, pass);
        if (dataInList == null) {
            dataInList = new AuthData(nick, log, pass);
            authList.add(dataInList);
            return dataInList;
        }
        if (dataInList.nick.equalsIgnoreCase(nick)) {
            return dataInList;
        }
        return null;
    }

    @Override
    public AuthData changeNick(String newNick, String log, String pass) {
        return null;
    }

    @Override
    public AuthData changePass(String newPass, String log, String pass) {
        return null;
    }
}
