package controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import dispatch.SMTPSender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import receipt.IMAPrecipient;

public class SendNewMailWindowController {

    private Stage dialogStage;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField recipientEmail;

    @FXML
    private TextField topicEmail;

    @FXML
    private Button attachButton;

    @FXML
    private Button sendButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label attachmentLabel;

    @FXML
    private HTMLEditor contentEditor;

    private final List<File> attachments = new ArrayList<>();

    @FXML
    void attachMethod() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            attachments.add(selectedFile);
            attachmentLabel.setText(attachmentLabel.getText() + selectedFile.getName() + "; ");
        }
    }

    @FXML
    void sendMethod() {
        errorLabel.setText("");
        if (!recipientEmail.getText().isEmpty() &&
                !contentEditor.getHtmlText().isEmpty()) {
            IMAPrecipient imaPrecipient = EnterWindowController.getImaPrecipient();
            SMTPSender smtpSender = new SMTPSender(imaPrecipient.getUser(), imaPrecipient.getPass(), imaPrecipient.getMailType());
            try {
                smtpSender.send(recipientEmail.getText(),
                        topicEmail.getText(),
                        contentEditor.getHtmlText(),
                        attachments);
                dialogStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("введите получателя");
        }
    }

    @FXML
    public void initialize(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
