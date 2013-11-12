jNeuroML
========

There are a number of repositories in active development under GitHub for handling [NeuroML](https://github.com/NeuroML) 
and [LEMS](https://github.com/LEMS) with Java. 

To make it easier to access all of this functionality, we've created a single package, jNeuroML, which allows access 
to most of this functionality through a simple command line interface and requires minimal installation. 

Binary distribution
-------------------

To get a precompiled binary for jNeuroML, type:

    svn checkout svn://svn.code.sf.net/p/neuroml/code/jNeuroMLJar
    cd jNeuroMLJar

and you have everything you need.

Typing ./jnml (or jnml.bat on Windows) will list the options available. Some of the current options include:

    ./jnml -validate MyNeuroML.nml              (validate NeuroML 2 document against the current schema)
    ./jnml -validatev1 MyNeuroML1.xml           (validate NeuroML v1 document against the v1.8.1 schema)
    ./jnml MyLEMS.xml                           (parse & simulate a LEMS model using jLEMS)
    ./jnml MyLEMS.xml -graph                    (generate png of structure of LEMS model using GraphViz)

Export and import features for [NEURON](http://www.neuron.yale.edu/neuron/), [SBML](http://sbml.org), 
[Brian](http://www.briansimulator.org/) etc. are in development (https://github.com/NeuroML/org.neuroml.export 
and https://github.com/NeuroML/org.neuroml.import) and this functionality will be included in the jnml utility as 
it is developed.

Points to note:

- Adding the environment variable JNML_HOME, pointing to the jNeuroMLJar folder, as well as adding this path to the PATH variable will let you use the jnml utility from any folder.

- Running svn update in the jNeuroMLJar folder will get the latest version of the binary. There are much better ways to distribute binaries than putting them in an SVN repo I know, but this is a rapidly changing application and this seems to best way to distribute the latest release at the moment with the minimum of hassle for users.


Getting the source for jNeuroML
-------------------------------

If you prefer to clone all of the individual repositories and build the jNeuroML application yourself, 
use the [getNeuroML.py](https://github.com/NeuroML/jNeuroML/blob/master/getNeuroML.py) utility in the jNeuroML repo:

    git clone git://github.com/NeuroML/jNeuroML.git neuroml_dev/jNeuroML
    cd neuroml_dev/jNeuroML
    python getNeuroML.py

This will clone ~11 repos for NML2 & LEMS (including Python based libraries) into neuroml_dev/ and compile 
the Java based ones using Maven. The full process may take 5-10 mins on first installation, but subsequently running:

    git pull
    python getNeuroML.py

in the jNeuroML folder will get the stable version of each repo & compile using Maven if necessary. 

*To access the very latest version* (the development branches of the GitHub repos) use:

    python getNeuroML.py development

Use of Maven is a great way to manage versions of applications being developed in distributed repositories, 
and will make it easy to use selected parts of this for different Java applications. For example, these packages 
will be used in various ways to provide NeuroML/LEMS support in [neuroConstruct](www.neuroConstruct.org) and for handling NeuroML on the 
[Open Source Brain website](www.OpenSourceBrain.org).





