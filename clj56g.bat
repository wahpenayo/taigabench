@echo off
:: wahpenayo (at) gmail (dot) com
:: 2017-11-17

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set COMPRESSED=
::set COMPRESSED=-XX:CompressedClassSpaceSize=3g 

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

set XMX=-Xms56g -Xmx56g -Xmn24g

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp ./src/scripts/clojure;lib/*
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% -ea %GC% %XMX% %COMPRESSED% %TRACE% %OPENS% %CP% clojure.main %*
::echo %CMD%
%CMD%
