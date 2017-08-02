db-services
===========

Database Web Services Application - Expose database results as REST web services without any coding

### Requirements
>This application requires Tomcat application server.  It may work in other application servers but has not been tested. It is assumed you have already installed Tomcat and have verified the it is working.  If not please search for Apache Tomcat and install the latest server.
	
### Install Instructions:
  1. download the latest application war file (api.war) from the bin directory on github
  2. copy api.war from your downloads directory into the <install dir>/webapps/ tomcat directory
  3. start tomcat if not already running
  4. open browser to http://localhost:8080/api/console
  5. walk through the Getting Started in 5min wiki page
  
### Why another project?

A simple search shows there are many good libraries, apis and frameworks for exposing information over REST so why another project?  I was looking for an application that I could configure exposing database information over REST without any programming.  I wanted presentation coders (HTML5 and javascript) to rapidly build out what they need for developing pages without needing to know server side languages, install and manage databases etc.  This is my attempt at such a product.  The current version is useable but I would definitely consider it in BETA state.  

### What this product is not; nor probably ever will be
This web application is not intended as a direct replacement for many of the server side frameworks and libraries that are out there today.  Many of the solutions out there today are geared for enterprise solutions that are very complicated and flexible.  This application is designed as a very thin wrapper around database calls.  As such, it can not nor probably ever will support the case where a vendor has provided an api (java library) but does not allow direct access to the database.  This also means that business rules must be pushed into the database tier in the form of stored procedures or database constraints or handled via javascript in the presentation layer.  While this is quite manageable with smaller to medium size sites, enterprise solutions very often will need more complex and full-featured solutions.  That being said, this product is still a valuable tool for rapid development and debugging.  Also, it can sit along side other enterprise frameworks to provide rapid REST exposure and deployment between environments for direct database access needs without any java coding effort.

### What this product is
This application is a way for a presentation developer to quickly configure JSON return data without any back-end coding effort.  In a production environment, it supports using keywords to package configuration data for migration between environments.  It has the ability to package and re-deploy connections for easy connecting to different environment databases which can be very useful for debugging issues.  Lastly, this product is designed with performance in mind.  Every effort was made to eliminate any additional object creation or destruction.  There is no Object Relational Mapping.  It simply takes simple SQL queries and directly converts the results to simple JSON strings (plan to provide other formats later).  The goal is to get the results as close as possible to a direct database call.  My long term vision is that this product would meet the need of developers, small and even some medium size sites for REST access to database data.
