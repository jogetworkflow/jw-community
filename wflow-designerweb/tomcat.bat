
set MAVEN_OPTS= -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5105
mvn tomcat:run
