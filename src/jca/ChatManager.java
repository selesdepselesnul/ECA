package jca;

import javafx.concurrent.Task;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatManager {
    private final Consumer<String> acceptData;
    private final Supplier<String> headerSupplier;
    private final Supplier<String> messageSupplier;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    private Socket clientSocket;

    ChatManager(Consumer<String> chatConsumer,
                Supplier<String> headerSupplier,
                Supplier<String> messageSupplier) {

        this.acceptData = chatConsumer;
        this.headerSupplier = headerSupplier;
        this.messageSupplier = messageSupplier;
    }

    public void connectAndReceive(Supplier<Socket> socketSupplier) {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                clientSocket = socketSupplier.get();
                clientSocket.setTcpNoDelay(true);

                dIn = new DataInputStream(clientSocket.getInputStream());
                dOut = new DataOutputStream(clientSocket.getOutputStream());
                String data;

                while ((data = dIn.readUTF()) != null)
                    acceptData.accept(data);

                return null;

            }
        };
        new Thread(task).start();
    }

    public void send() throws IOException {
        dOut.writeUTF(
                this.headerSupplier.get()+":\n"+this.messageSupplier.get()+"\n\n\n");
        dOut.flush();
    }

    public void close() throws IOException {
        dIn.close();
        dOut.close();
        clientSocket.close();
    }
}
