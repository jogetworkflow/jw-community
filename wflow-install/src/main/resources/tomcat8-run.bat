set JAVA_HOME=.\jre11.0.13
set CATALINA_HOME=.\apache-tomcat-9.0.85

set JAVA_OPTS=-Xmx768M -Dwflow.home=./wflow/
REM set JAVA_OPTS=-Xmx1024M -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/

%CATALINA_HOME%\bin\startup.bat

