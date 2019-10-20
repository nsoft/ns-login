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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.needhamsoftware.nslogin.model.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// using explicit returns in several places to ensure adding logic doesn't change the flow inadvertently
@SuppressWarnings("UnnecessaryReturnStatement")
public class LoginServlet extends HttpServlet implements LoginConstants {

  private static final Logger log = LogManager.getLogger();
  private static final String USER_NAME_OR_PASSWORD_INCORRECT = "Email or password incorrect.";
  private static final String LOGIN_FORM_EMAIL = "LOGIN_FORM_EMAIL";

  private volatile String lastKey;

  private LoadingCache<String, KeyPair> keyCache = CacheBuilder.newBuilder().expireAfterWrite(LoginConstants.KEY_CHANGE_SECONDS, TimeUnit.SECONDS).build(
      new CacheLoader<>() {
        @Override
        public KeyPair load(String key) {
          // fresh key every 30 min
          lastKey = key;
          KeyPair keyPair = Keys.keyPairFor(LoginConstants.SIGNATURE_ALGORITHM);
          oldKeys.put(key, keyPair);
          return keyPair;
        }
      }
  );

  private LoadingCache<String, KeyPair> oldKeys = CacheBuilder.newBuilder().expireAfterWrite(LoginConstants.KEY_EXPIRE_SECONDS, TimeUnit.SECONDS).build(
      new CacheLoader<>() {
        @Override
        public KeyPair load(String key) {
          throw new InvalidCacheLoadException("Public Key for kid=" + key + " not found");
        }
      }
  );

  @PersistenceUnit(unitName = "app")
  private EntityManagerFactory emf;

  private Executor ex = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
  });

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // make sure we have a database connection and it doesn't error out.
    emf = Persistence.createEntityManagerFactory("app");
    EntityManager em = emf.createEntityManager();
    Query query = em.createQuery("select count(*) from AppUser");
    Object singleResult = query.getSingleResult();
    log.info("FOUND {} user records on startup", singleResult);
    ex.execute(() -> {
      while (true) {
        boolean empty = keyCache.asMap().keySet().isEmpty();
        if (empty) {
          try {
            // we uses a random UUID to make it very difficult for an attacker to
            // predict and fish for the current list of keys
            keyCache.get(UUID.randomUUID().toString());
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          break;
        }
      }
    });
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Map<String, String[]> parameterMap = req.getParameterMap();
    String[] kid = parameterMap.get("kid");
    if (haveNone(kid)) {
      // assume this is an actual login attempt get not allowed in this case
      String method = req.getMethod();
      if ("GET".equalsIgnoreCase(method)) {
        super.doPost(req, resp); // will send method not allowed.
        return;
      }

      // who do they claim to be (extra values will be ignored)
      String[] uEmails = req.getParameterMap().get("email");
      String[] passwords = req.getParameterMap().get("password");
      if (haveNone(uEmails) || haveNone(passwords)) {
        error(req, resp, USER_NAME_OR_PASSWORD_INCORRECT);
        return;
      } else {
        EntityManager em = emf.createEntityManager();
        req.setAttribute(LOGIN_FORM_EMAIL, uEmails[0]);        // Check if we know of such an individual in the database.
        TypedQuery<AppUser> query = em.createQuery("select u from AppUser u where u.userEmail = :userEmail", AppUser.class);
        query.setParameter("userEmail", uEmails[0]);
        List<AppUser> resultList = query.getResultList();
        if (resultList.size() > 1) {
          // multiple users with the same email, we're hosed, flame the dev who wrote that the bug
          // that removed the db constraint...
          resp.sendError(500, "Internal server error, Please contact support. Dev=ID1075");
        }
        if (resultList.size() == 0) {
          error(req, resp, USER_NAME_OR_PASSWORD_INCORRECT);
          return;
        }
        AppUser user = resultList.get(0);
        String passwordHash = user.getSecurityInfo().getPasswordHash();
        if (passwordHash == null || !checkPw(passwords[0], passwordHash)) {
          if (passwordHash == null) {
            log.error("User with null for password hash = " + user.getUserEmail());
          }
          error(req, resp, USER_NAME_OR_PASSWORD_INCORRECT);
          return;
        } else {
          // generate JWT
          KeyPair keyPair;
          try {
            final String lk = this.lastKey; // only access this once to avoid race condition!
            keyPair = keyCache.get(lk);
            String jws = Jwts.builder()
                .setIssuer(ISSUER)
                .setHeaderParam("kid", lk)
                .setSubject(user.getUsername()) // can never be something not in our database
                .signWith(keyPair.getPrivate())
                .compact();
            HttpSession session = req.getSession();
            String returnPath = (String) session.getAttribute(X_LOGIN_RETURN_TO);
            if (returnPath == null) {
              returnPath = "/"; // main application assumed to be deployed as ROOT. avoid NPE below.
            }
            try {
              URIBuilder builder = new URIBuilder(returnPath).addParameter(X_JWT_TOKEN, jws);
              returnPath = builder.build().toString();
            } catch (URISyntaxException e) {
              e.printStackTrace();
            }
            session.removeAttribute(X_LOGIN_RETURN_TO);
            resp.sendRedirect(returnPath);
            return;
          } catch (ExecutionException e) {
            log.error(e);
            // never show errors relating to keys/crypto
            error(req, resp, USER_NAME_OR_PASSWORD_INCORRECT);
            return;
          }
        }
      }
    } else {
      // this is a request for a public key for verifying our token.
      try {

        KeyPair keyPair = oldKeys.get(kid[0]);
        if (keyPair == null) {
          resp.sendError(410);
          return;
        }
        byte[] encoded = keyPair.getPublic().getEncoded();
        resp.getOutputStream().write(encoded);

      } catch (ExecutionException e) {
        throw new ServletException(e);
      } catch (UncheckedExecutionException e) {
        if (e.getCause() instanceof CacheLoader.InvalidCacheLoadException) {
          log.error("Invalid Key ID:" + kid[0], e);
          req.getRequestDispatcher("/error.jsp").forward(req, resp);
          return;
        }
        throw new ServletException(e);
      }
    }
  }

  private boolean checkPw(String password, String passwordHash) {
    return BCrypt.checkpw(password, passwordHash);
  }

  @SuppressWarnings("SameParameterValue")
  private void error(HttpServletRequest req, HttpServletResponse resp, String error) throws ServletException, IOException {
    req.setAttribute("ERRORS", Collections.singletonList(error));
    getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
  }

  private boolean haveNone(String[] uNames) {
    return uNames == null || StringUtils.isBlank(uNames[0]);
  }
}
