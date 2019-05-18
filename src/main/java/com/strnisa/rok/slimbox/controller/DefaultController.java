package com.strnisa.rok.slimbox.controller;

import com.strnisa.rok.slimbox.model.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultController implements Controller {
  private static final Logger LOGGER = Logger.getLogger(DefaultController.class.getName());

  private final Model model;

  DefaultController(Model model) {
    this.model = model;
  }

  @Override
  public void clearPreviousSearch() {
    model.clearPreviousSearch();
  }

  @Override
  public GetEmailMetadataTask getSearchTask(String query) throws IOException, InterruptedException {
    return model.getSearchTask(query);
  }

  @Override
  public void donate(String item, int amount) {
    String uriString = Constants.DONATE_URL;
    uriString += "&coffee_type=" + item.replace(" ", "%20") + "&coffee_price=" + amount;
    openWebPage(uriString);
  }

  @Override
  public LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings) {
    return model.getProcessTask(email, processSettings);
  }

  @Override
  public List<Email> getEmails() {
    return model.getEmails();
  }

  @Override
  public void openSlimboxHomepage() {
    openWebPage(Constants.HOMEPAGE);
  }

  @Override
  public void openTermsAndConditions() {
    openWebPage(Constants.TERMS_AND_CONDITIONS_URL);
  }

  @Override
  public void setFilenameSchema(String filenameSchema) {
    model.setFilenameSchema(filenameSchema);
  }

  @Override
  public void openQueryLanguagePage() {
    openWebPage("https://support.google.com/mail/answer/7190");
  }

  @Override
  public String signIn() throws IOException, GeneralSecurityException {
      model.signIn();
      return model.getEmailAddress();
  }

  @Override
  public String getEmailAddress() throws IOException {
    return model.getEmailAddress();
  }

  @Override
  public SortedSet<String> getEmailLabels() {
    try {
      LOGGER.info("Getting email labels..");
      SortedSet<String> emailLabels = model.getEmailLabels();
      LOGGER.info("Getting email labels.. successful.");
      return emailLabels;
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Getting email labels.. failed.", t);
      return null;
    }
  }

  @Override
  public String getFilenameSchema() {
    return model.getFilenameSchema();
  }

  @Override
  public void signOut() {
    try {
      LOGGER.info("Signing out..");
      model.signOut();
      LOGGER.info("Signing out.. successful.");
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Signing out.. failed.", t);
    }
  }

  @Override
  public boolean sendToServer(String contentDescription, String stackTraceText, String userText) {
    try {
      LOGGER.info("Sending " + contentDescription + " ..");
      String userEmail = model.getEmailAddress();
      model.sendToServer(contentDescription, userEmail, stackTraceText, userText);
      LOGGER.info("Sending " + contentDescription + " .. successful. Thanks!");
      return true;
    } catch (Throwable t) {
      String logMessage = "Failed to send " + contentDescription + " to the server. " +
          "Please consider sending an email to " + Constants.CONTACT_EMAIL + " instead.";
      LOGGER.log(Level.SEVERE, logMessage, t);
      return false;
    }
  }

  @Override
  public void subscribe(String emailAddress) {
    try {
      LOGGER.info("Subscribing with " + emailAddress + " ..");
      model.subscribe(emailAddress);
      LOGGER.info("Subscription successful.");
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Failed to subscribe.", t);
    }
  }

  @Override
  public void openFile(File file) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
      LOGGER.info("Unable to open a file, since Desktop not supported.");
    } else {
      try {
        desktop.open(file);
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE, "Unable to open file.", t);
      }
    }
  }

  private void openWebPage(String uriString) {
    String manualInstructions = "Please visit " + uriString + " manually.";
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
      LOGGER.info("Unable to open a web page from within the application. " + manualInstructions);
    } else {
      try {
        desktop.browse(URI.create(uriString));
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE, "Failed to open a web page. " + manualInstructions, t);
      }
    }
  }
}
