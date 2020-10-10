import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.*;

public class Server extends Application {

    private ControllerServer controller;
    private Handler handler;
    public static final Logger logger = Logger.getLogger(Const.SERVER_NAME);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        try {
            handler = new FileHandler(Const.SERVER_NAME + "%g.log",1024*1024,20,true);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.setUseParentHandlers(true);
            logger.addHandler(handler);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка создания/открытия файла журнала ", e);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setTitle(Const.TITLE_SERVER);
        stage.setScene(new Scene(root));
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        controller.close();
        super.stop();
        handler.close();
    }

}
