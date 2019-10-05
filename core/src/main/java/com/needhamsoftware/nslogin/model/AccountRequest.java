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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

// intentionally similar to user, gets converted to a user upon email confirmation.
// this keeps all the login logic simple, without need to check if the confirmation
// has occurred for every login.
@SuppressWarnings("WeakerAccess")
@Entity
public class AccountRequest extends Persisted {
  private String username;

  private String userEmail;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public UserSecurity getSecurityInfo() {
    return securityInfo;
  }

  public void setSecurityInfo(UserSecurity securityInfo) {
    this.securityInfo = securityInfo;
  }

  @ManyToOne
  private UserSecurity securityInfo;

}
