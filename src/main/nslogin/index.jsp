<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
  --%>

<%--
  Created by IntelliJ IDEA.
  User: gus
  Date: 7/22/19
  Time: 3:24 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
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
<div>
    <form action="${pageContext.request.contextPath}/service" method="post">
        <table>
            <tbody>
            <tr>
                <td>Username</td>
                <td><input type="text" name="username"></td>
            </tr>
            <tr>
                <td>Password</td>
                <td><input type="password" name="password"></td>
            </tr>
            </tbody>
        </table>
        <input type="submit"/>
    </form>
</div>
</body>
</html>
