package com.strnisa.rok.slimbox;

import com.strnisa.rok.slimbox.model.Constants;
import com.strnisa.rok.slimbox.view.Scenes;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MainFx extends Application {
  private static final Logger LOGGER = Logger.getLogger(MainFx.class.getName());

  @Override
  public void start(Stage stage) {
    try {
      LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/logging.properties"));
      LOGGER.info("Starting " + Constants.PRODUCT_NAME + " .. (max memory in bytes: " + Runtime.getRuntime().maxMemory() + ")");
      Scenes.init(stage);
      Scenes.setScene(Scenes.SIGN_IN);
//      Scenes.setScene(Scenes.MAIN);
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Failed to start.", t);
    }
  }

  public static void main(String[] args) {
    Application.launch(MainFx.class, args);
  }
}
