package com.needhamsoftware.nslogin.servlet;

import com.needhamsoftware.nslogin.model.Notification;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
class RestResponse {
  private boolean ok;
  private List<Object> results = new ArrayList<>();
  private List<Notification> messages = new ArrayList<>();

  private Long numFound;

  public boolean isOk() {
    return ok;
  }

  @SuppressWarnings("WeakerAccess")
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
