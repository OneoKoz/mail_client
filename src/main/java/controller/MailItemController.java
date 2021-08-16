package controller;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import receipt.MailInfoHeader;

public class MailItemController {

    private static MailItemController activeController;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private GridPane colorPane;

    @FXML
    private Button spamButton;

    @FXML
    private CheckBox flagCheckBox;

    @FXML
    private Button deleteButton;

    @FXML
    private Label senderLabel;

    @FXML
    private Label topicLabel;

    @FXML
    private Label dateLabel;

    @FXML
    void changeStatusFlag(ActionEvent event) {

    }

    @FXML
    void deleteMail(ActionEvent event) {
        MainMailWindowController.getImaPrecipient().deleteMail(number);
        MainMailWindowController.mainMailWindowController.deleteMail(number);
    }

    @FXML
    void setMailSpam(ActionEvent event) {
    }

    private int number;

    public void showInfo(MailInfoHeader mailInfoHeader) {
        senderLabel.setText(mailInfoHeader.getFrom());
        topicLabel.setText(mailInfoHeader.getSubtract());
        dateLabel.setText(mailInfoHeader.getDate());
    }

    @FXML
    void initialize(int number) {
        this.number = number;
        anchorPane.setOnMouseClicked(mouseEvent -> {
            deleteButton.setVisible(true);
            if (!Objects.equals(activeController, null)) {
                activeController.hideInfo();
            }
            colorPane.setStyle("-fx-background-color: #a3ff99;");
            activeController = this;
            MainMailWindowController.getImaPrecipient().receiveMailBody(number);
            MainMailWindowController.mainMailWindowController.showMailBodyInfo();
        });
    }

    public void hideInfo() {
        colorPane.setStyle("-fx-background-color: #99CCFF;");
        deleteButton.setVisible(false);
        spamButton.setVisible(false);
    }


}
