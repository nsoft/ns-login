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

package com.needhamsoftware.nslogin.servlet;

import com.needhamsoftware.nslogin.model.Notification;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class RestResponse {
  private boolean ok;
  private List<Object> results = new ArrayList<>();
  private List<Notification> messages = new ArrayList<>();

  private Long numFound;

  public boolean isOk() {
    return ok;
  }

  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public List<Object> getResults() {
    return results;
  }

  public void setResults(List<Object> results) {
    this.results = results;
  }

  public List<Notification> getMessages() {
    return messages;
  }

  public void setMessages(List<Notification> messages) {
    this.messages = messages;
  }

  public Long getNumFound() {
    return numFound;
  }

  public void setNumFound(Long numFound) {
    this.numFound = numFound;
  }

}
