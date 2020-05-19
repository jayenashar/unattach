package com.strnisa.rok.slimbox.model;

import com.strnisa.rok.slimbox.controller.LongTask;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.SortedSet;

public interface Model {
  void clearPreviousSearch();
  GetEmailMetadataTask getSearchTask(String query) throws IOException, InterruptedException;
  String getEmailAddress() throws IOException;
  SortedSet<String> getEmailLabels() throws IOException;
  List<Email> getEmails();
  String getFilenameSchema();
  LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings);
  DefaultArtifactVersion getLatestVersion() throws IOException, InterruptedException;
  String getSearchQuery();
  String getTargetDirectory();
  void saveSearchQuery(String query);
  void saveTargetDirectory(String path);
  void signIn() throws IOException, GeneralSecurityException;
  void signOut() throws IOException;
  void sendToServer(String contentDescription, String userEmail, String stackTraceText, String userText)
      throws IOException, InterruptedException;
  void setFilenameSchema(String filenameSchema);
  void subscribe(String emailAddress) throws IOException, InterruptedException;
}
