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

package com.needhamsoftware.nslogin.service.impl;

import com.copyright.easiertest.AnnotatedElementAction;
import com.copyright.easiertest.AnnotationUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.needhamsoftware.nslogin.FieldUtil;
import com.needhamsoftware.nslogin.PasswordStandards;
import com.needhamsoftware.nslogin.model.*;
import com.needhamsoftware.nslogin.service.Filter;
import com.needhamsoftware.nslogin.service.ObjectService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ObjectServiceImpl implements ObjectService {

  private static Logger log = LogManager.getLogger();

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private Provider<EntityManager> entityManagerProvider;
  private AppUser SYSTEM_USER;

  @Override
  public void loadSystemUser() {
    EntityManager entityManager = entityManagerProvider.get();

    String qlString = "from AppUser where id=:id";
    try {
      SYSTEM_USER = (AppUser) entityManager
          .createQuery(qlString)
          .setParameter("id", 1L)
          .getSingleResult();
    } catch (NoResultException e) {
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();
      SYSTEM_USER = new AppUser();
      SYSTEM_USER.setId(1L);
      // this makes it impossible for users to log in as system so long as LoginServlet continues
      SYSTEM_USER.setUserEmail(" ");                                    // to reject blank emails
      SYSTEM_USER.setUsername("SYSTEM");
      UserSecurity security = new UserSecurity();
      // obviously one of the first tasks on deployment is to change this!
      security.setPasswordHash(PasswordStandards.makeHashPw("$ystem123ABC"));
      SYSTEM_USER = entityManager.merge(SYSTEM_USER);
      security = entityManager.merge(security);
      SYSTEM_USER.setSecurityInfo(security);
      entityManager.persist(security);
      entityManager.persist(SYSTEM_USER);
      try {
        tx.commit();
      } catch (RollbackException ex) {
        ex.printStackTrace();
        ex.getCause().printStackTrace();
        throw new RuntimeException("Transaction Rolled back due to:", ex.getCause());
      }
      entityManager.close();
    }
  }

  @Override
  public <T extends Persisted> T get(Class<T> clazz, Long identifier) {
    return get(clazz, identifier, false);
  }

  @Override
  public Persisted getFresh(Class<? extends Persisted> clazz, Long identifier, boolean privileged) {
    return get(clazz, identifier, true);
  }

  private <T extends Persisted> T get(Class<T> clazz, Long identifier, boolean fresh) {
    EntityManager entityManager = entityManagerProvider.get();

    String qlString = "from " + clazz.getName() +
        " where id=:id";
    Query q = entityManager
        .createQuery(qlString)
        .setParameter("id", identifier);
    if (fresh) {
      // this doesn't seem to be effective... not sure why
      q.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
    }

    List resultList = q.getResultList();
    if (resultList.size() > 1) {
      throw new PersistenceException("Non-unique ID for " + clazz);
    }
    //noinspection unchecked
    return resultList.size() == 1 ? (T) resultList.get(0) : null;
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows) {
    return list(clazz, start, rows, new ArrayList<>(), null);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, boolean privileged) {
    return list(clazz, start, rows, new ArrayList<>(), null, privileged);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts) {
    return list(clazz, start, rows, filters, sorts, false);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts, boolean privileged) {
    return list(clazz, start, rows, filters, sorts, privileged, false);
  }

  private <T extends Persisted> List<T> list(
      Class<T> clazz,
      int start, int rows,
      List<Filter> filters,
      List<String> sorts,
      boolean privileged,
      boolean fresh) {
    EntityManager entityManager = entityManagerProvider.get();
    TypedQuery<T> q;
    if (privileged) {
      q = buildQuery(clazz, filters, entityManager, sorts, false, clazz);
    } else {
      if (clazz.isAssignableFrom(UserSecurity.class) ||
          clazz.isAssignableFrom(AccountRequest.class)) {
        throw new SecurityException();
      }
      q = buildSecureQuery(clazz, filters, entityManager, sorts, false, clazz);
    }

    q.setMaxResults(rows);
    q.setFirstResult(start);
    if (fresh) {
      // this doesn't seem to always be effective, not sure why...
      q.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
    }
    return q.getResultList();
  }


  @Override
  public Long count(Class<? extends Persisted> clazz, List<Filter> filters) {
    return count(clazz, filters, false);
  }

  @Override
  public Long count(Class<? extends Persisted> clazz, List<Filter> filters, boolean privileged) {
    EntityManager entityManager = entityManagerProvider.get();
    TypedQuery<Long> q;
    if (privileged) {
      q = buildQuery(clazz, filters, entityManager, null, true, Long.class);
    } else {
      q = buildSecureQuery(clazz, filters, entityManager, null, true, Long.class);
    }
    return q.getSingleResult();
  }


  @Override
  @Transactional
  public Persisted insert(Persisted persisted) throws ObjectAlreadyHasIdException {
    if (persisted.getId() != null) {
      throw new ObjectAlreadyHasIdException("It is not permitted to specify the ID of a new object. Use update() for existing objects");
    }
    EntityManager entityManager = entityManagerProvider.get();
    AppUser actor;
    actor = SYSTEM_USER;
    log.debug("{} created by {}", persisted.getClass().getName(), actor);
    Instant now = Instant.now();
    persisted.setCreated(now);
    persisted.setModified(now);
    entityManager.persist(persisted);
    return persisted;
  }


  @Override
  @Transactional
  public Persisted update(Persisted persistMe) {

    log.debug("updating {}", persistMe);
    EntityManager entityManager = entityManagerProvider.get();
    //TODO: guard against sub-object user edits creation

    // collections and fields that are ignored during JSON
    // collections and fields that are ignored during JSON
    AnnotationUtil.doToAnnotatedElement(persistMe, new AnnotatedElementAction() {
      Persisted parent;

      @Override
      public void doTo(Field f, Annotation a) {
        if (Collection.class.isAssignableFrom(f.getType()) &&
            f.isAnnotationPresent(ManyToMany.class) ||
            f.isAnnotationPresent(OneToMany.class)) {
          if (parent == null) {
            parent = get(persistMe.getClass(), persistMe.getId());
          }
          try {
            f.setAccessible(true);
            Collection fromJson = (Collection) f.get(persistMe);
            Collection loaded = (Collection) f.get(parent);
            // make sure we don't miss any updates, note that we are not handling deletes here.
            for (Object j : fromJson) {
              if (!loaded.contains(j)) {
                //noinspection unchecked
                loaded.add(j);
              }
            }
            f.set(persistMe, loaded);

          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }, JsonIgnore.class);

    Instant now = Instant.now();
    persistMe.setModified(now);

    // new state introduced to the session here, hibernate will update DB if required
    return entityManager.merge(persistMe);

  }

  @Override
  public void delete(Class clazz, Long identifier) {
    //todo: softdelete (full delete usually wrong)
  }

  private <T extends Persisted, R> TypedQuery<R> buildQuery(
      Class<T> clazz,
      List<Filter> filters,
      EntityManager entityManager,
      List<String> sorts,
      boolean count,
      Class<R> retClazz) {

    StringBuilder qlString = new StringBuilder((count ? "select count(*) " : "") + "from " + clazz.getName());
    addFilters(filters, qlString, clazz);
    addSorts(sorts, qlString, clazz);
    TypedQuery<R> q = entityManager.createQuery(qlString.toString(), retClazz);
    applyParameterValues(filters, q);
    return q;
  }

  private <T extends Persisted, R> TypedQuery<R> buildSecureQuery(
      Class<T> clazz,
      List<Filter> filters,
      EntityManager entityManager,
      List<String> sorts,
      boolean count,
      Class<R> retClazz) {

    // TODO: Eventually this method also filters based on Authz info....

    StringBuilder qlString = new StringBuilder((count ? "select count(*) " : "") + "from " + clazz.getName());
    addFilters(filters, qlString, clazz);
    addSorts(sorts, qlString, clazz);
    TypedQuery<R> q = entityManager.createQuery(qlString.toString(), retClazz);
    applyParameterValues(filters, q);
    return q;
  }

  private void applyParameterValues(List<Filter> filters, TypedQuery q) {
    int count = 0;
    Set<Parameter<?>> parameters = q.getParameters();
    for (Filter filter : filters) {
      Object value = filter.getValue(this);
      int tmp = count;
      if (value != null && parameters.stream().anyMatch((parameter) -> ("f" + tmp).equals(parameter.getName()))) {
        q.setParameter("f" + count, value);
        count++;
      }
    }
  }

  private void addSorts(List<String> sorts, StringBuilder qlString, Class clazz) {
    if (sorts != null && sorts.size() > 0) {
      checkSorts(sorts, clazz);
      for (String sort : sorts) {
        qlString.append(" order by ").append(sort);
      }
    }
  }

  private void addFilters(List<Filter> filters, StringBuilder qlString, Class clazz) {
    if (filters != null && filters.size() > 0) {
      checkFilters(filters, clazz);
      qlString.append(" where");
      addFiltersRaw(filters, qlString, clazz);
    }
  }

  private void checkFilters(List<Filter> filters, Class clazz) {
    List<Filter> invalidFilters = new ArrayList<>(filters);
    new FieldUtil().doForEachAnnotatedField(clazz, RestFilterEnable.class, (f, o) ->
        invalidFilters.removeIf((filter) -> f.getName().equals(filter.getField())), null);
    if (invalidFilters.size() > 0) {
      throw new IllegalArgumentException("The following filters are not allowed:" + invalidFilters);
    }
  }

  private void checkSorts(List<String> sorts, Class clazz) {
    List<String> invalidSorts = sorts.stream().map(s -> s.split("\\s+")[0]).collect(Collectors.toList());
    new FieldUtil().doForEachAnnotatedField(clazz, null, (f, o) ->
        invalidSorts.removeIf((field) -> f.getName().equals(field)), null);
    if (invalidSorts.size() > 0) {
      throw new IllegalArgumentException("The following filters are not allowed:" + invalidSorts);
    }
  }

  private void addFiltersRaw(List<Filter> filters, StringBuilder qlString, Class clazz) {
    checkFilters(filters, clazz);

    int[] count = {0};
    List<String> clauses = filters.stream().map(f -> {
      if (f.getValue(this) == null) {
        if ("=".equals(f.getOperator())) {
          return f.getField() + " is null";
        }
        if ("!=".equals(f.getOperator())) {
          return f.getField() + " is not null";
        }
        throw new PersistenceException("Tried to filter " + f.getField() + f.getOperator() + "null which makes no sense!");
      } else {
        return f.getField() + " " + f.getOperator() + " :f" + count[0]++;
      }
    }).collect(Collectors.toList());
    qlString.append(" ").append(StringUtils.join(clauses, " and "));
  }


}
