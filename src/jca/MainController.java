package jca;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TextArea chatTextArea;

    @FXML
    private Button listenButton;

    @FXML
    private CheckBox serverCheckBox;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField destPortTextField;

    @FXML
    private TextField destIPTextField;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private TextField usernameTextField;

    private ServerSocket serverSocket;

    private ChatManager chatManager;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chatManager = new ChatManager(
                x -> chatTextArea.setText(x),
                () -> usernameTextField.getText(),
                () -> chatTextArea.getText());
    }

    private void disableServerInput(boolean isDisble) {
        portTextField.setDisable(isDisble);
        destPortTextField.setDisable(!isDisble);
        destIPTextField.setDisable(!isDisble);
    }

    public void handleServerCheckBox() {
        if (serverCheckBox.isSelected()) {
            disableServerInput(false);
            listenButton.setText("listen");
        } else {
            disableServerInput(true);
            listenButton.setText("connect");
        }
    }

    @FXML
    public void handleClickListenButton() throws IOException {

        String currentText = listenButton.getText();
        if(serverCheckBox.isSelected()) {
            if (currentText.equals("listen")) {
                int port = Integer.parseInt(portTextField.getText());
                serverSocket = new ServerSocket(port);
                this.chatManager.connectAndReceive(
                        () -> {
                            try {
                                return serverSocket.accept();
                            } catch (IOException e1) {
                                return null;
                            }
                        }
                );
                disableConnectionInput("unlisten");
            } else {
                cleanConnection("listen");
                serverSocket.close();
            }
        } else {
            if (currentText.equals("connect")) {
                String ip = destIPTextField.getText();
                int port = Integer.parseInt(destPortTextField.getText());
                chatManager.connectAndReceive(() -> {
                    try {
                        return new Socket(ip, port);
                    } catch (IOException e) {
                        return null;
                    }
                });
                disableConnectionInput("disconnect");
            } else {
                cleanConnection("connect");
            }
        }
    }

    private void disableConnectionInput(String label) {
        listenButton.setText(label);
        destIPTextField.setDisable(true);
        destPortTextField.setDisable(true);
        portTextField.setDisable(true);
        usernameTextField.setDisable(true);
    }

    private void cleanConnection(String label) throws IOException {
        chatManager.close();
        usernameTextField.setDisable(false);
        disableServerInput(true);
        listenButton.setText(label);
    }

    @FXML
    public void handleEveryTyping() throws IOException {
        this.chatTextArea.appendText(
                this.messageTextArea.getText());
        this.chatManager.send();
        this.messageTextArea.clear();
    }

}
