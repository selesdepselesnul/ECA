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
                x -> chatTextArea.appendText(x),
                () -> this.usernameTextField.getText(),
                () -> this.messageTextArea.getText());
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
                listenButton.setText("unlisten");
                portTextField.setDisable(true);
            } else {
                chatManager.close();
                serverSocket.close();
                listenButton.setText("listen");
                portTextField.setDisable(false);
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
                listenButton.setText("disconnect");
            } else {
                this.chatManager.close();
                listenButton.setText("connect");
            }
        }
    }

    @FXML
    public void handleSendButton() throws IOException {
        this.chatManager.send();
    }

}
