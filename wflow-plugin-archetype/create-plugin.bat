@echo off

if ""%1"" == """" goto usage
if ""%2"" == """" goto usage
if ""%3"" == """" goto usage

:create
mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=%3 -DgroupId=%1 -DartifactId=%2
goto end

:usage
echo   Usage: create-plugin (package) (pluginName) (jogetVersion)

:end


