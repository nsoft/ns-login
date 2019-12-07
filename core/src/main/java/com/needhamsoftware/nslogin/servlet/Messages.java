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

package com.needhamsoftware.nslogin.servlet;


import com.needhamsoftware.nslogin.model.Notification;
import com.needhamsoftware.nslogin.service.MessageService;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.needhamsoftware.nslogin.service.impl.ObjectAlreadyHasIdException;
import com.needhamsoftware.nslogin.websocket.NotificationSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

/**
 * A class to provide global access to to the message system so that intelligent errors can be rendered
 * to the user from any point during the request. This avoids explosion of exception classes and lots of
 * catch/rethrow, exception source traversal, message class if-else blocks or even worse, parsing of
 * exception messages to determine which error happened three levels down etc. Statics are generally bad,
 * but sometimes global access really is what you need... Think about trying to inject every object in the
 * system with a service...
 */
@SuppressWarnings("CdiInjectInspection")
public class Messages implements MessageService {
  private static Logger log = LogManager.getLogger();

  // Note these statics enable provisioning of websocket session in MessageCollectionFilter
  @Inject
  private static MessageService preNotificationService;
  @Inject
  private static ObjectService objectService;


  private MessageService preNotificationService0;

  private ObjectService objectService0;

  private static final ThreadLocal<MessageService> service = new ThreadLocal<>();


  // This makes the code read Messages.DO.sendWarningMessage("The sky is falling!!")...
  public static final Messages DO = new Messages();

  private Messages() {
  }


  @Override
  public int errorCount() {
    return getMessageService().errorCount();
  }

  private MessageService getMessageService() {
    if (service.get() != null) {
      return service.get();
    } else {
      // websocket notification service not active. Only errors will be handled.
      return preNotificationService;
    }
  }

  public static void supplyWebMessageService(MessageService webMessageService) {
    service.set(webMessageService);
  }

  public static void clearWebMessageService() {
    MessageService messageService = service.get();
    if (messageService != null) {
      messageService.clearRequestMessages();
    }
    preNotificationService.clearRequestMessages();
    service.set(null);
  }

  @Override
  public List<Notification> getErrorMessages() {
    return getMessageService().getErrorMessages();
  }

  @Override
  public boolean sendErrorMessage(String message) {
    return getMessageService().sendErrorMessage(message);
  }

  @Override
  public boolean sendWarningMessage(String warning) {
    return getMessageService().sendWarningMessage(warning);
  }

  @Override
  public boolean sendInfoMessage(String info) {
    return getMessageService().sendInfoMessage(info);
  }

  @Override
  public boolean sendRecommendation(Notification recommendation) {
    try {
      if (recommendation.getId() == null) {
        log.trace("created new recommendation");
        objectService.insert(recommendation);
      }
    } catch (ObjectAlreadyHasIdException e) {
      // can't happen, but we have to check...
    }
    MessageService messageService = getMessageService();
    MessageService messageSocket = null;
    if (recommendation.getRecipient() != null) {
      NotificationSocket notificationSocket = NotificationSocket.ENDPOINTS_BY_USER_ID
          .get(recommendation.getRecipient().getId());
      if (notificationSocket != null) {
        log.trace("found socket for {}", recommendation.getRecipient().getId());
        messageSocket = notificationSocket;
      }
    }
    try { // we may in some cases retain a stale socket...
      return doSend(recommendation, messageSocket);
    } catch (IllegalStateException e) {
      // socket was stale, remove it and persist the notification to be shown at a later date.
      NotificationSocket.ENDPOINTS_BY_USER_ID.remove(recommendation.getRecipient().getId());
      return doSend(recommendation, messageService);
    }
  }

  private boolean doSend(Notification recommendation, MessageService messageSocket) {
    if (messageSocket != null && messageSocket.sendRecommendation(recommendation)) {
      log.trace("recommendation sent");
      recommendation.setSent(Instant.now());
      objectService.update(recommendation);
      return true;
    } else {
      log.trace("recommendation not sent");
      return false;
    }
  }


  public boolean sendSuccess(String success) {
    return getMessageService().sendSuccess(success);
  }

  @Override
  public List<Notification> clearRequestMessages() {
    return getMessageService().clearRequestMessages();
  }


  // do the exception dance, with the appropriate logger.
  public void exception(Exception e, Logger log) {
    log.error(e);
    Messages.DO.sendErrorMessage(e.getClass().getSimpleName() + ":" + e.getMessage());
  }
}

