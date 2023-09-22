package org.neuroml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.lemsml.export.matlab.MatlabWriter;
import org.lemsml.export.modelica.ModelicaWriter;
import org.lemsml.export.sedml.SEDMLWriter;
import org.lemsml.export.vhdl.VHDLWriter;
import org.lemsml.export.dlems.DLemsWriter;
import org.lemsml.export.c.CWriter;
import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.logging.MinimalMessageHandler;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.logging.DefaultLogger;
import org.lemsml.jlems.io.out.FileResultWriterFactory;
import org.lemsml.jlems.io.util.FileUtil;
import org.lemsml.jlems.viz.datadisplay.SwingDataViewerFactory;
import org.neuroml.export.brian.BrianWriter;
import org.neuroml.export.cellml.CellMLWriter;
import org.neuroml.export.dnsim.DNSimWriter;
import org.neuroml.export.exceptions.GenerationException;
import org.neuroml.export.graph.GraphWriter;
import org.neuroml.export.info.InfoWriter;
import org.neuroml.export.nest.NestWriter;
import org.neuroml.export.moose.MooseWriter;
import org.neuroml.export.eden.EDENWriter;
import org.neuroml.export.netpyne.NetPyNEWriter;
import org.neuroml.export.neuron.NeuronWriter;
import org.neuroml.export.pynn.PyNNWriter;
import org.neuroml.export.sbml.SBMLWriter;
import org.neuroml.export.svg.SVGWriter;
import org.neuroml.export.utils.Format;
import org.neuroml.export.utils.NeuroMLInclusionReader;
import org.neuroml.export.utils.Utils;
import org.neuroml.export.vertex.VertexWriter;
import org.neuroml.export.xineml.XineMLWriter;
import org.neuroml.export.xpp.XppWriter;
import org.neuroml.importer.sbml.SBMLImporter;
import org.neuroml.importer.sbml.UnsupportedSBMLFeature;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLElements;
import org.neuroml.model.util.NeuroMLException;
import org.neuroml1.model.util.NeuroML1Validator;
import org.sbml.jsbml.SBMLException;
import org.xml.sax.SAXException;

public class JNeuroML
{

    public static final String JNML_SCRIPT = "jnml";

    public static final String JNML_VERSION = "0.13.0";

    public static final String HELP_FLAG = "-help";
    public static final String HELP_FLAG_SHORT = "-h";
    public static final String HELP_FLAG_SHORT_Q = "-?";

    public static final String VERSION_FLAG = "-v";
    public static final String VERSION_FLAG_LONG = "-version";

    public static final String SEARCH_PATH_FLAG = "-I";
    public static final String SEARCH_PATH_JLEMSLIKE_FLAG = "-cp";

    public static final String NO_GUI_FLAG = "-nogui";

    public static final String RUN_FLAG = "-run";

    public static final String NO_RUN_FLAG = "-norun";

    public static final String OUTPUT_DIR_FLAG = "-outputdir";

    public static final String VALIDATE_FLAG = "-validate";
    public static final String VALIDATE_V1_FLAG = "-validatev1";

    public static final String INFO_EXPORT_FLAG = "-info";

    public static final String XPP_EXPORT_FLAG = "-xpp";

    public static final String BRIAN_EXPORT_FLAG = "-brian";
    public static final String VHDL_EXPORT_FLAG = "-vhdl";
    public static final String BRIAN2_EXPORT_FLAG = "-brian2";

    public static final String MATLAB_EXPORT_FLAG = "-matlab";
    // public static String MATLAB_EULER_EXPORT_FLAG = "-matlab-euler";

    public static final String DNSIM_EXPORT_FLAG = "-dnsim";

    public static final String CVODE_EXPORT_FLAG = "-cvode";

    public static final String MODELICA_EXPORT_FLAG = "-modelica";

    public static final String DLEMS_EXPORT_FLAG = "-dlems"; // Subject to change/removal without notice!!

    public static final String SEDML_EXPORT_FLAG = "-sedml";
    public static final String SEDML_EXPORT_FLAG2 = "-sed-ml";

    public static final String CELLML_EXPORT_FLAG = "-cellml";

    public static final String NEURON_EXPORT_FLAG = "-neuron";
    public static final String NEURON_COMPILE_FLAG = "-compile";

    public static final String PYNN_EXPORT_FLAG = "-pynn";
    public static final String RUN_PYNN_NEURON_FLAG = "-run-neuron";

    public static final String NETPYNE_EXPORT_FLAG = "-netpyne";
    public static final String NETPYNE_JSON_FLAG = "-json";

    public static final String NUMBER_PROCESSORS_FLAG = "-np";

    public static final String VERTEX_EXPORT_FLAG = "-vertex";

    public static final String NINEML_EXPORT_FLAG = "-nineml";
    public static final String SPINEML_EXPORT_FLAG = "-spineml";

    public static final String NEST_EXPORT_FLAG = "-nest";

    public static final String MOOSE_EXPORT_FLAG = "-moose";

    public static final String EDEN_EXPORT_FLAG = "-eden";

    //public static final String GEPPETTO_EXPORT_FLAG = "-geppetto";

    public static final String SBML_IMPORT_FLAG = "-sbml-import";
    public static final String SBML_IMPORT_UNITS_FLAG = "-sbml-import-units";
    public static final String SBML_EXPORT_FLAG = "-sbml";
    public static final String SBML_SEDML_EXPORT_FLAG = "-sbml-sedml";

    public static final String OLD_GRAPH_FLAG = "-graph";
    public static final String LEMS_GRAPH_FLAG = "-lems-graph";

    public static final String SVG_FLAG = "-svg";

    public static final String PNG_FLAG = "-png";

    public static final String GENERATED_FILE = ">>> JNML generated file: ";

    static String usage = "Usage: \n\n" + "    " + JNML_SCRIPT + " LEMSFile.xml\n"
            + "           Load LEMSFile.xml using jLEMS, parse it and validate it as LEMS, and execute the model it contains\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + NO_GUI_FLAG + "\n"
            + "           As above, parse and execute the model and save results, but don't show GUI\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + NO_RUN_FLAG + "\n"
            + "           Parse the LEMS file, but don't run the simulation\n\n"

            + "    " + JNML_SCRIPT + " " + SEARCH_PATH_FLAG + " directory1:directory2:directoryN LEMSFile.xml\n"
            + "           Execute the LEMS file, inclusing the : separated list of directories on the search path for includes\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + LEMS_GRAPH_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to GraphViz format (note, previously this option was: "+OLD_GRAPH_FLAG+")\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + NEURON_EXPORT_FLAG + " [" + NO_GUI_FLAG + "] [" + RUN_FLAG+ "] ["+OUTPUT_DIR_FLAG+" dir]\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to NEURON format, OR load ModelFile.nml and generate hoc and mod files for cells, channels, synapses \n"
            + "             " + NO_GUI_FLAG+ "       Do not generate graphical elements in NEURON, just run, save data and quit (if input file is LEMS file)\n"
            + "             " + RUN_FLAG + "         Compile NMODL files and run the main NEURON Python file (only with LEMS file)\n"
            + "             " + NEURON_COMPILE_FLAG + "     Compile NMODL files, but don't run (Linux only currently)\n"
            + "             " + OUTPUT_DIR_FLAG + "   Generate NEURON files in another directory, dir\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + NETPYNE_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to NetPyNE format\n"
            + "             " + RUN_FLAG + "         Compile NMODL files and run the main NEURON Python file\n\n"
            + "             " + NETPYNE_JSON_FLAG + "         Generate network in NetPyNE JSON format\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + BRIAN_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to Brian v1 format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + BRIAN2_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to Brian v2 format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + MOOSE_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to MOOSE format (**EXPERIMENTAL**)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + EDEN_EXPORT_FLAG + "\n"
            + "           Generate a Python script for loading LEMSFile.xml in the EDEN simulator\n\n"

            + "    " + JNML_SCRIPT + " NMLFile.nml " + SVG_FLAG + "\n"
            + "           Load NMLFile.nml and convert cells & networks to SVG image format \n\n"

            + "    " + JNML_SCRIPT + " NMLFile.nml " + PNG_FLAG + "\n"
            + "           Load NMLFile.nml and convert cells & networks to PNG image format \n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + DLEMS_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to dLEMS, a distilled form of LEMS in JSON (**EXPERIMENTAL - single components only**)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + VERTEX_EXPORT_FLAG+ "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to VERTEX format (*EXPERIMENTAL*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + XPP_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to XPPAUT format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + DNSIM_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to DNsim format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + SBML_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to SBML format (**EXPERIMENTAL - single components only**)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + SEDML_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it (just the simulation run/display/save information) to SED-ML format\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + SBML_SEDML_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to SBML format with a SED-ML file describing the experiment to run (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml "+ MATLAB_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to MATLAB format (**EXPERIMENTAL - single components only**)\n\n"

            + "    " + JNML_SCRIPT+ " LEMSFile.xml " + CVODE_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to C format using CVODE package (**EXPERIMENTAL - single components only**)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + NINEML_EXPORT_FLAG + "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to NineML format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " LEMSFile.xml " + SPINEML_EXPORT_FLAG+ "\n"
            + "           Load LEMSFile.xml using jLEMS, and convert it to SpineML format (*EXPERIMENTAL - single components only*)\n\n"

            + "    " + JNML_SCRIPT + " " + SBML_IMPORT_FLAG+ " SBMLFile.sbml duration dt\n"
            + "           Load SBMLFile.sbml using jSBML, and convert it to LEMS format using values for duration & dt in ms (ignoring SBML units)\n\n"

            + "    " + JNML_SCRIPT + " " + SBML_IMPORT_UNITS_FLAG + " SBMLFile.sbml duration dt\n"
            + "           Load SBMLFile.sbml using jSBML, and convert it to LEMS format using values for duration & dt in ms (attempt to extract SBML units; ensure units are valid in the SBML!)\n\n"

            + "    " + JNML_SCRIPT+" LEMSFile.xml " + VHDL_EXPORT_FLAG + " neuronid \n" +
              "           Load LEMSFile.xml using jLEMS, and convert it to VHDL format (**EXPERIMENTAL - point models only - single neurons only**)\n\n"

            + "    " + JNML_SCRIPT + " " + VALIDATE_FLAG + " NMLFile.nml\n"
            + "           Validate NMLFile.nml against latest v2 Schema & perform a number of other tests\n\n"

            + "    " + JNML_SCRIPT + " " + VALIDATE_V1_FLAG + " NMLFile.nml\n" + "           Validate NMLFile.nml against NeuroML v1.8.1 Schema \n\n"

            + "    " + JNML_SCRIPT + " " + VERSION_FLAG + "\n"

            + "    " + JNML_SCRIPT + " " + VERSION_FLAG_LONG + "\n" + "           Print information on versions of packages used\n\n"

            + "    " + JNML_SCRIPT + " " + HELP_FLAG + "\n"

            + "    " + JNML_SCRIPT + " " + HELP_FLAG_SHORT + "\n"

            + "    " + JNML_SCRIPT + " " + HELP_FLAG_SHORT_Q + "\n" + "           Print this help information\n\n";

    public static void showUsage()
    {
        System.out.println(usage);
    }

    private static Lems loadLemsFile(File lemsFile) throws LEMSException, NeuroMLException
    {
        return loadLemsFile(lemsFile, true);
    }

    private static Lems loadLemsFile(File lemsFile, boolean includeConnectionsFromHDF5) throws LEMSException, NeuroMLException
    {
        if(!lemsFile.exists())
        {
            System.err.println("File does not exist: " + lemsFile.getAbsolutePath());
            showUsage();
            System.exit(1);
        }
        return Utils.readLemsNeuroMLFile(lemsFile,includeConnectionsFromHDF5).getLems();
    }

    private static Lems loadNmlFileAsLems(File nmlFile) throws LEMSException, NeuroMLException, IOException
    {
        if(!nmlFile.exists())
        {
            System.err.println("File does not exist: " + nmlFile.getAbsolutePath());
            showUsage();
            System.exit(1);
        }
        return Utils.readNeuroMLFile(nmlFile).getLems();
    }

    private static String generateFormatFilename(File lemsFile, Format format, String extra)
    {
        String filename = lemsFile.getName();
        return generateFormatFilename(filename, format, extra);
    }

    private static String generateFormatFilename(String lemsFilename, Format format, String extra)
    {
        String newSuffix = (extra!=null ? extra : "") + "." + format.getExtension();

        String filename = lemsFilename;

        String oldSuffix = "."+Format.LEMS.getExtension();

        if (filename.endsWith(oldSuffix)) {
            filename = filename.substring(0, filename.length() - oldSuffix.length());
        }

        filename = filename + newSuffix;

        return filename;
    }

    public static void main(String[] args) throws SBMLException, org.sbml.jsbml.text.parser.ParseException, RuntimeError
    {

        MinimalMessageHandler.setVeryMinimal(true);
        E.setDebug(false);

        System.out.println(" jNeuroML v" + JNML_VERSION);

        try
        {
            ArrayList<String> argsToUse = new ArrayList();
            for (int i=0; i<args.length; i++)
            {
                if(args[i].equals(SEARCH_PATH_FLAG) || args[i].equals(SEARCH_PATH_JLEMSLIKE_FLAG))
                {
                    String search = args[i+1];
                    for(String s: search.split(":"))
                    {
                        NeuroMLInclusionReader.addSearchPath((new File(s)).getAbsoluteFile());
                    }
                    i+=1;
                }
                else
                {
                    argsToUse.add(args[i]);
                }
            }

            args = new String[argsToUse.size()];
            args = argsToUse.toArray(args);

            //System.out.println("args: "+args.length            );
            //System.out.println("args: "+args[0]           );

            if(args.length == 0)
            {
                System.err.println("Error, no arguments to " + JNML_SCRIPT + "...");
                showUsage();
                System.exit(1);

                // One argument
            }
            else if(args.length == 1)
            {

                if(args[0].startsWith("-"))
                {
                    if(args[0].equals(HELP_FLAG) || args[0].equals(HELP_FLAG_SHORT) || args[0].equals(HELP_FLAG_SHORT_Q))
                    {
                        showUsage();
                        System.exit(0);
                    }
                    else if(args[0].equals(VERSION_FLAG) || args[0].equals(VERSION_FLAG_LONG))
                    {
                        // Version has just been displayed...
                        String jars = "    org.neuroml.import  v" + org.neuroml.importer.Main.ORG_NEUROML_IMPORT_VERSION + "\n" + "    org.neuroml.export  v" + Utils.ORG_NEUROML_EXPORT_VERSION + "\n"
                                + "    org.neuroml.model   v" + NeuroMLElements.ORG_NEUROML_MODEL_VERSION + "\n" + "    jLEMS               v" + org.lemsml.jlems.io.Main.VERSION;
                        System.out.println(jars);
                        System.exit(0);
                    }
                    else
                    {
                        System.err.println("Unrecognised argument: " + args[0]);
                        showUsage();
                        System.exit(1);
                    }
                }
                else
                {
                    File lemsFile = new File(args[0]);
                    if(!lemsFile.exists())
                    {
                        System.err.println("File does not exist: " + args[0]);
                        showUsage();
                        System.exit(1);
                    }

                    System.out.println("Loading: " + lemsFile.getAbsolutePath() + " with jLEMS...");
                    FileResultWriterFactory.initialize();
                    SwingDataViewerFactory.initialize();
                    DefaultLogger.initialize();

                    Utils.runLemsFile(lemsFile, true);

                }

                // Multiple arguments, starting with a validate flag
            }
            else if(args[0].equals(VALIDATE_FLAG))
            {
                boolean fail = false;
                int passedNoWarnings = 0;
                int warnings = 0;
                for(int i = 1; i < args.length; i++)
                {

                    File xmlFile = new File(args[i]);
                    System.out.println("Validating: " + xmlFile.getAbsolutePath());
                    if(!xmlFile.exists())
                    {
                        System.err.println("File does not exist: " + args[i]);
                        showUsage();
                        System.exit(1);
                    }
                    NeuroML2Validator nmlv = new NeuroML2Validator();
                    nmlv.validateWithTests(xmlFile.getCanonicalFile());

                    if(nmlv.isValid())
                    {
                        if(nmlv.hasWarnings())
                        {
                            warnings++;
                        }
                        else
                        {
                            passedNoWarnings++;
                        }

                    }
                    else
                    {
                        fail = true;
                    }

                    System.out.println(nmlv.getValidity());
                    System.out.println(nmlv.getWarnings());
                }
                String passed = (passedNoWarnings == args.length - 1) ? "All valid and no warnings" : passedNoWarnings + " passed, ";
                String warn = (warnings == 0) ? "" : warnings + " passed with warnings, ";
                int failed = args.length - 1 - passedNoWarnings - warnings;
                String failure = (failed == 0) ? "" : failed + " failed";

                System.out.println("\nValidated " + (args.length - 1) + " files: " + passed + warn + failure + "\n");

                if(fail)
                {
                    System.exit(1);
                }

            }
            else if(args[0].equals(VALIDATE_V1_FLAG))
            {

                boolean fail = false;
                for(int i = 1; i < args.length; i++)
                {

                    File xmlFile = new File(args[i]);
                    // System.out.println("Validating: "+xmlFile.getAbsolutePath());

                    if(!xmlFile.exists())
                    {
                        System.err.println("File does not exist: " + args[i]);
                        showUsage();
                        System.exit(1);
                    }
                    NeuroML1Validator nmlv = new NeuroML1Validator();
                    nmlv.validateWithTests(xmlFile);
                }
                if(fail)
                {
                    System.exit(1);
                }
                // Lots of options for Neuron
            }
            else if(args[1].equals(NEURON_EXPORT_FLAG))
            {

                File lemsNmlFile = (new File(args[0])).getCanonicalFile();
                Lems lems = null;

                if (lemsNmlFile.getName().endsWith("nml"))
                {
                    lems = loadNmlFileAsLems(lemsNmlFile);
                }
                else
                {
                    lems = loadLemsFile(lemsNmlFile);
                }
                boolean nogui = false;
                boolean run = false;
                boolean compile = false;
                File outputDir = lemsNmlFile.getParentFile();

                int i  = 2;
                while (i<args.length)
                {
                    if (args[i].equals(NO_GUI_FLAG))
                        nogui = true;
                    else if (args[i].equals(RUN_FLAG))
                    {
                        run = true;
                        compile = true;
                    }
                    else if (args[i].equals(NEURON_COMPILE_FLAG))
                        compile = true;
                    else if (args[i].equals(OUTPUT_DIR_FLAG))
                    {
                        i = i+1;
                        outputDir = (new File(args[i])).getAbsoluteFile();
                        if (!outputDir.exists())
                        {
                            System.out.println("Cannot find dir: " + args[i] + " ("+outputDir+")");
                            System.exit(1);
                        }
                    }
                    else
                    {
                        System.out.println("Unrecognised argument: " + args[i]);
                        System.exit(1);
                    }
                    i = i+1;
                }
                String mainNrnFilename = generateFormatFilename(lemsNmlFile, Format.NEURON, "_nrn");
                NeuronWriter nw = new NeuronWriter(lems, outputDir, mainNrnFilename);

                if (lemsNmlFile.getName().endsWith("nml"))
                {
                    nw.generateFilesForNeuroMLElements(compile);
                }
                else
                {
                    List<File> files = nw.generateAndRun(nogui, compile, run, false);
                    for(File genFile : files)
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }

            }
            else if(args[1].equals(PYNN_EXPORT_FLAG))
            {
                File lemsFile = (new File(args[0])).getAbsoluteFile();
                Lems lems = loadLemsFile(lemsFile);
                String nFile = generateFormatFilename(lemsFile, Format.PYNN, "_pynn");

                boolean runNrn = (args.length==3 && args[2].equals(RUN_PYNN_NEURON_FLAG));

                PyNNWriter pw = new PyNNWriter(lems, lemsFile.getParentFile(), nFile);

                List<File> files = pw.generateAndRun(false, runNrn);
                for(File genFile : files)
                {
                    System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                }
            }
            else if(args[1].equals(NETPYNE_EXPORT_FLAG))
            {
                File lemsFile = (new File(args[0])).getAbsoluteFile();
                Lems lems = loadLemsFile(lemsFile, false);
                boolean nogui = false;
                boolean run = false;
                boolean json = false;
                File outputDir = lemsFile.getParentFile();
                int np = 1;

                int i  = 2;
                while (i<args.length)
                {
                    if (args[i].equals(NO_GUI_FLAG))
                        nogui = true;
                    else if (args[i].equals(RUN_FLAG))
                        run = true;
                    else if (args[i].equals(NETPYNE_JSON_FLAG))
                        json = true;
                    else if (args[i].equals(OUTPUT_DIR_FLAG))
                    {
                        i = i+1;
                        outputDir = (new File(args[i])).getAbsoluteFile();
                        if (!outputDir.exists())
                        {
                            System.out.println("Cannot find dir: " + args[i] + " ("+outputDir+")");
                            System.exit(1);
                        }
                    }
                    else if (args[i].equals(NUMBER_PROCESSORS_FLAG))
                    {
                        i = i+1;
                        np = Integer.parseInt(args[i]);
                    }
                    else
                    {
                        System.out.println("Unrecognised argument: " + args[i]);
                        System.exit(1);
                    }
                    i = i+1;
                }

                String mainFilename = generateFormatFilename(lemsFile, Format.NETPYNE, "_netpyne");
                NetPyNEWriter npw = new NetPyNEWriter(lems, outputDir, mainFilename);
                List<File> files = npw.generateAndRun(nogui, run, np, json);
                
                for(File genFile : files)
                {
                    System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                }
            }
                // Two arguments
            else if(args.length == 2)
            {

                // / Run LEMS with no gui
                if(args[1].equals(NO_GUI_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    if(!lemsFile.exists())
                    {
                        System.err.println("File does not exist: " + args[0]);
                        showUsage();
                        System.exit(1);
                    }

                    System.out.println("Loading: " + lemsFile.getAbsolutePath() + " with jLEMS, NO GUI mode...");
                    FileResultWriterFactory.initialize();
                    DefaultLogger.initialize();

                    Utils.runLemsFile(lemsFile, false);
                } // / Parse LEMS & exit
                else if(args[1].equals(NO_RUN_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    if(!lemsFile.exists())
                    {
                        System.err.println("File does not exist: " + args[0]);
                        showUsage();
                        System.exit(1);
                    }

                    System.out.println("Loading: " + lemsFile.getAbsolutePath() + " with jLEMS, NO RUN mode...");

                    Utils.loadLemsFile(lemsFile, false, false);

                    // / exporting formats
                }
                else if(args[1].equals(INFO_EXPORT_FLAG))
                {

                    File nmlFile = new File(args[0]);

                    NeuroMLConverter nmlc = new NeuroMLConverter();
                    NeuroMLDocument nmlDocument = nmlc.loadNeuroML(nmlFile);

                    InfoWriter infow = new InfoWriter(nmlDocument);
                    String info = infow.getMainScript();

                    System.out.println("\n" + info);

                }
                else if(args[1].equals(VERTEX_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    VertexWriter vw = new VertexWriter(lems,
                                                       lemsFile.getParentFile(),
                                                       generateFormatFilename(lemsFile, Format.VERTEX, "_run"));
                    for(File genFile : vw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(SBML_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    SBMLWriter sbmlw = new SBMLWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.SBML, null));
                    for(File genFile : sbmlw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(SBML_SEDML_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    SBMLWriter sbmlw = new SBMLWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.SBML, null));
                    for(File genFile : sbmlw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                    SEDMLWriter sedw = new SEDMLWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.SEDML, null), lemsFile.getName(), Format.SBML);
                    for(File genFile : sedw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }
                else if(args[1].equals(XPP_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    XppWriter xppw = new XppWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.XPP, null));
                    for(File genFile : xppw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(DNSIM_EXPORT_FLAG))
                {

                    File lemsFile = (new File(args[0])).getAbsoluteFile();
                    Lems lems = loadLemsFile(lemsFile);

                    DNSimWriter dnsimw = new DNSimWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.DN_SIM, null));
                    for(File genFile : dnsimw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(NEST_EXPORT_FLAG))
                {

                    File lemsFile = (new File(args[0])).getAbsoluteFile();
                    Lems lems = loadLemsFile(lemsFile);

                    String suffix = "_nest";
                    String nFile = generateFormatFilename(lemsFile, Format.NEST, null);

                    NestWriter nw = new NestWriter(lems, lemsFile.getParentFile(), nFile);
                    for(File genFile : nw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }
                else if(args[1].equals(MOOSE_EXPORT_FLAG))
                {

                    File lemsFile = (new File(args[0])).getAbsoluteFile();
                    Lems lems = loadLemsFile(lemsFile);

                    String suffix = "_moose";
                    String nFile = generateFormatFilename(lemsFile, Format.MOOSE, suffix);

                    MooseWriter nw = new MooseWriter(lems, lemsFile.getParentFile(), nFile);
                    for(File genFile : nw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }
                else if(args[1].equals(EDEN_EXPORT_FLAG))
                {

                    File lemsFile = (new File(args[0])).getAbsoluteFile();
                    Lems lems = loadLemsFile(lemsFile);

                    String suffix = "_eden";
                    String nFile = generateFormatFilename(lemsFile, Format.EDEN, suffix);

                    EDENWriter nw = new EDENWriter(lems, lemsFile, lemsFile.getParentFile(), nFile);
                    for(File genFile : nw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }

                /*
                Needs to be updated!!
                else if(args[1].equals(GEPPETTO_EXPORT_FLAG))
                {

                    File lemsFile = (new File(args[0])).getAbsoluteFile();
                    Lems lems = loadLemsFile(lemsFile);

                    String suffix = ".geppetto";
                    String gFileName = generateFormatFilename(lemsFile, Format.GEPPETTO, null);
                    File gFile = new File(lemsFile.getParentFile(), gFileName);
                    GeppettoWriter gw = new GeppettoWriter(lems, lemsFile.getParentFile(), gFileName, lemsFile);
                    for(File genFile : gw.convert())
                    {
                        System.out.println("Writing to: " + genFile.getAbsolutePath());

                        if (genFile.getName().indexOf("geppetto") > 0) {
                            System.out.println("\nTry running this file locally with Geppetto using:\n\n    "
                                + "http://localhost:8080/org.geppetto.frontend/?sim=file://" + genFile + "\n");
                        }
                    }
                }*/
                else if(args[1].equals(SEDML_EXPORT_FLAG) || args[1].equals(SEDML_EXPORT_FLAG2))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    SEDMLWriter sedw = new SEDMLWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.SEDML, null), lemsFile.getName(), Format.NEUROML2);
                    for(File genFile : sedw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(CELLML_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    CellMLWriter cellmlw = new CellMLWriter(lems,
                                                            lemsFile.getParentFile(),
                                                            generateFormatFilename(lemsFile, Format.CELLML, null));
                    for(File genFile : cellmlw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(NINEML_EXPORT_FLAG) || args[1].equals(SPINEML_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    Format v = args[1].equals(SPINEML_EXPORT_FLAG) ? Format.SPINEML : Format.NINEML;
                    String suffix = args[1].equals(SPINEML_EXPORT_FLAG) ? ("." + Format.SPINEML.getExtension()) : ("." + Format.NINEML.getExtension());
                    XineMLWriter xw = new XineMLWriter(lems, v, lemsFile.getParentFile(), lemsFile.getName().replaceAll("." + Format.LEMS.getExtension(), suffix));
                    for(File genFile : xw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(MATLAB_EXPORT_FLAG)/* || args[1].equals(MATLAB_EULER_EXPORT_FLAG) */)
                {
                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    //String filename = lemsFile.getName().replaceAll("-", "_").replaceAll("." + Format.LEMS.getExtension(), "." + Format.MATLAB.getExtension());
                    String filename = generateFormatFilename(lemsFile.getName().replaceAll("-", "_"), Format.MATLAB, null);
                    if(!Character.isLetter(filename.charAt(0)))
                    {
                        filename = "M_" + filename;
                    }
                    MatlabWriter matlabw = new MatlabWriter(lems, lemsFile.getParentFile(), filename);
                    for(File genFile : matlabw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(CVODE_EXPORT_FLAG))
                {
                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    //String filename = lemsFile.getName().replaceAll("-", "_").replaceAll("." + Format.LEMS.getExtension(), "." + Format.C.getExtension());
                    String filename = generateFormatFilename(lemsFile.getName().replaceAll("-", "_"), Format.C, null);
                    CWriter cw = new CWriter(lems, lemsFile.getParentFile(), filename);
                    cw.setSolver(CWriter.Solver.CVODE);
                    for(File genFile : cw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(MODELICA_EXPORT_FLAG))
                { // Subject to change/removal without notice!!

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    String mFile = generateFormatFilename(lemsFile, Format.MODELICA, null);

                    ModelicaWriter modw = new ModelicaWriter(lems, lemsFile.getParentFile(), mFile);
                    for(File genFile : modw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(DLEMS_EXPORT_FLAG))
                { // Subject to change/removal without notice!!

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    DLemsWriter dlemsw = new DLemsWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.DLEMS, null), null);
                    for(File genFile : dlemsw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }

                }
                else if(args[1].equals(BRIAN_EXPORT_FLAG) || args[1].equals(BRIAN2_EXPORT_FLAG))
                {

                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    String suffix = "_brian";
                    boolean brain2 = false;
                    if(args[1].equals(BRIAN2_EXPORT_FLAG))
                    {
                        brain2 = true;
                        suffix = "_brian2";
                    }
                    String bFile = generateFormatFilename(lemsFile, Format.BRIAN, suffix);

                    BrianWriter bw = new BrianWriter(lems, lemsFile.getParentFile(), bFile);
                    bw.setBrian2(brain2);

                    for(File genFile : bw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }
                else if(args[1].equals(OLD_GRAPH_FLAG) || args[1].equals(LEMS_GRAPH_FLAG))
                {
                    File lemsFile = new File(args[0]);
                    Lems lems = loadLemsFile(lemsFile);

                    GraphWriter gw = new GraphWriter(lems, lemsFile.getParentFile(), generateFormatFilename(lemsFile, Format.GRAPH_VIZ, null));
                    List<File> outputFiles = gw.convert();
                    File gvFile = outputFiles.get(0);
                    System.out.println(GENERATED_FILE + gvFile.getAbsolutePath());

                    String imgFile = gvFile.getAbsolutePath().replace("." + Format.GRAPH_VIZ.getExtension(), "." + Format.PNG.getExtension());
                    String cmd = "dot -Tpng  " + gvFile.getAbsolutePath() + " -o " + imgFile;
                    String[] env = new String[] {};
                    Runtime run = Runtime.getRuntime();
                    Process pr = run.exec(cmd, env, gvFile.getParentFile());

                    try
                    {
                        pr.waitFor();

                        /* Successful termination of command */
                        if (pr.exitValue() == 0)
                        {
                            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                            String line;
                            while((line = buf.readLine()) != null)
                            {
                                System.out.println("----" + line);
                            }
                            System.out.println("Have successfully run command: " + cmd);
                            System.exit(0);
                        }
                        /* Unsuccessful termination of command */
                        else
                        {
                            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                            String line;
                            while((line = buf.readLine()) != null)
                            {
                                System.out.println("----" + line);
                            }
                            System.out.println("Error running command: " + cmd);
                            System.exit(1);
                        }
                    }
                    catch(InterruptedException e)
                    {

                        System.out.println("Error running command: " + cmd);
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                else if(args[1].equals(SVG_FLAG))
                {
                    File nmlFile = new File(args[0]);
                    NeuroMLConverter nmlc = new NeuroMLConverter();
                    NeuroMLDocument nmlDocument = nmlc.loadNeuroML(nmlFile, true, false);

                    SVGWriter svgw = new SVGWriter(nmlDocument, nmlFile.getParentFile(), nmlFile.getName().replaceAll("." + Format.NEUROML2.getExtension(), "." + Format.SVG.getExtension()));
                    for(File genFile : svgw.convert())
                    {
                        System.out.println(GENERATED_FILE + genFile.getAbsolutePath());
                    }
                }
                else if(args[1].equals(PNG_FLAG))
                {
                    File nmlFile = new File(args[0]);
                    NeuroMLConverter nmlc = new NeuroMLConverter();
                    NeuroMLDocument nmlDocument = nmlc.loadNeuroML(nmlFile, true, false);

                    String pngFileName = nmlFile.getName().replaceAll("." + Format.NEUROML2.getExtension(), "." + Format.PNG.getExtension());
                    File pngFile = new File(nmlFile.getParentFile(), pngFileName);
                    SVGWriter svgw = new SVGWriter(nmlDocument, nmlFile.getParentFile(), pngFileName);

                    svgw.convertToPng(pngFile);
                    System.out.println(GENERATED_FILE + pngFile.getAbsolutePath());

                }
                else
                {
                    System.err.println("Unrecognised 2 arguments: " + args[0] + " " + args[1]);
                    showUsage();
                    System.exit(1);

                }
            }
            else if(args.length == 3)
            {
                if (args[1].equals(VHDL_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					VHDLWriter vw = new VHDLWriter(lems);

					Map<String,String> componentScripts = vw.getNeuronModelScripts(args[2],false);
					//String testbenchScript = vw.getSimulationScript(ScriptType.TESTBENCH, args[2], false);
					String prjScript = vw.getPrjFile(componentScripts.keySet());

					for (Map.Entry<String, String> entry : componentScripts.entrySet()) {
						String key = entry.getKey();
						String val = entry.getValue();
						File vwFile = new File(lemsFile.getParentFile(), "/" + key + ".vhdl");
						FileUtil.writeStringToFile(val, vwFile);
						System.out.println(GENERATED_FILE+vwFile.getAbsolutePath());
					}

					/*File vwFile = new File(lemsFile.getParentFile(), "/testbench.vhdl");
					FileUtil.writeStringToFile(testbenchScript, vwFile);
					System.out.println("Writing to: "+vwFile.getAbsolutePath());*/
					File vwFile = new File(lemsFile.getParentFile(), "/testbench.prj");
					FileUtil.writeStringToFile(prjScript, vwFile);
					System.out.println(GENERATED_FILE+vwFile.getAbsolutePath());

                }
            }
            else if(args.length == 4)
            {

                // / importing formats
                if(args[0].equals(SBML_IMPORT_FLAG) || args[0].equals(SBML_IMPORT_UNITS_FLAG))
                {

                    File sbmlFile = new File(args[1]);
                    if(!sbmlFile.exists())
                    {
                        System.err.println("File does not exist: " + sbmlFile.getAbsolutePath());
                        showUsage();
                        System.exit(1);
                    }
                    float duration = Float.parseFloat(args[2]);
                    float dt = Float.parseFloat(args[3]);

                    if(args[0].equals(SBML_IMPORT_UNITS_FLAG))
                    {
                        SBMLImporter.useUnits(true);
                    }
                    else
                    {
                        SBMLImporter.useUnits(false);
                    }

                    File lemsFile = SBMLImporter.convertSBMLToLEMSFile(sbmlFile, duration, dt, true);

                    System.out.println(GENERATED_FILE + lemsFile.getAbsolutePath());

                }
                else
                {
                    System.err.println("Unrecognised 4 arguments: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3]);
                    showUsage();
                    System.exit(1);

                }

            }
            else
            {
                System.err.println("Unrecognised arguments! ");
                showUsage();
                System.exit(1);

            }

        }
        catch(LEMSException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(NeuroMLException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(GenerationException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(JAXBException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(SAXException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(XMLStreamException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch(UnsupportedSBMLFeature e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
