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

import com.needhamsoftware.nslogin.model.AccountRequest;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.UserSecurity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewUserServlet extends HttpServlet implements LoginConstants {
  private static final Logger log = LogManager.getLogger();

  @PersistenceUnit(unitName = "app")
  private EntityManagerFactory emf;


  private Session session;
  private String fromAddr = "no-reply@example.com";
  private String subject = "New Account Requested";
  private String returnUrl = "http://localhost:8080/newuser/confirmation?token=";

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // make sure we have a database connection and it doesn't error out.
    emf = Persistence.createEntityManagerFactory("app");

    Context initCtx;
    try {
      initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      session = (Session) envCtx.lookup("mail/Session");
    } catch (NamingException e) {
      e.printStackTrace();
      throw new ServletException(e);
    }

    String from = getServletContext().getInitParameter("fromAddr");
    if (StringUtils.isNotBlank(from)) {
      fromAddr = from;
    }
    String subject = getServletContext().getInitParameter("subject");
    if (StringUtils.isNotBlank(from)) {
      this.subject = subject;
    }
    String returnUrl = getServletContext().getInitParameter("returnUrl");
    if (StringUtils.isNotBlank(from)) {
      this.returnUrl = returnUrl;
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    EntityManager em = emf.createEntityManager();
    try {
      EntityTransaction tx = em.getTransaction();
      tx.begin();
      String[] userNames = req.getParameterMap().get("username");
      String[] passwords = req.getParameterMap().get("password");
      String[] passwordConfirms = req.getParameterMap().get("passwordConfirm");
      String[] emails = req.getParameterMap().get("email");

      List<String> messages = new ArrayList<>();

      if (userNames == null || userNames.length != 1 || !isValidUsername(userNames[0])) {
        messages.add("Please provide a valid username");
      }

      if (passwords == null || passwords.length != 1 || !isValidPassword(passwords[0])) {
        messages.add("Please provide a valid password. One each of uppercase, " +
            "lowercase, number and one other character are required with a length of at least 8");
      }

      if (emails == null || emails.length != 1 || !isValidEmail(emails[0])) {
        messages.add("Please provide a valid email");
      }

      if (passwordConfirms == null || passwordConfirms.length != 1 ||
          (passwords != null && !passwordConfirms[0].equals(passwords[0]))) {
        messages.add("Please provide a matching password confirmation");
      }

      if (userNames != null && alreadyExists(userNames[0], em)) {
        messages.add("Someone already has that username");
      }

      if (emails != null && isEmailTaken(emails[0], em)) {
        messages.add("This email is already registered, only one account per email is allowed.");
      }

      if (messages.size() > 0) {
        error(req, resp, messages);
      } else {
        AccountRequest userRequest = new AccountRequest();
        // any of these nulls would result in a message and we wouldn't get here.
        assert userNames != null;
        assert emails != null;
        assert passwords != null;
        userRequest.setUsername(userNames[0]);
        userRequest.setUserEmail(emails[0]);
        UserSecurity newSecurity = new UserSecurity();
        userRequest.setSecurityInfo(newSecurity);
        newSecurity.setPasswordHash(BCrypt.hashpw(passwords[0], BCrypt.gensalt(10)));
        newSecurity.setResetToken(BCrypt.gensalt());
        newSecurity.setResetRequestedAt(Instant.now());
        em.persist(newSecurity);
        em.persist(userRequest);
        tx.commit();

        Message message = new MimeMessage(session);
        try {
          message.setFrom(new InternetAddress(fromAddr));
        } catch (MessagingException e) {
          log.error("FAIL: bad address for from address in new user email {}", fromAddr);
          error(req, resp, Collections.singletonList("Internal Error: Server Mail Configuration (mail not sent)"));
          return;
        }
        InternetAddress[] to = new InternetAddress[1];
        try {
          to[0] = new InternetAddress(userRequest.getUserEmail());
        } catch (AddressException e) {
          log.debug("bad address for 'to' address in new user email {}", userRequest.getUserEmail());
          error(req, resp, Collections.singletonList("Invalid email address"));
          return;
        }
        try {
          message.setRecipients(Message.RecipientType.TO, to);
          message.setSubject(subject);

          VelocityEngine ve = new VelocityEngine();
          ve.init();
          org.apache.velocity.context.Context context = new VelocityContext();
          context.put("returnUrl", returnUrl);
          context.put("resetToken", newSecurity.getResetToken());
          StringWriter emailWriter = new StringWriter();
          InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
              .getResourceAsStream("reg-email.vsl");
          if (resourceAsStream == null) {
            log.fatal("reg email template not found!");
            error(req, resp, Collections.singletonList("Internal Error: Could not generate email!"));
            return;
          }
          String template = new String(resourceAsStream.readAllBytes());

          ve.evaluate(context, emailWriter, "foo", template);

          log.trace(emailWriter.toString());
          message.setContent(emailWriter.toString(), "text/plain");
          Transport.send(message);
        } catch (MessagingException e) {
          log.error("FAIL: could not send new user email", e);
          error(req, resp, Collections.singletonList("Internal Error: Server Mail Configuration (mail not sent)"));
          return;
        }
        getServletContext().getRequestDispatcher("/email_sent.jsp").forward(req, resp);
      }
    } finally {
      em.close();
    }
  }

  private boolean isValidEmail(String email) {
    //Please read https://davidcel.is/posts/stop-validating-email-addresses-with-regex/

    // The registration process provides validation, this just helps for folks who fat fingered the @
    // the rest is up to them. We aren't creating the user until after confirmation so we don't need to
    // worry about perfection here. (hooray for not making work for ourselves!)
    return email.contains("@");
  }

  private boolean isEmailTaken(String email, EntityManager em) {
    TypedQuery<AppUser> query = em.createQuery(
        "SELECT a FROM AppUser a WHERE a.userEmail = :email", AppUser.class);
    return query.setParameter("email", email).getResultList().size() != 0;
  }

  private void error(HttpServletRequest req, HttpServletResponse resp, List<String> errors) throws ServletException, IOException {
    req.setAttribute("ERRORS", errors);
    getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
  }

  private boolean isValidPassword(String password) {
    // implement any rules (like complexity) here. By default upper/lower/decimal/other required
    return password.length() >= 8 &&
        password.matches(".*\\d.*") &&
        password.matches(".*[A-Z].*") &&
        password.matches(".*\\w.*") &&
        password.matches(".*\\W.*");
  }

  private boolean alreadyExists(String username, EntityManager em) {
    TypedQuery<AppUser> query = em.createQuery(
        "SELECT a FROM AppUser a WHERE a.username = :username", AppUser.class);
    return query.setParameter("username", username).getResultList().size() != 0;
  }

  private boolean isValidUsername(String username) {
    // implement any rules (like bad words) here
    return username != null;
  }
}
