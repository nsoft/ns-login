<%--suppress JspAbsolutePathInspection HtmlUnknownTarget --%>
<%--
  ~    Copyright (c) 2019, Needham Software LLC
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  --%><%
%><%@ page import="static com.needhamsoftware.nslogin.servlet.LoginConstants.X_LOGIN_RETURN_TO" %><%
%><%@ page import="org.apache.commons.lang3.StringUtils" %><%
%><%@ page import="static com.needhamsoftware.nslogin.servlet.LoginConstants.X_ERROR_MESSAGE" %><%
%><%@ page import="java.util.Collections" %><%
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%
%><%@ page contentType="text/html;charset=UTF-8" %><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setHeader("Expires", "0"); // Proxies.

    // Note: scriptlets around line breaks are to avoid whitespace at top of page

    String returnPath = request.getParameter("from");
    if (StringUtils.isNotBlank(returnPath)) {
        request.getSession().setAttribute(X_LOGIN_RETURN_TO, returnPath);
        response.sendRedirect(request.getRequestURL().toString()); // hide the parameter
        return;
    }

    // Did the application have trouble with a token it received?
    if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getHeader(X_ERROR_MESSAGE))) {
        request.setAttribute("ERRORS", Collections.singletonList(request.getHeader(X_ERROR_MESSAGE)));
    }

%><html>
<head>
    <title>Please Log In</title>
</head>
<body>
<h1>Please Log In</h1>
<c:catch var="exception"><span style="display:none">${ERRORS.size()}</span></c:catch>
<c:choose>
    <c:when test="${not empty exception}"> </c:when>
    <c:otherwise>
        <ul>
            <c:forEach items="${ERRORS}" var="error">
                <li><c:out value="${error}"/></li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
<c:set var="loginEmail" value='${requestScope["LOGIN_FORM_EMAIL"]}' />
<div>
    <form action="<c:url value="/service"/>" method="post">
        <table>
            <tbody>
            <tr>
                <td><label for="email">Email</label></td>
                <td>
                    <input id="email" type="email" name="email" value="<c:out value="${loginEmail}"/> ">
                </td>
            </tr>
            <tr>
                <td><label for="loginPassword">Password</label></td>
                <td><input id="loginPassword" type="password" name="password"></td>
            </tr>
            </tbody>
        </table>
        <input type="submit"/>
    </form>
    <div><a href="/newuser/">Create a new account</a> </div>
    <div><a href="/reset/">Reset Password</a> </div>
</div>
</body>
</html>
