package jca;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

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
    private TextField destIPTextField;

    private ServerSocket serverSocket;

    private ChatManager chatManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.chatManager = new ChatManager(
                chatTextArea::getText,
                chatTextArea::setText
        );

        chatTextArea.setOnKeyReleased(__ -> {
            try {
                chatManager.send();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void handleServerCheckBox() {
        if (serverCheckBox.isSelected()) {
            destIPTextField.setDisable(true);
            listenButton.setText("listen");
        } else {
            destIPTextField.setDisable(false);
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
            } else {
                cleanConnection("listen");
                serverSocket.close();
            }
        } else {
            if (currentText.equals("connect")) {
                String ip = destIPTextField.getText();
                int port = Integer.parseInt(portTextField.getText());
                chatManager.connectAndReceive(() -> {
                    try {
                        return new Socket(ip, port);
                    } catch (IOException e) {
                        return null;
                    }
                });
                listenButton.setText("disconnect");
            } else {
                cleanConnection("connect");
            }
        }
    }


    private void cleanConnection(String label) throws IOException {
        chatManager.close();
        listenButton.setText(label);
    }

}
