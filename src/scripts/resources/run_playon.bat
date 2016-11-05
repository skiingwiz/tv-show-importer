@echo off
setlocal

cd /D "%~dp0"

java -Dlogback.configurationFile=./logback.groovy -jar @jar.name@ -c conf/playon.properties %*

pushd ..\sage-scripting-framework
java -jar ssf.jar scripts\scanLibrary.py
popd

endlocal
