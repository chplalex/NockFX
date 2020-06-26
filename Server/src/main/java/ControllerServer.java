import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerServer implements Initializable {

    List<ClientEntry> clients;
    AuthService authService;
    private ServerSocket serverSocket;
    private boolean serverRunning;
    private ExecutorService executorService;

    @FXML
    private TextArea textArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clients = new Vector<>();
        authService = new DBAuthService();
        serverRunning = false;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

        try {

            serverSocket = new ServerSocket(Const.SERVER_PORT);
            serverRunning = true;
            putText("Сервер запущен. " + serverSocket.toString());

            execute(() -> {
                while (serverRunning) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        putText("Клиент подключён. " + socket.toString());
                        clients.add(new ClientEntry(this, socket));
                    } catch (IOException e) {
                        if (serverRunning) {
                            serverRunning = false;
                            putText("Ошибка сервера. " + e.toString());
                        } else {
                            putText("Сервер закрыт. " + e.toString());
                        }
                    }
                }
            });

        } catch (IOException e) {
            putText("Ошибка запуска сервера. " + e.toString());
        }
    }

    public void close() {
        serverRunning = false;
        for (ClientEntry clientEntry : clients) {
            clientEntry.sendMsg(Const.CMD_STOP_SERVER);
            clientEntry.closeConnection();
        }
        clients.clear();
        try {
            serverSocket.close();
        } catch (IOException e) {
            putText("Ошибка закрытия сервера. " + e.toString());
        }
        authService.close();
        executorService.shutdown();
    }

    public void putText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        textArea.appendText(dateFormat.format(new Date()) + "\n" + text + "\n\n");
    }

    public void broadcastMsg(String sender, String msg) {
        for (ClientEntry clientEntry : clients) {
            clientEntry.sendMsg(Const.CMD_BROADCAST_MSG + " " + sender + " " + msg);
        }
    }

    public void privateMsg(String sender, String recipient, String msg) {
        for (ClientEntry clientEntry : clients) {
            if (recipient.equals(clientEntry.getNick())) {
                clientEntry.sendMsg(Const.CMD_PRIVATE_MSG + " " + sender + " " + msg);
            }
        }
    }

    public void clientsListMsg() {
        StringBuffer stringBuffer = new StringBuffer(clients.size());
        stringBuffer.append(Const.CMD_CLIENTS_LIST);

        for (ClientEntry clientEntry: clients) {
            String nick = clientEntry.getNick();
            if (nick == null) {
                continue;
            }
            stringBuffer.append(" " + nick);
        }

        String msg = stringBuffer.toString();

        for (ClientEntry clientEntry: clients) {
            clientEntry.sendMsg(msg);
        }
    }

    public void removeClient(ClientEntry clientEntry) {
        clients.remove(clientEntry);
    }

    public void execute(Runnable task) {
        executorService.execute(task);
    }

}