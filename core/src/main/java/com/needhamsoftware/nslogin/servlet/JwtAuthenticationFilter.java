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
import com.needhamsoftware.nslogin.AuthzException;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("UnnecessaryReturnStatement")
public class JwtAuthenticationFilter implements Filter, LoginConstants {

  private static final Logger log = LogManager.getLogger();
  private static final String ADMINISTRATOR = "Please contact your system administrator for assistance with your account.";
  private static final String TOKEN = "com.needhamsoftware.nslogin.jwt";
  private static final String CLAIMS = "com.needhamsoftware.nslogin.jwt.claims";

  private URL keyFetchUrl; // the url to the login servlet with a parameter that requests the public key for the given id
  private boolean redirectToLogin;

  private LoadingCache<String, byte[]> keyCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(
      new CacheLoader<>() {
        @Override
        public byte[] load(String key) throws Exception {
          URL u = new URL(keyFetchUrl + key); // expects the url ends with  kid= and that we get a valid url
          Content content = Request.Get(u.toExternalForm())
              .connectTimeout(100000)
              .socketTimeout(100000)
              .execute().returnContent();
          return content.asBytes();
        }
      }
  );

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
      this.keyFetchUrl = new URL(filterConfig.getInitParameter("keyFetchUrl"));
      this.redirectToLogin = Boolean.parseBoolean(filterConfig.getInitParameter("redirectToLogin"));
    } catch (MalformedURLException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    // Security strategy: We trust our session information, so it's important that we don't
    // write a principle into the session unless we actually verify it. Once the user is in,
    // they are in until the session expires.

    ///////////////////////////////////////////////////////////////////////////////////
    //                                                                               //
    // HTTPS is REQUIRED otherwise this is easily attacked with a man-in the middle! //
    //                                                                               //
    //       (and session cookie stealing and stealing the JWT token etc etc)        //
    //                                                                               //
    ///////////////////////////////////////////////////////////////////////////////////


    if (!(request instanceof HttpServletRequest)) {
      // send blank response if request wasn't http some how.
      response.getWriter().println();
      response.flushBuffer();
      return;
    }
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    // First order of business, ensure that only css and js resources etc are cached, otherwise the
    // Browser at a public web terminal may show content cached from a previous user and
    // never even consult us about it!

    String requestURI = req.getRequestURI();
    // if you have some bizarre affinity for upper case file extensions add them yourself...
    // also if you have other types of files that need to be cached by the browser...
    if (!requestURI.endsWith(".js") &&
        !requestURI.endsWith(".css") &&
        !requestURI.endsWith(".jpg") &&
        !requestURI.endsWith(".jpeg") &&
        !requestURI.endsWith(".png")) { // don't cache
          // Credits: https://stackoverflow.com/questions/49547/how-do-we-control-web-page-caching-across-all-browsers
          resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
          resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
          resp.setHeader("Expires", "0"); // Proxies.
        }  // ok to cache


    // are we logging out?
    String logout = req.getParameter("logout");
    boolean loggingOut = false;
    if (logout != null) {
      logout(req);
      loggingOut = true;
    }

    // Did we just finish login?
    String token = req.getParameter(X_JWT_TOKEN); // did we just log in?

    // Use back to cookie if it exists
    if (token == null && !loggingOut) {
      Cookie[] cookies = req.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (X_JWT_TOKEN.equals(cookie.getName())) {
            token = cookie.getValue();
          }
        }
      }
    }

    HttpSession session = req.getSession();

    Object sessionToken = session.getAttribute(TOKEN);
    if (token == null || sessionToken != null && !token.equals(sessionToken)) {
      // User has logged out, new user logged in need a new session
      logout(req);
      session = req.getSession();
    }

    Object email = session.getAttribute(PRINCIPAL);

    // are we already authenticated, and have a principle?
    // note the token check is to avoid polluting the URL with the JWT token.
    // if we see the token process it and redirect to get rid of it.

    /* ****************************************************************** */
    if (LOGIN_ACCEPTED(request, response, chain, session, email)) return;
    /* ****************************************************************** */

    // If we get here, something was missing, need to to reset everything and either redirect
    // or try again, depending on whether or not we are meant to redirect

    if (token == null) {
      // no soup for you, talk to the hand.
      String returnPath = req.getRequestURL().toString();

      URIBuilder builder;
      try {
        // this try/catch block can be removed it there's no desire to support
        // incoming requests with query parameters from users who have been
        // logged out and then click on the user interface.
        builder = new URIBuilder(returnPath);
        //builder.addParameter("_","" + System.currentTimeMillis());
        // keep params if we came here via a get, toss them for post since that's a
        // potentially write/update operation from a user not logged in (danger!)
        if (!"post".equalsIgnoreCase(req.getMethod())) {
          for (String p : req.getParameterMap().keySet()) {
            if ("logout".equals(p)) {
              continue; // otherwise we can get logged out as soon as we log in.
            }
            String[] values = req.getParameterMap().get(p);
            for (String val : values) {
              builder.addParameter(p, val);
            }
          }
        }
        returnPath = builder.build().toString();
      } catch (URISyntaxException e) {
        // should never happen we got it from the request.
        // if it does it might be a malicious set of query parameters so just
        // continue without parameters.
      }

      // without this check the user can get redirected to favicon.ico after logging in!
      if (returnPath.contains("favicon.ico")) {
        ((HttpServletResponse) response).setHeader("Cache-Control", "no-store");
        ((HttpServletResponse) response).sendError(404); // for now just fail, browser can collect it after login
        return;
      }

      session.setAttribute(X_LOGIN_RETURN_TO, returnPath);
      Cookie jwt = new Cookie(X_JWT_TOKEN, "");
      jwt.setMaxAge(0); // cookie to expire before session
      resp.addCookie(jwt);
      if (redirectToLogin) {
        resp.sendRedirect("/login/?from=" + req.getRequestURL());
      } else {
        resp.sendError(401);
      }
      return;
    }

    // Just returned from log-in, or first time arriving with cookie so we need to (re)set the principle
    try {
      // important not to do anything to the user's session until they are validated by this next
      // line, otherwise the user might be affected by the actions of an unauthenticated user.
      Jws<Claims> claimsJws = checkToken(token);

      // No Exception thrown so this token came from the service that was configured as our login url so
      // now we can trust it's claim about who the logged in user is. At this point we will now trust the user
      // until their J2EE session expires or they manually log out which invalidates the session.
      email = claimsJws.getBody().getSubject();
      session.setAttribute(PRINCIPAL, email);
      session.setAttribute(TOKEN, token);
      session.setAttribute(CLAIMS, claimsJws.getBody());
      String originalDestination = (String) session.getAttribute(X_LOGIN_RETURN_TO);
      session.removeAttribute(X_LOGIN_RETURN_TO);
      if (StringUtils.isBlank(originalDestination)) {
        originalDestination = requestURI;
      }
      Cookie uid = new Cookie("nslogin-uid", claimsJws.getBody().getSubject());
      uid.setPath("/");
      uid.setMaxAge(session.getMaxInactiveInterval() - 5); // cookie to expire before session
      resp.addCookie(uid);
      Cookie jwt = new Cookie(X_JWT_TOKEN, token);
      jwt.setMaxAge(session.getMaxInactiveInterval() - 5); // cookie to expire before session
      jwt.setPath("/");
      resp.addCookie(jwt);
      if (redirectToLogin) {
        resp.sendRedirect(originalDestination); // finally go to the original url with query params restored
      } else {

        /* ****************************************************************** */
        if (LOGIN_ACCEPTED(request, response, chain, session, email)) {
          return;
        } else  {
          throw new RuntimeException();
        }
        /* ****************************************************************** */

      }
    } catch (Exception e) {
      // reveal nothing
      log.error(e);
      errorToLogin(resp);
    }
  }

  /**
   * This is the critical bit. Call this when we think we are ready to log in.
   */
  private boolean LOGIN_ACCEPTED(ServletRequest request, ServletResponse response, FilterChain chain, HttpSession session, Object email) throws IOException, ServletException {
    Object claims = session.getAttribute(CLAIMS);
    Object attribute = session.getAttribute(TOKEN);
    if (email != null && attribute != null && claims != null) {
      try {
        proceed(request, response, chain, claims);
      } catch (AuthzException e) {
        log.fatal("User {} is authenticating successfully but un authorized to load themselves from the database", email);
      }
      return true;
    }
    return false;
  }

  protected void logout(HttpServletRequest req) {
    req.getSession().invalidate();
  }

  /**
   * Override this in sub classes that need to do additional configuration of Authorization infrastructure.
   * One example might involve setting the subject for shiro...
   */
  protected void proceed(ServletRequest request, ServletResponse response, FilterChain chain, Object principal) throws IOException, ServletException, AuthzException {
    chain.doFilter(request, response);
  }

  private Jws<Claims> checkToken(String token) {
    return Jwts.parser()
        .setSigningKeyResolver(new SigningKeyResolverAdapter() {

          @Override
          public Key resolveSigningKey(JwsHeader header, Claims claims) {
            try {
              byte[] keyBytes = resolveSigningKeyBytes(header, claims);
              KeyFactory keyFactory = KeyFactory.getInstance(LoginConstants.SIGNATURE_ALGORITHM.getFamilyName());
              EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
              return keyFactory.generatePublic(publicKeySpec);
            } catch (Exception e) {
              throw new IllegalArgumentException(e);
            }
          }

          @Override
          public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
            try {
              return keyCache.get(header.getKeyId());
            } catch (ExecutionException e) {
              // this should never fail unless the URL is bad this is very very bad
              // since it means nobody can log in.
              log.fatal("Invalid URL for login service!!");
              return new byte[]{};
            }
          }
        })
        // blow up if the token didn't come from us! This is verified when the
        // decryption yields non-garbage, and claims with this value were therefore
        // successfully encrypted by the private key associated with the public key
        // retrieved from our login service by the loading cache above.
        .requireIssuer(ISSUER)
        .parseClaimsJws(token);
  }

  private void errorToLogin(HttpServletResponse resp) throws IOException {
    resp.setHeader(X_ERROR_MESSAGE, ADMINISTRATOR);
    resp.sendRedirect("/login/");
  }


  @Override
  public void destroy() {

  }
}
