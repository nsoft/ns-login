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
<%--@elvariable id="ERRORS" type="java.util.List<String>"--%><%
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%
%><%@ page contentType="text/html;charset=UTF-8" %><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setHeader("Expires", "0"); // Proxies.
    // Note: scriptlets around line breaks are to avoid whitespace at top of page

%><html>
<head>
    <title>Reset Password</title>
</head>
<body>
<h1>Password Reset</h1>
<p>Please enter the email you use for logging in.</p>
<div>
    <form action="<c:url value="/request"/>" method="post">
        <table>
            <tbody>
            <tr>
                <td><label for="resetEmail">Email Address</label></td>
                <td><input id="resetEmail" type="email" name="email"></td>
            </tr>
            </tbody>
        </table>
        <input type="submit"/>
    </form>
</div>
</body>
</html>
