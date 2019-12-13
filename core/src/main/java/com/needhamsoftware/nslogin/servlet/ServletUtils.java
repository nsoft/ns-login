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
import com.needhamsoftware.nslogin.model.Role;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.needhamsoftware.nslogin.service.SimpleObjectFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.needhamsoftware.nslogin.servlet.LoginConstants.PRINCIPAL;

public class ServletUtils {

  public static final String NSLOGIN_ROLES = "nslogin-roles";

  static AppUser lookUpPrincipal(HttpServletRequest req, ObjectService objectService) {
    String userEmail = (String) req.getSession().getAttribute(PRINCIPAL);
    return lookUpUserByEmail(objectService, userEmail);
  }

  static AppUser lookUpUserByEmail(ObjectService objectService, String userEmail) {
    AppUser siteUser;
    if (userEmail == null) {
      throw new UnauthenticatedException("No Principal in session");
    } else {
      List<AppUser> res = objectService.list(AppUser.class, 0, 1,
          Arrays.asList(new com.needhamsoftware.nslogin.service.Filter[]{
              new SimpleObjectFilter("userEmail", "=", userEmail)}), Collections.emptyList(), true);
      if (res.size() != 1) {
        throw new UnauthenticatedException("Non-unitary result when selecting user by email");
      }
      siteUser = res.get(0);
    }
    return siteUser;
  }

  static List<Role> lookUpRolesByIdList(ObjectService objectService, String ids) {
    if (StringUtils.isBlank(ids)) {
      return Collections.emptyList();
    }
    List<Long> idList = Stream.of(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
    return objectService.get(Role.class,idList);
  }

  public void handleError(HttpServletResponse resp, int code, ObjectMapper mapper) throws IOException {
    resp.setStatus(code);
    RestResponse response = new RestResponse();
    response.setMessages(Messages.DO.getErrorMessages());
    resp.setContentType("application/json");
    ServletOutputStream out = resp.getOutputStream();
    mapper.writeValue(out, response);
  }

}
