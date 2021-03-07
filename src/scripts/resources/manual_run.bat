cd /D "%~dp0"
java -Dlogback.configurationFile=./logback.groovy -jar @jar.name@ -c conf/base.properties %*

