@ECHO OFF

REM Stop MySQL
set MYSQL_HOME=.\mysql-5.0.22-win32
ECHO == Stopping MySQL from %MYSQL_HOME% ==
ECHO.
start %MYSQL_HOME%\bin\mysqladmin -u root shutdown
ECHO.

REM Stop Tomcat
set JAVA_HOME=.\jdk1.6.0
set CATALINA_HOME=.\apache-tomcat-6.0.18
ECHO == Stopping Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\shutdown.bat

ECHO.
ECHO Shutdown initiated. Please close the Tomcat console window to complete shutdown.
PAUSE
