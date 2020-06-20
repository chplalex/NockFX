import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {

    private ControllerClient controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStageClient(stage);
        stage.setTitle(Const.TITLE_CLIENT_AUTH_NO);
        stage.setScene(new Scene(root));
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.close();
    }

}