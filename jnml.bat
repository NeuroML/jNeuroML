set CLASSPATH=*.jar;%JNML_HOME%\*.jar

java -Xmx400M -cp %CLASSPATH% org.neuroml.JNeuroML %1 %2 %3 %4 %5 %6
