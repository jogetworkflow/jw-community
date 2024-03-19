@ECHO OFF

chcp 65001

REM Start MariaDB
ECHO == Starting MariaDB ==
ECHO.
CALL .\mariadb-start.bat
ECHO.

REM Start Tomcat
set JAVA_HOME=.\jre11.0.18
set CATALINA_HOME=.\apache-tomcat-9.0.86
set JAVA_OPTS=-Xmx768M -Dfile.encoding=UTF-8 -Dwflow.home=./wflow/
REM set JAVA_OPTS=-Xmx1024M -Dfile.encoding=UTF-8 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/
ECHO == Starting Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\catalina.bat run


