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

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
public class Persisted {
  @RestFilterEnable
  @GeneratedValue
  @Id
  private Long id;
  @Version
  private long version;
  @ManyToOne
  private AppUser owner;
  @ManyToOne
  private AppUser createdBy;
  @ManyToOne
  private AppUser modifiedBy;
  private Instant created;
  private Instant modified;

  public AppUser getOwner() {
    return owner;
  }

  public void setOwner(AppUser owner) {
    this.owner = owner;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public AppUser getCreatedBy() {
    return createdBy;
  }

  void setCreatedBy(AppUser createdBy) {
    this.createdBy = createdBy;
  }

  public AppUser getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(AppUser modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }

  @PrePersist
  public void onUpdate() {
    created = Instant.now();
  }
  @PreUpdate
  public void businessUpdate() {
    modified = Instant.now();
  }
}
