@ECHO OFF

REM Start MariaDB
ECHO == Starting MariaDB ==
ECHO.
CALL .\mariadb-start.bat
ECHO.

REM Start Tomcat
set JAVA_HOME=.\jre1.8.0_112
set CATALINA_HOME=.\apache-tomcat-8.5.78
set JAVA_OPTS=-Xmx512M -Dwflow.home=./wflow/
REM set JAVA_OPTS=-Xmx1024M -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/
ECHO == Starting Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\catalina.bat run


