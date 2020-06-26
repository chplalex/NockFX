import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientEntry {
    private ControllerServer controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private String log;

    public ClientEntry(ControllerServer controller, Socket socket) {

        this.controller = controller;
        this.socket = socket;

        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            setLogAndNick(null);

            controller.execute(() -> {
                try {
                    while (true) {

                        String msg = in.readUTF().trim();

                        // Клиент запрашивает регистрацию
                        if (msg.startsWith(Const.CMD_SING_UP)) {

                            String[] msgArr = msg.split(Const.CMD_REGEX, 4);

                            if (msgArr.length != 4) {
                                controller.putText("Некорректный запрос от клиента :: " + msg);
                                continue;
                            }

                            setLogAndNick(controller.authService.getDataBySingUp(msgArr[1], msgArr[2], msgArr[3]));

                            if (nick == null) {
                                out.writeUTF(Const.CMD_AUTH_NO);
                                continue;
                            }

                            out.writeUTF(Const.CMD_AUTH_OK + " " + nick + " " + log);
                            controller.putText(log + " :: " + nick + " :: авторизован");
                            controller.clientsListMsg();
                            continue;
                        }

                        // Клиент запрашивает авторизацию
                        if (msg.startsWith(Const.CMD_AUTH)) {

                            String[] msgArr = msg.split(Const.CMD_REGEX, 3);

                            if (msgArr.length != 3) {
                                controller.putText("Некорректный запрос от клиента :: " + msg);
                                continue;
                            }

                            setLogAndNick(controller.authService.getDataByLogAndPass(msgArr[1], msgArr[2]));

                            if (log == null) {
                                out.writeUTF(Const.CMD_AUTH_NO);
                                continue;
                            }

                            out.writeUTF(Const.CMD_AUTH_OK + " " + nick + " " + log);
                            controller.putText(log + " :: " + nick + " авторизован");
                            controller.clientsListMsg();
                            continue;
                        }

                        // Клиент запрашивает деавторизацию
                        if (msg.startsWith(Const.CMD_DE_AUTH)) {
                            controller.putText(log + "::" + nick + " деавторизован");
                            setLogAndNick(null);
                            out.writeUTF(Const.CMD_DE_AUTH);
                            controller.clientsListMsg();
                            continue;
                        }

                        // Клиент просит разослать широковещательное сообщение
                        if (msg.startsWith(Const.CMD_BROADCAST_MSG)) {
                            String[] msgArr = msg.split(Const.CMD_REGEX, 2);
                            if (msgArr.length != 2) {
                                controller.putText("Некорректный запрос от клиента :: " + msg);
                                continue;
                            }
                            controller.broadcastMsg(nick, msgArr[1]);
                            continue;
                        }

                        // Клиент просит отправить приватное сообщение
                        if (msg.startsWith(Const.CMD_PRIVATE_MSG)) {
                            String[] msgArr = msg.split(Const.CMD_REGEX, 3);
                            if (msgArr.length != 3) {
                                controller.putText(log + " :: " + nick + " :: " + "Некорректный запрос от клиента :: " + msg);
                                continue;
                            }
                            controller.privateMsg(nick, msgArr[1], msgArr[2]);
                            continue;
                        }

                        // Клиент запрашивает разрешение на отключение
                        if (msg.startsWith(Const.CMD_STOP_CLIENT)) {
                            out.writeUTF(Const.CMD_STOP_CLIENT);
                            controller.putText(log + " :: " + nick + " :: получен запрос на отключение. Клиент отключен");
                            setLogAndNick(null);
                            controller.clientsListMsg();
                            break;
                        }

                        // Неопознаный запрос от клиента
                        controller.putText("Неопознанный запрос от клиента :: " + msg);

                    }
                } catch (IOException e) {
                    controller.putText("Проблема связи с клиентом " + socket.toString() + " " + e.toString());
                } finally {
                    controller.removeClient(this);
                    closeConnection();
                }
            });

        } catch (IOException e) {
            controller.putText("Ошибка подключения клиента " + e.toString());
        }
    }

    public String getNick() {
        return nick;
    }

    public void setLogAndNick(AuthData newData) {
        if (newData == null) {
            log = null;
            nick = null;
            new Thread(()-> {
                Thread currentThread = Thread.currentThread();
                try {
                    currentThread.sleep(Const.TIMEOUT_NO_AUTH);
                } catch (InterruptedException e) {
                    controller.putText("Ошибка прерывания таймаута ожидания авторизации клиента " + e.toString());
                }
                if (log == null && socket != null && !socket.isClosed()) {
                    sendMsg(Const.CMD_STOP_CLIENT);
                    closeConnection();
                    controller.putText("Неавторизованный клиент отключен по тайм-ауту неактивности");
                }
            }).start();
        } else {
            log = newData.log;
            nick = newData.nick;
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            controller.putText("Ошибка отправки сообщения клиенту " + e.toString());
        }
    }

    public void closeConnection() {
        controller.putText("Отключаю клиента " + socket.toString());
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            controller.putText("Ошибка закрытия потоков " + e.toString());
        }
        nick = null;
    }

}