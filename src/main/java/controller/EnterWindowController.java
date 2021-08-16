package controller;

import dispatch.MailType;
import initiation.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import receipt.IMAPrecipient;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class EnterWindowController {

    private static IMAPrecipient imaPrecipient;
    private final static String filePath = "src/main/resources/temp.txt";

    @FXML
    private TextField loginTextField;

    @FXML
    private PasswordField passTextField;

    @FXML
    private ChoiceBox<String> emailTypeBox;

    @FXML
    private Button enterButton;

    @FXML
    private Label errorLabel;

    @FXML
    public void enterMethod() {
        errorLabel.setText("");
        boolean flag = false;
        if (!loginTextField.getText().isEmpty() && !passTextField.getText().isEmpty()) {
            if (emailTypeBox.isVisible()) {
                imaPrecipient = new IMAPrecipient(loginTextField.getText() + emailTypeBox.getValue(), passTextField.getText(),
                        MailType.resolveTypeById(emailTypeBox.getValue().replace("@", "smtp.")));
                flag = true;
            } else {
                String[] temp = loginTextField.getText().split("@");
                imaPrecipient = new IMAPrecipient(loginTextField.getText(), passTextField.getText(),
                        MailType.resolveTypeById("smtp." + temp[temp.length - 1]));
                flag = false;
            }

        } else {
            errorLabel.setText("Введите все данные");
        }
        if (imaPrecipient.checkConnection()) {
            errorLabel.setText("неверные данные или отсутствует подключение к интернету");
        } else {
            createSignIn(flag);
            try {
                Main.main.loadMainMailWindow(Main.getPrimaryStage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void initialize() {
        ObservableList<String> mailtype = FXCollections.observableArrayList();
        Arrays.asList(MailType.values()).forEach(item -> {
            mailtype.add(item.getAddress().replace("smtp.", "@"));
        });
        emailTypeBox.setItems(mailtype);
        emailTypeBox.getSelectionModel().select(0);

        loginTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!emailTypeBox.isVisible() && !newValue.contains("@")) {
                emailTypeBox.setVisible(true);
            }
            if (newValue.contains("@") && emailTypeBox.isVisible()) {
                emailTypeBox.setVisible(false);
            }
            if (!emailTypeBox.isVisible() && newValue.lastIndexOf("@") != newValue.indexOf("@")) {
                loginTextField.setText(oldValue);
            }
        });
    }


    public static IMAPrecipient getImaPrecipient() {
        return imaPrecipient;
    }

    public boolean isEntered() {
        String[] data = loadSignIn();
        if (data[0] == null || data[1] == null || data[2] == null) {
            return false;
        }
        imaPrecipient = new IMAPrecipient(data[0], data[1], MailType.resolveTypeById(data[2]));
        if (imaPrecipient.checkConnection()) {
            errorLabel.setText("неверные данные или отсутствует подключение к интернету");
            return false;
        } else {
            return true;
        }
    }

    private String[] loadSignIn() {
        String[] data = new String[3];
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = bufferedReader.readLine();
                    if (data[i] == null) {
                        return data;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private void createSignIn(boolean flag) {
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            if (flag) {
                stringBuilder.append(loginTextField.getText()).append(emailTypeBox.getValue());
                stringBuilder.append("\n");
                stringBuilder.append(passTextField.getText());
                stringBuilder.append("\n");
                stringBuilder.append(emailTypeBox.getValue().replace("@", "smtp."));
            } else {
                stringBuilder.append(loginTextField.getText());
                stringBuilder.append("\n");
                stringBuilder.append(passTextField.getText());
                stringBuilder.append("\n");
                String[] temp = loginTextField.getText().split("@");
                stringBuilder.append("smtp.").append(temp[temp.length - 1]);
            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void signOut() {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            Main.main.loadEnterWindow(Main.getPrimaryStage());
        }
    }
}
