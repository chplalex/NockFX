import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerSingUp {

    @FXML
    private TextField txtNick;
    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword1;
    @FXML
    private PasswordField txtPassword2;

    private ControllerClient controllerClient;

    public void setControllerClient(ControllerClient controllerClient) {
        this.controllerClient = controllerClient;
    }

    public void doSingUp(ActionEvent actionEvent) {
        String nick = txtNick.getText().trim();
        String login = txtLogin.getText().trim();
        String password1 = txtPassword1.getText().trim();
        String password2 = txtPassword2.getText().trim();

        if (nick.length() == 0) {
            txtNick.setPromptText("введи ник");
            return;
        }

        if (login.length() == 0) {
            txtLogin.setPromptText("введи логин");
            return;
        }

        if (password1.length() == 0) {
            txtPassword1.setPromptText("введи пароль");
            return;
        }

        if (password2.length() == 0) {
            txtPassword2.setPromptText("введи пароль");
            return;
        }

        if (!password1.equals(password2)) {
            txtPassword2.clear();
            txtPassword2.setPromptText("пароль не совпадает");
            return;
        }

        controllerClient.trySingUp(nick, login, password1);
    }

    public void doCancel(ActionEvent actionEvent) {
        controllerClient.hideSingUpWindow();
    }

    public void clearFields() {
        txtNick.requestFocus();
        txtNick.clear();
        txtLogin.clear();
        txtPassword1.clear();
        txtPassword2.clear();
    }
}
