
README
======

Prerequisites:
--------------
- Java 8 and above
- MySQL 5 and above


Installation for Linux:
=======================
1. Create a new directory (e.g. /opt/joget) and extract the tar.gz bundle into that directory
2. Install the Java Runtime Environment (JRE) or Java Development Kit (JDK) version 7 and above
3. Install MySQL Server version 5 and above
4. Create an empty database 'jwdb' in the MySQL server
5. Execute the setup script to create the required database tables: ./setup.sh
6. Execute the bundled Apache Tomcat application server: ./tomcat8.sh run
7. Access the App Center at http://localhost:8080/jw


Installation for Windows:
=========================
1. Create a new folder e.g. C:\Joget and extract the ZIP bundle
2. Install the Java Runtime Environment (JRE) or Java Development Kit (JDK) version 7 and above
3. Install MySQL Server version 5 and above
4. Create an empty database 'jwdb' in the MySQL server
5. Populate the jwdb database with the SQL script in data\jwdb-empty.sql
6. Edit wflow\app_datasource-default.properties to match your database settings
7. Edit tomcat8-run.bat and change JAVA_HOME to your Java installation directory
8. Run tomcat8-run.bat to start the bundled Apache Tomcat application server
9. Access the App Center at http://localhost:8080/jw


Java Security Settings for the Workflow Designer:
------------------------------------------------

If you encounter a security problem launching the Workflow Designer with the 
message "Java applications are blocked by your security settings", 
this is due to security changes introduced in Java 7 Update 51 onwards for 
self-signed certificates.

There are several solutions for this in the knowledge base article 
Java Security Settings for the Workflow Designer 
(http://dev.joget.org/community/display/KBv4/Java+Security+Settings+for+the+Workflow+Designer)
 