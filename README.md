# NS Login

A basic system designed to protect single page (Angular/etc) web pages deployed
on a J2EE servlet container.

**Note** this is about to be reorganized to a better gradle/directory structure

## Motiviation

How many times have you had an idea for a web application, and then sat down to 
code it and then said " Hmm, I need a database with users, and a login 
page/widget, and new user creation and password recovery, and some way to see/manage 
the users, and I want to write it as a modern JS based app, but I can't trust the
browser so need a server side based login... Ugh... nevermind..." And 
then not bothered with the idea because of the work that setting all of that
entails. 

## Components

This system is highly modular so that it is easy to replace each part of it if you deem it
unsuitable (or you have out grown it).

1. **Login** - this app provides a read only view into a database and generates a
   JWT token that can be then verified by the UI using a servlet filter configured
   to wrap the entire context of the ui application. The login is set up as a separate
   application with it's context so that one doesn't have to fiddle with excluding
   the login page from the security wrapper. The security wrapper remains a dead
   simple /* (everything) that can't be screwed up or accidentally let folks through.
1. **Application Stub** - a dead simple single html file that is properly
   configured to be protected by the security filter. Just drop your application in
   here and off you go.
1. **New User App** - (TODO) an entirely separate application that you can direct users to 
   for creating new accounts. This is designed so all it can do is add a new user
   after having validated that the user controls the email they supplied and that 
   no existing user has the same email. No user record exists until after verification
   to ensure that there's no chance of your system being fooled by half created
   users.
1. **Password Reset App** - (TODO) email based password reset with expiring tokens. 
1. Database access is mapped with hibernate/JPA via a common object model.
   If you don't already have a database any of these apps can bootstrap it via hibernate's 
   Hbbm2DDL functionality. (just edit persistence.xml in one of the to enable) 
   Should work with any database supported by Hibernate. Of course this should just
   be an initial starting point and good DB change management practices via liquibase or 
   similar are your responsibility going forward.

With these 4 components easily deployed, just add your UI and REST code to implement
your idea for the next facebook (or whatever)!

## Intended Usage

It will never be feasible for these war-files to be configurable enough to be
useable without modification, so the expectation is that you download the code
from this repository, and then check it into your own repo to bootstrap your
repository. 

It is expected that you be facile with J2EE and web concepts like web.xml files
request redirection, and Object Relational Mapping.

## 2 Factor Auth

Not yet, something for the future...

