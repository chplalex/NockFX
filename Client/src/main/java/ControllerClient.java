import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Thread.currentThread;

public class ControllerClient implements Initializable {

    private Stage stageClient;
    private Stage stageSigUp;
    private Stage stageSettings;
    private ControllerSingUp controllerSingUp;
    private ControllerSettings controllerSettings;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean clientConnected;
    private String clientNick;
    private String clientLog;
    private ReversedLinesFileReader inHistory;
    private BufferedWriter outHistory;

    @FXML
    private HBox boxLogAndPass;
    @FXML
    private TextField logField;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox boxButtons;
    @FXML
    private TextArea textArea;
    @FXML
    private ListView<String> listView;
    @FXML
    private HBox boxTextAndSettings;
    @FXML
    private TextField textField;
    @FXML
    private Button btnSettings;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        currentThread().setName("Для отладки. Основной поток клиента");

        stageSigUp = createSigUpWindow();
        stageSettings = createSettigsWindow();
        clientConnected = false;
        setControlsVisibility(false);
        connect();

    }

    private void connect() {

        try {
            socket = new Socket(Const.SERVER_ADDR, Const.SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            clientConnected = true;
            putText("Подключение к серверу установлено", true);
        } catch (IOException e) {
            putText("Отсутствует подключение к серверу", true);
            return;
        }

        new Thread(() -> {
            currentThread().setName("Для отладки. Поток прослушивания сообщений от сервера");
            try {
                do {

                    String msg = in.readUTF().trim();

                    // Сервер отклоняет авторизацию
                    if (msg.startsWith(Const.CMD_AUTH_NO)) {
                        putText("Запрос на авторизацию отклонён сервером", true);
                    }

                    // Сервер подтверждает авторизацию
                    if (msg.startsWith(Const.CMD_AUTH_OK)) {
                        String[] msgArr = msg.split(Const.CMD_REGEX, 3);
                        if (msgArr.length != 3) {
                            putText("Некорректная команда от сервера :: " + msg, true);
                        } else {
                            clientNick = msgArr[1];
                            clientLog = msgArr[2];
                            closeHistory();
                            textArea.clear();
                            if (openHistory()) {
                                readHistory();
                            }
                            setControlsVisibility(true);
                        }
                    }

                    // Сервер даёт команду на деавторизацию
                    if (msg.startsWith(Const.CMD_DE_AUTH)) {
                        textArea.clear();
                        putText("Вы вышли из чата", true);
                        clientNick = null;
                        clientLog = null;
                        closeHistory();
                        textArea.clear();
                        setControlsVisibility(false);
                    }

                    // Получено широковещательное сообщение
                    if (msg.startsWith(Const.CMD_BROADCAST_MSG)) {
                        String[] msgArr = msg.split(Const.CMD_REGEX, 3);
                        if (msgArr.length != 3) {
                            putText("Некорректная команда от сервера :: " + msg, true);
                        } else {
                            writeHistory(putText(msgArr[1] + " -> (всем) :: " + msgArr[2], true));
                        }
                    }

                    // Получено приватное сообщение
                    if (msg.startsWith(Const.CMD_PRIVATE_MSG)) {
                        String[] msgArr = msg.split(Const.CMD_REGEX, 3);
                        if (msgArr.length != 3) {
                            putText("Некорректная команда от сервера :: " + msg, true);
                        } else {
                            writeHistory(putText(msgArr[1] + " -> (только мне) :: " + msgArr[2], true));
                        }
                    }

                    // Получена команда на закрытие клиента
                    if (msg.startsWith(Const.CMD_STOP_CLIENT)) {
                        clientConnected = false;
                    }

                    // Получен список клиентов
                    if (msg.startsWith(Const.CMD_CLIENTS_LIST)) {
                        String[] msgArr = msg.split(Const.CMD_REGEX);
                        Platform.runLater(() -> {
                            ObservableList<String> list = listView.getItems();
                            list.clear();
                            for (int i = 1; i < msgArr.length; i++) {
                                list.add(String.format("%d. %s", i, msgArr[i]));
                            }
                        });
                    }

                } while (clientConnected);
            } catch (IOException e) {
                putText("Ошибка чтения сообщения сервера " + e.toString(), true);
            } finally {
                close();
            }
        }).start();
    }

    private boolean openHistory() {
        if (clientLog == null) {
            return false;
        }

        if (inHistory != null || outHistory != null) {
            closeHistory();
        }

        File fileHistory = new File("history_" + clientLog + ".txt");

        if (!fileHistory.exists()) {
            try {
                if (!fileHistory.createNewFile()) {
                    putText("Ошибка создания файла истории :: " + fileHistory.toString(), true);
                    return false;
                };
            } catch (IOException e) {
                putText("Ошибка создания файла истории :: " + fileHistory.toString(), true);
                return false;
            }
        }

        try {
            inHistory = new ReversedLinesFileReader(fileHistory, Charset.defaultCharset());
            outHistory = new BufferedWriter(new FileWriter(fileHistory, true));
            return true;
        } catch (FileNotFoundException e) {
            putText("Файл истории не найден :: " + fileHistory.toString(), true);
            return false;
        } catch (IOException e) {
            putText("Ошибка открытия файла истории :: " + fileHistory.toString(), true);
            return false;
        }
    }

    private void closeHistory() {
        if (inHistory != null) {
            try {
                inHistory.close();
            } catch (IOException e) {
                putText("Ошибка закрытия файла истории (входящий поток)", true);
            }
            inHistory = null;
        }
        if (outHistory != null) {
            try {
                outHistory.close();
            } catch (IOException e) {
                putText("Ошибка закрытия файла истории (исходящий поток)", true);
            }
            outHistory = null;
        }
    }

    private void readHistory() {
        String str;
        int count = 100;
        List<String> list = new ArrayList<>(count);

        try {
            while ((str = inHistory.readLine()) != null || --count > 0) {
                list.add(str);
            }
        } catch (IOException e) {
            putText("Ошибка чтения файла истории", true);
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            putText(list.get(i), false);
        }

        putText("", false);
    }

    private void writeHistory(String str) {
        try {
            outHistory.write(str);
            outHistory.flush();
        } catch (IOException e) {
            putText("Ошибка записи в файл истории", true);
        }
    }

    public void setStageClient(Stage stageClient) {
        this.stageClient = stageClient;
    }

    public void close() {
        try {
            if (clientNick != null) {
                clientNick = null;
                clientLog = null;
                setControlsVisibility(false);
            }
            if (clientConnected) {
                out.writeUTF(Const.CMD_STOP_CLIENT);
                return;
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            putText("Ошибка закрытия клиента. " + e.toString(), true);
        }
        closeHistory();
    }

    public String putText(String text, boolean insertDateTime) {
        String str;
        if (insertDateTime) {
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            str = dateFormat.format(new Date()) + System.lineSeparator() + text;
        } else {
            str = text;
        }
        textArea.appendText(str + System.lineSeparator());
        return str;
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (!clientConnected) {
            putText("Подключение к серверу не установлено", true);
            return;
        }

        String msg = textField.getText().trim();

        if (msg.startsWith(Const.CMD_STOP_CLIENT) ||
                msg.startsWith(Const.CMD_AUTH) ||
                msg.startsWith(Const.CMD_BROADCAST_MSG) ||
                msg.startsWith(Const.CMD_PRIVATE_MSG)) {
            putText("Служебные символы в начале сообщения недопустимы", true);
            return;
        }

        try {
            if (msg.startsWith(Const.USER_PRIVATE_MSG)) {
                String[] msgArr = msg.split(Const.CMD_REGEX, 3);
                if (msgArr.length != 3) {
                    putText("Используйте корректный формат команды:\n/w <кому> <сообщение>", true);
                } else {
                    out.writeUTF(Const.CMD_PRIVATE_MSG + " " + msgArr[1] + " " + msgArr[2]);
                    textField.clear();
                }
                return;
            }

            if (msg.startsWith(Const.USER_DE_AUTH)) {
                out.writeUTF(Const.CMD_DE_AUTH);
                textField.clear();
                return;
            }

            out.writeUTF(Const.CMD_BROADCAST_MSG + " " + msg);
            textField.clear();
        } catch (IOException e) {
            putText("Ошибка отправки сообщения " + e.toString(), true);
        }
    }

    public void makeAuth(ActionEvent actionEvent) {
        if (!clientConnected) {
            connect();
        }

        if (!clientConnected) {
            return;
        }

        // временно для отладки
        int clientCount = (int) (Math.random() * 5) + 1;
        logField.setText("log5"); // + clientCount);
        passField.setText("pass5"); // + clientCount);

        String log = logField.getText().trim();
        String pass = passField.getText().trim();

        if (log.equals("") || pass.equals("")) {
            putText("Введите ваши логин и пароль", true);
            return;
        }

        try {
            out.writeUTF(Const.CMD_AUTH + " " + log + " " + pass);
        } catch (IOException e) {
            putText("Ошибка отправки сообщения " + e.toString(), true);
        }
    }

    private void setControlsVisibility(boolean clientAuthenticated) {

        boxLogAndPass.setVisible(!clientAuthenticated);
        boxLogAndPass.setManaged(!clientAuthenticated);

        boxButtons.setVisible(!clientAuthenticated);
        boxButtons.setManaged(!clientAuthenticated);

        listView.setVisible(clientAuthenticated);
        listView.setManaged(clientAuthenticated);

        boxTextAndSettings.setVisible(clientAuthenticated);
        boxTextAndSettings.setManaged(clientAuthenticated);

        if (stageClient == null) {
            return;
        }

        if (clientAuthenticated) {
            setTitle(Const.TITLE_CLIENT_AUTH_OK + clientNick);
            hideSingUpWindow();
        } else {
            setTitle(Const.TITLE_CLIENT_AUTH_NO);
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            stageClient.setTitle(title);
        });
    }

    private Stage createSettigsWindow() {

        Stage stage = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            Parent root = fxmlLoader.load();

            stage = new Stage();
            stage.setTitle("NockFX :: Settings");
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);

            controllerSettings = fxmlLoader.getController();
            controllerSettings.setControllerClient(this);

        } catch (IOException e) {
            putText("Ошибка загрузки окна настроек " + e.toString(), true);
        }

        return stage;
    }

    private Stage createSigUpWindow() {

        Stage stage = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SingUp.fxml"));
            Parent root = fxmlLoader.load();

            stage = new Stage();
            stage.setTitle("NockFX :: New Client");
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);

            controllerSingUp = fxmlLoader.getController();
            controllerSingUp.setControllerClient(this);

        } catch (IOException e) {
            putText("Ошибка загрузки окна регистрации " + e.toString(), true);
        }

        return stage;
    }

    public void showSettingsWindow(ActionEvent actionEvent) {
        controllerSettings.txtNick.setText(clientNick);
        controllerSettings.txtLogin.setText(clientLog);
        stageSettings.show();
    }

    public void showSingUpWindow(ActionEvent actionEvent) {
        stageSigUp.show();
    }

    public void hideSettingsWindow() {
        Platform.runLater(() -> {
            stageSettings.hide();
        });
    }

    public void hideSingUpWindow() {
        Platform.runLater(() -> {
            controllerSingUp.clearFields();
            stageSigUp.hide();
        });
    }

    public void trySingUp(String nick, String login, String password) {
        if (!clientConnected) {
            connect();
        }

        if (!clientConnected) {
            return;
        }

        try {
            out.writeUTF(Const.CMD_SING_UP + " " + nick + "  " + login + " " + password);
        } catch (IOException e) {
            putText("Ошибка отправки сообщения " + e.toString(), true);
        }
    }

    public void onMouseClickedListView(MouseEvent mouseEvent) {
        String receiver = listView.getSelectionModel().getSelectedItem();
        if (receiver == null) {
            return;
        }
        receiver = receiver.split(Const.CMD_REGEX, 2)[1];

        String msg = textField.getText().trim();
        if (msg.startsWith(Const.USER_PRIVATE_MSG)) {
            String[] msgArr = msg.split(Const.CMD_REGEX, 3);
            if (msgArr.length == 3) {
                msg = msgArr[2];
            } else {
                msg = "";
            }
        }

        textField.setText(Const.USER_PRIVATE_MSG + " " + receiver + " " + msg);
    }

    public void tryChangeSettings(String nick, String passOld, String passNew1, String passNew2) {
        putText("Временная заглушка. Здесь будет реализован метод изменения учётных данных пользователя", true);
    }
}
