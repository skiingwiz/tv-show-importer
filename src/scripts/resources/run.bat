@echo off
setlocal

cd /D "%~dp0"

IF "%1"=="multi" (
   set multipleparam=--preprocess-largest-file-in-dir
) ELSE (
   set multipleparam=
)

shift /1

java -Dlogback.configurationFile=./logback.groovy -jar @jar.name@ --series-banners --season-banners --fanart-dir D:\Data\fanart\TV\ %multipleparam% %1 %2 %3 %4 %5 %6 %7 %8 %9

pushd ..\sage-scripting-framework
java -jar ssf.jar scripts\scanLibrary.py
popd

endlocal
