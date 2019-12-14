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

package com.needhamsoftware.nslogin.service;

import com.needhamsoftware.nslogin.AuthzException;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.Permission;
import com.needhamsoftware.nslogin.model.Persisted;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Required API for Authentication and Authorization
 */
public interface PermissionService {
  AppUser lookUpPrincipal(HttpServletRequest req, ObjectService objectService) throws AuthzException;

  void checkPerms(List<Permission> permissions) throws NotPermittedException;

  /**
   * Obtain a user {@link AppUser} object based on an email. This is syntactic sugar
   * on top of {@link ObjectService#list(Class, int, int, List, List)}
   *
   * @param objectService a service capable of obtaining the user from the database
   * @param userEmail the email to be used to locate the user.
   * @return The specified user, or null if not found
   */
  AppUser lookUpUserByEmail(ObjectService objectService, String userEmail) throws AuthzException;

  /**
   * Check the permissions and return a comma separated list of object ID's that the user has explicit permission
   * to view. The result of this method is meant to be substituted into a JPQL clause such as
   * <pre>where t.id in ( {return value} )</pre>
   *
   * @param clazz the persistent type for which the action may or may not be permissible
   * @param action the action that may or may not be permitted
   * @param <T> the persistent type that will be filtered
   * @return A comma separated list of ids for which access IS granted.
   * @throws AuthzException if the current user doesn't have sufficient permissions for the action/type combination
   */
  <T extends Persisted> String checkPermsAndFilter(Class<T> clazz, String action) throws AuthzException;

  /**
   * Get the currently relevant principal. Some implementations such as shiro can track multiple principals
   * for features such as impersonation. This method should return the current acting user.
   *
   * @return The user which the system should use for permitting and attributing work.
   */
  AppUser getTopPrincipal();
}
