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

/**
 * A servlet and filter combo to handle logins.
 *
 * The servlet peforms 2 functions:
 * <ol>
 *   <li>Validates username/password against a database that stores users with password hashes and redirects
 *       back to the application with a header containing a JWT token. The app may manage this token as it
 *       sees fit or use the provided filter</li>
 *   <li>Responds with the public key corresponding to the key id passed back in the token</li>
 * </ol>
 *
 * The JWT token returned contains a subject equal to the username, a random UUID key id and an issuer. It is signed
 * with a private key from a public/private key pair that is re-generated on a periodic basis. Validation of the
 * JWT token is performed in the included security filter and follows the following steps:
 * <ol>
 *   <li>Read the key id from the header of the token</li>
 *   <li>Fetch the public key from the servlet by sending a request ending in ?kid=[UUID]</li>
 *   <li>Decrypt the secret from the token using the public key</li>
 *   <li>Check that the secret contains a valid value for issuer</li>
 *   <li>Trust the username from the token and log the user in as that user</li>
 * </ol>
 *
 * Old keys are cached for a period longer than the regeneration time, but not indefinitely, meaning that anyone
 * logging in at the key regen-boundary race condition can still fetch the previous key, but there is only a
 * very short window of time for attackers to attempt reverse-engineer the private key from the public key.
 * <p></p>
 * <p><strong>The goal of this system is to be stronger than the HTTPS layer that protects it and make HTTPS
 * the weakest link. Without HTTPS this is very easily compromised by stealing cookies/tokens/etc, so HTTPS is
 * required!</strong></p>
 * <p></p>
 * <p> This login system provides zero user management facilities and zero new user creation facilities, and in fact
 * cannot on it's own ever write to your database (by design).</p>
 */
package com.needhamsoftware.nslogin.servlet;