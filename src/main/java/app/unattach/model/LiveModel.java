package app.unattach.model;

import app.unattach.controller.LongTask;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.codec.binary.Base64.*;

public class LiveModel implements Model {
  private static final Logger LOGGER = Logger.getLogger(LiveModel.class.getName());
  private static final String USER = "me";

  private final Config config;
  private GmailServiceLifecycleManager serviceLifecycleManager;
  private Gmail service;
  private List<Email> emails;
  private String emailAddress;

  public LiveModel() {
    this.config = new FileConfig();
    configureMimeLibrary();
    reset();
  }

  private void configureMimeLibrary() {
    // see http://docs.oracle.com/javaee/6/api/javax/mail/internet/package-summary.html
    allowEmptyPartsInEmails();
    allowNonConformingEmailHeaders();
    // see https://stackoverflow.com/a/5292975/974531
    disablePrivateFetch();
    // see https://community.oracle.com/thread/1590013?start=0&tstart=0
    enableIgnoreErrors();
  }

  private void allowEmptyPartsInEmails() {
    System.setProperty("mail.mime.multipart.allowempty", "true");
  }

  private void allowNonConformingEmailHeaders() {
    System.setProperty("mail.mime.parameters.strict", "false");
  }

  private void disablePrivateFetch() {
    System.setProperty("mail.imaps.partialfetch", "false");
  }

  private void enableIgnoreErrors() {
    System.setProperty("mail.mime.base64.ignoreerrors", "true");
  }

  private void reset() {
    serviceLifecycleManager = null;
    service = null;
    emailAddress = null;
    clearPreviousSearch();
  }

  @Override
  public void clearPreviousSearch() {
    emails = new ArrayList<>();
  }

  @Override
  public DefaultArtifactVersion getLatestVersion() throws IOException, InterruptedException {
    return HttpClient.getLatestVersion();
  }

  @Override
  public String getSearchQuery() {
    return config.getSearchQuery();
  }

  @Override
  public String getTargetDirectory() {
    return config.getTargetDirectory();
  }

  @Override
  public void saveSearchQuery(String query) {
    config.saveSearchQuery(query);
  }

  @Override
  public void saveTargetDirectory(String path) {
    config.saveTargetDirectory(path);
  }

  @Override
  public void signIn() throws IOException, GeneralSecurityException {
    configureService();
    try {
      // Test call to the service. This can fail due to token issues.
      getEmailAddress();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Initial signing in failed. Explicitly signing out and retrying..", e);
      signOut();
      configureService();
    }
  }

  private void configureService() throws GeneralSecurityException, IOException {
    serviceLifecycleManager = new GmailServiceLifecycleManager();
    // 250 quota units / user / second
    // each set of requests should assume they start with clean quota
    service = serviceLifecycleManager.signIn();
  }

  @Override
  public void signOut() throws IOException {
    serviceLifecycleManager.signOut();
    reset();
  }

  @Override
  public void sendToServer(String contentDescription, String userEmail, String stackTraceText, String userText)
      throws IOException, InterruptedException {
    HttpClient.sendToServer(contentDescription, userEmail, stackTraceText, userText);
  }

  @Override
  public void setFilenameSchema(String filenameSchema) {
    config.saveFilenameSchema(filenameSchema);
  }

  @Override
  public void subscribe(String emailAddress) throws IOException, InterruptedException {
    HttpClient.subscribe(emailAddress);
  }

  @Override
  public String getEmailAddress() throws IOException {
    if (emailAddress == null) {
      // unknown quota units
      Profile profile = service.users().getProfile(USER).setFields("emailAddress").execute();
      emailAddress = profile.getEmailAddress();
    }
    return emailAddress;
  }

  @Override
  public List<Email> getEmails() {
    return emails;
  }

  @Override
  public String getFilenameSchema() {
    return config.getFilenameSchema();
  }

  @Override
  public LongTask<ProcessEmailResult> getProcessTask(Email email, ProcessSettings processSettings) {
    return new ProcessEmailTask(email, e -> processEmail(e, processSettings) /* 40 quota units */);
  }

  private ProcessEmailResult processEmail(Email email, ProcessSettings processSettings)
      throws IOException, MessagingException {
    Message message = getRawMessage(email.getGmailId()); // 5 quota units
    MimeMessage mimeMessage = getMimeMessage(message);
    Set<String> fileNames = EmailProcessor.process(email, mimeMessage, processSettings);
    if (processSettings.processOption.shouldRemove() && !fileNames.isEmpty()) {
      updateRawMessage(message, mimeMessage);
      insertSlimMessage(message); // 25 quota units
      removeOriginalMessage(email); // 10 quota units
    }
    return new ProcessEmailResult(fileNames);
  }

  private Message getRawMessage(String emailId) throws IOException {
    // 1 messages.get == 5 quota units
    // download limit = 2500 MB / day / user
    return service.users().messages().get(USER, emailId).setFormat("raw").execute();
  }

  private MimeMessage getMimeMessage(Message message) throws MessagingException {
    String rawBefore = message.getRaw();
    byte[] emailBytes = decodeBase64(rawBefore);
    Properties props = new Properties();
    Session session = Session.getInstance(props);
    return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
  }

  private void updateRawMessage(Message message, MimeMessage mimeMessage) throws IOException, MessagingException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    mimeMessage.writeTo(buffer);
    String raw = encodeBase64URLSafeString(buffer.toByteArray());
    message.setRaw(raw);
  }

  private void insertSlimMessage(Message message) throws IOException {
    // 1 messages.insert == 25 quota units
    // upload limit = 500 MB / day / user
    service.users().messages().insert(USER, message).setInternalDateSource("dateHeader").execute();
  }

  private void removeOriginalMessage(Email email) throws IOException {
    // 1 messages.delete == 10 quota units
    service.users().messages().delete(USER, email.getGmailId()).execute();
  }

  @Override
  public GetEmailMetadataTask getSearchTask(String query) throws IOException, InterruptedException {
    List<String> emailIdsToProcess = getEmailIds(query).stream().map(Message::getId).collect(Collectors.toList());

    JsonBatchCallback<Message> perEmailCallback = new JsonBatchCallback<>() {
      @Override
      public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
        throw new IOException(googleJsonError.getMessage());
      }

      @Override
      public void onSuccess(Message message, HttpHeaders httpHeaders) {
        Map<String, String> headerMap = getHeaderMap(message);
        String emailId = message.getId();
        String uniqueId = headerMap.get("message-id");
        List<String> labelIds = message.getLabelIds();
        String from = headerMap.get("from");
        String subject = headerMap.get("subject");
        long timestamp = message.getInternalDate();
        Email email = new Email(emailId, uniqueId, labelIds, from, subject, timestamp, message.getSizeEstimate());
        emails.add(email);
      }
    };

    return new GetEmailMetadataTask(
        emailIdsToProcess,
        (startIndexInclusive, endIndexExclusive) -> {
          BatchRequest batch = service.batch();
          for (int emailIndex = startIndexInclusive; emailIndex < endIndexExclusive; ++emailIndex) {
            getEmailMetadata(service, emailIdsToProcess.get(emailIndex), batch, perEmailCallback);
          }
          batch.execute();
        }
    );
  }

  private List<Message> getEmailIds(String query) throws IOException, InterruptedException {
    List<Message> messages = new ArrayList<>();
    String pageToken = null;
    do {
      // 1 messages.list == 5 quota units
      Gmail.Users.Messages.List request = service.users().messages().list(USER).setFields("messages/id").setQ(query)
          .setMaxResults(500L).setPageToken(pageToken);
      ListMessagesResponse response = request.execute();
      if (response == null) {
        break;
      }
      List<Message> responseMessages = response.getMessages();
      if (responseMessages == null) {
        break;
      }
      messages.addAll(responseMessages);
      pageToken = response.getNextPageToken();
      Thread.sleep(25);
    } while (pageToken != null);
    return messages;
  }

  @Override
  public SortedSet<String> getEmailLabels() throws IOException {
    // 1 labels.get == 1 quota unit
    ListLabelsResponse response = service.users().labels().list(USER).setFields("labels/id,labels/name").execute();
    SortedSet<String> emailLabels = new TreeSet<>();
    for (Label label : response.getLabels()) {
      emailLabels.add(label.getName());
    }
    return emailLabels;
  }

  private static void getEmailMetadata(Gmail service, String messageId, BatchRequest batch,
                                       JsonBatchCallback<Message> callback) throws IOException {
    // 1 messages.get == 5 quota units
    service.users().messages().get(LiveModel.USER, messageId).setFields("id,labelIds,internalDate,payload/headers,sizeEstimate")
        .queue(batch, callback);
  }

  private static Map<String, String> getHeaderMap(Message message) {
    List<MessagePartHeader> headers = message.getPayload().getHeaders();
    Map<String, String> headerMap = new HashMap<>(headers.size());
    for (MessagePartHeader header : headers) {
      headerMap.put(header.getName().toLowerCase(), header.getValue());
    }
    return headerMap;
  }
}
