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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class ServletBase extends javax.servlet.http.HttpServlet {
  @Inject
  protected ObjectService objectService;
  @Inject
  private PermissionService permissionService;

  AppUser getSiteUser(HttpServletRequest req) throws AuthzException {
    AppUser siteUser = permissionService.lookUpPrincipal(req,objectService);
    return objectService.get(AppUser.class, siteUser.getId());
  }
}
