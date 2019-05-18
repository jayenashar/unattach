package com.strnisa.rok.slimbox.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ProgressBarWithText extends StackPane {
  private static final int TEXT_PADDING = 5;

  private final ProgressBar progressBar = new ProgressBar();
  private final Text text = new Text();

  public ProgressBarWithText() {
    progressBar.setMaxWidth(Double.MAX_VALUE);
    text.textProperty().addListener((observable, oldValue, newValue) -> {
      progressBar.setMinHeight(text.getBoundsInLocal().getHeight() + TEXT_PADDING * 2);
      progressBar.setMinWidth(text.getBoundsInLocal().getWidth() + TEXT_PADDING * 2);
    });
    getChildren().setAll(progressBar, text);
  }

  DoubleProperty progressProperty() {
    return progressBar.progressProperty();
  }

  StringProperty textProperty() {
    return text.textProperty();
  }
}