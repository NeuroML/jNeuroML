#!/bin/bash

# Copyright 2021 Ankur Sinha
# Author: Ankur Sinha <sanjay DOT ankur AT gmail DOT com>
# File : linux-install.sh
#
# Install jNeuroML on Linux machines
jNeuroMLJarDir="$(pwd)"

if command -v java &> /dev/null
then
    echo "Found java installation:"
    java -version
else
    echo "Could not find 'java' command."
    echo "Please ensure that a Java Runtime Environment is installed on your system and that the 'java' command is usable in the terminal"
    exit -1
fi

if [ -e "jnml" ] && [ -f jNeuroML*jar-with-dependencies.jar ];
then
    echo "Found jar and jnml in current directory: $jNeuroMLJarDir"
    echo "Updating ~/.bashrc to make it available in \$PATH."
else
    echo "ERROR: jnml or jneuroML pre-compiled JAR file not found."
    echo "ERROR: Please run this script from the jNeuroMLJar directory that contains both jnml and the jar file."
    exit -1
fi

# Remove previous JNML additions
sed -i '/JNML_HOME/ d' ~/.bashrc

# Export JNML_HOME so that jnml can find the JAR
cat >> ~/.bashrc << EOF
# JNML_HOME etc for jNeuroML
export JNML_HOME="$jNeuroMLJarDir"
export PATH="\$PATH:\$JNML_HOME"
EOF

echo "Done: Please log out and back in and try to run jnml in a terminal."
exit 0
