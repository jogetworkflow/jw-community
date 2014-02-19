#!/bin/sh
package=$1
plugin=$2
version=$3
if [ -z $package ]; then 
    echo "Usage: create-plugin.sh (package) (plugin name) (joget version)"
elif [ -z $plugin ]; then 
    echo "Usage: create-plugin.sh $package (plugin name) (joget version)"
elif [ -z $version ]; then 
    echo "Usage: create-plugin.sh $package $plugin (joget version)"
else 
    clear
    mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=${version} -DgroupId=$package -DartifactId=$plugin
fi
exit 1
