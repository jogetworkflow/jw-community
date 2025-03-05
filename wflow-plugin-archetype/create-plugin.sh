#!/bin/sh
package=$1
plugin=$2
label=$3
description=$4
version=$5
if [ -z "$package" ] || [ -z "$plugin" ] || [ -z "$label" ] || [ -z "$description" ] || [ -z "$version" ]; then
    echo "Usage: create-plugin.sh (package) (plugin name) (plugin label) (plugin description) (joget version)"
    exit 1
else
    mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=${version} -DgroupId="$package" -DartifactId="$plugin" -Dlabel="$label" -Ddescription="$description"
    exit $?
fi
