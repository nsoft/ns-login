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

package com.needhamsoftware.nslogin.shiro.servlet;

import com.google.inject.Provides;
import com.needhamsoftware.nslogin.shiro.HibernateRealm;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.guice.web.ShiroWebModule;

import javax.servlet.ServletContext;

public class HibernateShiroWebModule extends ShiroWebModule {
  public HibernateShiroWebModule(ServletContext ctx) {
    super(ctx);
  }

  @Override
  protected void configureShiroWeb() {
    try {
      bindRealm().toConstructor(HibernateRealm.class.getConstructor(CredentialsMatcher.class));
    } catch (NoSuchMethodException e) {
      addError(e);
    }
  }

  @Provides
  CredentialsMatcher getCredentialsMatcher() {
    return new JWTCredentialsMatcher();
  }
}
