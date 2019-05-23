#!/bin/sh

export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar -javaagent:./wflow/glowroot/glowroot.jar"

#For Java 9
#export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.8.5.jar -javaagent:./wflow/glowroot/glowroot.jar --add-modules java.se.ee"

apache-tomcat-8.5.41/bin/catalina.sh $*
 

