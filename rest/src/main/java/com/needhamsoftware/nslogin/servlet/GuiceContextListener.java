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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.needhamsoftware.nslogin.PersistenceUtil;
import com.needhamsoftware.nslogin.guice.ObjectServiceWrapper;
import com.needhamsoftware.nslogin.hibernate.HibernateUtil;
import com.needhamsoftware.nslogin.service.MessageService;
import com.needhamsoftware.nslogin.service.ObjectService;
import com.needhamsoftware.nslogin.service.impl.MessageServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;

public class GuiceContextListener extends GuiceServletContextListener {


  private static Logger log = LogManager.getLogger();

  private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE
      = new ThreadLocal<>();

  @Inject
  private static ObjectService objectService;
  private ServletContext ctx;
  private Injector injector;


  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    super.contextInitialized(servletContextEvent);
    objectService.loadSystemUser();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    log.info("Destroying GUICE context");
    super.contextDestroyed(servletContextEvent);
    EntityManagerFactory entityManagerFactory = injector.getProvider(EntityManagerFactory.class).get();
    if (entityManagerFactory.isOpen()) {
      entityManagerFactory.close();
    }

    // do some brittle hacking to make up for the deficiencies in dependent libraries.
    try {
      Field localContext = injector.getClass().getDeclaredField("localContext");
      localContext.setAccessible(true);
      ThreadLocal guiceInjectorContext = (ThreadLocal) localContext.get(injector);
      applyToActiveThreads(
          getThreadLocalClearer(ENTITY_MANAGER_CACHE),
          getThreadLocalClearer(guiceInjectorContext),
          getGuiceThreadCleanup()
      );
    } catch (NoSuchFieldException | IllegalAccessException e) {
      log.error(e);
      e.printStackTrace();
    }

    // avoid tomcat messages about jdbc drivers...
    unregisterJdbcDrivers();
    ctx = null;

    log.info("GUICE Context Destroyed");
  }

  @Override
  protected Injector getInjector() {
    if (injector == null) {
      injector = Guice.createInjector(
          new ServletModule() {

            @Override
            protected void configureServlets() {
              bind(PersistenceUtil.class).to(HibernateUtil.class);
              bind(ObjectService.class).to(ObjectServiceWrapper.class);
              bind(MessageService.class).to(MessageServiceImpl.class);

              // do our static injections before we serve up any requests
              requestStaticInjection(GuiceContextListener.class);
              requestStaticInjection(Messages.class);

              super.configureServlets();
              install(new JpaPersistModule("app"));
              // working around https://github.com/google/guice/issues/598
              final InjectionListener<PersistService> injectionListener = PersistService::start;
              TypeListener persistServiceListener = new TypeListener() {
                public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                  if (PersistService.class.isAssignableFrom(type.getRawType())) {
                    @SuppressWarnings("unchecked")
                    TypeEncounter<PersistService> disposableEncounter = (TypeEncounter<PersistService>) encounter;
                    disposableEncounter.register(injectionListener);
                  }
                }
              };

              bindListener(new AbstractMatcher<>() {
                @Override
                public boolean matches(TypeLiteral<?> typeLiteral) {
                  return PersistService.class.isAssignableFrom(typeLiteral.getRawType());
                }
              }, persistServiceListener);

              serve("/*").with(RestServlet.class);


              serve("/messages/*").with(PendingNotificationsServlet.class);

              filter("/*").through(PersistFilter.class);
              filter("/*").through(UserFilter.class);
              filter("/*").through(MessageCollectionFilter.class);
              filter("/msocket/*").through(WebSocketFilter.class);
            }

          });
    }
    return injector;

  }

  private PerThreadAction getThreadLocalClearer(final ThreadLocal<EntityManager> threadLocal) {
    return t -> {
      try {
        Field threadLocals = Thread.class.getDeclaredField("threadLocals");
        threadLocals.setAccessible(true);
        Object o = threadLocals.get(t);
        if (o != null) {
          Method remove = o.getClass().getDeclaredMethod("remove", ThreadLocal.class);
          remove.setAccessible(true);
          remove.invoke(o, threadLocal);
        }
      } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        // we are un-deploying anyway so not much point in doing anything other than complaining.
        e.printStackTrace();
      }
    };
  }

  private PerThreadAction getGuiceThreadCleanup() {
    return t -> {
      String name = t.getName();
      log.trace("inspecting {}", name);
      if (name.matches("com\\.google\\.inject\\.internal\\.util\\.\\$Finalizer.*")) {
        log.info("Stopping thread:{}", t);
        //noinspection deprecation
        t.stop(); // don't care we're shutting down.
      }
    };
  }

  private void unregisterJdbcDrivers() {
    System.out.flush();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    // Loop through all drivers
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      if (driver.getClass().getClassLoader() == cl) {
        // This driver was registered by the webapp's ClassLoader, so deregister it:
        try {
          log.info("Unregistering JDBC driver {}", driver);
          DriverManager.deregisterDriver(driver);
        } catch (SQLException ex) {
          log.error("Error Un-registering JDBC driver {}", driver, ex);
        }
      } else {
        // driver was not registered by the application's ClassLoader and may be in use elsewhere
        log.trace("Not un-registering JDBC driver {} as it does not belong to this application's ClassLoader", driver);
      }
    }
  }

  private void applyToActiveThreads(PerThreadAction... actions) {
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Thread thread : threadSet) {
      for (PerThreadAction action : actions) {
        action.doToThread(thread);
      }
    }
  }

  private interface PerThreadAction {
    void doToThread(Thread t);
  }


}