package jca;

import javafx.concurrent.Task;
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
    private Thread systemTextThread;

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

        chatTextArea.setStyle("-fx-background-color: black;");
        chatTextArea.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.F1) {
                if(!systemTextThread.isAlive()) {
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


                        }
                    }
                    System.out.println(connection);
                    try {
                        checkModeAndStatus();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else if(connection.isConnect) {
                try {
                    chatManager.send();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        String welcomeMessage = "Welcome to SWAG CHAT App\n"+
                "in order to use this app\n"+
                "you, yes you who watch me right now ! and your friend over there\n"+
                "need to connect to each other\n"+
                "delete this message and type\n"+
                "port=port\nmode=server\nin the server side and for client side\n"+
                "ip=ip\nport=port\nmode=client\nalso make sure open the server first\n"+
                "before client try to connect, what do u waiting for\n"+
                "start chatting ! oh i forget one thing, press F1 to connect\n"+
                "so delete my message btw and start config your connection after i stop talking :)\n";
        animateText(welcomeMessage);
    }

    private void animateText(String text) {
        this.chatTextArea.clear();
        Task<Void> task = new Task() {
            @Override
            protected Object call() throws Exception {
                for (int i=0; i<text.length();i++) {
                    try {
                        Thread.sleep(100);
                        chatTextArea.appendText(String.valueOf(text.charAt(i)));
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        systemTextThread = new Thread(task);
        systemTextThread.start();

    }

    public void checkModeAndStatus() throws IOException {
        if(connection.mode != null) {
            if(connection.mode.equalsIgnoreCase("server")) {
                if (!connection.isConnect) {
                    serverSocket = new ServerSocket(connection.port);
                    animateText("Server active, please wait for client to connect and give client first echo to you!");
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
                    animateText("Client connect to server, type some word to your friend over there!");
                    connection.isConnect = true;
                }
            }
        }

    }
}
