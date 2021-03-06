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

import com.needhamsoftware.nslogin.AuthzException;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.needhamsoftware.nslogin.service.PermissionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Singleton
public class UserFilter implements Filter {
  private static Logger log = LogManager.getLogger();

  @Inject
  private ObjectService objectService;

  @Inject
  private PermissionService permissionService;

  @Override
  public void init(FilterConfig filterConfig) {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    AppUser siteUser = null;
    try {
      siteUser = permissionService.lookUpPrincipal(req, objectService);
    } catch (AuthzException e) {
      log.debug(e);
      Messages.DO.sendErrorMessage("Insufficient Access Rights");
    }

    req.getSession().setAttribute("com.needhamsoftware.nslogin.SITE_USER",siteUser);
    chain.doFilter(request,response);
  }

  @Override
  public void destroy() {

  }
}
