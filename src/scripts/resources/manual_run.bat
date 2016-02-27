cd /D "%~dp0"
rem java -classpath @classpath@ -jar @jar.name@ --series-banners --season-banners --fanart-dir D:\Data\fanart\TV\ %*
java -classpath @classpath@ -jar @jar.name@ --no-fanart %*

