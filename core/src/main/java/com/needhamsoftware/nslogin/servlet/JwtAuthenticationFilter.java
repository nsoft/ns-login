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
import io.jsonwebtoken.*;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
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

  private URL keyFetchUrl; // the url to the login servlet with a parameter that requests the public key for the given id

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

    // are we logging out?
    String logout = req.getParameter("logout");
    if (logout != null) {
      req.getSession().invalidate();
    }

    // Did we just finish login?
    String token = req.getParameter(X_JWT_TOKEN); // did we just log in?

    // are we already authenticated, and have a principle?
    // note the token check is to avoid polluting the URL with the JWT token.
    // if we see the token process it and redirect to get rid of it.
    HttpSession session = req.getSession();
    if (session.getAttribute(PRINCIPAL) != null && token == null) {
      chain.doFilter(request, response);
      return;
    }

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
      resp.sendRedirect("/login/?from=" + req.getRequestURL());
      return;
    }

    // Just returned from log-in so we need to (re)set the principle
    try {
      // important not to do anything to the user's session until they are validated by this next
      // line, otherwise the user might be affected by the actions of an unauthenticated user.
      Jws<Claims> claimsJws = checkToken(token);

      // No Exception thrown so this token came from the service that was configured as our login url so
      // now we can trust it's claim about who the logged in user is. At this point we will now trust the user
      // until their J2EE session expires or they manually log out which invalidates the session.
      session.removeAttribute(PRINCIPAL);
      session.setAttribute(PRINCIPAL, claimsJws.getBody().getSubject());
      session.setAttribute("com.needhamsoftware.nslogin.jwt", token);
      String originalDestination = (String) session.getAttribute(X_LOGIN_RETURN_TO);
      session.removeAttribute(X_LOGIN_RETURN_TO);
      resp.sendRedirect(originalDestination); // finally go to the original url with query params restored
      return;
    } catch (IllegalArgumentException | JwtException e) {
      // reveal nothing
      log.error(e);
      errorToLogin(resp);
      return;
    }
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
