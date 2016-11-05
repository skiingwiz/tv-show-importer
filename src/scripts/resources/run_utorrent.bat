@echo off
setlocal

cd /D "%~dp0"

java -Dlogback.configurationFile=./logback.groovy -jar @jar.name@ -c conf/utorrent-%1.properties %2 %3 %4 %5 %6 %7 %8 %9

pushd ..\sage-scripting-framework
java -jar ssf.jar scripts\scanLibrary.py
popd

endlocal
