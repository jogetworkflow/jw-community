#/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ "

apache-tomcat-8.0.20/bin/catalina.sh $*
 

