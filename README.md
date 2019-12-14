# NS Login
[![License](https://img.shields.io/badge/license-Apache%202.0-B70E23.svg?style=plastic)](http://www.opensource.org/licenses/Apache-2.0)

A basic system designed to protect single page (Angular/etc) web pages deployed
on a J2EE servlet container. 

* Redirect to login/logout
* Single Sign On across J2EE contexts
* New user registration via email
* _Optional_  REST system
  * JSOG encoded JSON for full object graph support
  * Object model with Hibernate mapping (Maria Db)
  * Google Guice dependency injection
  * Rest Envelope for error messages, 
  * Websocket message channel
  * _WIP: Actions to specify complex operations beyond CRUD_
  * _Optional_ javascript library for simplified interaction and caching
* _Optional_ Authorization via Apache Shiro with database backed permissions
* _Comming Soon: Optional web UI for simple user management_

The basic login/logout features can be added to any existing application by adding a servlet 
filter. Shiro Authorization presently requires a Guice environment. Any of the supplied
applications can be deployed on separate physical machines though you will want to 
pay attention to Same Origin Policy issues and perhaps proxy some of them through the same
top level url, or if your are brave, properly configure CORS.

https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy

Also note that XSS and CSRF security is not attempted here and that is your application's 
responsibility to handle. If you don't know what XSS and CSRF are, stop everything, right now
and go research them.

## Motivation

How many times have you had an idea for a web application, and then sat down to 
code it and then said " Hmm, I need a database with users, and a login 
page/widget, and new user creation and password recovery, and some way to see/manage 
the users, and I want to write it as a modern JS based app, but I can't trust the
browser so need a server side based login... I could just whip up something quick
but then I'll probably just be Yet Another Insecure Application (tm) on the web. 
If I take the time to do it right... I probably won't remember half my idea
by the time I get the users management available... ugh.

Sure you could use wordpress, but do you REALLY want to spend your life programming 
in PHP? And wordpress is a deep, complex tool in and of itself with a lot of 
conventions and rules you have to learn.

Sure you could use LifeRay or other portal software, but then you have to support
an armada of features for composible pages (which you likely don't want) and 
somehow wrangle a theme that acheives the style you want... with a whole
ecosystem of conventions you need to learn (or just build a really ugly app that 
looks like something people only use if they are paid to). And let's not even
talk about the weight of their build process...

And the story goes on for any one of a number of other frameworks (Spring etc?) or 
CMS variants  (Drupal etc?). If you're already steeped in something, you KNOW it 
won't get in your way later and it's easy for YOU, great, go use it. However, usually
I feel like I want to focus on the idea behind my project and not spend endless time 
evaluating frameworks to try to figure out if they will get in my way. What I
really want is a starting point which is easily picked apart and loosely coupled.
and places minimum constraints on my UI and future technology.

If you have a conscience, or even a modicum of prudence proper application security 
is the slayer of big ideas. To be developed ideas need to be used and adapted to user 
needs. You have to "get it out there" to find out if anyone cares, if your idea really
works in the real world and if it's worth putting real time and effort into. Sure
some big social media apps started by just throwing things together But times
have changed and they got lucky. Lottery style lucky. The following serious concerns 
arise from deploying an insecure web application:

1. **Ethics** - An insecure web application can seriously harm its users by disclosing
   personally identifiable information to be used by phishing scams, or expose a
   password the user has also used elsewhere on the web. While users shouldn't do 
   that, most of them do and your insecure application may thus do great harm if 
   it is successfully hacked.
1. **Reputation** - Sure your idea might be about to make you a $B or save the world
   etc, but if it becomes the focus of the scandal you're hosed.
2. **Liability** - In this day and age, if you do someone harm (see Ethics above) 
   they are all too likely to sue you. Sure you don't have 2 cents to rub together
   because the idea is in it's infancy, but that won't stop vengeful users and 
   greedy lawyers from making your life hell.
   
Decent application security is a problem that get's solved over and over and over again. 
This project aims to provide a **skinny** starting point that JUST gives you log
in and log out without getting in your way or imposing serious constraints on how 
you build your application or your ui. It is broken into several archives any
one of which could be replaced or enhanced so long as it is replaced with something 
that does the same job. 


## Components

**WARNING**: The first, most basic component is a properly configured and maintained
HTTPS connection! If you fail that, you and your users are toast, and there's nothing
I or anyone else can do here to enact that for you. Here's a link to get you started:

https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html

This system is highly modular so that it is easy to replace each part of it if you deem it
unsuitable (or you have out grown it).

1. **Login App** - this app provides a read only view into a database and generates a
   JWT token that can be then verified by the UI using a servlet filter configured
   to wrap the entire context of the ui application. The login is set up as a separate
   application with it's context so that one doesnt have to fiddle with excluding
   the login page from the security wrapper. The security wrapper remains a dead
   simple /* (everything) that can't be screwed up or accidentally let folks through.
1. **Application Stub** - a dead simple single html file that is properly
   configured to be protected by the security filter. Just drop your application in
   here and off you go.
1. **New User App** - an entirely separate application that you can direct users to 
   for creating new accounts. This is designed so all it can do is add a new user
   after having validated that the user controls the email they supplied and that 
   no existing user has the same email. No user record exists until after verification
   to ensure that there's no chance of your system being fooled by half created
   users.
1. **Password Reset App** - email based password reset with expiring tokens.
1. **REST API stub** - serving JSOG encoded object graphs for user objects including not
   exposing sensitive info such as passwords, and properly breaking the chain of created_by
   and modified_by to avoid pulling back long graphs of users based on who created who.
1. **Simple User Management App** - (TODO) using the rest api to manually add users,
   manually suspend users. Manually force password reset. (database integrated only no LDAP etc)
   
Database access is mapped with hibernate/JPA via a common object model.
If you don't already have a database, any of these apps can bootstrap it via hibernate's 
Hbbm2DDL functionality. (just edit persistence.xml in core to enable, please read)
Hibernate's JPA related docs for options here Should work with any database supported 
by Hibernate. Of course this should just be an initial starting point and good DB 
change management practices via liquibase or similar are your responsibility going 
forward.   

With these 4 components easily deployed, just add your UI and REST code to implement
your idea for the next facebook (or whatever)!

## Intended Usage

It will never be feasible for these war-files to be configurable enough to be usable 
without modification, so the expectation is that you download the code from this 
repository, and then check it into your own repo to bootstrap your repository. 

It is expected that you be facile with J2EE and web concepts like web.xml files
request redirection, and Object Relational Mapping.

### Things to Configure

#### Email
You should configure your server with a JNDI email service that can be found by 
this code (or tweak this code in the apps to your preference):
```$java
    Context initCtx;
    try {
      initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      session = (Session) envCtx.lookup("mail/Session");
    } catch (NamingException e) {
      e.printStackTrace();
      throw new ServletException(e);
    }
```
#### Database 

You will want to configure hibernate to talk to your database. The configuration can
be found here, and you should edit it to something more secure: 

https://github.com/nsoft/ns-login/blob/master/core/src/main/resources/META-INF/persistence.xml

#### Securing Existing Applications

You can add this system to existing apps/contexts by adding the following filter
```$xml
    <filter>
        <filter-name>jwt-auth</filter-name>
        <filter-class>com.needhamsoftware.nslogin.servlet.JwtAuthenticationFilter</filter-class>
        <init-param>
            <param-name>keyFetchUrl</param-name>
            <param-value>http://localhost:8080/login/service?kid=</param-value>
        </init-param>
        <init-param>
            <param-name>redirectToLogin</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>jwt-auth</filter-name>
        <url-pattern>*</url-pattern>
    </filter-mapping>
```
`keyFetchUrl` must point to the login service and end in `?kid=`. `redirectToLogin` controls whether 
the request is redirected, or simply receives a 401 unauthorized. The latter is desirable for 
Ajax calls so that the javascript calling the ajax can redirect to login. Without this users with 
expired sessions attempting my experience unresponsive pages.

#### Browser Caching
By default any non css, js png or jpeg resource wrapped by the authentication filter is 
not cached. This is controled by the code here, which should be edited to suit your needs.
Please do not submit patches to change this list. A patch to make it configurable might be 
of interest however.

https://github.com/nsoft/ns-login/blob/master/core/src/main/java/com/needhamsoftware/nslogin/servlet/JwtAuthenticationFilter.java#L115

JS/CSS obfuscation/mimimization and preventing cached copies after a new release is an
exercise left to the reader.

## Auth Styles

The default setup is for an app like Facebook or Gmail that offers zero functionality to 
guest users. The static/public portion of the site would likely be hosted on Apache
httpd or nginix elsewhere. This appropriate for apps with a strong draw (the need to 
connect with your friends, or the need for email communication for example) to get users 
past the sign up process, or apps with a higher security risk where you simply don't want
to risk interaction with anyone you don't know. 

For more welcoming apps that hope to provide some free content as incentive to motivate user
signups like F1 TV or the New York Times you will want to do one of 3 things:
1. Write your own custom filter (because it's your app!). It's expected that eventually almost
   everyone will do this as their needs grow beyond the basic. Remember this is a kit to
   get you started without hosing yourself, not a final solution. That said, there are a 
   couple things to try before you go there (see below)
1. Change the path setting in the filter to protect a sub-directory and place controlled content
   in the sub directory. Details of this are up to you and your application, and ns-login
   endeavors not to make any assumptions here that might get in your way.
1. Wrap your REST service in the same filter, and detect redirection of AJAX calls either via 
   responseURL comparison or via use of the fetch() api.
   
Future versions of the login servlet may provide an attribute or header which forces all 
responses to a 200 OK and returns JSON indicating success and token, or failure with message 
to facilitate this more inviting style.

The feature creep line is drawn on this side of unauthenticated user tracking schemes or
user recognition before login. That's your application's custom concern and not provided in 
this starter kit.

## How Secure

The security is guaranteed by the strength of the public/private key pair generated in the
login servlet and used to encrypt the secret of the JWT token. The class LoginConstants has 
a constant the determines the JCA algorithm used. For any public/private key pair (or really 
any encryption) one must assume that given enough time/cpu/memory an attacker can break the 
encryption keys and once they do that they can craft tokens that would fool the application 
into accepting the attacker as an arbitrary user (and if authorization is included, arbitrary 
privileges). 

This is not an "if" but a "when", and more CPU and memory can be applied to reduce the time
it takes to do reverse engineer the private key. CPU and memory are limited primarily by available
funding in the real world so the goal is not to make cracking the system impossible, just 
prohibitively expensive. Increasing the key complexity and reducing the validity period of the
key are the two ways to increase the cost of hacking the encryption used for the JWT tokens. 

**Note:** Increasing key complexity also raises the work YOUR system must do to log users in.

To facilitate keeping up with the ever increasing availability of computing resources the 
algorithm is easily changed via LoginConstants, which defines the algorithm used, the 
frequency with which a new key is generated, and the duration after which an old key is 
forgotten;

## 2 Factor Auth

Not yet, something for the future... contributions welcome :smiley:

