chcp 65001

set JAVA_HOME=.\jre11.0.18
set CATALINA_HOME=.\apache-tomcat-9.0.86

set JAVA_OPTS=-Xmx768M -Dfile.encoding=UTF-8 -Dwflow.home=./wflow/
REM set JAVA_OPTS=-Xmx1024M -Dfile.encoding=UTF-8 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/

%CATALINA_HOME%\bin\startup.bat

