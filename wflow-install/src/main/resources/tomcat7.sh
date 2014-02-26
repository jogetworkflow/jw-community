#/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ "

apache-tomcat-7.0.52/bin/catalina.sh $*
 

