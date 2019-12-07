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

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Singleton
public class WebSocketFilter implements Filter {
  private static Logger log = LogManager.getLogger();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    final Map<String, String[]> fakedParams = Collections.singletonMap("sessionId",
        new String[] { httpRequest.getSession().getId() });
    log.debug("wrapping request {}", this.getClass());

    HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
      @Override
      public Map<String, String[]> getParameterMap() {
        return fakedParams;
      }
    };
    chain.doFilter(wrappedRequest, response);
  }

  @Override
  public void init(FilterConfig config) { }
  @Override
  public void destroy() { }
}