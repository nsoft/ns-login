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
 * A servlet to handle logins.
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
 * JWT token is performed by the application upon return and should follow the following steps:
 *
 * <ol>
 *   <li>Read the JWT token from the parameter on the return response</li>
 *   <li>Read the key id from the header of the JWT Token </li>
 *   <li>Fetch the public key from the servlet by sending a request with a single parameter ?kid=[UUID]</li>
 *   <li>Decrypt the secret from the token using the public key returned by this servlet</li>
 *   <li>Check that the secret contains a valid value for issuer</li>
 *   <li>Trust the username from the token and log the user in as that user</li>
 *   <li>Optionally redirect the user one more time to hide the token on the return url.</li>
 * </ol>
 *
 * <p>This system provides TRUST not SECRECY!</p>
 *
 * <p><strong>Since the public key is freely available, anyone can read the contents of a token (until the
 * key cache expires) the token "secret" in this design is therefore meant to be considered PUBLIC INFORMATION.
 * Nothing sensitive should be placed in the token "secret." It's only function is to communicate the trust that
 * this system has in the identity of the user, (and if the base system is enhanced, perhaps the entitlements that that
 * user has). If sensitive information needs to be placed in the token, then a pre shared secret key system
 * such as HMAC should be substituted. It is also VERY important to fetch the public key independently over a
 * <i>SECURE</i> channel. DO NOT change this code to put the key in the header. That would mean anyone could sign with any
 * private key and simply include their OWN public key, which would of course then successfully decrypt the secret
 * that they substituted rendering the whole process useless</strong></p>
 *
 * <p>Note that in general, secrecy is the job of your SSL (https) connections, so in combination with https, the
 * contents of the token would only be visible to the current browser session. It is important to keep in mind that
 * browsers can become compromised by plugins or viruses, or have bugs that leak information so the user you trust may
 * not be the only one observing the connection, and therefore the contents of the token. While a fully compromised
 * browser might steal the password directly, it would only do so if the crack was specifically engineered for that
 * purpose. A potentially larger risk is that other, less malicious (data gathering) plugins or advertising based,
 * user tracking code might export URLs or other info from the browser and expose the token to a secondary (truly
 * malicious) attacker at a location outside the browser. This risk is one of the major motivations for having token
 * encryption key pairs that expire fairly quickly. Key expiration makes the any historical token information useless
 * to attackers because the public  key becomes unavailable, and the token secret inscrutable forevermore without
 * directly cracking the encryption algorithm.</p>
 *
 * <p>In summary, the way an attacker can see the contents of the token is to obtain the token (somehow) and query the
 * login servlet (which can never be protected by login of course) to get the public key to before the key identified
 * by the token header expires (or continuously harvest and store keys). The way the attacker can impersonate the user
 * is to steal the token and supply it to the system before the key expires. This key expiration obviates the need for
 * an exp (token expiration) value in the secret, and is superior since it could never be fooled by a forged exp value,
 * and tokens that last forever can't be created for testing and then stolen or inadvertently leaked. That said, your
 * release testing should be sure to verify that tokens expire on time and a developer hasn't inadvertently checked
 * in a convenience configuration with a long expiration period!</p>
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