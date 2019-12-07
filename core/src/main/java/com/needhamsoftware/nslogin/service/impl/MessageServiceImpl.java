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

package com.needhamsoftware.nslogin.service.impl;

import com.needhamsoftware.nslogin.model.Notification;
import com.needhamsoftware.nslogin.model.NotificationType;
import com.needhamsoftware.nslogin.service.MessageService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of message service for not-logged in users or users that have not yet initiated the
 * web socket for general notifications. This service only handles error messages
 */
public class MessageServiceImpl implements MessageService {

  private ThreadLocal<List<Notification>> messages = ThreadLocal.withInitial(ArrayList::new);

  private void addRequestMessage(String message) {
    messages.get().add(new Notification(message, NotificationType.ERROR));
  }

  @Override
  public boolean sendErrorMessage(String error) {
    addRequestMessage(error);
    return true;
  }

  // TODO: 10/22/19 Obviously, this needs more work...

  @Override
  public boolean sendWarningMessage(String warning) {
    return false;
  }

  @Override
  public boolean sendInfoMessage(String info) {
    return false;
  }

  @Override
  public boolean sendSuccess(String success) {
    return false;
  }

  @Override
  public List<Notification> clearRequestMessages() {
    List<Notification> m = messages.get();
    messages.set(new ArrayList<>());
    return m;
  }

  @Override
  public int errorCount() {
    // in the future, distinguish between errors and warnings, and info messages...
    return messages.get().size();
  }

  @Override
  public List<Notification> getErrorMessages() {
    return messages.get();
  }

  @Override
  public boolean sendRecommendation(Notification recommendation) {
    return false;
  }
}
