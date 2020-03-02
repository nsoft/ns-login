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
import com.needhamsoftware.nslogin.service.ActionVisitor;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class Action extends Persisted {

  @Column(unique = true,length = 128)
  @RestFilterEnable
  private String name;
  @ManyToMany
  private List<Permission> requires;

  protected Action() {
  }

  public Action(String name) {
    this.name = name;
  }

  public abstract void prePersist(List<Object> objectsActedUpon);
  public abstract void postPersist(List<Object> objectsActedUpon);


  public abstract ActionInvocation accept(ActionVisitor visitor) ;

  public List<Permission> getRequires() {
    return requires;
  }

  public void setRequires(List<Permission> requires) {
    this.requires = requires;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
