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

import javax.persistence.*;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Role extends Persisted {

  private String name;
  @Column(name = "keyName")
  private String key;

  @OneToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
  private List<Permission> grants;

  @OneToMany
  private List<AppUser> members;

  public List<AppUser> getMembers() {
    return members;
  }

  public void setMembers(List<AppUser> members) {
    this.members = members;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String name) {
    this.key = name;
  }

  public List<Permission> getGrants() {
    return grants;
  }

  public void setGrants(List<Permission> grants) {
    this.grants = grants;
  }
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
