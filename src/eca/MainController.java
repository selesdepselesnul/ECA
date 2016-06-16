package eca;

import javafx.application.Platform;
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
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML
    private TextArea chatTextArea;

    private ServerSocket serverSocket;
    private ChatManager chatManager;
    private Connection connection = new Connection();
    private Thread systemTextThread;
    private Task<Void> messageTask;

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


    private void giveIntro() {
        chatTextArea.setText(
                "F1 connect it (if you know what do i mean ?)\nF2 to stop it \n"
                        +"F3 freakin boring intro\nF4 back to this message\nF5 exec command\n"
                        +"F6 make font smaller\nF7 make font bigger\nF8 exit");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.chatManager = new ChatManager(
                chatTextArea::getText,
                chatTextArea::setText
        );

        giveIntro();
        chatTextArea.setOnKeyReleased(e -> {
            try {
                if(connection.isConnect)
                    chatManager.send();
                if(messageTask ==  null || !messageTask.isRunning()) {
                    if(e.getCode() == KeyCode.F1) {

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
                    } else if(e.getCode() == KeyCode.F3) {
                        try(BufferedReader buff = new BufferedReader(new InputStreamReader(
                                ClassLoader.getSystemResourceAsStream("eca/message.txt")));) {
                            animateText(buff.lines().collect(Collectors.joining("\n")));
                        } catch (IOException ioE) {
                            ioE.printStackTrace();
                        }
                    } else if(e.getCode() == KeyCode.F4) {
                        giveIntro();
                    } else if(e.getCode() == KeyCode.F5) {
                        Runtime runtime = Runtime.getRuntime();
                        Process process = runtime.exec(chatTextArea.getSelectedText());
                        try(BufferedReader buff = new BufferedReader(new InputStreamReader(
                                process.getInputStream()));) {
                            chatTextArea.appendText("\noutput:\n"+buff.lines().collect(Collectors.joining("\n")));
                        } catch (IOException ioE) {
                            ioE.printStackTrace();
                        }

                    } else if(e.getCode() == KeyCode.F6) {
                        chatTextArea.setStyle("-fx-font-size : " + (chatTextArea.getFont().getSize() - 1));
                    } else if(e.getCode() == KeyCode.F7) {
                        chatTextArea.setStyle("-fx-font-size : " + (chatTextArea.getFont().getSize() + 1));
                    } else if(e.getCode() == KeyCode.F8) {
                        Platform.exit();
                    }

                }


            } catch (IOException err) {
                animateText(err.getMessage());
            }
        });

    }


    private void animateText(String text) {
        this.chatTextArea.clear();
        this.chatTextArea.setEditable(false);
        messageTask = new Task<Void>() {
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
        systemTextThread = new Thread(messageTask);
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
                            "Client connect to the server !");
                    connection.isConnect = true;
                }
            }
        } else {
           animateText("what r u doin here ?\nread the intro if u don't know nothing !");
        }

    }
}
