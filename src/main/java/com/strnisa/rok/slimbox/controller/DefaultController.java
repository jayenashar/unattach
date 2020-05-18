package com.strnisa.rok.slimbox.controller;

import com.strnisa.rok.slimbox.model.*;
import org.apache.commons.lang3.SystemUtils;

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
  private final Config config;

  DefaultController(Model model) {
    this.model = model;
    this.config = new FileConfig();
  }

  @Override
  public Config getConfig() {
    return config;
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
    try {
      if (SystemUtils.IS_OS_LINUX) {
        // Desktop.getDesktop().browse() only works on Linux with libgnome installed.
        if (Runtime.getRuntime().exec(new String[] {"which", "xdg-open"}).getInputStream().read() != -1) {
          Runtime.getRuntime().exec(new String[] {"xdg-open", file.getAbsolutePath()});
        } else {
          LOGGER.log(Level.SEVERE, "Unable to open a file on this operating system.");
        }
      } else {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
          Desktop.getDesktop().open(file);
        } else {
          LOGGER.log(Level.SEVERE, "Unable to open a file on this operating system.");
        }
      }
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Failed to open a file.", t);
    }
  }

  @Override
  public void openWebPage(String uriString) {
    String manualInstructions = "Please visit " + uriString + " manually.";
    try {
      if (SystemUtils.IS_OS_LINUX) {
        // Desktop.getDesktop().browse() only works on Linux with libgnome installed.
        if (Runtime.getRuntime().exec(new String[] {"which", "xdg-open"}).getInputStream().read() != -1) {
          Runtime.getRuntime().exec(new String[] {"xdg-open", uriString});
        } else {
          LOGGER.log(Level.SEVERE, "Unable to open a web page on this operating system. " + manualInstructions);
        }
      } else {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
          Desktop.getDesktop().browse(URI.create(uriString));
        } else {
          LOGGER.log(Level.SEVERE, "Unable to open a web page on this operating system. " + manualInstructions);
        }
      }
    } catch (Throwable t) {
      LOGGER.info("Unable to open a web page from within the application. " + manualInstructions);
    }
  }
}
