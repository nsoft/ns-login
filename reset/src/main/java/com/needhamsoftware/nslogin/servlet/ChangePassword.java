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

import com.needhamsoftware.nslogin.PasswordStandards;
import com.needhamsoftware.nslogin.model.AppUser;
import com.needhamsoftware.nslogin.model.UserSecurity;

import javax.persistence.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangePassword extends HttpServlet implements LoginConstants {

  @PersistenceUnit(unitName = "app")
  private EntityManagerFactory emf;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // make sure we have a database connection and it doesn't error out.
    emf = Persistence.createEntityManagerFactory("app");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    EntityManager em = emf.createEntityManager();
    try {
      String[] passwords = req.getParameterMap().get("password");
      String[] passwordConfirms = req.getParameterMap().get("passwordConfirm");
      String[] tokens = req.getParameterMap().get("token");

      List<String> messages = new ArrayList<>();

      if (passwords == null || passwords.length != 1 || PasswordStandards.isInvalid(passwords[0])) {
        messages.add("Please provide a valid password. One each of uppercase, " +
            "lowercase, number and one other character are required with a length of at least 8");
      }

      if (passwordConfirms == null || passwordConfirms.length != 1 ||
          (passwords != null && !passwordConfirms[0].equals(passwords[0]))) {
        messages.add("Please provide a matching password confirmation");
      }
      if (messages.size() > 0) {
        if (tokens != null) {
          req.setAttribute("RESET_FORM_TOKEN", tokens[0]);
        }
        error(req, resp, messages);
        return;
      }
      assert passwords != null;
      if (tokens != null && tokens.length == 1) {
        TypedQuery<UserSecurity> query = em.createQuery(
            "SELECT s FROM UserSecurity s WHERE s.resetToken = :token and s.passwordHash is null", UserSecurity.class);
        List<UserSecurity> requests = query.setParameter("token", tokens[0]).getResultList();
        if (requests.size() != 1) {
          error(req, resp, Collections.singletonList("Token Error: Invalid Token"));
          return;
        }
        UserSecurity request = requests.get(0);
        if (request.getCreated().isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
          error(req, resp, Collections.singletonList("Token Error: Expired (1 hr time limit)"));
          return;
        }

        // if we get here, the token matches a request, and the password is acceptable
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        AppUser user = request.getForUser();
        request.setResetToken(null);
        request.setPasswordHash(PasswordStandards.makeHashPw(passwords[0]));
        user.setSecurityInfo(request);
        em.merge(user);
        tx.commit();
        resp.sendRedirect("/login/");

      } else {
        error(req, resp, Collections.singletonList("Token Error: Invalid Token"));
      }
    } finally {
      em.close();
    }
  }

  private void error(HttpServletRequest req, HttpServletResponse resp, List<String> errors) throws ServletException, IOException {
    req.setAttribute("ERRORS", errors);
    getServletContext().getRequestDispatcher("/confirm.jsp").forward(req, resp);
  }

}
