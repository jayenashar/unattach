package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.controller.Controller;
import com.strnisa.rok.slimbox.controller.ControllerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.UnsupportedEncodingException;

public class CoffeeController {
  private Controller controller;

  @FXML
  private TextField amountTextField;
  @FXML
  private Button donateButton;

  @FXML
  public void initialize() {
    controller = ControllerFactory.getDefaultController();
    amountTextField.textProperty().addListener(observable -> {
      try {
        Integer.parseInt(amountTextField.getText());
        donateButton.setDisable(false);
      } catch (NumberFormatException e) {
        donateButton.setDisable(true);
      }
    });
  }

  @FXML
  private void onDonateButtonPressed() throws UnsupportedEncodingException {
    amountTextField.setDisable(true);
    donateButton.setDisable(true);
    String amountString = amountTextField.getText();
    int amount = Integer.parseInt(amountString);
    controller.donate("Bags of Coffee", amount);
    donateButton.getScene().getWindow().hide();
  }
}
