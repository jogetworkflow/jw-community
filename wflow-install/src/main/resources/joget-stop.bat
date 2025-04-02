@ECHO OFF

REM Stop MariaDB
ECHO == Stopping MariaDB ==
ECHO.
CALL .\mariadb-stop.bat
ECHO.

REM Stop Tomcat
set JAVA_HOME=.\jre21.0.5
set CATALINA_HOME=.\apache-tomcat-11.0.5
ECHO == Stopping Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\shutdown.bat

ECHO.
ECHO Shutdown initiated. Please close the Tomcat console window to complete shutdown.
PAUSE
