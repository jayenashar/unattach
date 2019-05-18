package com.strnisa.rok.slimbox.view;

import com.strnisa.rok.slimbox.controller.Controller;
import com.strnisa.rok.slimbox.controller.ControllerFactory;
import com.strnisa.rok.slimbox.controller.LongTask;
import com.strnisa.rok.slimbox.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.strnisa.rok.slimbox.model.Constants.BYTES_IN_MEGABYTE;

public class MainViewController {
  private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

  private Controller controller;
  @FXML
  private VBox root;

  // Menu
  @FXML
  private MenuItem emailMenuItem;
  @FXML
  private MenuItem signOutMenuItem;
  @FXML
  private CheckMenuItem addMetadataCheckMenuItem;
  @FXML
  private Menu viewColumnMenu;
  @FXML
  private MenuItem donateTwo;
  @FXML
  private MenuItem donateFive;
  @FXML
  private MenuItem donateTen;
  @FXML
  private MenuItem donateTwenty;
  @FXML
  private MenuItem donateCustom;

  // Search view
  @FXML
  private Tab basicSearchTab;
  @FXML
  private ComboBox<ComboItem<Integer>> emailSizeComboBox;
  @FXML
  private ListView<String> labelsListView;
  @FXML
  private TextField searchQueryTextField;
  @FXML
  private Button searchButton;
  @FXML
  private ProgressBarWithText searchProgressBarWithText;
  @FXML
  private Button stopSearchButton;
  private boolean stopSearchButtonPressed;

  // Results view
  private static final String DESELECT_ALL_CAPTION = "Deselect all";
  private static final String SELECT_ALL_CAPTION = "Select all";
  @FXML
  private TableView<Email> resultsTable;
  @FXML
  private TableColumn<Email, CheckBox> selectedTableColumn;
  @FXML
  private CheckBox toggleAllEmailsCheckBox;
  private boolean toggleAllShouldSelect = true;

  // Download view
  @FXML
  private TextField targetDirectoryTextField;
  @FXML
  private Button browseButton;
  @FXML
  private Button openButton;
  @FXML
  private Button downloadButton;
  @FXML
  private Button downloadAndDeleteButton;
  @FXML
  private Button deleteButton;
  @FXML
  private Button stopProcessingButton;
  @FXML
  private ProgressBarWithText processingProgressBarWithText;
  private long bytesProcessed = 0;
  private long allBytesToProcess = 0;
  private boolean stopProcessingButtonPressed = false;

  @FXML
  private void initialize() throws IOException {
    controller = ControllerFactory.getDefaultController();
    emailMenuItem.setText("Signed in as " + controller.getEmailAddress() + ".");
    addMenuForHidingColumns();
    emailSizeComboBox.setItems(FXCollections.observableList(getEmailSizeOptions()));
    emailSizeComboBox.getSelectionModel().selectFirst();
    searchProgressBarWithText.progressProperty().setValue(0);
    searchProgressBarWithText.textProperty().setValue("(Searching not started yet.)");
    toggleAllEmailsCheckBox.setTooltip(new Tooltip(DESELECT_ALL_CAPTION));
    selectedTableColumn.setComparator((cb1, cb2) -> Boolean.compare(cb1.isSelected(), cb2.isSelected()));
    targetDirectoryTextField.setText(getDefaultTargetDirectory());
    processingProgressBarWithText.progressProperty().setValue(0);
    processingProgressBarWithText.textProperty().setValue("(Processing of emails not started yet.)");
    labelsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    labelsListView.setItems(FXCollections.observableList(new ArrayList<>(controller.getEmailLabels())));
  }

  private void addMenuForHidingColumns() {
    resultsTable.getColumns().forEach(column -> {
      CheckMenuItem menuItem = new CheckMenuItem(column.getText());
      menuItem.setSelected(true);
      menuItem.setOnAction(event -> column.setVisible(menuItem.isSelected()));
      viewColumnMenu.getItems().add(menuItem);
    });
  }

  private Vector<ComboItem<Integer>> getEmailSizeOptions() {
    Vector<ComboItem<Integer>> options = new Vector<>();
    for (int minEmailSizeInMb : new int[] {1, 2, 3, 5, 10, 25, 50, 100}) {
      String caption = String.format("more than %d MB", minEmailSizeInMb);
      options.add(new ComboItem<>(caption, minEmailSizeInMb));
    }
    return options;
  }

  @FXML
  private void onAboutButtonPressed() {
    controller.openSlimboxHomepage();
  }

  @FXML
  private void onFeedbackMenuItemPressed() throws IOException {
    Stage dialog = Scenes.createNewStage("feedback");
    dialog.initOwner(root.getScene().getWindow());
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setScene(Scenes.loadScene("/feedback.view.fxml"));
    Scenes.showAndPreventMakingSmaller(dialog);
  }

  @FXML
  private void onSignOutButtonPressed() throws IOException {
    controller.signOut();
    Scenes.setScene(Scenes.SIGN_IN);
  }

  @FXML
  private void onSearchButtonPressed() {
    disableControls();
    stopSearchButton.setDisable(false);
    stopSearchButtonPressed = false;
    controller.clearPreviousSearch();
    resultsTable.setItems(FXCollections.emptyObservableList());
    AtomicInteger currentBatch = new AtomicInteger();
    AtomicInteger numberOfBatches = new AtomicInteger();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        updateProgress(0, 1);
        updateMessage("Obtaining email metadata ..");
        String query = getQuery();
        LOGGER.info("Obtaining email metadata (query: " + query + ") ..");
        GetEmailMetadataTask longTask = controller.getSearchTask(query);
        currentBatch.set(0);
        numberOfBatches.set(longTask.getNumberOfSteps());
        updateProgress(currentBatch.get(), numberOfBatches.get());
        updateMessage(String.format("Obtaining email metadata (%s) ..", getStatusString()));
        while (!stopSearchButtonPressed && longTask.hasMoreSteps()) {
          GetEmailMetadataTask.Result result = longTask.takeStep();
          currentBatch.set(result.currentBatchNumber);
          updateProgress(currentBatch.get(), numberOfBatches.get());
          updateMessage(String.format("Obtaining email metadata (%s) ..", getStatusString()));
        }
        return null;
      }

      private String getStatusString() {
        if (numberOfBatches.get() == 0) {
          return "no emails matched the query";
        } else {
          return String.format("completed %d of %d batches, %d%%",
              currentBatch.get(), numberOfBatches.get(), 100 * currentBatch.get() / numberOfBatches.get());
        }
      }

      @Override
      protected void succeeded() {
        try {
          updateMessage(String.format("Finished obtaining email metadata (%s).", getStatusString()));
          List<Email> emails = controller.getEmails();
          resultsTable.setItems(FXCollections.observableList(emails));
        } catch (Throwable t) {
          String message = "Failed to process email metadata.";
          updateMessage(message);
          onError(message, t);
        } finally {
          resetControls();
        }
      }

      @Override
      protected void failed() {
        String message = "Failed to obtain email metadata.";
        updateMessage(message);
        onError(message, getException());
        resetControls();
      }
    };

    searchProgressBarWithText.progressProperty().bind(task.progressProperty());
    searchProgressBarWithText.textProperty().bind(task.messageProperty());

    new Thread(task).start();
  }

  private void onError(String message, Throwable t) {
    LOGGER.log(Level.SEVERE, message, t);
    try {
      Stage dialog = Scenes.createNewStage("an error occurred");
      dialog.initOwner(root.getScene().getWindow());
      dialog.initModality(Modality.APPLICATION_MODAL);
      Scene scene = Scenes.loadScene("/error.view.fxml");
      ErrorController errorController = (ErrorController) scene.getUserData();
      errorController.setException(t);
      dialog.setScene(scene);
      Scenes.showAndPreventMakingSmaller(dialog);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to open the error dialog.", e);
    }
  }

  @FXML
  private void onStopSearchButtonPressed() {
    stopSearchButtonPressed = true;
  }

  @FXML
  private void onToggleAllEmailsButtonPressed() {
    EmailStatus targetStatus = toggleAllShouldSelect ? EmailStatus.TO_PROCESS : EmailStatus.IGNORED;
    resultsTable.getItems().forEach(email -> {
      if (email.getStatus() == EmailStatus.IGNORED || email.getStatus() == EmailStatus.TO_PROCESS) {
        email.setStatus(targetStatus);
      }
    });
    resultsTable.refresh();
    toggleAllShouldSelect = !toggleAllShouldSelect;
    toggleAllEmailsCheckBox.setTooltip(new Tooltip(toggleAllShouldSelect ? SELECT_ALL_CAPTION : DESELECT_ALL_CAPTION));
  }

  private String getQuery() {
    StringBuilder query = new StringBuilder();
    if (basicSearchTab.isSelected()) {
      int minEmailSizeInMb = emailSizeComboBox.getSelectionModel().getSelectedItem().value;
      query.append(String.format("has:attachment size:%dm", minEmailSizeInMb));
      ObservableList<String> emailLabels = labelsListView.getSelectionModel().getSelectedItems();
      for (String emailLabel : emailLabels) {
        query.append(String.format(" label:\"%s\"", emailLabel));
      }
    } else {
      query = new StringBuilder(searchQueryTextField.getText());
    }
    return query.toString();
  }

  @FXML
  private void onBrowseButtonPressed() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Set directory for storing attachments");
    File initialDirectory = new File(targetDirectoryTextField.getText());
    if (initialDirectory.exists()) {
      directoryChooser.setInitialDirectory(initialDirectory);
    }
    File newTargetDirectory = directoryChooser.showDialog(targetDirectoryTextField.getScene().getWindow());
    if (newTargetDirectory != null) {
      targetDirectoryTextField.setText(newTargetDirectory.getAbsolutePath());
    }
  }

  @FXML
  private void onOpenButtonPressed() {
    Desktop desktop = Desktop.getDesktop();
    try {
      File targetDirectory = getTargetDirectory();
      if (targetDirectory.mkdirs()) {
        LOGGER.info("Created directory \"" + targetDirectory.getAbsolutePath() + "\".");
      }
      desktop.open(targetDirectory);
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Failed to open directory.", t);
    }
  }

  private File getTargetDirectory() {
    return new File(targetDirectoryTextField.getText());
  }

  private String getDefaultTargetDirectory() {
    String userHome = System.getProperty("user.home");
    Path defaultPath = Paths.get(userHome, "Downloads", Constants.PRODUCT_NAME);
    return defaultPath.toString();
  }

  @FXML
  private void onDownloadButtonPressed() {
    processEmails(ProcessOption.DOWNLOAD);
  }

  @FXML
  private void onDownloadAndDeleteButtonPressed() {
    processEmails(ProcessOption.DOWNLOAD_AND_REMOVE);
  }

  @FXML
  private void onDeleteButtonPressed() {
    processEmails(ProcessOption.REMOVE);
  }

  private void processEmails(ProcessOption processOption) {
    List<Email> emailsToProcess = getEmailsToProcess();
    if (emailsToProcess.isEmpty()) {
      showNoEmailsAlert();
      return;
    }
    disableControls();
    stopProcessingButton.setDisable(false);
    stopProcessingButtonPressed = false;
    File targetDirectory = getTargetDirectory();
    bytesProcessed = 0;
    allBytesToProcess = emailsToProcess.stream().mapToLong(email -> (long) email.getSizeInBytes()).sum();
    processingProgressBarWithText.progressProperty().setValue(0);
    String filenameSchema = controller.getFilenameSchema();
    ProcessSettings processSettings = new ProcessSettings(processOption, targetDirectory, filenameSchema,
        addMetadataCheckMenuItem.isSelected());
    processEmail(emailsToProcess, 0, processSettings);
  }

  private void showNoEmailsAlert() {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("No emails selected");
    alert.setHeaderText(null);
    alert.setContentText("Please select some or all emails in the search results. You can de/select an individual " +
        "email by clicking on the checkbox in its selected row. Alternatively, you can de/select all emails by " +
        "clicking on the checkbox in the table header.");
    alert.showAndWait();
  }

  private void processEmail(List<Email> emailsToProcess, int processedEmails, ProcessSettings processSettings) {
    if (stopProcessingButtonPressed || processedEmails >= emailsToProcess.size()) {
      processingProgressBarWithText.textProperty().setValue(
          String.format("Processing stopped (%s).", getProcessingStatusString(emailsToProcess, processedEmails)));
      resetControls();
      return;
    }
    Email email = emailsToProcess.get(processedEmails);
    processingProgressBarWithText.textProperty().setValue(
        String.format("Processing selected emails (%s) ..", getProcessingStatusString(emailsToProcess, processedEmails)));

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        LongTask<ProcessEmailResult> longTask = controller.getProcessTask(email, processSettings);
        while (!stopProcessingButtonPressed && longTask.hasMoreSteps()) {
          longTask.takeStep();
        }
        return null;
      }

      @Override
      protected void succeeded() {
        bytesProcessed += email.getSizeInBytes();
        processingProgressBarWithText.progressProperty().setValue(1.0 * bytesProcessed / allBytesToProcess);
        resultsTable.refresh();
        processEmail(emailsToProcess, processedEmails + 1, processSettings);
      }

      @Override
      protected void failed() {
        email.setStatus(EmailStatus.FAILED);
        resultsTable.refresh();
        String message = "Failed to process selected emails.";
        updateMessage(message);
        onError(message, getException());
        resetControls();
      }
    };

    new Thread(task).start();
  }

  private String getProcessingStatusString(List<Email> emailsToProcess, int processedEmails) {
    return String.format("completed %d of %d, %dMB / %dMB, %d%% by size",
        processedEmails, emailsToProcess.size(), toMegaBytes(bytesProcessed), toMegaBytes(allBytesToProcess),
        100 * bytesProcessed / allBytesToProcess);
  }

  private static int toMegaBytes(long bytes) {
    return (int) (bytes / BYTES_IN_MEGABYTE);
  }

  @FXML
  private void onStopProcessingButtonPressed() {
    stopProcessingButton.setDisable(true);
    stopProcessingButtonPressed = true;
  }

  private List<Email> getEmailsToProcess() {
    return resultsTable.getItems().stream()
        .filter(email -> email.getStatus() == EmailStatus.TO_PROCESS).collect(Collectors.toList());
  }

  private void disableControls() {
    signOutMenuItem.setDisable(true);
    searchButton.setDisable(true);
    stopSearchButton.setDisable(true);
    resultsTable.setEditable(false);
    toggleAllEmailsCheckBox.setDisable(true);
    targetDirectoryTextField.setDisable(true);
    browseButton.setDisable(true);
    downloadButton.setDisable(true);
    downloadAndDeleteButton.setDisable(true);
    deleteButton.setDisable(true);
    stopProcessingButton.setDisable(true);
  }

  private void resetControls() {
    signOutMenuItem.setDisable(false);
    searchButton.setDisable(false);
    stopSearchButton.setDisable(true);
    resultsTable.setEditable(true);
    toggleAllEmailsCheckBox.setDisable(false);
    targetDirectoryTextField.setDisable(false);
    browseButton.setDisable(false);
    downloadButton.setDisable(false);
    downloadAndDeleteButton.setDisable(false);
    deleteButton.setDisable(false);
    stopProcessingButton.setDisable(true);
  }

  @FXML
  private void onDonateMenuItemPressed(ActionEvent event) throws IOException {
    Object source = event.getSource();
    if (source == donateTwo) {
      controller.donate("Espresso", 2);
    } else if (source == donateFive) {
      controller.donate("Cappuccino", 3);
    } else if (source == donateTen) {
      controller.donate("Caramel Machiato", 5);
    } else if (source == donateTwenty) {
      controller.donate("Bag of Coffee", 10);
    } else if (source == donateCustom) {
      Stage dialog = Scenes.createNewStage("donate");
      dialog.initOwner(root.getScene().getWindow());
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setScene(Scenes.loadScene("/coffee.view.fxml"));
      dialog.resizableProperty().setValue(false);
      Scenes.showAndPreventMakingSmaller(dialog);
    }
  }

  @FXML
  private void onOpenLogMenuItemPressed() {
    try {
      String home = System.getProperty("user.home");
      File homeFile = new File(home);
      File[] logFiles = homeFile.listFiles(file -> {
        String name = file.getName();
        return name.startsWith(".slimbox-") && name.endsWith(".log");
      });
      long latestTimestamp = 0;
      File latest = null;
      if (logFiles == null) {
        return;
      }
      for (File logFile : logFiles) {
        if (logFile.lastModified() > latestTimestamp) {
          latestTimestamp = logFile.lastModified();
          latest = logFile;
        }
      }
      controller.openFile(latest);
    } catch (Throwable t) {
      onError("Couldn't open the log file.", t);
    }
  }

  @FXML
  private void onQueryButtonPressed() {
    controller.openQueryLanguagePage();
  }

  @FXML
  private void onFilenameSchemaMenuItemPressed() {
    try {
      Stage dialog = Scenes.createNewStage("file name scheme");
      dialog.initOwner(root.getScene().getWindow());
      dialog.initModality(Modality.APPLICATION_MODAL);
      Scene scene = Scenes.loadScene("/filename-schema.view.fxml");
      FilenameSchemaController filenameSchemaController = (FilenameSchemaController) scene.getUserData();
      filenameSchemaController.setSchema(controller.getFilenameSchema());
      dialog.setScene(scene);
      Scenes.showAndPreventMakingSmaller(dialog);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to open the error dialog.", e);
    }
  }
}
