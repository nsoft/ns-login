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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

// class name to avoid clashes with user keyword in various DB systems.
@Entity
public class AppUser extends Persisted {

  // making this unique leads to user enumeration vulnerability in the create user page
  @Column(length = 40)
  private String username;

  @Column(unique = true, length = 128)
  private String userEmail;

  @ManyToOne
  private UserSecurity securityInfo;

  public AppUser() {}

  public AppUser(AccountRequest request) {
    this.username = request.getUsername();
    this.userEmail = request.getUserEmail();
    this.securityInfo = request.getSecurityInfo();
    securityInfo.setResetToken(null); // extremely important, makes token unusable in future.
    securityInfo.setExpiration(null);
    securityInfo.setExpirationReason(null);
    securityInfo.setResetRequestedAt(null);
  }

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
}
