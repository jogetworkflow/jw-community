@ECHO OFF

REM Start MySQL
set MYSQL_HOME=.\mysql-5.0.22-win32
set MYSQL_INI_HOME=.\
ECHO == Starting MySQL from %MYSQL_HOME% ==
ECHO.
start %MYSQL_HOME%\bin\mysqld-nt --defaults-file="%MYSQL_HOME%/my.ini"

REM Start Tomcat
set JAVA_HOME=.\jdk1.6.0
set CATALINA_HOME=.\apache-tomcat-6.0.18
set JAVA_OPTS=-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/
REM set JAVA_OPTS=-XX:MaxPermSize=128m -Xmx1024M -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/
ECHO == Starting Tomcat from %CATALINA_HOME% ==
ECHO.
%CATALINA_HOME%\bin\catalina.bat run


