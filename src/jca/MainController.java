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


    private void setControlDisable(boolean isDisble) {
        portTextField.setDisable(isDisble);
        destPortTextField.setDisable(!isDisble);
        destIPTextField.setDisable(!isDisble);
    }
    public void handleServerCheckBox(ActionEvent actionEvent) {
        if (serverCheckBox.isSelected()) {
            setControlDisable(false);
            listenButton.setText("listen");
        } else {
            setControlDisable(true);
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

                        Socket clientSocket;
                        while ((clientSocket = serverSocket.accept()) != null) {
                            BufferedReader buff = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            chatTextArea.setText(buff.readLine());
                            buff.close();
                        }

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
                System.out.println(port);
                System.out.println(ip);
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            socket = new Socket(ip, port);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
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

        } else {
            int port = Integer.parseInt(destPortTextField.getText());
            String ip = destIPTextField.getText();
            socket = new Socket(ip, port);
            socket.setTcpNoDelay(true);
            System.out.println("send");
            BufferedWriter buff = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            buff.write(messageTextArea.getText());
            buff.flush();
            buff.close();
            socket.close();
        }
    }

}
