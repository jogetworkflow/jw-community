#!/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ "

#For Java 9
#export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ --add-modules java.se.ee"

apache-tomcat-8.5.23/bin/catalina.sh $*
 

