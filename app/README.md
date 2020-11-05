# YOUR APPLICATION HERE

This web app is a place holder for YOUR application. You should be free to replace it
with ANYTHING else. You will probably want to ensure that this application and the one
holding your users talk to the same database, but even that is not strictly necessary as
long as this app can connect (via code in core.jar) to the user database.

WARNING: remove restTest.jsp once you verify that all the tests there pass. It is a
security risk (enumerates a list of usernames).

The only things you MUST preserve are the bundling of the core jar in the 
WEB-INF/lib dir and this bit from web.xml:

````
    <filter>
        <filter-name>jwt-auth</filter-name>
        <filter-class>com.needhamsoftware.nslogin.servlet.JwtAuthenticationFilter</filter-class>
        <init-param>
            <param-name>keyFetchUrl</param-name>
            <param-value>http://localhost:8080/login/service?kid=</param-value>
        </init-param>
    </filter>
````

This needs to be edited to reflect the address of the server containing
the login (login.war) application from the perspective of the server
serving this application's server, and there must be connectivity
between those servers. The above value should work if the two war
files (ROOT.war and login.war) are deployed on the same server.

## Action Security

One thing shown in this place holder worth noting is the means by which user actions are authorized. 

* When a request is made here for the reverse_things action [restTest.jsp#L194](https://github.com/nsoft/ns-login/blob/master/app/src/main/webapp/restTest.jsp#L194)
* that calls update here [nsRest.js#L807](https://github.com/nsoft/ns-login/blob/master/rest/src/main/webapp/js/nsRest.js#L807)
* which calls mutate here [nsRest.js#L811](https://github.com/nsoft/ns-login/blob/master/rest/src/main/webapp/js/nsRest.js#L811)
* which adds a request header denoting the action to be performed here [nsRest.js#L817](https://github.com/nsoft/ns-login/blob/master/rest/src/main/webapp/js/nsRest.js#L817)
* On the server the header is picked up here [ActionFilter.java#L64](https://github.com/nsoft/ns-login/blob/d00822088257f9bf51fec8417577c838f3fdc977/core/src/main/java/com/needhamsoftware/nslogin/servlet/ActionFilter.java#L64)
* and since [actions are model objects](https://github.com/nsoft/ns-login/tree/d00822088257f9bf51fec8417577c838f3fdc977/core/src/main/java/com/needhamsoftware/nslogin/model/action) with a [database representation](https://github.com/nsoft/ns-login/blob/67634b284f515ea66399718c8d82a395bd94e594/core/src/main/java/com/needhamsoftware/nslogin/service/impl/ObjectServiceImpl.java#L188) that have [permissions](https://shiro.apache.org/permissions.html), the user's role will only allow them to read actions they are allowed to perform.
* failure to [load](https://github.com/nsoft/ns-login/blob/d00822088257f9bf51fec8417577c838f3fdc977/core/src/main/java/com/needhamsoftware/nslogin/servlet/ActionFilter.java#L75) the action prevents the request from proceeding and generates an error message.
