package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.controller.Controller;
import com.strnisa.rok.slimbox.controller.ControllerFactory;
import com.strnisa.rok.slimbox.model.Constants;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorController {
  private Controller controller;

  @FXML
  private TextArea exceptionTextArea;
  @FXML
  private Label statusLabel;
  @FXML
  private Button okButton;

  @FXML
  public void initialize() {
    controller = ControllerFactory.getDefaultController();
  }

  void setException(Throwable t) {
    String stackTraceText = ExceptionUtils.getStackTrace(t);
    exceptionTextArea.setText(stackTraceText);
    Task<Boolean> task = new Task<>() {
      @Override
      protected Boolean call() {
        return controller.sendToServer("stack trace", stackTraceText, null);
      }

      @Override
      protected void succeeded() {
        if (getValue()) {
          statusLabel.setText("Stack trace sent to developers.");
        } else {
          statusLabel.setText("Failed to report error. Please consider emailing the above stack " +
              "trace to " + Constants.CONTACT_EMAIL + ".");
        }
      }
    };
    new Thread(task).start();
  }

  @FXML
  private void onOkButtonPressed() {
    okButton.setDisable(true);
    okButton.getScene().getWindow().hide();
  }
}
