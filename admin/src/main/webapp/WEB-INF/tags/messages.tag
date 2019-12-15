<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%--@elvariable id="MESSAGES" type="java.util.List<com.needhamsoftware.nslogin.model.Notification>"--%>
<%--

If you are not using JSP, delete the foreach and ifs and extract the div into your page

Also the following may be useful to avoid having to customize nsRest.js https://stackoverflow.com/a/13335898
if you want to use font-awsome or some other similar library

--%>
<div class="messages<c:if test="${MESSAGES != null && (MESSAGES.size() > 0)}">error</c:if>">
    <ul>
      <%-- this li is used for errors during ajax, or messages that come via websocket --%>
      <li class="alert alert-danger alert-dismissable hidden-template">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
          <span class="ns-close-icon"></span>
        </button>
        <span class="ns-message-icon"></span>
        <span class="ns-message-text"></span>
      </li>
      <c:forEach var="message" items="${MESSAGES}"> <%-- for errors during page render --%>
        <c:if test="${message.notificationType == 'ERROR'}">
        <li class="alert alert-danger alert-dismissable">
          <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
            <span class="ns-close-icon"></span>
          </button>
          <span class="ns-message-icon"></span>
          <span class="ns-message-text"><c:out value="${message.message}"/></span>
        </li>
        </c:if>
      </c:forEach>
    </ul>
</div>