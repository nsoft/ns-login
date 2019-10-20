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
<%--@elvariable id="ERRORS" type="java.util.List<String>"--%>
<%
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%
%><%@ page contentType="text/html;charset=UTF-8" %><%

    // Note: scriptlets around line breaks are to avoid whitespace at top of page

%><html>
<head>
    <title>Password Reset</title>
</head>
<body>
<h1>Password Reset</h1>
<p>Enter a new password for your account</p>

<c:catch var="exception">
<span style="display:none">${ERRORS.size()}</span></c:catch>
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
<c:set var="resetToken" value='${empty param.token ? requestScope["RESET_FORM_TOKEN"] : param.token}' />

<div>
    <form action="<c:url value="/confirmation"/>" method="post">
        <table>
            <tbody>
            <tr>
                <td><label for="password">Password</label></td>
                <td><input id="password" type="password" name="password"></td>
            </tr>
            <tr>
                <td><label for="confirmPassword">Confirm</label></td>
                <td><input id="confirmPassword" type="password" name="passwordConfirm"></td>
            </tr>
            <tr>
                <td><label for="newEmail">Token From Email</label></td>
                <td><input id="newEmail" type="text" name="token" value="<c:out value="${resetToken}"/>"></td>
            </tr>
            </tbody>
        </table>
        <input type="submit"/>
    </form>
</div>
</body>
</html>
