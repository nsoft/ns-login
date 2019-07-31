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
 * A filter to enforce authentication.
 *
 * The filter is designed to work in conjunction with a servlet that provides the following services:
 *
 * <ol>
 *   <li>responds with the encoded representation of a key when queried with kid=[key id]</li>
 *   <li>Accepts a redirect for an authenticated user, performs authentication and then returns the
 *       user to the original URL (including query parameters)</li>
 * </ol>
 *
 * <p>The filter does NOT return 401 Unauthorized because almost no modern application wants the browser to
 * pop up a window by default. If you want it to do so you are of course welcome to modify the code to
 * suit your needs.</p>
 *
 * The JWT token returned must contain a random UUID key id in the header, subject equal to the username, and an
 * encrypted value for issuer in the secret. It is signed with a private key from a public/private key pair that is
 * re-generated on a periodic basis. Validation of the JWT token is performed in the included security filter and follows the following steps:
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