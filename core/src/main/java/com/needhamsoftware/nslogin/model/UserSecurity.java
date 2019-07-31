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
import java.time.Instant;

@Entity
public class UserSecurity extends Persisted {
  private String passwordHash;
  private String salt;
  private String resetToken;
  private Instant resetRequestedAt;
  private Instant expiration;
  private Expiration expirationReason;

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
}
