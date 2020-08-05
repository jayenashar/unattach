package app.unattach.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;

import java.io.IOException;
import java.net.URL;

public class SubView extends TitledPane {
  public SubView() {
    loadView();
  }

  private void loadView() {
    URL resource = getClass().getResource("/sub-view.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(resource);
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}