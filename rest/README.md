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

Note that the choice of put vs post for object creation is a matter of some debate
in the web development community, but this is easily swapped by switching the method 
names in RestServlet. If you disagree, just change it :)

## Status

Completed:
1. GET object by id, or filtered/sorted list of objects
1. PUT to create objects 
1. POST to update objects 
1. Javascript framework to perform above
1. Test page to demonstrate use of Javascript framework 
1. Notifications of errors/messages via response envelope or via websocket

To Do:
1. More Demos on test page
1. Authentication
1. Authorization 
1. Actions for complex operations