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

import com.needhamsoftware.nslogin.servlet.Messages;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Interface indicating that the object is capable of verifying it's internal state and relationships to
 * nearby members of it's object graph. This method is not meant to be responsible for cross coordination
 * among instances of the same class or consistency with objects not normally navigable in a single hop.
 * Local validations should be done here and broader validations should be done in the context of a
 * subclass of {@link Action}
 */
public interface Validatable {


  /**
   * Check that the internal state is consistent. This should not normally throw an exception. Issues with the state
   * of the object can be signaled by adding messages via {@link com.needhamsoftware.nslogin.servlet.Messages#DO}.
   * Exceptions should only be thrown in the event that the state is dangerous and always requires immediate
   * cancellation of any ongoing operation (without further validation for example). Please use <code>if</code>
   * statements or <code>return</code> to short circuit validation logic rather than exceptions.
   */
  boolean validate();

  /**
   * Check that the map keyed by names of properties on this object appears to contain proper data to populate
   * the corresponding properties on this object. This is used for validating incoming json in a manner that
   * produces error messages that are under our control.
   *
   * @return true if validation was successful
   */
  // note that this till falls short because messages tend to be based on property names not
  // The name used in the UI.. But it's starting point, which is the goal, further improvement
  // is an excrcise left to the reader
  boolean validateMap(Map<String,Object>  map);


  default boolean isValidated() { return false; }

  static void checkInteger(Object o, String propName) {
    if (!(o instanceof Integer)) {
      if (o instanceof String) {
        try {
          Integer.parseInt((String)o);
        } catch (NumberFormatException e) {
          Messages.DO.sendErrorMessage(propName + " must be an integer");
        }
      }
    }
  }

  static void checkLong(Object o, String propName) {
    if (!(o instanceof Long)) {
      if (o instanceof String) {
        try {
          Long.parseLong((String)o);
        } catch (NumberFormatException e) {
          Messages.DO.sendErrorMessage(propName + " must be a long integer");
        }
      }
    }
  }

  static void checkFloat(Object o, String propName) {
    if (!(o instanceof Float)) {
      if (o instanceof String) {
        try {
          Float.parseFloat((String)o);
        } catch (NumberFormatException e) {
          Messages.DO.sendErrorMessage(propName + " must be a floating point number");
        }
      }
    }
  }

  static void checkDouble(Object o, String propName) {
    if (!(o instanceof Double)) {
      if (o instanceof String) {
        try {
          Double.parseDouble((String)o);
        } catch (NumberFormatException e) {
          Messages.DO.sendErrorMessage(propName + " must be a double precision floating point number");
        }
      }
    }
  }

  static void checkInstant(Object o, String propName) {
    if (!(o instanceof Instant)) {
      if (o instanceof Number) {
        // ok since numbers can be converted to instants
        return;
      }
      if (o instanceof String) {
        try {
          Instant.parse((String) o);
        } catch (DateTimeParseException e) {
          try {
            Double.parseDouble((String) o);
          } catch (NumberFormatException nfe) {
            Messages.DO.sendErrorMessage(propName + " must represent an ISO instant");
          }
        }
      }
    }
  }

}
