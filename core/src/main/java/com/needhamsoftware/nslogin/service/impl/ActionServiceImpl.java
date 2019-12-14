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

import com.needhamsoftware.nslogin.model.Action;
import com.needhamsoftware.nslogin.model.ActionInvocation;
import com.needhamsoftware.nslogin.model.action.ReverseThings;
import com.needhamsoftware.nslogin.service.ActionService;
import com.needhamsoftware.nslogin.service.ObjectService;

import javax.inject.Inject;

public class ActionServiceImpl implements ActionService {

  @Inject
  ObjectService objectService;

  @Override
  public ActionInvocation visit(Action action) {
    return new ActionInvocation(action);
  }

  @Override
  public ActionInvocation visit(ReverseThings action) {
    ActionInvocation invocation = new ActionInvocation(action);
    invocation.getObjectsActedUpon().add(objectService);
    return invocation;
  }

}
