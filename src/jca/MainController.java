package jca;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TextArea chatTextArea;

    private ServerSocket serverSocket;

    private ChatManager chatManager;
    private Connection connection = new Connection();

    private String[] splitKeyValue(String str) {
        String[] splitedStr = str.split("=");
        if(splitedStr.length == 2) {
            String[] keyValueArr = {splitedStr[0], splitedStr[1]};
            return keyValueArr;
        }
        return null;
    }
    private Map<String, String> parse(String connectionConfig) {
        Map<String, String> connectionMap = new HashMap<>();
        String[] connections = connectionConfig.split("\n");
        if(connections.length == 3) {
            String[] firstPair = splitKeyValue(connections[0]);
            String[] secondPair = splitKeyValue(connections[1]);
            String[] thirdPair = splitKeyValue(connections[2]);
            if (firstPair != null)
                connectionMap.put(firstPair[0], firstPair[1]);
            if(secondPair != null)
                connectionMap.put(secondPair[0], secondPair[1]);
            if(thirdPair != null)
                connectionMap.put(thirdPair[0], thirdPair[1]);
        } else if(connections.length == 2) {
            String[] firstPair = splitKeyValue(connections[0]);
            String[] secondPair = splitKeyValue(connections[1]);
            if (firstPair != null)
                connectionMap.put(firstPair[0], firstPair[1]);
            if(secondPair != null)
                connectionMap.put(secondPair[0], secondPair[1]);
        }
        return connectionMap;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.chatManager = new ChatManager(
                chatTextArea::getText,
                chatTextArea::setText
        );


        chatTextArea.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.ESCAPE) {

                Map<String, String> connectionMap = parse(chatTextArea.getText());
                if(connectionMap != null) {
                    if(connectionMap.containsKey("mode") && connectionMap.size() >=2) {
                        if(connectionMap.get("mode").equalsIgnoreCase("server")
                                && connectionMap.containsKey("port")
                                && connectionMap.size() == 2) {
                            connection.mode = connectionMap.get("mode");
                            connection.port = Integer.parseInt(connectionMap.get("port"));
                        } else if(connectionMap.get("mode").equalsIgnoreCase("client")
                                && connectionMap.containsKey("ip")
                                && connectionMap.containsKey("port")) {
                            connection.mode = connectionMap.get("mode");
                            connection.port = Integer.parseInt(connectionMap.get("port"));
                            connection.ip = connectionMap.get("ip");
                        } else {
                            connection.mode = null;
                            connection.port = 0;
                            connection.ip = null;
                        }


                    } else {
                        chatTextArea.setText("mode must be selected !");
                    }
                }
                System.out.println(connection);
                try {
                    checkModeAndStatus();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } else if(connection.isConnect) {
                try {
                    chatManager.send();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void checkModeAndStatus() throws IOException {
        if(connection.mode.equalsIgnoreCase("server")) {
            if (!connection.isConnect) {
                serverSocket = new ServerSocket(connection.port);
                chatTextArea.setText("server active");
                this.chatManager.connectAndReceive(
                        () -> {
                            try {
                                return serverSocket.accept();
                            } catch (IOException e1) {
                                return null;
                            }
                        }
                );
                connection.isConnect = true;
            } else {
                connection.isConnect = false;
                serverSocket.close();
            }
        } else {
            if (!connection.isConnect) {
                chatManager.connectAndReceive(() -> {
                    try {
                        return new Socket(connection.ip, connection.port);
                    } catch (IOException e) {
                        return null;
                    }
                });
                chatTextArea.setText("client connect");
                connection.isConnect = true;
            } else {
                connection.isConnect = false;
                chatManager.close();
            }
        }
    }
}
