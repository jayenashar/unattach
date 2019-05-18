package com.strnisa.rok.slimbox.model;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;

class HttpClient {
  static void sendToServer(String contentDescription, String userEmail, String stackTrace, String userText)
      throws IOException, InterruptedException {
    JSONObject payload = new JSONObject()
        .put("version", Constants.VERSION)
        .put("environment", System.getProperty("os.name"))
        .put("contentDescription", contentDescription)
        .put("userEmail", userEmail);
    if (stackTrace != null) {
      payload.put("stackTrace", stackTrace);
    }
    if (userText != null) {
      payload.put("userText", userText);
    }
    post("feedback", payload);
  }

  static void subscribe(String emailAddress) throws IOException, InterruptedException {
    JSONObject payload = new JSONObject()
        .put("emailAddress", emailAddress);
    post("subscribe", payload);
  }

  private static void post(String path, JSONObject payload) throws IOException, InterruptedException {
    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://rok.strnisa.com/slimbox/s/" + path))
        .timeout(Duration.ofMinutes(1))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), Charset.forName("UTF-8")))
        .build();
    client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
