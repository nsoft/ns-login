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
import com.needhamsoftware.nslogin.websocket.NotificationSocket;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Singleton
class MessageCollectionFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    List<Notification> messages = Messages.DO.getErrorMessages();
    try {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpSession session = req.getSession();
      if (session != null) {
        Messages.supplyWebMessageService(NotificationSocket.ACTIVE_ENDPOINTS.get(session.getId()));
      }
      request.setAttribute("MESSAGES", messages);
      chain.doFilter(request, response);
    } finally {
      // put them in the request attributes for external JSP's that don't have a web socket
      messages.addAll(Messages.DO.getErrorMessages());
      Messages.clearWebMessageService();
    }
  }

  @Override
  public void destroy() {

  }
}
