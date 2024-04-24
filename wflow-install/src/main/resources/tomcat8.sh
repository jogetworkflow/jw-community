#!/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx768M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.9.22.jar"

#For Java 9
#export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx768M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.9.22.jar --add-modules java.se.ee"

apache-tomcat-9.0.86/bin/catalina.sh $*
 

