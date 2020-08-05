package app.unattach.view;

import app.unattach.controller.Controller;
import app.unattach.controller.ControllerFactory;
import app.unattach.model.Email;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LinkButtonTableCellFactory implements Callback<TableColumn.CellDataFeatures<Email, Button>, ObservableValue<Button>> {
  private final Controller controller;

  public LinkButtonTableCellFactory() {
    controller = ControllerFactory.getDefaultController();
  }

  @Override
  public ObservableValue<Button> call(TableColumn.CellDataFeatures<Email, Button> cellDataFeatures) {
    Email email = cellDataFeatures.getValue();
    Button button = new Button();
    button.setText("Open");
    addOnActionHandler(email, button);
    return new SimpleObjectProperty<>(button);
  }

  private void addOnActionHandler(Email email, Button button) {
    button.setOnAction(event -> {
      String query = URLEncoder.encode("rfc822msgid:" + email.getUniqueId(), UTF_8);
      String baseUrl = "https://mail.google.com/mail/u/0/#search/";
      controller.openWebPage(baseUrl + query);
    });
  }
}