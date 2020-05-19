package com.strnisa.rok.slimbox.controller;

import com.strnisa.rok.slimbox.model.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.SortedSet;

public interface Controller {
  void clearPreviousSearch();
  void donate(String item, int amount);
  List<Email> getEmails();
  String getEmailAddress() throws IOException;
  SortedSet<String> getEmailLabels();
  String getFilenameSchema();
  DefaultArtifactVersion getLatestVersion();
  LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings);
  String getSearchQuery();
  GetEmailMetadataTask getSearchTask(String query) throws IOException, InterruptedException;
  String getTargetDirectory();
  void openFile(File file);
  void openQueryLanguagePage();
  void openSlimboxHomepage();
  void openTermsAndConditions();
  void openWebPage(String uriString);
  void setFilenameSchema(String filenameSchema);
  void saveSearchQuery(String query);
  void saveTargetDirectory(String path);
  String signIn() throws IOException, GeneralSecurityException;
  void signOut();
  boolean sendToServer(String contentDescription, String exceptionText, String userText);
  void subscribe(String emailAddress) throws Exception;
}
