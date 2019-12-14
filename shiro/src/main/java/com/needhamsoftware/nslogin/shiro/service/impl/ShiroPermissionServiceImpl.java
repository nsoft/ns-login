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

package com.needhamsoftware.nslogin.shiro.service.impl;

import com.needhamsoftware.nslogin.AuthzException;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.Permission;
import com.needhamsoftware.nslogin.model.Persisted;
import com.needhamsoftware.nslogin.model.Role;
import com.needhamsoftware.nslogin.service.*;
import com.needhamsoftware.nslogin.shiro.DoubleWildcardPermission;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.needhamsoftware.nslogin.servlet.LoginConstants.PRINCIPAL;

public class ShiroPermissionServiceImpl implements PermissionService {
  @Override
  public AppUser lookUpPrincipal(HttpServletRequest req, ObjectService objectService) throws AuthzException {
    String userEmail = (String) req.getSession().getAttribute(PRINCIPAL);
    return lookUpUserByEmail(objectService, userEmail);
  }
  /**
   * Check that the current user has permissions to execute this action
   *
   * @throws NotPermittedException if the user doesn't have the appropriate permission(s)
   * @param permissions The permissions that the user is required to have
   */
  @Override
  public void checkPerms(List<Permission> permissions) throws NotPermittedException {
    Subject currentUser = SecurityUtils.getSubject();
    for (Permission permission : permissions) {
      if (!currentUser.isPermitted(permission.shiroString())) {
        throw new NotPermittedException(permission);
      }
    }
  }
  @Override
  public AppUser lookUpUserByEmail(ObjectService objectService, String userEmail) throws AuthzException {
    AppUser siteUser;
    if (userEmail == null) {
      throw new IllegalArgumentException("Cant look up user with null email");
    } else {
      List<AppUser> res;
      res = objectService.list(AppUser.class, 0, 1,
          Arrays.asList(new Filter[]{
              new SimpleObjectFilter("userEmail", "=", userEmail)}), Collections.emptyList(), true);
      if (res.size() != 1) {
        throw new UnauthenticatedException("Non-unitary result when selecting user by email");
      }
      siteUser = res.get(0);
    }
    return siteUser;

  }

  @Override
  public <T extends Persisted> String checkPermsAndFilter(Class<T> clazz, String action) throws AuthzException {
    {
      try {
        SecurityUtils.getSubject().checkPermission(new DoubleWildcardPermission(clazz.getSimpleName() + ":" + action + ":*:*"));
      } catch (UnauthorizedException e) {
        throw new AuthzException(e);
      }
      String specificPermittedIds = "";
      AppUser topPrincipal = getTopPrincipal();

      List<Permission> allPerms = new ArrayList<>();
      if (topPrincipal != null) {
        allPerms.addAll(topPrincipal.getIntrinsicPermissions());
        for (Role role : topPrincipal.getRoles()) {
          allPerms.addAll(role.getGrants());
        }
      }
      List<String> objIds = allPerms.stream()
          .filter((p) ->
              (p.getType().equals(clazz.getSimpleName()) || "*".equals(p.getType())) &&
                  (p.getAction().equals(action) || "*".equals(p.getAction())) &&
                  p.getObjId() != null)
          .map(Permission::getObjId)
          .collect(Collectors.toList());
      if (!objIds.contains("*")) {
        specificPermittedIds = String.join(",", objIds);
      }
      return specificPermittedIds;
    }
  }

  @Override
  public AppUser getTopPrincipal() {
    PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
    if (principals != null) {
      for (Object principal : principals) {
        if (principal instanceof AppUser) {
          return (AppUser) principal;
        }
      }
    }
    return null;
  }
}
