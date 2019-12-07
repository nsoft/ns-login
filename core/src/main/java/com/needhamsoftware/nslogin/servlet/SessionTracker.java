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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebListener
public class SessionTracker implements ServletContextListener, HttpSessionListener, Serializable {
  private static Logger log = LogManager.getLogger();

  private final ConcurrentMap<String, HttpSession> sessions = new ConcurrentHashMap<>();

  @Override
  public void contextInitialized(ServletContextEvent event) {
    event.getServletContext().setAttribute(getClass().getName(), this);
    log.debug("Initializing context detected by {}", this.getClass());
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    sessions.put(event.getSession().getId(), event.getSession());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    sessions.remove(event.getSession().getId());
  }

  public HttpSession getSessionById(String id) {
    return sessions.get(id);
  }
}