@echo off
:: wahpenayo (at) gmail (dot) com
:: 2025-09-23

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set COMPRESSED=
::set COMPRESSED=-XX:CompressedClassSpaceSize=3g 

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

set XMX=-Xms48g -Xmx48g -Xmn20g
::set XMX=-Xms12g -Xmx12g -Xmn5g 

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp ./src/scripts/clojure;lib/*
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% -ea -dsa -Xbatch %GC% %XMX% %COMPRESSED% %TRACE% %OPENS% %CP% clojure.main %*
::echo %CMD%
%CMD%
