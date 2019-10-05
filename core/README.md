# CORE

This library is intended to be shared across the various J2EE application in the NS-Login
system. It contains:

1. A hibernate/JPA persistence layer (see persistence.xml & annotations for details)
1. A model representing the user and associated security objects
1. An J2EE servlet filter that can be used to wrap any application meant to be authenticated
   via login.war
   
It is up to you if you want to extend and use this persistence layer in your other apps to 
coordinate your business models, or employ an entirely different model in each application. 
NS-Login is just a starting point. Unleash your creativity from here :)


