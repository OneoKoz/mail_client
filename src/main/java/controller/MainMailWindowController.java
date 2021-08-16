package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import initiation.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import receipt.BoxType;
import receipt.IMAPrecipient;
import receipt.MailBody;
import receipt.MailInfoHeader;

public class MainMailWindowController {

    public static MainMailWindowController mainMailWindowController;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox allMailVBox;

    @FXML
    private WebView mainInfoWebView;


    @FXML
    private HBox allAttachmentHBox;

    @FXML
    void sendNewMail() throws Exception {
        Main.main.loadSendNewMail();
    }

    @FXML
    void showBasket() {
        imaPrecipient.setBoxType(BoxType.TRASH);
        readLoadMail();
    }

    @FXML
    void showDraft(ActionEvent event) {
        imaPrecipient.setBoxType(BoxType.DRAFTS);
        readLoadMail();
    }

    @FXML
    void showIncoming(ActionEvent event) {
        imaPrecipient.setBoxType(BoxType._INBOX);
        readLoadMail();
    }

    @FXML
    void showSent(ActionEvent event) {
        imaPrecipient.setBoxType(BoxType.SENT);
        readLoadMail();
    }

    @FXML
    void showSpam(ActionEvent event) {
        imaPrecipient.setBoxType(BoxType.SPAM);
        readLoadMail();
    }


    @FXML
    void updateBox() {
        //todo можно сделать подгрузку новых сообщений, а не все заново перемещать
        loadAllMail(imaPrecipient.updateMail());
    }

    @FXML
    void signOut() {
        EnterWindowController.signOut();
    }

    private static final IMAPrecipient imaPrecipient = EnterWindowController.getImaPrecipient();

    @FXML
    void initialize() {
        readLoadMail();
        mainMailWindowController = this;
    }

    void readLoadMail() {
        allMailVBox.getChildren().clear();
        try {
            imaPrecipient.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadAllMail(imaPrecipient.getAllMail());
    }

    void deleteMail(int number) {
        allMailVBox.getChildren().remove(number, number);
        imaPrecipient.getAllMail().remove(imaPrecipient.getNumMail() - number);
        imaPrecipient.setNumMail(imaPrecipient.getNumMail()-1);
        loadAllMail(imaPrecipient.getAllMail());
        WebEngine webEngine = mainInfoWebView.getEngine();
        webEngine.loadContent("");
        allAttachmentHBox.getChildren().clear();
    }

    private void loadAllMail(List<String> current) {
        AtomicInteger count = new AtomicInteger(imaPrecipient.getNumMail());
        AtomicInteger number = new AtomicInteger(0);
        current.forEach(item -> {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource("/fxml/MailItemLayout.fxml"));
                AnchorPane orderLayOut = loader.load();

                if (allMailVBox.getChildren().isEmpty()) {
                    number.set(-1);
                    allMailVBox.getChildren().add(orderLayOut);
                } else if (number.intValue() == -1) {
                    allMailVBox.getChildren().add(orderLayOut);
                } else {
                    allMailVBox.getChildren().add(number.getAndIncrement(), orderLayOut);
                }

                MailItemController mailItemController = loader.getController();
                mailItemController.initialize(count.getAndDecrement());
                mailItemController.showInfo(new MailInfoHeader(item));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showMailBodyInfo() {
        WebEngine webEngine = mainInfoWebView.getEngine();
        webEngine.loadContent("");
        List<MailBody> allBodies = new ArrayList<>();
        imaPrecipient.getMailBody().forEach(item -> {
            if (item.length() > 0) {
                allBodies.add(new MailBody(item));
//            DecoderFactory decoderFactory = new DecoderFactory(item);
//            if(!Objects.equals(decoderFactory.getIdecoder(),null)){
//                String[] temp = item.split("\n");
//                WebEngine webEngine = mainInfoWebView.getEngine();
//                if(decoderFactory.isHtmlText(item)){
//                    webEngine.loadContent(temp[temp.length-1]);
//                }else {
//                    webEngine.loadContent("");
//                }
//            }
            }
        });
        allAttachmentHBox.getChildren().clear();
        allBodies.forEach(item -> {
            if (item.isHtmlText()) {
                webEngine.loadContent(new String(item.decode()));
            } else if (item.isPlaintext()) {
                webEngine.loadContent("<div>" + new String(item.decode()) + "</div>");
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("/fxml/AttachmentItemLayout.fxml"));
                    AnchorPane orderLayOut = loader.load();

                    allAttachmentHBox.getChildren().add(orderLayOut);
                    AttachmentItemController attachmentItemController = loader.getController();
                    attachmentItemController.initialize(item);
                    attachmentItemController.showInfo();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static IMAPrecipient getImaPrecipient() {
        return imaPrecipient;
    }
}
