package com.needhamsoftware.nslogin.servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.needhamsoftware.nslogin.AuthzException;
import com.needhamsoftware.nslogin.FieldUtil;
import com.needhamsoftware.nslogin.model.ActionInvocation;
import com.needhamsoftware.nslogin.model.Persisted;
import com.needhamsoftware.nslogin.model.RestFilterEnable;
import com.needhamsoftware.nslogin.model.Validatable;
import com.needhamsoftware.nslogin.service.Filter;
import com.needhamsoftware.nslogin.service.ObjectService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.hibernate.exception.ConstraintViolationException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.*;
import java.util.*;

@SuppressWarnings("CdiInjectionPointsInspection")
@javax.servlet.annotation.WebServlet(name = "RestServlet")
@Singleton
public class RestServlet extends javax.servlet.http.HttpServlet {
  private static Logger log = LogManager.getLogger();

  @Inject
  private ObjectService objectService;
  @Inject
  private ObjectMapper mapper;

  private FieldUtil util = new FieldUtil();

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    ObjectReference ref = new ObjectReference(pathInfo);
    // TODO: Multi-Post (more than one object at a time)
    if (ref.isValid()) {
      Persisted p;
      try {
        String JSOG;
        try(java.util.Scanner s = new java.util.Scanner(req.getInputStream())) {
          JSOG = readAll(s);
        }
        Map<String,Object> m = mapper.readValue(JSOG, new TypeReference<Map<String, Object>>() {});
        if (Validatable.class.isAssignableFrom(ref.getType())) {
          //noinspection unchecked
          ((Validatable) ref.getType().getConstructor().newInstance()).validateMap(m);
        }
        if (Messages.DO.errorCount() != 0) {
          handleError(resp, 400);
          return;
        }
        p = (Persisted) mapper.readValue(JSOG, Class.forName(ref.getType().getCanonicalName()));
        Long id = ref.getId();
        if (id != null) {
          p.setId(id);
        } else {
          Messages.DO.sendErrorMessage("Can't create new objects with POST, use PUT");
        }
        if (p instanceof Validatable && !((Validatable)p).isValidated()) {
          ((Validatable) p).validate();
        }
        if (Messages.DO.errorCount() != 0) {
          handleError(resp, 400);
        } else {
          invokeActions(req,resp,p);
          Persisted updated = objectService.update(p);
          success(resp, null);
        }
      } catch (ClassNotFoundException | ClassCastException e) {
        resp.addHeader("NSL-Unknown-Type", ref.toString());
        e.printStackTrace();
        Messages.DO.exception(e, log);
        handleError(resp, 400);
      } catch (OptimisticLockException e) {
        resp.addHeader("NSL-Optimistic-Lock-Failure", ref.toString());
        Messages.DO.sendErrorMessage("Someone (or something) has made a conflicting change while you were working. Please refresh the page to load the new edits and retry your submission.");
        handleError(resp, 400);
      } catch (AuthzException e) {
        resp.addHeader("NSL-Authz-Failure", SecurityUtils.getSubject().toString());
        log.debug(e);
        Messages.DO.sendErrorMessage("Insufficient Access Rights");
        handleError(resp, 403);
      } catch (Exception e) {
        resp.addHeader("NSL-Unexpected-Exception", e.getMessage() + "(" + ref + ")");
        Messages.DO.sendErrorMessage("Internal Error:" + e.getMessage());
        log.error("Unexpected Exception!", e);
        handleError(resp,500);
      }
    }
  }

  private String readAll(Scanner s) {
    return s.useDelimiter("\\A").hasNext() ? s.next() : "";
  }

  private void handleError(HttpServletResponse resp, int code) throws IOException {
    ServletUtils.handleError(resp, code, mapper);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String pathInfo = req.getPathInfo();
      if (pathInfo.startsWith("/js/")) {
        String contextPath = getServletContext().getRealPath(File.separator);

        File jsFile = new File(contextPath, pathInfo);
        resp.setContentType("text/javascript");
        resp.setContentLength((int) jsFile.length());

        FileInputStream fileInputStream = new FileInputStream(jsFile);
        OutputStream respOutputStream = resp.getOutputStream();
        int bytes;
        while ((bytes = fileInputStream.read()) != -1) {
          respOutputStream.write(bytes);
        }
        return;
      }
      ObjectReference ref = new ObjectReference(pathInfo);
      if (!ref.isValid()) {
        Messages.DO.sendErrorMessage("Invalid reference:" + ref);
        handleError(resp, 400);
        return;
      }
      Set<String> params = req.getParameterMap().keySet();
      List<Filter> filters = new ArrayList<>();

      // IMPORTANT! This check guards against injection attacks! If removed or carelessly modified
      // then arbitrary filter names can be included in jpa queries, meaning it would be possible
      // to execute arbitrary JPA queries. This section limits the "injectable" strings to the
      // names of fields the referenced class from our object model.
      util.doForEachAnnotatedField(ref.getType(), RestFilterEnable.class, (f, o) -> {
        if (params.contains(f.getName())) {
          filters.add(new ObjectPropertyFilter(f.getName(), req.getParameter(f.getName()), f));
        }
      }, null);
      // END injection attack protection.

      if (ref.isValid()) {
        if (ref.getId() != null) {
          @SuppressWarnings("unchecked")
          Persisted obj = objectService.get(ref.getType(), ref.getId());
          invokeActions(req,resp,obj);
          success(resp, 1L, obj);
        } else {
          String startParam = req.getParameter("start");
          String rowsParam = req.getParameter("rows");
          int start = startParam != null ? Integer.parseInt(startParam) : 0;
          int rows = rowsParam != null ? Integer.parseInt(rowsParam) : 0;
          String sortStr = req.getParameter("sort");
          List<String> sorts = null;
          if (sortStr != null) {
            sorts = parseSorts(sortStr, ref);
          }
          @SuppressWarnings("unchecked")
          List<Persisted> objects = objectService.list(ref.getType(), start, rows, filters, sorts);
          @SuppressWarnings("unchecked")
          Long numFound = objectService.count(ref.getType(), filters);
          success(resp, numFound, objects.toArray());
        }
      } else {
        log.error("invalid object reference: " + ref);
        handleError(resp, 404);
      }
    } catch (NumberFormatException nfe) {
      Messages.DO.exception(nfe, log);
      handleError(resp, 400);
      log.debug("NFE:",nfe);
    } catch(SecurityException | AuthzException e) {
      log.debug(e);
      Messages.DO.sendErrorMessage("Insufficient Access Rights");
      handleError(resp, 403);
    } catch (Exception e) {
      e.printStackTrace();
      Messages.DO.exception(e, log);
      handleError(resp, 500);
      log.debug("OOPS:", e);
    }
  }

  /**
   * Parse the supplied sort spec. Sorts must be separated by '|' characters, and contain a valid name
   * of a property on the class referenced, followed by whitespace followed by either 'asc' or 'desc'.
   * Whitespace variations should be tolerated.
   * <p><b style="color:red">All sorts passed to {@link ObjectService#list(Class, int, int, List, List)}
   * MUST be validated by this method to avoid hql injection attacks.</b>
   *
   * @param sort A string contianing sorts
   * @param ref  a reference to a class.
   * @return a list of validated sort specs with no leading or trailing space and only one internal space character.
   */
  private List<String> parseSorts(String sort, ObjectReference ref) {
    String[] strings = sort.split("\\|");
    List<String> result = new ArrayList<>(4);
    for (String possibleSort : strings) {
      possibleSort = possibleSort.trim();
      String[] parts = possibleSort.split("\\s+");
      if (parts.length != 2) {
        throw new RuntimeException("Sort spec " + possibleSort + " is invalid (must have 2 parts separated by whitespace)");
      }
      if (!"asc".equals(parts[1]) && !"desc".equals(parts[1])) {
        throw new RuntimeException("Sort spec " + possibleSort + " is invalid (must end with asc or desc)");
      }
      try {
        BeanInfo beanInfo = Introspector.getBeanInfo(ref.getType());
        boolean isProp = Arrays.stream(beanInfo.getPropertyDescriptors())
            .anyMatch((d) -> d.getName().equals(parts[0]));
        if (!isProp) {
          throw new RuntimeException("Sort spec " + possibleSort + " is invalid (" + parts[0] +
              " does not appear to be a property on " + ref.getType().getSimpleName() + ")");
        }
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
      result.add(String.format("%s %s", parts[0], parts[1]));
    }
    return result;
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    ObjectReference ref = new ObjectReference(pathInfo);
    // TODO: multi-put
    if (ref.isValid()) {
      if (ref.getId() != null) {
        Messages.DO.sendErrorMessage("ID must not be specified for put operations");
        handleError(resp, 400);
        log.error("Id sent to PUT");
        return;
      }
      Persisted p;
      try {
        Class<?> valueType = Class.forName(ref.getType().getCanonicalName());
        ServletInputStream inputStream = req.getInputStream();
        p = (Persisted) mapper.readValue(inputStream, valueType);
        if (p instanceof Validatable && !((Validatable)p).isValidated()) {
          ((Validatable) p).validate();
        }
        if (Messages.DO.errorCount() != 0) {
          handleError(resp, 400);
        } else {
          invokeActions(req,resp,p);
          objectService.insert(p);
          success(resp, null);
        }
      } catch (ClassNotFoundException | ClassCastException e) {
        e.printStackTrace();
        Messages.DO.exception(e, log);
        handleError(resp, 400);
      } catch (ValidationException e) {
        Messages.DO.sendErrorMessage(e.getMessage());
        handleError(resp, 400);
      } catch (PersistenceException e) {
        // one time hack, if we get more of these invest in a generic solution.
        Throwable cause = e.getCause().getCause();
        if (cause instanceof ConstraintViolationException) {
          Messages.DO.sendErrorMessage(ref.getType().getSimpleName() + " exists");
        }
        handleError(resp, 409);
      } catch (AuthzException e) {
        log.debug(e);
        Messages.DO.sendErrorMessage("Insufficient Access Rights");
        handleError(resp, 403);
      } catch (Exception e) {
        e.printStackTrace();
        Messages.DO.exception(e, log);
        handleError(resp, 500);
      }
    }

  }

  private void success(HttpServletResponse resp, Long numFound, Object... objs) throws IOException {
    resp.setContentType("application/json");
    RestResponse rr = new RestResponse();
    rr.setOk(true);
    rr.getResults().addAll(Arrays.asList(objs));
    rr.getMessages().addAll(Messages.DO.getErrorMessages());
    rr.setNumFound(numFound);
    // if we don't do this sad, wasteful string writer, we loose any opportunity
    // to set the error code in the event of an exception during serialization. :(
    StringWriter w = new StringWriter();
    mapper.writeValue(w, rr);
    ServletOutputStream out = resp.getOutputStream();
    out.print(w.toString());
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
  }

  private void invokeActions(HttpServletRequest req, HttpServletResponse resp, Persisted p) throws IOException {
    @SuppressWarnings("unchecked")
    List<ActionInvocation> invocations = (List<ActionInvocation>) req.getAttribute("NS_ACTION");
    for (ActionInvocation invocation : invocations) {
      if (invocation != null) {
        invocation.getObjectsActedUpon().add(p);
        try {
          invocation.prePersist();
        } catch (ValidationException e) {
          if (Messages.DO.errorCount() == 0) {
            Messages.DO.sendErrorMessage(e.getMessage());
          }
        }
        if (Messages.DO.errorCount() > 0) {
          handleError(resp, 400);
        }
      }
    }
  }
}
