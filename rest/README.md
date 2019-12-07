# REST

This application is meant to give simple access to the database mapped model objects using
the following HTTP verb mapping:

* Create ==> PUT
* Read ==> GET
* Update ==> POST
* Delete ==> DELETE

Getting user number 3:

    GET /rest/AppUser/3

Listing all app users:

    GET /rest/AppUser/

Objects are returned as JSOG/JSON such that complex graphs of objects can be easily
re-assembled, and duplication of data is avoided. A JavaScript library designed to
interact with this service is provided as nsRest.js

Results are returned in a response envelope that provides a location to transmit
validation errors, and other messages. The message system also provides a web-socket
on which web clients can listen to collect messages instead. Typically the envelope is
used for per-request messages and the socket for system broadcasts, additionally messages
generated while the user was off line may be stored and delivered on login via the
PendingNotificationServlet