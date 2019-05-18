package com.strnisa.rok.slimbox;

import com.strnisa.rok.slimbox.model.Constants;
import com.strnisa.rok.slimbox.view.Scenes;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

  public static void main(String[] args) throws IOException, URISyntaxException {
    if (args.length == 0 && Runtime.getRuntime().maxMemory() < Math.pow(1000, 3)) {
      String currentPath = MainFx.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1)
          .replace("/", File.separator);
      Runtime.getRuntime().exec("java -jar -Xmx1024m \"" + currentPath + "\" restart");
      return;
    }
    Application.launch(MainFx.class, args);
  }
}
