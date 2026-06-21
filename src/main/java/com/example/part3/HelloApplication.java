package com.example.part3;

import View.MyViewController;
import ViewModel.MyViewModel;
import Model.MyModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));        Parent root = fxmlLoader.load();
        MyViewController view = fxmlLoader.getController();
        var url = HelloApplication.class.getResource("/View/MyView.fxml");
        System.out.println("FXML URL: " + url);

        MyModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);
        view.setViewModel(viewModel);

        primaryStage.setTitle("World Cup Maze Game 🏆⚽");
        primaryStage.setScene(new Scene(root, 750, 600));
        primaryStage.setOnCloseRequest(event -> {
            viewModel.shutDownServers();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}