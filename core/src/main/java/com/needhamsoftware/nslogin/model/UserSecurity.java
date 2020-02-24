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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
@JsonIdentityInfo(generator= JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserSecurity extends Persisted {
  @JsonIgnore
  private String passwordHash;
  @JsonIgnore
  private String salt;
  @JsonIgnore
  private String resetToken;
  private Instant resetRequestedAt;
  private Instant expiration;
  private Expiration expirationReason;
  @ManyToOne
  private AppUser forUser;

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public String getResetToken() {
    return resetToken;
  }

  public void setResetToken(String resetToken) {
    this.resetToken = resetToken;
  }

  public Instant getResetRequestedAt() {
    return resetRequestedAt;
  }

  public void setResetRequestedAt(Instant resetRequestedAt) {
    this.resetRequestedAt = resetRequestedAt;
  }

  public Instant getExpiration() {
    return expiration;
  }

  public void setExpiration(Instant expiration) {
    this.expiration = expiration;
  }

  public Expiration getExpirationReason() {
    return expirationReason;
  }

  public void setExpirationReason(Expiration expirationReason) {
    this.expirationReason = expirationReason;
  }

  public AppUser getForUser() {
    return forUser;
  }

  public void setForUser(AppUser forUser) {
    this.forUser = forUser;
  }
}
