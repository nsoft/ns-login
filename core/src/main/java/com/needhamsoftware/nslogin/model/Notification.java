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
import java.util.Date;

/**
 * Notification class for messages generated for a user while they are off line. Typically
 * instances of this class would not be saved for errors/warnings etc that are displayed
 * to the user in real time. Typically users should be authorized to only have access to
 * their own notifications.
 */
@Entity
@JsonIdentityInfo(generator=JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Notification extends Persisted {
  private String message;
  private NotificationType notificationType;
  private int displayMs;
  private String link;
  private String onClickFn;
  @RestFilterEnable
  private Instant sent;
  @RestFilterEnable
  private Instant expires = new Date(new Date().getTime() + (1000L * 60L * 60L * 24L * 365L * 1000L)).toInstant();
  @RestFilterEnable
  private Instant acknowledged; // date dismissed or clicked on.
  @ManyToOne
  @JsonIgnore
  @RestFilterEnable
  private AppUser recipient;

  public Notification(String message, NotificationType notificationType) {
    this.message = message;
    this.notificationType = notificationType;
  }

  public Notification() {
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  public int getDisplayMs() {
    return displayMs;
  }

  public void setDisplayMs(int displayMs) {
    this.displayMs = displayMs;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public Instant getSent() {
    return sent;
  }

  public void setSent(Instant sent) {
    this.sent = sent;
  }

  public Instant getExpires() {
    return expires;
  }

  public void setExpires(Instant expires) {
    this.expires = expires;
  }

  public Instant getAcknowledged() {
    return acknowledged;
  }

  public void setAcknowledged(Instant acknowledged) {
    this.acknowledged = acknowledged;
  }

  public AppUser getRecipient() {
    return recipient;
  }

  public void setRecipient(AppUser recipeint) {
    this.recipient = recipeint;
  }

  public String getOnClickFn() {
    return onClickFn;
  }

  @SuppressWarnings("SameParameterValue")
  public void setOnClickFn(String onClickFn) {
    this.onClickFn = onClickFn;
  }

}
