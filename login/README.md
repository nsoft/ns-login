# LOGIN

This web app takes care of the user login process. As with all
of the apps in the ns-login system it is small focused, and entirely
about functionality. Zero effort has been placed on styling on the 
assumption that you will probably entirely rework the UI portion of this.

A few things to note: 
1. Persistence access will be independent and race with the main 
   application. Adding complex functionality that writes to the 
   data base here should keep that in mind. Presently all access 
   in this app is read only.
   
This app uses a simple JSP page, and if you want to handle this as part of
a component on a larger page you will need to engineer that yourself. 
Remember the NS-Login system is just meant to get you started quickly
without completely screwing yourself security-wise. The advantage of the
JSPs is that they are dead simple and rely on no javascript, and are 
rendered server side so there's very little opportunity for an attacker
to fool with them. Less complexity = less attack surface.

## SPOOFING

Simplicity is also a weakness in that the page is easily spoofed. An ideal
first addition is to implement user recognition via cookies and display a custom
image/text unique to that user on the login form, such as "Welcome Back Gus"
or their profile image (or both, but keep in mind the potential privacy
concerns of your users too).

Spoofing is a problem because users will sometimes not notice they've 
been misdirected and enter their password on the spoofed site. Presto!
the attacker now can use that to take over the account.

Spoofing protection may be added in the future.