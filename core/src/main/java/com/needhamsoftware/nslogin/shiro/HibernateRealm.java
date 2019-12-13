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

package com.needhamsoftware.nslogin.shiro;

import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("CdiInjectionPointsInspection")
public class HibernateRealm extends AuthorizingRealm {

  @SuppressWarnings("JpaQlInspection")
  private static final String FROM_USER_WHERE_EMAIL_PRINCIPAL = "from AppUser where email = :principal";
  @SuppressWarnings("unused")
  private static final Logger log = LogManager.getLogger();

  @Inject
  private Provider<EntityManager> entityManagerProvider;

  @Inject
  public HibernateRealm(CredentialsMatcher matcher) {
    super(matcher);
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(this);
    SecurityUtils.setSecurityManager(securityManager);
  }

  @SuppressWarnings("Convert2streamapi")
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    refresh(principals);

    SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
    for (Object principal : principals) {
      AppUser u = (AppUser) principal;
      List<String> roles = new ArrayList<>();
      List<Permission> perms = new ArrayList<>();
      for (Role role : u.getRoles()) {
        List<com.needhamsoftware.nslogin.model.Permission> grants = role.getGrants();
        for (com.needhamsoftware.nslogin.model.Permission grant : grants) {
          perms.add(new WildcardPermission(grant.shiroString()));
        }
        roles.add(role.getKey());
      }
      authInfo.addRoles(roles);
      List<com.needhamsoftware.nslogin.model.Permission> intrinsicPermissions = u.getIntrinsicPermissions();
      for (com.needhamsoftware.nslogin.model.Permission ip : intrinsicPermissions) {
        perms.add(new WildcardPermission(ip.shiroString()));
      }
      authInfo.addObjectPermissions(perms);
    }
    return authInfo;
  }

  /**
   * Keep our principals up to date with any changes since we last loaded them. (including roles/permissions)
   *
   * @param principals The principles currently held by our shiro session.
   */
  private void refresh(PrincipalCollection principals) {

    Map<AppUser, String> realmsForOldUsers = new HashMap<>();
    Map<AppUser, AppUser> newForOld = new HashMap<>();
    // must synchronize to avoid concurrent modification issues.
    synchronized (principals) {
      //noinspection unchecked
      principals.getRealmNames()
          .forEach(r -> principals.fromRealm(r).forEach(p -> {
            AppUser freshUser = loadUser(((AppUser) p).getId());
            newForOld.put((AppUser) p, freshUser);
            realmsForOldUsers.put((AppUser) p, r);
          }));

      List retainOrder = principals.asList();
      MutablePrincipalCollection mutable = (MutablePrincipalCollection) principals;
      mutable.clear();

      //noinspection unchecked,SuspiciousMethodCalls
      retainOrder.forEach(old -> mutable.add(newForOld.get(old), realmsForOldUsers.get(old)));
    }
  }

  private AppUser loadUser(Long id) {
    EntityManager entityManager = entityManagerProvider.get();
    return entityManager.createQuery("from AppUser where id=:id", AppUser.class)
        .setParameter("id", id)
        .getSingleResult();
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

    String primaryPrincipal = token.getPrincipal().toString();
    char[] credentialsAry = (char[]) token.getCredentials();
    String creds = new String(credentialsAry);

    EntityManager entityManager = entityManagerProvider.get();
    Query query = entityManager.createQuery(FROM_USER_WHERE_EMAIL_PRINCIPAL);
    query.setParameter("principal", primaryPrincipal);
    AppUser u = (AppUser) query.getSingleResult();
    try {
      if (BCrypt.checkpw(creds, u.getSecurityInfo().getPasswordHash())) {
        return new SimpleAuthenticationInfo(u, u.getSecurityInfo().getPasswordHash(), getName());
      } else {
        return null;
      }
    } finally {
      // interfere with timing attacks by returning on quarter second boundaries.
      long now = System.currentTimeMillis();
      try {
        Thread.sleep(250 - (now % 250));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}