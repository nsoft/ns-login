# LOGIN

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


