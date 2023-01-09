#!/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx768M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar"

#For Java 9
#export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx768M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar --add-modules java.se.ee"

apache-tomcat-8.5.78/bin/catalina.sh $*
 

