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

package com.needhamsoftware.nslogin;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordStandards {
  public static boolean isValid(String password) {
    // implement any rules (like complexity) here. By default upper/lower/decimal/other required
    return password.length() >= 8 &&
        password.matches(".*\\d.*") &&
        password.matches(".*[A-Z].*") &&
        password.matches(".*\\w.*") &&
        password.matches(".*\\W.*");
  }

  public static String getHashpw(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(10));
  }
}
