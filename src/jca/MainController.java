package jca;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MainController {

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

    ServerSocket serverSocket;
    Socket socket;
    Socket clientSocket;
    private DataOutputStream dOut;


    private void disableServerInput(boolean isDisble) {
        portTextField.setDisable(isDisble);
        destPortTextField.setDisable(!isDisble);
        destIPTextField.setDisable(!isDisble);
    }
    public void handleServerCheckBox(ActionEvent actionEvent) {
        if (serverCheckBox.isSelected()) {
            disableServerInput(false);
            listenButton.setText("listen");
        } else {
            disableServerInput(true);
            listenButton.setText("connect");
        }
    }

    @FXML
    public void handleClickListenButton(ActionEvent e) throws IOException {

        String currentText = listenButton.getText();
        if(serverCheckBox.isSelected()) {
            if (currentText.equals("listen")) {
                System.out.println("listen");

                int port = Integer.parseInt(portTextField.getText());

                System.out.println(port);
                serverSocket = new ServerSocket(port);
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        clientSocket = serverSocket.accept();
                        DataInputStream dat = new DataInputStream(clientSocket.getInputStream());

                        String data;
                        while ((data = dat.readUTF()) != null)
                                chatTextArea.setText(data);

                        return null;

                    }
                };
                new Thread(task).start();
                listenButton.setText("unlisten");
                portTextField.setDisable(true);
            } else {
                serverSocket.close();
                listenButton.setText("listen");
                portTextField.setDisable(false);
            }
        } else {
            if (currentText.equals("connect")) {
                int port = Integer.parseInt(destPortTextField.getText());
                String ip = destIPTextField.getText();
                socket = new Socket(ip, port);
                socket.setTcpNoDelay(true);
                dOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream dat = new DataInputStream(socket.getInputStream());
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        String data;
                        while ((data = dat.readUTF()) != null)
                            chatTextArea.setText(data);

                        return null;
                    }
                };
                new Thread(task).start();
                listenButton.setText("disconnect");
            } else {
                socket.close();
                listenButton.setText("connect");
            }
        }
    }

    @FXML
    public void handleSendButton() throws IOException {
        if(serverCheckBox.isSelected()) {
            dOut = new DataOutputStream(this.clientSocket.getOutputStream());
            dOut.writeUTF(messageTextArea.getText());
        } else {

            System.out.println("send");
            dOut.writeUTF(messageTextArea.getText());
            dOut.flush();
        }
    }

}
