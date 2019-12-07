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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class ObjectReference {

  private final static Logger log = LogManager.getLogger();

  private Class type;
  private Long id;
  private boolean valid = true;

  public ObjectReference(String pathInfo) {
    String[] parts = pathInfo.split("/");
    if (parts.length < 2) {
      valid = false;
    } else {
      Class tmp = null;
      try {
        tmp = Class.forName("com.needhamsoftware.nslogin.model." + parts[1]);
      } catch (ClassNotFoundException e) {
        // ok try the next package
      }
      type = tmp;
      if (parts.length > 2) {
        try {
          id = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
          valid = false;
        }
      }
    }
  }

  public Class getType() {
    return type;
  }

  public Long getId() {
    return id;
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String toString() {
    return "ObjectReference{" +
        "type=" + type +
        ", id=" + id +
        ", valid=" + valid +
        '}';
  }
}
