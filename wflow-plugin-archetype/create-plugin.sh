#!/bin/sh
package=$1
plugin=$2
if [ -z $package ]; then 
	echo "Usage: create-plugin.sh (package) (plugin name)"
elif [ -z $plugin ]; then 
	echo "Usage: create-plugin.sh $package (plugin name)"
else 
	clear
	mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=3.1-SNAPSHOT -DgroupId=$package -DartifactId=$plugin
fi
exit 1
