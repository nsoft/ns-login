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
import com.needhamsoftware.nslogin.model.Action;
import com.needhamsoftware.nslogin.model.ActionInvocation;
import com.needhamsoftware.nslogin.service.ActionService;
import com.needhamsoftware.nslogin.service.NotPermittedException;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.needhamsoftware.nslogin.service.PermissionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ActionFilter implements Filter {
  private Logger log = LogManager.getLogger();

  @Inject
  ObjectService objectService;
  @Inject
  ActionService actionService;
  @Inject
  ObjectMapper mapper;
  @Inject
  PermissionService permissionService;

  @Override
  public void init(FilterConfig filterConfig) {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    try {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse resp = (HttpServletResponse) response;
      List<ActionInvocation> invocations = new ArrayList<>();
      req.setAttribute("NS_ACTION", invocations);

      String actionStr = req.getHeader("X-Site-Action");
      if (actionStr != null) {
        String[] actions = actionStr.split(",");
        try {
          List<Action> actionsToInvoke = new ArrayList<>();
          for (String action : actions) {
            // todo optimize this by allowing a single query with OR semantics
            ObjectPropertyFilter restFilter =
                new ObjectPropertyFilter("name", "= " + action, Action.class.getDeclaredField("name"));
            List<com.needhamsoftware.nslogin.service.Filter> filters =
                Arrays.asList(new ObjectPropertyFilter[]{restFilter});
            List list = objectService.list(Action.class, 0, 2, filters, null);
            if (list.size() > 1) {
              ServletUtils.handleError(resp, 500, mapper);
              log.error(new RuntimeException("Database contains multiple action instances for the same action name. This should be impossible."));
            }
            if (list.isEmpty()) {
              Messages.DO.sendErrorMessage("Action not found:" + action);
              log.error("Action not found:" + action);
              ServletUtils.handleError(resp, 400, mapper);
            } else {
              actionsToInvoke.add((Action) list.get(0));
            }
          }
          for (Action action : actionsToInvoke) {
            ActionInvocation actionInvocation = action.accept(actionService);
            permissionService.checkPerms(actionInvocation.requiredPerms());
            invocations.add(actionInvocation);
          }
          chain.doFilter(request, response);
          if (resp.getStatus() < 400) {
            for (ActionInvocation invocation : invocations) {
              invocation.postPersist();
            }
          }
        } catch (NoSuchFieldException e) {
          Messages.DO.exception(e, log);
          ServletUtils.handleError(resp, 500, mapper);
        } catch (NotPermittedException e) {
          Messages.DO.exception(e, log);
          ServletUtils.handleError(resp, 403, mapper);
        }
      } else {
        chain.doFilter(request, response);
      }
    } catch (Exception e) {
      Messages.DO.exception(e, log);
    }
  }


  @Override
  public void destroy() {

  }
}
