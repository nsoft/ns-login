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

import io.jsonwebtoken.SignatureAlgorithm;

public interface LoginConstants {

  // Security strength parameters.
  int KEY_CHANGE_SECONDS = 30 * 60;
  int KEY_EXPIRE_SECONDS = 60 * 60; // must always be greater than KEY_CHANGE_SECONDS to prevent valid keys from failing
  SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;

  String ISSUER = "nslogin";

  // session key to identify the user
  String PRINCIPAL = "com.needhamsoftware.nslogin.principal";

  // Login dance headers
  String X_LOGIN_RETURN_TO = "X-Login-Return-To";
  String X_JWT_TOKEN = "X-JWT-Token";
  String X_ERROR_MESSAGE = "X-Error-Message";

}
