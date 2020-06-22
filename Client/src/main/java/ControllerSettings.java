import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerSettings {

    @FXML
    TextField txtNick;
    @FXML
    TextField txtLogin;
    @FXML
    PasswordField txtPasswordOld;
    @FXML
    PasswordField txtPasswordNew1;
    @FXML
    PasswordField txtPasswordNew2;

    private ControllerClient controllerClient;

    public void onChangeSettings(ActionEvent actionEvent) {
        boolean inputCorrect = true;

        String passOld = txtPasswordOld.getText().trim();
        if (passOld.length() == 0) {
            txtPasswordOld.setPromptText("введите пароль");
            inputCorrect = false;
        }

        String nick = txtNick.getText().trim();
        if (nick.length() == 0) {
            txtNick.setPromptText("Введите ник");
            inputCorrect = false;
        }

        String passNew1 = txtPasswordNew1.getText().trim();
        String passNew2 = txtPasswordNew2.getText().trim();
        if ((passNew1.length() != 0 || passNew2.length() !=0) && !passNew1.equals(passNew2)) {
            txtPasswordNew1.clear();
            txtPasswordNew2.clear();
            txtPasswordNew1.setPromptText("Пароли не совпадают");
            txtPasswordNew2.setPromptText("Пароли не совпадают");
            inputCorrect = false;
        }

        if (inputCorrect) {
            controllerClient.tryChangeSettings(nick, passOld, passNew1, passNew2);
        }

    }

    public void onCancel(ActionEvent actionEvent) {
        controllerClient.hideSettingsWindow();
    }

    public void setControllerClient(ControllerClient controllerClient) {
        this.controllerClient = controllerClient;
    }
}
