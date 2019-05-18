package com.strnisa.rok.slimbox.model;

import com.strnisa.rok.slimbox.controller.LongTask;

import java.util.*;
import java.util.logging.Logger;

public class MockModel implements Model {
  private static final Logger LOGGER = Logger.getLogger(MockModel.class.getName());

  private String filenameSchema = FilenameFactory.DEFAULT_SCHEMA;
  private ArrayList<Email> emails = new ArrayList<>();

  @Override
  public void clearPreviousSearch() {
    emails = new ArrayList<>();
  }

  @Override
  public GetEmailMetadataTask getSearchTask(String query) {
    int minEmailSizeInMb = 1;
    List<String> emailIds = new ArrayList<>();
    emails = new ArrayList<>();
    int minEmailSizeInBytes = minEmailSizeInMb * (int) Math.pow(2, 20);
    int maxEmailId = 15;
    for (int i = minEmailSizeInBytes / 1000 / 1000; i < maxEmailId; ++i) {
      String emailId = String.valueOf(i);
      emailIds.add(emailId);
    }
    return new GetEmailMetadataTask(emailIds, (startIndexInclusive, endIndexExclusive) -> {
      for (int i = minEmailSizeInBytes / 1000 / 1000; i < maxEmailId; ++i) {
        if (startIndexInclusive <= i && i < endIndexExclusive) {
          String emailId = String.valueOf(i);
          emails.add(new Email(emailId, emailId, Arrays.asList("INBOX", "IMPORTANT"),
              "some@example.com", "Subject " + i, System.currentTimeMillis(),
              i * (int) Math.pow(2, 20)));
        }
      }
    });
  }

  @Override
  public String getEmailAddress() {
    return "user@mock.com";
  }

  @Override
  public List<Email> getEmails() {
    return emails;
  }

  @Override
  public String getFilenameSchema() {
    return filenameSchema;
  }

  @Override
  public SortedSet<String> getEmailLabels() {
    SortedSet<String> emailLabels = new TreeSet<>();
    for (int i = 0; i < 10; ++i) {
      emailLabels.add("Label Name " + i);
    }
    return emailLabels;
  }

  @Override
  public LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings) {
    return new ProcessEmailTask(email, e -> new ProcessEmailResult(Collections.singleton(e.getGmailId())));
  }

  @Override
  public void signIn() {
    LOGGER.info("signIn");
  }

  @Override
  public void signOut() {
    LOGGER.info("signOut");
  }

  @Override
  public void sendToServer(String contentDescription, String userEmail, String stackTraceText, String userText) {
    LOGGER.info("========== sendToServer ==========");
    LOGGER.info(userEmail);
    LOGGER.info(stackTraceText);
    LOGGER.info(userText);
    LOGGER.info("========== sendToServer ==========");
  }

  @Override
  public void setFilenameSchema(String filenameSchema) {
    this.filenameSchema = filenameSchema;
  }

  @Override
  public void subscribe(String emailAddress) {
    LOGGER.info("subscribe: " + emailAddress);
  }
}
