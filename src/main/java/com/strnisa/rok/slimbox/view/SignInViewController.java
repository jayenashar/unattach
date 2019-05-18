package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.controller.Controller;
import com.strnisa.rok.slimbox.controller.ControllerFactory;
import com.strnisa.rok.slimbox.model.Constants;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignInViewController {
  private static final Logger LOGGER = Logger.getLogger(SignInViewController.class.getName());

  private Controller controller;
  @FXML
  private Text welcomeText;
  @FXML
  private Text termsAndConditionsText;
  @FXML
  private Button homepageButton;
  @FXML
  private Button termsAndConditionsButton;
  @FXML
  private Button signInButton;
  @FXML
  private CheckBox subscribeToUpdatesCheckBox;

  @FXML
  public void initialize() {
    controller = ControllerFactory.getDefaultController();
    welcomeText.setText("Welcome to " + Constants.PRODUCT_NAME + "!");
    termsAndConditionsText.setText(
        "6. Liability: To the extent permitted under Law, the Software is provided under an AS-IS basis. Licensor\n" +
        "shall never, and without any limit, be liable for any damage, cost, expense or any other payment incurred\n" +
        "by Licensee as a result of Software's actions, failure, bugs and/or any other interaction between the\n" +
        "Software and Licensee's end-equipment, computers, other software or any 3rd party, end-equipment, computer\n" +
        "or services. Moreover, Licensor shall never be liable for any defect in source code written by Licensee\n" +
        "when relying on the Software or using the Software's source code.");
    Platform.runLater(() -> signInButton.requestFocus());
  }

  @FXML
  private void onHomepageButtonPressed() {
    controller.openSlimboxHomepage();
  }

  @FXML
  protected void onTermsAndConditionsButtonPressed() {
    controller.openTermsAndConditions();
  }

  @FXML
  protected void onSignInWithGoogleButtonPressed() {
    disableControls();
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        String emailAddress = controller.signIn();
        if (subscribeToUpdatesCheckBox.isSelected()) {
          LOGGER.info("Subscribing to updates..");
          controller.subscribe(emailAddress);
          LOGGER.info("Subscribing to updates.. successful.");
        }
        return null;
      }

      @Override
      protected void succeeded() {
        try {
          Scenes.setScene(Scenes.MAIN);
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Failed to load the main view.", e);
          e.printStackTrace();
        } finally {
          resetControls();
        }
      }

      @Override
      protected void cancelled() {
        LOGGER.log(Level.WARNING, "Signing in cancelled.");
      }

      @Override
      protected void failed() {
        LOGGER.log(Level.SEVERE, "Failed to process sign in button click.", getException());
        resetControls();
      }
    };

    new Thread(task).start();
  }

  private void disableControls() {
    homepageButton.setDisable(true);
    termsAndConditionsButton.setDisable(true);
    signInButton.setDisable(true);
    subscribeToUpdatesCheckBox.setDisable(true);
  }

  private void resetControls() {
    homepageButton.setDisable(false);
    termsAndConditionsButton.setDisable(false);
    signInButton.setDisable(false);
    subscribeToUpdatesCheckBox.setDisable(false);
  }
}
