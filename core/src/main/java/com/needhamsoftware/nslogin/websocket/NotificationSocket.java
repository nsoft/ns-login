/*
 *    Copyright (c) 2019, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.needhamsoftware.nslogin.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.Notification;
import com.needhamsoftware.nslogin.model.NotificationType;
import com.needhamsoftware.nslogin.service.MessageService;
import com.needhamsoftware.nslogin.servlet.SessionTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
@ServerEndpoint(value = "/socket/notifications", configurator = NotificationSocket.GetHttpSessionConfigurator.class)
public class NotificationSocket implements MessageService {
  public static final NotificationType ERROR = NotificationType.ERROR;
  public static final NotificationType WARNING = NotificationType.WARNING;
  public static final NotificationType INFO = NotificationType.INFO;
  public static final NotificationType SUCCESS = NotificationType.SUCCESS;
  private static Logger log = LogManager.getLogger();

  static {
    log.debug("loaded NotificationSocket class");
  }
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static final Map<Serializable, NotificationSocket> ACTIVE_ENDPOINTS = new ConcurrentHashMap<>();
  public static final Map<Long, NotificationSocket> ENDPOINTS_BY_USER_ID = new ConcurrentHashMap<>();

  private Session wsSession;
  private HttpSession httpSession;
  private final AtomicInteger errorCount = new AtomicInteger();

  // to support the login form and password reset forms we have to still keep track of thread local
  // list of error messages...
  private ThreadLocal<List<Notification>> messages = ThreadLocal.withInitial(ArrayList::new);


  @OnOpen
  public void init(Session session, EndpointConfig config) {
    log.debug("Initializing {}", this.getClass());
    this.wsSession = session;
    String sessionId = session.getRequestParameterMap().get("sessionId").get(0);
    SessionTracker tracker =
        (SessionTracker) config.getUserProperties().get(SessionTracker.class.getName());
    httpSession = tracker.getSessionById(sessionId);
    ACTIVE_ENDPOINTS.put(httpSession.getId(), this);
    AppUser user = (AppUser) httpSession.getAttribute("com.needhamsoftware.nslogin.SITE_USER");
    if (user != null) {
      ENDPOINTS_BY_USER_ID.put(user.getId(), this);
    }
  }

  @OnClose
  public void destroy() {
    log.debug("Destroying {}", this.getClass());
    AppUser user = (AppUser) httpSession.getAttribute("com.needhamsoftware.nslogin.SITE_USER");
    if (user != null) {
      ENDPOINTS_BY_USER_ID.remove(user.getId());
      ACTIVE_ENDPOINTS.remove(httpSession.getId(), this);
    }
  }

  @SuppressWarnings("UnusedParameters")
  @OnMessage
  public String onMessage(String message) {
    log.debug("Message Received: {}", this.getClass());
    Notification notification = new Notification();
    notification.setDisplayMs(5000);
    notification.setMessage("This is a push only service. Your message has been ignored.");
    notification.setSent(Instant.now());
    notification.setNotificationType(NotificationType.WARNING);
    try {
      return MAPPER.writeValueAsString(notification);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }

  public synchronized boolean send(Notification notification) {
    log.debug("sending from {}", this.getClass());

    // Send to the indicated recipient or default to the current user.
    Session wss = notification.getRecipient() == null ? wsSession :
        NotificationSocket.ENDPOINTS_BY_USER_ID.get(notification.getRecipient().getId()) == null ? null :
            NotificationSocket.ENDPOINTS_BY_USER_ID.get(notification.getRecipient().getId()).wsSession;
    try {
      // call get here on purpose. We don't actually want async behavior, but rather we do want to
      // avoid the Illegal state exceptions of a BasicRemote
      if (wss != null) {
        // user is logged on send now.
        wss.getAsyncRemote().sendText(MAPPER.writeValueAsString(notification)).get();
        return true;
      }
    } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
      log.error(e);
      try {
        // fall if the problem wasn't with the connection, perhaps we can tell the user about it...
        ExceptionReport report = new ExceptionReport(e.getMessage());
        String responseJson = MAPPER.writeValueAsString(report);
        wss.getAsyncRemote().sendText(responseJson).get();
      } catch (InterruptedException | ExecutionException | JsonProcessingException e1) {
        log.error(e1);
      }
    }
    return false;
  }

  @Override
  public boolean sendErrorMessage(String error) {
    messages.get().add(new Notification(error, ERROR));
    if (send(new Notification(error, ERROR))) {
      errorCount.incrementAndGet();
      return true;
    } else {
      return false;
    }
  }


  @Override
  public boolean sendWarningMessage(String warning) {
    return send(new Notification(warning, WARNING));
  }

  @Override
  public boolean sendInfoMessage(String info) {
    return send(new Notification(info, INFO));
  }

  @Override
  public boolean sendSuccess(String success) {
    return send(new Notification(success, SUCCESS));
  }

  /**
   * Method with which for forms that actually submit the page may. Otherwise this should not be uses
   *
   * @deprecated only should be required by login and password reset forms, all other forms should support
   * websocket based messages
   */
  @Override
  @Deprecated
  public List<Notification> clearRequestMessages() {
    errorCount.set(0);
    List<Notification> m = getErrorMessages();
    messages.set(new ArrayList<>());
    return m;
  }

  @Override
  public int errorCount() {
    return errorCount.get();
  }

  @Override
  public List<Notification> getErrorMessages() {
    return messages.get();
  }

  public static class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {

      log.debug("handshake:{} -> " + config.getPath(), this.getClass());
      Object tracker = ((HttpSession) request.getHttpSession()).getServletContext().getAttribute(
          SessionTracker.class.getName());
      // This is safe to do because it's the same instance of SessionTracker all the time
      config.getUserProperties().put(SessionTracker.class.getName(), tracker);

      super.modifyHandshake(config, request, response);
    }
  }

  private class ExceptionReport {
    public ExceptionReport(String exception) {
      this.exception = exception;
    }

    private String exception;

    public String getException() {
      return exception;
    }

    public void setException(String exception) {
      this.exception = exception;
    }
  }
}
