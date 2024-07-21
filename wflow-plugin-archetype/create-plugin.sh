#!/bin/sh
package=$1
plugin=$2
label=$3
description=$4
version=$5
if [ -z $package ]; then 
    echo "Usage: create-plugin.sh (package) (plugin name) (plugin label) (plugin description) (joget version)"
elif [ -z $plugin ]; then 
    echo "Usage: create-plugin.sh $package (plugin name) (plugin label) (plugin description) (joget version)"
elif [ -z "$label" ]; then 
    echo "Usage: create-plugin.sh $package $plugin (plugin label) (plugin description) (joget version)"
elif [ -z "$description" ]; then 
    echo "Usage: create-plugin.sh $package $plugin \"$label\" (plugin description) (joget version)"
elif [ -z $version ]; then 
    echo "Usage: create-plugin.sh $package $plugin \"$label\" \"$description\" (joget version)"
else 
    clear
    mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=${version} -DgroupId=$package -DartifactId=$plugin -Dlabel="$label" -Ddescription="$description"
fi
exit 1
