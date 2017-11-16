@echo off
:: John Alan McDonald
:: 2016-11-03

::set GC=-XX:+AggressiveHeap
set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 

::set SAFE=-XX:+PrintSafepointStatistics -XX:+PrintGCApplicationStoppedTime -XX:PrintSafepointStatisticsCount=1
set SAFE=

::set PROF=-Xrunhprof:cpu=samples,depth=96,thread=y,doe=y
::set PROF=-agentlib:hprof=cpu=samples,depth=100,interval=20,lineno=y,thread=y,file=out.hprof
set PROF=-agentlib:hprof=cpu=samples,depth=100,interval=20,lineno=y,thread=y
::set PROF=

::set THRUPUT=-d64 -server -XX:+AggressiveOpts -XX:+UseLargePages 
set THRUPUT=-d64 -server -XX:+AggressiveOpts 

:: Leave a couple gb for Windows, Xmx about 2 times Xmn
::set XMX=-Xms26g -Xmx26g -XX:NewRatio=2
set XMX=-Xms24g -Xmx24g -Xmn10g 
::set XMX=-Xms28g -Xmx28g -Xmn11g 
::set XMX=-Xms29g -Xmx29g 

set CP=-cp ./src/scripts/clojure;lib/*
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% %THRUPUT% -ea %GC% %SAFE% %PROF% %XMX% %CP% clojure.main %*
::echo %CMD%
%CMD%
