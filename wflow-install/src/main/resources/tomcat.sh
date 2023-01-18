#!/bin/sh

export JAVA_OPTS="-Xmx768M -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.9.7.jar"

apache-tomcat-9.0.71/bin/catalina.sh $*
