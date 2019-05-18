package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.model.Email;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LinkButtonTableCellFactory implements Callback<TableColumn.CellDataFeatures<Email, Button>, ObservableValue<Button>> {
  private static final Logger LOGGER = Logger.getLogger(LinkButtonTableCellFactory.class.getName());

  private final Desktop desktop;
  private final boolean browsingSupported;

  public LinkButtonTableCellFactory() {
    desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    browsingSupported = desktop != null && desktop.isSupported(Desktop.Action.BROWSE);
  }

  @Override
  public ObservableValue<Button> call(TableColumn.CellDataFeatures<Email, Button> cellDataFeatures) {
    Email email = cellDataFeatures.getValue();
    Button button = new Button();
    button.setText("Open");
    if (browsingSupported) {
      addOnActionHandler(email, button);
    } else {
      button.setTooltip(new Tooltip("Opening a browser is unsupported in the current environment."));
    }
    return new SimpleObjectProperty<>(button);
  }

  private void addOnActionHandler(Email email, Button button) {
    button.setOnAction(event -> {
      URI uri = null;
      try {
        String query = URLEncoder.encode("rfc822msgid:" + email.getUniqueId(), UTF_8);
        String baseUrl = "https://mail.google.com/mail/u/0/#search/";
        uri = URI.create(baseUrl + query);
        desktop.browse(uri);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to open URI: " + uri, e);
      }
    });
  }
}