package controller;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import initiation.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import receipt.MailBody;

public class AttachmentItemController {

    @FXML
    private Label nameFileLabel;

    @FXML
    void downloadFile() {
        byte[] fileBody = mailBody.decode();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "INFO(*.*)", "*."+mailBody.getFileType());
        fileChooser.getExtensionFilters().add(extFilter);
        //показать диалоговое окно для выбора и сохрания файла и привязать его к главному окну приложения
        File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
        //если файл создан
        if (file != null) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(fileBody, 0, fileBody.length);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private MailBody mailBody;

    @FXML
    void initialize(MailBody mailBody) {
        this.mailBody = mailBody;
    }

    public void showInfo(){
        nameFileLabel.setText(mailBody.getName()==null?mailBody.getFileType():mailBody.getName());
    }
}
