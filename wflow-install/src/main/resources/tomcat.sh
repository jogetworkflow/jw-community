#!/bin/sh

export JAVA_OPTS="-Xmx768M -Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.9.22.jar"

apache-tomcat-11.0.1/bin/catalina.sh $*
