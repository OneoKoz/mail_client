package initiation;

import controller.EnterWindowController;
import controller.SendNewMailWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.IIOException;
import java.io.IOException;
import java.util.Objects;


public class Main extends Application {

    public static Main main;
    public static Stage primary;

    @Override
    public void start(Stage primaryStage) {
        main = this;
        loadEnterWindow(primaryStage);
    }

    public void loadEnterWindow(Stage primaryStage){
        primary = primaryStage;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/fxml/EnterWindow.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Hello World");
            primaryStage.setScene(new Scene(root, 600, 335));
            primaryStage.show();
            EnterWindowController enterWindowController = loader.getController();
            if(enterWindowController.isEntered()){
                loadMainMailWindow(primaryStage);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSendNewMail() {
        try {
            //загрузить из fxml разметку диалогового окна редактирования передаваемого заказа устройства
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/fxml/SendNewMailWindow.fxml"));
            AnchorPane dialogLayout = loader.load();

            //создать новое окно
            Stage dialogStage = new Stage();
            //задать окну название
            dialogStage.setTitle("ONEO_MAIL");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //привязать окно к главному окну
            dialogStage.initOwner(primary);
            //создать сцену из загуженной разметки
            Scene scene = new Scene(dialogLayout);
            //установить сцену в окне
            dialogStage.setScene(scene);

            SendNewMailWindowController sendNewMailWindowController = loader.getController();
            sendNewMailWindowController.initialize(dialogStage);
            // показать диалоговое окно и ждать когда его закроют
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/SendNewMailWindow.fxml")));
//            primaryStage.setTitle("ONEO_MAIL");
//            primaryStage.setScene(new Scene(root, 900, 600));
//            primaryStage.show();
//        } catch (IIOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void loadMainMailWindow(Stage primaryStage) throws Exception {
        primary = primaryStage;
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MainMailWindow.fxml")));
            primaryStage.setTitle("ONEO_MAIL");
            primaryStage.setScene(new Scene(root, 950, 700));
            primaryStage.show();
        } catch (IIOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stage getPrimaryStage() {
        return primary;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
