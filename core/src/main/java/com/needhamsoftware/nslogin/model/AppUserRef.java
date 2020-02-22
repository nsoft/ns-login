/*
 *    Copyright (c) 2020, Needham Software LLC
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

@Entity
@JsonIdentityInfo(generator= JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AppUserRef extends Persisted {
  // NOTE for REST usage: everything but username and id is hidden, because the vast majority of
  // information about users is potential PII, or security related. Sections of the application
  // that want to provide views to the user (who should be able to see their info) or provide
  // administrators with views into additional info should map an additional class for those
  // use cases providing access to the proper attributes for that use case

  // making this unique leads to user enumeration vulnerability in the create user page
  @RestFilterEnable
  @Column(length = 40)
  private String username;


  public AppUserRef() {}

  public AppUserRef(AccountRequest request) {
    this.username = request.getUsername();
  }

  AppUserRef(AppUser appUser) {
    this.username = appUser.getUsername();
    this.setId(appUser.getId());
    this.setCreated(appUser.getCreated());
    this.setModified(appUser.getModified());
    this.setCreatedBy(appUser.getCreatedBy());
    this.setModifiedBy(appUser.getModifiedBy());
    this.setVersion(appUser.getVersion());
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }





}
