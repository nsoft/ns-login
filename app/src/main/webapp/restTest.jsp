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
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>
<html>
<head>
  <title>Rest Tester JSP</title>
  <%--suppress JspAbsolutePathInspection --%>
  <link type="text/css" rel="stylesheet" href="/test.css">
</head>
<body>
<header>Please Note that this page provides testing and examples of how the rest framework can be used, but
  none of this is required to use the login/logout features. /rest is entirely decoupled from the login/out basics
</header>
<div id="test" class="page home-page">
  <p id="welcome">Finding filtered objects: User name should appear here --&gt; </p><br/>
  <p>Below a table of users and their id's should appear</p>
  <table data-type="AppUser">
    <thead>
    <tr>
      <th></th>
      <th>ID</th>
      <th>User Name</th>
    </tr>
    </thead>
    <tbody>
    <tr class="rowTemplate">
      <td data-render="checkBoxId">id</td>
      <td>id</td>
      <td>username</td>
    </tr>
    </tbody>
  </table>
  <p>Below is a region in which messages of various types should randomly appear if
    websocket based messages are working. Errors from other tests will also appear here</p>
  <button id="wsTestBtn">Click Here to Begin Testing Web Sockets</button>
  <div>
    <app:messages/>
  </div>

  <form id="ttForm" data-type="TestThing" data-method="POST" onsubmit="REST.submit">
    <table>
      <tr>
        <td><label for="test_id">Id:</label> </td>
        <td><input type="text" id="test_id" name="test_id" ></td>
      </tr>
      <tr>
        <td><label for="test_aDouble">A Double</label></td>
        <td><input type="text" id="test_aDouble" name="test_aDouble"> </td>
      </tr>
      <tr>
        <td><label for="test_aString">A String:</label></td>
        <td><input type="text" id="test_aString" name="test_aString"> </td>
      </tr>
      <tr>
        <td><label for="test_anInstant">An Instant</label></td>
        <td><input type="text" id="test_anInstant" name="test_anInstant"> </td>
      </tr>
      <tr>
        <td><label for="test_anInt">An Integer:</label></td>
        <td><input type="text" id="test_anInt" name="test_anInt"> </td>
      </tr>
    </table>
    <input type="hidden" name="test_version">
    <input type="submit">
  </form>

  <h2>Test things</h2>
  <button id="newThing">New</button>
  <button id="addSomeThings">Add some TestThings to above TestThing</button>
  <button id="reverseThings">Reverse Action</button>

  <table id="ttTable" data-type="TestThing" data-post-render="ttTableHighlight">
    <thead>
    <tr>
      <th></th>
      <th>ID</th>
      <th>An Integer</th>
      <th>A Double</th>
      <th>A String</th>
      <th>An Instant</th>
      <th>Last Modified</th>
      <th>Modified By</th>
    </tr>
    </thead>
    <tbody>
    <tr class="rowTemplate">
      <td data-render="checkBoxId">id</td>
      <td>id</td>
      <td>anInt</td>
      <td>aDouble</td>
      <td>aString</td>
      <td>anInstant</td>
      <td>modified</td>
      <td>modifiedBy.username</td>
    </tr>
    </tbody>
  </table>
</div>
<!-- Easy mode script loading, see https://www.html5rocks.com/en/tutorials/speed/script-loading/ if performance is key -->
<script src="https://code.jquery.com/jquery-3.4.1.min.js"
        integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="
        crossorigin="anonymous"></script>
<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jsog@1.0.7/lib/JSOG.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.bundle.min.js"
        integrity="sha384-6khuMg9gaYr5AxOqhkVIODVIvm9ynTT5J4V1cfthmT+emCG6yVmEZsRHdxlotUnm"
        crossorigin="anonymous"></script>
<script type="text/javascript" src="rest/js/URI.js"></script>
<script type="text/javascript">
  window.CONTEXT = "/"; // if using JSP or other templating, this can be made to automatically adjust...
</script>
<script type="text/javascript" src="rest/js/nsRest.js"></script>
<script type="text/javascript" src="rest/js/renderers.js"></script>
<script type="text/javascript" src="rest/js/restNotifications.js"></script>
<script src="https://cdn.jsdelivr.net/npm/js-cookie@2/src/js.cookie.min.js"></script>
<script type="text/javascript">
  jQuery(document).ready(function () {
    REST.addPostRender("ttTableHighlight", highlightRowsForTestThingDisplayed);
    // a very very simple validation that the rest framework is (or is not up and running)
    // see restTest.jsp for more complete testing
    if (REST) {
      //alert('foo');
      let email = Cookies.get("nslogin-uid");
      //alert(email)
      REST.find("AppUser", "userEmail='" + email + "\'", function (data) {
        if (data && data.length && data.length > 0) {
          $("#welcome").append(' ' + data[0].username)
        }
      }, 1)
    }
    $('#wsTestBtn').click(function (evt) {
      evt.preventDefault();
      REST_SOCKET.send("test");
      return false;
    });
    $("#test_id").on("change", function () {
      let $testId = $("#test_id");
      $testId.closest("*[data-type]").attr("data-id", $testId.val());
      REST.refreshPage($testId)
    });
    $("#newThing").click(function (evt) {
      evt.preventDefault();
      REST.create("TestThing", {}, function () {
        let $testId = $("#test_id");
        REST.refreshPage($testId)
      });
      return false;
    });
    $("#addSomeThings").click(function (evt) {
      evt.preventDefault();
      let $form = $("#ttForm");
      let id = $form.attr("data-id");
      let object = REST.objectify($form, REST.lookup("TestThing", id));
      let ids = [];
      let $ttTable = $("#ttTable");
      let find = $ttTable.find('input[type="checkbox"]:checked');
      find.each(function () {
        let item = this.id.substr(5);
        ids.push(item);
      });
      //todo: should add filters to REST.list for this
      REST.find("TestThing", "id in '" + ids.join()+"'", function (data) {
        object["someThings"] = data;
        REST.update("TestThing", object, function () {
          let $testId = $("#test_id");
          REST.refreshPage($testId)
        });
      }, 999, 0);

      return false;
    })
  });

  function highlightRowsForTestThingDisplayed() {
    let $this = $("#ttTable");
    let $form = $("#ttForm");
    let id = $form.find("#test_id").val();
    let thing = REST.lookup("TestThing", id);
    if (thing) {
      let subThings = thing.someThings;
      let count = 1;
      for (const t of subThings) {
        $this.find("#item_" + t.id).closest("tr").css("background","rgb(200," + (100 + 20*count++) + ",200)");
      }
    }
  }
</script>
</body>
</html>
