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

    ServerSocket serverSocket;

    private NetworkManager networkManager;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkManager = new NetworkManager(
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
                System.out.println("listen");
                int port = Integer.parseInt(portTextField.getText());
                System.out.println(port);
                serverSocket = new ServerSocket(port);
                this.networkManager.connectAndReceive(
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
                networkManager.close();
                serverSocket.close();
                listenButton.setText("listen");
                portTextField.setDisable(false);
            }
        } else {
            if (currentText.equals("connect")) {
                int port = Integer.parseInt(destPortTextField.getText());
                String ip = destIPTextField.getText();
                networkManager.connectAndReceive(() -> {
                    try {
                        return new Socket(ip, port);
                    } catch (IOException e) {
                        return null;
                    }
                });
                listenButton.setText("disconnect");
            } else {
                this.networkManager.close();
                listenButton.setText("connect");
            }
        }
    }

    @FXML
    public void handleSendButton() throws IOException {
        this.networkManager.send();
    }

}
