@echo off

if ""%1"" == """" goto usage
if ""%2"" == """" goto usage
if ""%3"" == """" goto usage
if ""%4"" == """" goto usage
if ""%5"" == """" goto usage

:create
mvn archetype:generate -DarchetypeGroupId=org.joget -DarchetypeArtifactId=wflow-plugin-archetype -DarchetypeVersion=%5 -DgroupId=%1 -DartifactId=%2 -Dlabel=%3 -Ddescription=%4
goto end

:usage
echo   Usage: create-plugin (package) (plugin name) (plugin label) (plugin description) (joget version)

:end
