package com.bqsummer.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class FXApplication extends Application {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    MainPanel mainPanel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPanel.fxml"));
        Parent rootLayout = loader.load();
        mainPanel = (MainPanel) loader.getController();
        initImg();
        Scene scene = new Scene(rootLayout, 2200, 1200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initImg() throws Exception {
        mainPanel.initImg();
        mainPanel.initValue();
        mainPanel.initBind();
    }


}
