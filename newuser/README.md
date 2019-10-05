# NEW USER

This web app takes care of the new user registration process. As with all
of the apps in the ns-login system it is small focused, and entirely
about functionality. Zero effort has been placed on styling on the 
assumption that you will probably entirely rework the UI portion of this.

A few things to note: 
1. The email is presumed to be provided by your container. You will need to
   add some version of mail.jar and activation.jar to your container's lib 
   directory.
1. The default configuration assumes a mail server active on the localhost
   which is very unlikely to be what you want in production. Edit 
   `META-INF/context.xml` to correct this.
1. If you do stand up a local exim/postfix/sendmail for testing keep in mind
   that google and many other mail systems will almost certainly send
   your mails to spam. They will also reject your mail several times
   before finally accepting it, and it can take up to 15 minutes to see
   the mail in your inbox. This is anti-spam protection implemented by
   your provider and not a bug in this software or their system.
1. The email is generated via a velocity template in src/main/resources, 
   but is VERY basic. You should edit this to provide a better experience
   and reduce your clash with spam filters.
   
This app uses a simple JSP page, and if you want to handle this as part of
a component on a larger page you will need to engineer that yourself. 
Remember the NS-Login system is just meant to get you started quickly
without completely screwing yourself security-wise. The advantage of the
JSPs is that they are dead simple and rely on no javascript, and are 
rendered server side so there's very little opportunity for an attacker
to fool with them. Less complexity = less attack surface.