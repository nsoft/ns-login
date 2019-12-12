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

import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.service.ObjectService;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Singleton
public class ShiroJWTAuthenticationFilter extends JwtAuthenticationFilter {

  @Inject
  private ObjectService objectService;

  @Override
  protected void proceed(ServletRequest request, ServletResponse response, FilterChain chain, Object principal) throws IOException, ServletException {
    // the subject in the JWT token is the email, we need need to look up the user by email.
    String email = (String) principal;
    AppUser user = ServletUtils.lookUpUserByEmail(objectService,email);
    PrincipalCollection principals = new SimplePrincipalCollection(user, "rest");
    Subject subject = new Subject.Builder().principals(principals).buildSubject();
    try {
      subject.execute(() -> {
        superProceed(request, response, chain, principal);
        return null;
      });
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      if (cause instanceof ServletException) {
        throw (ServletException) cause;
      }
      throw e;
    }
  }

  private void superProceed(ServletRequest request, ServletResponse response, FilterChain chain, Object principal) throws IOException, ServletException {
    super.proceed(request, response, chain, principal);
  }
}
