REM set JAVA_HOME=c:\jdk1.6.0
set MAVEN_OPTS= -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5125
start mvn -e tomcat:run

