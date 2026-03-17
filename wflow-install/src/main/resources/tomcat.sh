#!/bin/sh

# For a list of available Java system properties for Joget, please refer to:
# https://kb.joget.org/jw/web/userview/jdocs/docs/DX9/joget-java_opts-properties
export JAVA_OPTS="-Xmx768M -Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED -Dwflow.home=./wflow/ -javaagent:./wflow/aspectjweaver-1.9.22.jar"

apache-tomcat-11.0.18/bin/catalina.sh $*
