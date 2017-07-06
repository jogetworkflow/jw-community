@ECHO OFF

REM Stop MySQL
ECHO == Stopping MySQL ==
ECHO.
CALL .\mysql-stop.bat
ECHO.

REM Stop Tomcat
set JAVA_HOME=.\jre1.8.0_112
set CATALINA_HOME=.\apache-tomcat-8.5.16
ECHO == Stopping Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\shutdown.bat

ECHO.
ECHO Shutdown initiated. Please close the Tomcat console window to complete shutdown.
PAUSE
