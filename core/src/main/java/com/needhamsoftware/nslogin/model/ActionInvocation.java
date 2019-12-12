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

package com.needhamsoftware.nslogin.model;

import com.needhamsoftware.nslogin.service.NotPermittedException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.ArrayList;
import java.util.List;

public class ActionInvocation {
  public ActionInvocation(Action action) {
    this.action = action;
  }

  private Action action;

  private List<Object> objectsActedUpon = new ArrayList<>();

  /**
   * Check that the current user has permissions to execute this action
   *
   * @throws NotPermittedException if the user doesn't have the appropriate permission(s)
   */
  public void checkPerms() throws NotPermittedException {
    Subject currentUser = SecurityUtils.getSubject();
    for (Permission permission : action.getRequires()) {
      if (!currentUser.isPermitted(permission.shiroString())) {
        throw new NotPermittedException(permission);
      }
    }
  }

  public void prePersist() {
    action.prePersist(objectsActedUpon);
  }

  public void postPersist() {
    action.postPersist(objectsActedUpon);
  }

  public List<Object> getObjectsActedUpon() {
    return objectsActedUpon;
  }

}
