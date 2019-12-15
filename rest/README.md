# REST

This application is meant to give simple access to the database mapped model objects using
the following HTTP verb mapping:

* Create ==> PUT
* Read ==> GET
* Update ==> POST
* Delete ==> DELETE 

DELETE is not supported out of the box though there are placeholders. Every application needs
to decide on an appropriate data lifecycle. This will vary a lot and thus is left
up to you to implement as you see fit.

Getting user number 3:

    GET /rest/AppUser/3

Listing all app users:

    GET /rest/AppUser/

Objects are returned as JSOG/JSON such that complex graphs of objects can be easily
re-assembled, and duplication of data in the request/response is avoided. A JavaScript 
library designed to interact with this service is provided as nsRest.js, but you are
entirely free to use another, or write your own.

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
1. More Demos on test page
1. Authentication
1. Authorization 
1. Actions for complex operations

To Do:
1. Field level permissions? Requires fiddling with Jackson Serialization

## Actions

One of the less common aspects of this Rest service is that it accommodates complex server 
side actions, which may be specified via an X-Site-Action header included with the request.
This avoids the need for JSON in the request body to perform an action
during a GET or DELETE operation, and is properly orthogonal to the data. This feature
also reduces the temptation to perform sensitive operations in javascript on the client 
where user installed browser plugins, or wrapping frames could interfere with and
manipulate them.
 
To Create your own actions subclass Action, add a method to ActionVisitor, And then include
the action header with a comma separated list of Action.name elements on any 
GET/PUT/POST/DELETE request. 