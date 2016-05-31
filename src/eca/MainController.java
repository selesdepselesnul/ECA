package eca;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
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
        final String[] keyValArr = str.split("=");
        if(keyValArr.length == 2)
            return keyValArr;
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
            try {
                if(connection.isConnect)
                    chatManager.send();

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
                        checkModeAndStatus();

                    }
                }  else if(e.getCode() == KeyCode.F2) {
                    if(!systemTextThread.isAlive() && connection.isConnect) {
                        if(connection.mode.equals("client")) {
                            System.out.println("close client");
                            chatManager.close();
                            animateText("client connection close!");
                            connection.isConnect = false;
                        } else {
                            System.out.println("close server");
                            if(!serverSocket.isClosed())
                                serverSocket.close();
                            chatManager.close();
                            animateText("server connection close!");
                            connection.isConnect = false;
                        }
                    }
                }


            } catch (IOException err) {
                animateText(err.getMessage());
            }

        });

        String welcomeMessage =
                "Welcome to SWAG CHAT App in order to use this app\n"+
                "you and your friend over there need to connect to each other\n"+
                "to be able to connect to each other you need type some config\n"+
                "the format is shown below\n" + "\nfor server side:\nport={port}\nmode=server\n"+
                "\nfor client side:\nip={ip}\nport={port}\nmode=client\n\n"+
                        "with each {ip} and {port} placeholder is customized with your own config\n"+
                "don't forget server came first and then client no the other way around!\n"+
                "what else do you waiting for ? start chatting !\noh i forget one thing, press F1 to connecting and F2 to disconnecting!\n"+
                "btw you can't delete this message right now,"
                        +" wait until i finish speaking\n";
        animateText(welcomeMessage);
    }

    private void animateText(String text) {
        this.chatTextArea.clear();
        this.chatTextArea.setEditable(false);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i=0; i<text.length();i++) {
                    try {
                        Thread.sleep(100);
                        chatTextArea.appendText(String.valueOf(text.charAt(i)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                chatTextArea.setEditable(true);
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
                    animateText(
                            "Server active !");
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
                    animateText(
                            "Client connect to server, "
                            +"type some word to your friend over there!");
                    connection.isConnect = true;
                }
            }
        }

    }
}
