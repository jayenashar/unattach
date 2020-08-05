package app.unattach.view;

import app.unattach.controller.Controller;
import app.unattach.controller.ControllerFactory;
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
