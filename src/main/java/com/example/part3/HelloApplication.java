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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.apache.logging.log4j.Level;

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
        org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("ServerLogger");

        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
            private StringBuilder lineBuffer = new StringBuilder();

            @Override
            public void write(int b) throws java.io.IOException {
                if (b == '\n') {
                    String line = lineBuffer.toString().trim();
                    if (!line.isEmpty()) {
                        processAndLog(line, logger);
                    }
                    lineBuffer.setLength(0);
                } else if (b != '\r') {
                    lineBuffer.append((char) b);
                }
            }
        }));

        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            private StringBuilder errorBuffer = new StringBuilder();

            @Override
            public void write(int b) throws java.io.IOException {
                if (b == '\n') {
                    String line = errorBuffer.toString().trim();
                    if (!line.isEmpty()) {
                        logger.error("[STACK TRACE ERROR] " + line);
                    }
                    errorBuffer.setLength(0);
                } else if (b != '\r') {
                    errorBuffer.append((char) b);
                }
            }
        }));

        launch(args);
    }

    private static void processAndLog(String line, org.apache.logging.log4j.Logger logger) {
        String lower = line.toLowerCase();

        if (lower.contains("unable to find config.properties") || lower.contains("fatal")) {
            logger.fatal("[FATAL] " + line);
        } else if (lower.contains("error") || lower.contains("failed")) {
            logger.error("[ERROR] " + line);
        } else if (lower.contains("server stopped")) {
            logger.warn("[WARN] " + line);
        } else if (lower.contains("client connected") ||
                lower.contains("strategy: received request") ||
                lower.contains("received maze to solve") ||
                lower.contains("solving using") ||
                lower.contains("retrieve from memory")) {
            logger.info("[SERVER EVENT] " + line);
        } else {
            logger.info(line);
        }
    }
}