set JAVA_HOME=.\jre1.8.0_112
set CATALINA_HOME=.\apache-tomcat-8.5.78

set JAVA_OPTS=-Xmx512M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar
REM set JAVA_OPTS=-Xmx1024M -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5115 -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar

%CATALINA_HOME%\bin\startup.bat

