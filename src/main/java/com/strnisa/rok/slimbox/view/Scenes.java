package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.model.Constants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public enum Scenes {
  MAIN, SIGN_IN;

  private static Stage stage;

  public static void init(Stage stage) {
    Scenes.stage = stage;
  }

  public static void setScene(Scenes scenes) throws IOException {
    Scene scene = null;
    switch (scenes) {
      case MAIN:
        scene = loadScene("/main.view.fxml");
        break;
      case SIGN_IN:
        scene = loadScene("/sign-in.view.fxml");
        break;
    }
    Stage newStage = createNewStage();
    newStage.setScene(scene);
    stage.hide();
    stage = newStage;
    showAndPreventMakingSmaller(stage);
  }

  private static Stage createNewStage() {
    return createNewStage(null);
  }

  static Stage createNewStage(String subTitle) {
    Stage newStage = new Stage();
    newStage.setTitle(Constants.PRODUCT_NAME + (subTitle == null ? "" : ": " + subTitle) + " (" + Constants.VERSION + ")");
    newStage.getIcons().add(new Image(Scenes.class.getResourceAsStream("/logo-256.png")));
    return newStage;
  }

  static void showAndPreventMakingSmaller(Stage stage) {
    stage.show();
    stage.setMinHeight(stage.getHeight());
    stage.setMinWidth(stage.getWidth());
  }

  static Scene loadScene(String resource) throws IOException {
    FXMLLoader loader = new FXMLLoader(Scenes.class.getResource(resource));
    Parent root = loader.load();
    Scene scene = new Scene(root);
    scene.setUserData(loader.getController());
    return scene;
  }
}
