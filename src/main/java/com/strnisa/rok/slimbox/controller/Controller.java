package com.strnisa.rok.slimbox.controller;

import com.strnisa.rok.slimbox.model.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.SortedSet;

public interface Controller {
  void clearPreviousSearch();
  void donate(String item, int amount);
  Config getConfig();
  List<Email> getEmails();
  String getEmailAddress() throws IOException;
  SortedSet<String> getEmailLabels();
  String getFilenameSchema();
  LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings);
  GetEmailMetadataTask getSearchTask(String query) throws IOException, InterruptedException;
  void openFile(File file);
  void openQueryLanguagePage();
  void openSlimboxHomepage();
  void openTermsAndConditions();
  void setFilenameSchema(String filenameSchema);
  String signIn() throws IOException, GeneralSecurityException;
  void signOut();
  boolean sendToServer(String contentDescription, String exceptionText, String userText);
  void subscribe(String emailAddress) throws Exception;

  void openWebPage(String uriString);
}
