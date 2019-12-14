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

package com.needhamsoftware.nslogin.model.action;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.needhamsoftware.nslogin.model.Action;
import com.needhamsoftware.nslogin.model.ActionInvocation;
import com.needhamsoftware.nslogin.model.TestThing;
import com.needhamsoftware.nslogin.service.ActionVisitor;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;

/**
 * A demonstration action, should be removed
 */
@Entity
@JsonIdentityInfo(generator= JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReverseThings extends Action {
  @Override
  public void prePersist(List<Object> objectsActedUpon) {
    Objects o = findObjs(objectsActedUpon);
    Collections.reverse(o.thing.getSomeThings());
  }

  @Override
  public void postPersist(List<Object> objectsActedUpon) {

  }

  @Override
  public ActionInvocation accept(ActionVisitor visitor) {
    return visitor.visit(this);
  }

  private Objects findObjs(List<Object> objectsActedUpon) {
    Objects o = new Objects();
    for (Object candidate : objectsActedUpon) {
      if (candidate instanceof ObjectService) {
        o.oserv = (ObjectService) candidate;
      }
      if (candidate instanceof TestThing) {
        o.thing = (TestThing) candidate;
      }
    }
    if (o.thing == null) {
      throw new IllegalArgumentException("Internal Error: no TestThing object supplied");
    }
    if(o.oserv == null) {
      throw new RuntimeException("Internal Error: ReverseThings did not receive an ObjectService");
    }
    return o;
  }
  private static class Objects {
    TestThing thing = null;
    ObjectService oserv = null;
  }
}
