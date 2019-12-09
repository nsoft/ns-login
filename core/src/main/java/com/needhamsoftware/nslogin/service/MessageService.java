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

import com.needhamsoftware.nslogin.model.Notification;

import java.util.List;

public interface MessageService {

  /**
   * Send an error back to the user. Errors should be used to indicate that a requested operation was
   * not performed, and supply a reason.
   *
   * @param error The textual message for the user.
   * @return true if the calling code can expect the user to have received the message, false if the message may
   *              not have been delivered to the user.
   */
  boolean sendErrorMessage(String error);

  /**
   * Send a warning to the user. Warnings should generally relate to an operation recently requested may produce
   * unexpected results, or imply further action should be taken by the user. Warnings indicate that the
   * requested operation was performed.
   *
   * @param warning The textual message for the user
   * @return true if the calling code can expect the user to have received the message, false if the message may
   *              not have been delivered to the user.
   */
  boolean sendWarningMessage(String warning);

  /**
   * Send a success message to the user. Success messages affirm the successful completion of an operation requested
   * by the user.
   *
   * @param success The textual message to the user.
   * @return true if the calling code can expect the user to have received the message, false if the message may
   *              not have been delivered to the user.
   */
  boolean sendSuccess(String success);

  /**
   * Send an informational message to the user. Info messages do not relate to any particular user request.
   *
   * @param info The textual message for the user
   * @return true if the calling code can expect the user to have received the message, false if the message may
   *              not have been delivered to the user.
   */
  boolean sendInfoMessage(String info);


  List<Notification> clearRequestMessages();

  int errorCount();

  /**
   * For cases where the calling code is not associated with an http session, or the main notification web
   * socket has not yet been constructed, a list of errors is maintained. This primarily gets used by
   * the login form and other unauthenticated pages.
   *
   * @return A list of any error messages that have been supplied.
   */
  List<Notification> getErrorMessages();


  /**
   * Inspect the supplied notification and send it as per it's type.
   *
   * @param notification The notification to send
   * @return true if the calling code can expect the user to have received the message, false if the message may
   *              not have been delivered to the user.
   */
  default boolean send(Notification notification){
    switch (notification.getNotificationType()) {
      case ERROR:
        return sendErrorMessage(notification.getMessage());
      case WARNING:
        return sendWarningMessage(notification.getMessage());
      case SUCCESS:
        return sendSuccess(notification.getMessage());
      case INFO:
        return sendSuccess(notification.getMessage());
    }
    return false;
  }
}
