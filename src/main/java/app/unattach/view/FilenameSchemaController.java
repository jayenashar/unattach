package app.unattach.view;

import app.unattach.controller.Controller;
import app.unattach.controller.ControllerFactory;
import app.unattach.model.Email;
import app.unattach.model.FilenameFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.Arrays;

public class FilenameSchemaController {
  private static final Email email = new Email("17b4aed892cc3f0b", "17b4aed892cc3f0b",
          Arrays.asList("IMPORTANT", "SENT"), "\"John Doe\" <john.doe@example.com>", "Re: Holiday plans",
          1501545600000L, 1234567);
  private Controller controller;

  @FXML
  private Text introductionText;
  @FXML
  private TextField filenameSchemaTextField;
  @FXML
  private Label filenameExampleLabel;
  @FXML
  private Label errorLabel;
  @FXML
  private Button cancelButton;
  @FXML
  private Button okButton;

  @FXML
  public void initialize() {
    controller = ControllerFactory.getDefaultController();
    introductionText.setText(
        "You can here configure the file name schema for the downloaded attachments.\n\n" +
            "For example, to configure that every downloaded attachment should first contain Google's email\n" +
            "ID, followed by the index of the attachment within the email, followed by a normalized name of\n" +
            "the attachment file itself, with dashes as separators, you could write the following schema below:\n\n" +
            "    ${ID}-${BODY_PART_INDEX}-${ATTACHMENT_NAME}\n\n" +
            "The available schema variables are:\n" +
            "- FROM_EMAIL, e.g. " + getSchemaVariableExample("FROM_EMAIL") + "\n" +
            "- FROM_NAME, e.g. " + getSchemaVariableExample("FROM_NAME") + "\n" +
            "- SUBJECT, e.g. " + getSchemaVariableExample("SUBJECT") + "\n" +
            "- TIMESTAMP, e.g. " + getSchemaVariableExample("TIMESTAMP") + "\n" +
            "- DATE, e.g. " + getSchemaVariableExample("DATE") + "\n" +
            "- ID, e.g. " + getSchemaVariableExample("ID") + "\n" +
            "- BODY_PART_INDEX, e.g. " + getSchemaVariableExample("BODY_PART_INDEX") + "\n" +
            "- ATTACHMENT_NAME, e.g. " + getSchemaVariableExample("ATTACHMENT_NAME") + "\n" +
            "- LABELS, e.g. " + getSchemaVariableExample("LABELS") + "\n\n" +
            "There are also RAW_ (e.g. RAW_SUBJECT) variants of the above variables, but they are not recommended,\n" +
            "since they may contain symbols not suitable for file names.\n\n" +
            "Additionally, you can set the maximum length for each variable's expansion. For example, to use\n" +
            "up to 5 characters of the SUBJECT, use ${SUBJECT:5}. For attachment name variables, the extension\n" +
            "of the file is preserved."
    );
    filenameSchemaTextField.textProperty().addListener(observable -> {
      String schema = filenameSchemaTextField.getText();
      FilenameFactory filenameFactory = new FilenameFactory(schema);
      try {
        String filename = filenameFactory.getFilename(email, 3, "the beach.jpg");
        filenameExampleLabel.setText(filename);
        errorLabel.setText("");
        okButton.setDisable(false);
      } catch (Throwable t) {
        filenameExampleLabel.setText("");
        errorLabel.setText(t.getMessage());
        okButton.setDisable(true);
      }
    });
  }

  void setSchema(String schema) {
    filenameSchemaTextField.setText(schema);
  }

  @FXML
  private void onCancelButtonPressed() {
    cancelButton.getScene().getWindow().hide();
  }

  @FXML
  private void onOkButtonPressed() {
    controller.setFilenameSchema(filenameSchemaTextField.getText());
    okButton.getScene().getWindow().hide();
  }

  private String getSchemaVariableExample(String variable) {
    FilenameFactory filenameFactory = new FilenameFactory("${" + variable + "}");
    return filenameFactory.getFilename(email, 3, "the beach.jpg");
  }
}
