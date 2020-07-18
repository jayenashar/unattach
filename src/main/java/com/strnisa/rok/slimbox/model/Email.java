package com.strnisa.rok.slimbox.model;

import javafx.fxml.FXML;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class Email {
  private final String gmailId;
  private final String uniqueId;
  private final SortedSet<String> labelIds;
  private final String from;
  private final String subject;
  private final long timestamp;
  private final Date date;
  private final int sizeInBytes;
  private EmailStatus status;

  public Email(String gmailId, String uniqueId, List<String> labelIds, String from, String subject, long timestamp,
               int sizeInBytes) {
    this.gmailId = gmailId;
    this.uniqueId = uniqueId;
    this.labelIds = Collections.unmodifiableSortedSet(labelIds == null ? Collections.emptySortedSet() : new TreeSet<>(labelIds));
    this.from = from;
    this.subject = subject;
    this.timestamp = timestamp;
    this.date = new Date(timestamp);
    this.sizeInBytes = sizeInBytes;
    status = EmailStatus.IGNORED;
  }

  @FXML
  public String getGmailId() {
    return gmailId;
  }

  @FXML
  public String getUniqueId() {
    return uniqueId;
  }

  @FXML
  public String getLabelIdsString() {
    return StringUtils.join(labelIds, "_");
  }

  @FXML
  public Date getDate() {
    return date;
  }

  String getDateIso8601() {
    return new SimpleDateFormat("yyyy-MM-dd").format(getDate());
  }

  @FXML
  public String getFrom() {
    return from;
  }

  String getFromEmail() {
    if (from == null) {
      return "";
    } else if (from.endsWith(">")) {
      return from.substring(from.lastIndexOf('<') + 1, from.length() - 1);
    } else {
      return from;
    }
  }

  String getFromName() {
    if (from.endsWith(">")) {
      return StringUtils.strip(from.substring(0, from.lastIndexOf('<')), " \"");
    } else {
      return "_";
    }
  }

  @FXML
  public String getSubject() {
    return subject;
  }

  public int getSizeInBytes() {
    return sizeInBytes;
  }

  @FXML
  public int getSizeInMegaBytes() {
    return sizeInBytes / Constants.BYTES_IN_MEGABYTE;
  }

  @FXML
  public boolean isSelected() {
    return status == EmailStatus.TO_PROCESS;
  }

  @FXML
  public EmailStatus getStatus() {
    return status;
  }

  @FXML
  public void setStatus(EmailStatus status) {
    this.status = status;
  }

  long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "Email{" +
        "gmailId='" + gmailId + '\'' +
        ", uniqueId='" + uniqueId + '\'' +
        ", labelIds=" + labelIds +
        ", from='" + from + '\'' +
        ", subject='" + subject + '\'' +
        ", timestamp=" + timestamp +
        ", sizeInBytes=" + sizeInBytes +
        '}';
  }
}
