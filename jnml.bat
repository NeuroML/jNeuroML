@echo off

set JNML_VERSION=0.7.3

set CLASSPATH=target\jNeuroML-%JNML_VERSION%-jar-with-dependencies.jar;%JNML_HOME%\jNeuroML-%JNML_VERSION%-jar-with-dependencies.jar

java -Xmx400M -cp %CLASSPATH% org.neuroml.JNeuroML %1 %2 %3 %4 %5 %6
