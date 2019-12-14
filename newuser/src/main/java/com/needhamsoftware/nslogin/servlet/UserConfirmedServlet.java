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
import com.needhamsoftware.nslogin.model.Permission;
import org.mindrot.jbcrypt.BCrypt;

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

public class UserConfirmedServlet extends HttpServlet implements LoginConstants {

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
    doGet(req,resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    EntityManager em = emf.createEntityManager();
    try {
      String[] passwords = req.getParameterMap().get("confirmPassword");
      String[] tokens = req.getParameterMap().get("token");

      if (passwords == null || passwords.length != 1) {
        error(req, resp, Collections.singletonList("Please enter the password supplied when you requested this account."));
        return;
      }

      if (tokens != null && tokens.length == 1) {
        TypedQuery<AccountRequest> query = em.createQuery(
            "SELECT c FROM AccountRequest c WHERE c.securityInfo.resetToken = :token", AccountRequest.class);
        List<AccountRequest> requests = query.setParameter("token", tokens[0]).getResultList();
        if (requests.size() != 1) {
          error(req, resp, Collections.singletonList("Token Error: Invalid Token"));
          return;
        }
        AccountRequest request = requests.get(0);
        if (request.getCreated().isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
          error(req, resp, Collections.singletonList("Token Error: Expired (1 hr time limit)"));
          return;
        }

        if (BCrypt.checkpw(passwords[0], request.getSecurityInfo().getPasswordHash())) {
          EntityTransaction tx = em.getTransaction();
          tx.begin();
          AppUser newUser = new AppUser(request);
          em.persist(newUser);
          Permission selfControl = new Permission();
          selfControl.setType("AppUser");
          selfControl.setAction("*");
          selfControl.setObjId(String.valueOf(newUser.getId()));
          em.persist(selfControl);
          newUser.setIntrinsicPermissions(new ArrayList<>());
          newUser.getIntrinsicPermissions().add(selfControl);
          tx.commit();
          resp.sendRedirect("/login/");
        } else {
          error(req, resp, Collections.singletonList("Password mismatch"));
        }
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
