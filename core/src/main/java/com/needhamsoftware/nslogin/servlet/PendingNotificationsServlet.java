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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.Notification;
import com.needhamsoftware.nslogin.model.NotificationType;
import com.needhamsoftware.nslogin.service.Filter;
import com.needhamsoftware.nslogin.service.ObjectService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class PendingNotificationsServlet extends ServletBase {
  private static Logger log = LogManager.getLogger();

  private static final Field sent;
  private static final Field expires;
  private static final Field type;
  private static final Field recipient;
  private static final Field acknowledged;
  private static final ObjectPropertyFilter NEVER_SENT;
  private static final ObjectPropertyFilter IS_REC;
  private static final ObjectPropertyFilter IS_NOT_ACK;


  static {
    try {
      sent = Notification.class.getDeclaredField("sent");
      expires = Notification.class.getDeclaredField("expires");
      type = Notification.class.getDeclaredField("notificationType");
      recipient = Notification.class.getDeclaredField("recipient");
      acknowledged = Notification.class.getDeclaredField("acknowledged");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    IS_REC = new ObjectPropertyFilter("notificationType", "= " + NotificationType.RECOMMENDATION, PendingNotificationsServlet.type);
    NEVER_SENT = new ObjectPropertyFilter("sent", "= null", PendingNotificationsServlet.sent);
    IS_NOT_ACK = new ObjectPropertyFilter("acknowledged", "= null", PendingNotificationsServlet.acknowledged);
  }


  @Inject
  private ObjectService objectService;
  @Inject
  private ObjectMapper mapper;

  private ServletUtils servletUtils = new ServletUtils();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      AppUser principal = getSiteUser(req);
      List<Filter> filters = new ArrayList<>();
      Date now = new Date();
      ObjectPropertyFilter hasNotExpired = new ObjectPropertyFilter("expires", ">" + now.getTime(), PendingNotificationsServlet.expires);
      ObjectPropertyFilter isCurrUser = new ObjectPropertyFilter("recipient", "=" + principal.getId(), PendingNotificationsServlet.recipient);

      filters.add(NEVER_SENT);
      filters.add(IS_NOT_ACK);
      filters.add(hasNotExpired);
      filters.add(isCurrUser);
      notify(filters);

      resp.setContentType("application/json");
      resp.getWriter().write("{}"); // silly bogus output to keep jquery happy.}
    }catch (Exception e) {
      servletUtils.handleError(resp, 500, mapper);
      log.error("ERR:", e);
    }
  }

  private void notify(List<Filter> filters) {
    List notificationsToSend = objectService.list(Notification.class, 0, Integer.MAX_VALUE, filters, null, true);
    for (Object o : notificationsToSend) {
      Notification n = (Notification) o;
      Messages.DO.send(n);
    }
  }
}
