import java.util.ArrayList;
import java.util.List;

public class TestAuthService implements AuthService {

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

    List<AuthEntry> authList;
    final int AUTH_LIST_SIZE = 5;

    public TestAuthService() {
        authList = new ArrayList<>(AUTH_LIST_SIZE);
        for (int i = 1; i <= AUTH_LIST_SIZE; i++) {
            authList.add(new AuthEntry("nick" + i, "log" + i, "pass" + i));
        }
    }

    @Override
    public String getNickByLogAndPass(String log, String pass) {
        for (AuthEntry authEntry: authList) {
            if (authEntry.log.equalsIgnoreCase(log) && authEntry.pass.equals(pass)) {
                return authEntry.nick;
            }
        }
        return null;
    }

    @Override
    public String getNickBySingUp(String nick, String log, String pass) {
        String nickInList = getNickByLogAndPass(log, pass);
        if (nickInList == null) {
            authList.add(new AuthEntry(nick, log, pass));
            return nick;
        }
        if (nickInList.equalsIgnoreCase(nick)) {
            return nick;
        }
        return null;
    }
}
