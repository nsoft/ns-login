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
import com.needhamsoftware.nslogin.model.Role;
import com.needhamsoftware.nslogin.service.ObjectService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServletUtils {

  public static final String NSLOGIN_ROLES = "nslogin-roles";

  private ServletUtils(){}

  public static List<Role> lookUpRolesByIdList(ObjectService objectService, String ids) {
    if (StringUtils.isBlank(ids)) {
      return Collections.emptyList();
    }
    List<Long> idList = Stream.of(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
    return objectService.get(Role.class,idList);
  }

  public static void handleError(HttpServletResponse resp, int code, ObjectMapper mapper) throws IOException {
    resp.setStatus(code);
    RestResponse response = new RestResponse();
    response.setMessages(Messages.DO.getErrorMessages());
    resp.setContentType("application/json");
    ServletOutputStream out = resp.getOutputStream();
    mapper.writeValue(out, response);
  }

}
