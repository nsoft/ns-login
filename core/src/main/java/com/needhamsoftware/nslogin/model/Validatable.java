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
   * of the object can be signaled by adding messages via {@link com.needhamsoftware.nslogin.servlet.Messages#DO}. Exceptions should
   * only be thrown in the event that the state is dangerous and always requires immediate cancellation of any
   * ongoing operation (without further validation for example). Please use <code>if</code> statements or
   * <code>return</code> to short circuit validation logic rather than exceptions.
   */
  boolean validate();

  default boolean isValidated() { return false; }
}
