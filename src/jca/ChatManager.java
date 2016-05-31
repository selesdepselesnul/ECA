package jca;

import javafx.concurrent.Task;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatManager {

    private final Consumer<String> chatConsumer;
    private final Supplier<String> chatSupplier;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    private Socket clientSocket;

    public ChatManager(
                Supplier<String> chatSupplier,
                Consumer<String> chatConsumer) {

        this.chatConsumer = chatConsumer;
        this.chatSupplier = chatSupplier;
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
                    chatConsumer.accept(data);


                return null;

            }
        };
        new Thread(task).start();
    }

    public void send() throws IOException {
        dOut.writeUTF(
                this.chatSupplier.get());
        dOut.flush();
    }

    public void close() throws IOException {
        if (dIn != null)
            dIn.close();
        if (dOut != null)
            dOut.close();
        if(clientSocket != null)
            clientSocket.close();
    }
}
