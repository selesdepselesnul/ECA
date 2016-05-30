package jca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        AnchorPane root = fxmlLoader.load(ClassLoader.getSystemResourceAsStream("jca/main.fxml"));
        primaryStage.setTitle("JANCOK CHAT");
        primaryStage.setScene(new Scene(root));
        primaryStage.setWidth(658);
        primaryStage.setHeight(388);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
