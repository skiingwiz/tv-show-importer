cd /D "%~dp0"
java -Dlogback.configurationFile=./logback.groovy -jar @jar.name@ --no-fanart %*

