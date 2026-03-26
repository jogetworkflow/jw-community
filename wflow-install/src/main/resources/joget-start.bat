@ECHO OFF

chcp 65001

REM Start MariaDB
ECHO == Starting MariaDB ==
ECHO.
CALL .\mariadb-start.bat
ECHO.

REM Start Tomcat
set JAVA_HOME=.\jre21.0.5
set CATALINA_HOME=.\apache-tomcat-11.0.18

REM For a list of available Java system properties for Joget, please refer to:
REM https://kb.joget.org/jw/web/userview/jdocs/docs/DX9/joget-java_opts-properties
set JAVA_OPTS=-Xmx768M -Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED -Dwflow.home=./wflow/
REM set JAVA_OPTS=-Xmx1024M -Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/

ECHO == Starting Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\catalina.bat run
