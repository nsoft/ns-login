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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.Instant;

// class name to avoid clashes with user keyword in various DB systems.
@Entity
@JsonIdentityInfo(generator= JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AppUser extends Persisted {

  // NOTE for REST usage: everything but username and id is hidden, because the vast majority of
  // information about users is potential PII, or security related. Sections of the application
  // that want to provide views to the user (who should be able to see their info) or provide
  // administrators with views into additional info should map an additional class for those
  // use cases providing access to the proper attributes for that use case

  // making this unique leads to user enumeration vulnerability in the create user page
  @RestFilterEnable
  @Column(length = 40)
  private String username;

  @RestFilterEnable
  @Column(unique = true, length = 128)
  @JsonIgnore // avoid user enumeration attacks
  private String userEmail;

  @JsonIgnore // this should never be shipped to the user.
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

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public Long getId() {
    return super.getId();
  }

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public long getVersion() {
    return super.getVersion();
  }

  @JsonIgnore // this should never be shipped to the user.
  public Instant getPasswordResetRequestedAt() {
    return securityInfo.getResetRequestedAt();
  }

  public void setPasswordResetRequestedAt(Instant ignored) {}

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public AppUser getCreatedBy() {
    return super.getCreatedBy();
  }

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public AppUser getModifiedBy() {
    return super.getModifiedBy();
  }

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public Instant getCreated() {
    return super.getCreated();
  }

  @Override
  @JsonIgnore // this should never be shipped to the user.
  public Instant getModified() {
    return super.getModified();
  }
}
