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

import javax.persistence.Entity;

/**
 * Class representing a permission to perform an action on an object type with a given qualifier.
 * {@link com.needhamsoftware.nslogin.service.ObjectService} will require a permission based on the concrete type of any
 * object it is handling. Permissions on abstract classes and interfaces will be ignored but may be used for
 * protecting {@link Action}s that relate to a class of objects. Standard qualifiers are a <code>long</code>
 * identifier indicating that the permission is only valid for a specific instance with that database identifier
 * or a domain name indicating that the user is permitted to perform the action only on the objects owned by
 * a member of the organization with that domain name.
 */
@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Permission extends Persisted {
  private String type;
  private String action;
  private String objId;
  private String field;

  public Permission() {}


  public String shiroString() {

    return type + ":" + action + applyQualifier() + applyField();
  }

  // override for more compelx permissions
  @SuppressWarnings("WeakerAccess")
  protected String applyQualifier() {
    return objId == null ? ":*" : ":" + objId;
  }

  // override for more compelx permissions
  @SuppressWarnings("WeakerAccess")
  protected String applyField() {
    return field == null ? ":*" : ":" + field;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String name) {
    this.action = name;
  }

  public String getObjId() {
    return objId;
  }

  public void setObjId(String qualifier) {
    this.objId = qualifier;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

}
