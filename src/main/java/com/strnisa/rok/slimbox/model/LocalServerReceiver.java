package com.strnisa.rok.slimbox.model;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class LocalServerReceiver implements VerificationCodeReceiver {
  private static final String CALLBACK_PATH = "/Callback";
  private final Lock lock;
  private final Condition gotAuthorizationResponse;
  private final String host;
  private int port;
  private Server server;
  private String code;
  private String error;

  LocalServerReceiver() {
    this("localhost", -1);
  }

  private LocalServerReceiver(String host, int port) {
    lock = new ReentrantLock();
    gotAuthorizationResponse = lock.newCondition();
    this.host = host;
    this.port = port;
  }

  public String getRedirectUri() throws IOException {
    if (port == -1) {
      port = getUnusedPort();
    }
    server = new Server(port);

    for (Connector connector : server.getConnectors()) {
      connector.setHost(host);
    }

    server.addHandler(new CallbackHandler());

    try {
      server.start();
    } catch (Exception e) {
      Throwables.propagateIfPossible(e);
      throw new IOException(e);
    }

    return "http://" + host + ":" + port + CALLBACK_PATH;
  }

  public String waitForCode() throws IOException {
    lock.lock();

    String result;
    try {
      while (code == null && error == null) {
        gotAuthorizationResponse.awaitUninterruptibly();
      }

      if (error != null) {
        throw new IOException("User authorization failed (" + error + ").");
      }

      result = code;
    } finally {
      lock.unlock();
    }

    return result;
  }

  public void stop() throws IOException {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        Throwables.propagateIfPossible(e);
        throw new IOException(e);
      }

      server = null;
    }
  }

  private static int getUnusedPort() throws IOException {
    try (Socket socket = new Socket()) {
      socket.bind(null);
      return socket.getLocalPort();
    }
  }

  class CallbackHandler extends AbstractHandler {
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException {
      if (CALLBACK_PATH.equals(target)) {
        writeLandingHtml(request, response);
        response.flushBuffer();
        ((Request) request).setHandled(true);
        LocalServerReceiver.this.lock.lock();

        try {
          LocalServerReceiver.this.error = request.getParameter("error");
          LocalServerReceiver.this.code = request.getParameter("code");
          LocalServerReceiver.this.gotAuthorizationResponse.signal();
        } finally {
          LocalServerReceiver.this.lock.unlock();
        }
      }
    }

    private void writeLandingHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
      boolean success = !request.getQueryString().contains("error=access_denied");
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");
      InputStream in = getClass().getResourceAsStream(success ? "/success.html" : "/failure.html");
      String html = IOUtils.toString(in, "UTF-8");
      PrintWriter doc = response.getWriter();
      doc.print(html);
      doc.flush();
    }
  }
}
