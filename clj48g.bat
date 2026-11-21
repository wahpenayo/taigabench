@echo off
:: wahpenayo (at) gmail (dot) com
:: 2026-11-20

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication
set GC=

set XMX=-Xms48g -Xmx48g -Xmn20g

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp ./src/scripts/clojure;lib/*
set JAVA="%JAVA_HOME%\bin\java"

:: TODO: safer way of serializing arbitrary clojure functions built thru calls to fn
:: see https://clojure.org/news/2026/11/18/clojure-1-12-7
set SERIALIZED_FNS=-Dclojure.serializable.fns=true

set CMD=%JAVA% -dsa -ea %GC% %XMX% %SERIALIZED_FNS% %OPENS% %CP% clojure.main %*
::echo %CMD%
%CMD%
