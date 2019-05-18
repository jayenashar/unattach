package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.controller.Controller;
import com.strnisa.rok.slimbox.controller.ControllerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class FeedbackController {
  private Controller controller;

  @FXML
  private TextArea userTextTextArea;
  @FXML
  private Button submitButton;

  @FXML
  public void initialize() {
    controller = ControllerFactory.getDefaultController();
  }

  @FXML
  private void onSubmitButtonPressed() {
    userTextTextArea.editableProperty().setValue(false);
    submitButton.setDisable(true);
    controller.sendToServer("feedback", null, userTextTextArea.getText());
    submitButton.getScene().getWindow().hide();
  }
}
