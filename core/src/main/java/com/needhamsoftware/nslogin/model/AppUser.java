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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

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
  private String userEmail;

  @ManyToOne
  private UserSecurity securityInfo;

  // the user's roles, populated only for Principals and derived from JWT token
  private transient List<Role> roles;

  @OneToMany
  // permissions that the user has regardless of roles (edit self, etc)
  private List<Permission> intrinsicPermissions;

  public AppUser() {}

  public AppUser(AccountRequest request) {
    this.username = request.getUsername();
    this.userEmail = request.getUserEmail();
    this.securityInfo = request.getSecurityInfo();
    securityInfo.setResetToken(null); // extremely important, makes token unusable in future.
    securityInfo.setExpiration(null);
    securityInfo.setExpirationReason(null);
    securityInfo.setResetRequestedAt(null);
    this.setCreated(Instant.now());
  }

  public AppUserRef asRef() {
    return new AppUserRef(this);
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
  public long getVersion() {
    return super.getVersion();
  }

  public Instant getPasswordResetRequestedAt() {
    return securityInfo.getResetRequestedAt();
  }

  public void setPasswordResetRequestedAt(Instant ignored) {}

  @Override
  public AppUserRef getCreatedBy() {
    return super.getCreatedBy();
  }

  @Override
  public AppUserRef getModifiedBy() {
    return super.getModifiedBy();
  }

  @Override
  public Instant getCreated() {
    return super.getCreated();
  }

  @Override
  public Instant getModified() {
    return super.getModified();
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  public List<Permission> getIntrinsicPermissions() {
    return intrinsicPermissions;
  }

  public void setIntrinsicPermissions(List<Permission> intrinsicPermissions) {
    this.intrinsicPermissions = intrinsicPermissions;
  }
}
