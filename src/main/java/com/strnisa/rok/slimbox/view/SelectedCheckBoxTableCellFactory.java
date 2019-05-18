package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.model.Email;
import com.strnisa.rok.slimbox.model.EmailStatus;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import static com.strnisa.rok.slimbox.model.EmailStatus.*;

public class SelectedCheckBoxTableCellFactory implements Callback<TableColumn.CellDataFeatures<Email, CheckBox>, ObservableValue<CheckBox>> {
  @Override
  public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<Email, CheckBox> cellDataFeatures) {
    Email email = cellDataFeatures.getValue();
    CheckBox checkBox = new CheckBox();
    boolean enabled = cellDataFeatures.getTableView().isEditable()
        && email.getStatus() != FAILED && email.getStatus() != PROCESSED;
    checkBox.setDisable(!enabled);
    checkBox.selectedProperty().setValue(email.isSelected());
    checkBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
      EmailStatus targetStatus = newValue ? TO_PROCESS : IGNORED;
      email.setStatus(targetStatus);
      cellDataFeatures.getTableView().refresh();
    });
    cellDataFeatures.getTableView().editableProperty().addListener((observable, oldValue, newValue) ->
        checkBox.setDisable(!newValue));
    return new SimpleObjectProperty<>(checkBox);
  }
}