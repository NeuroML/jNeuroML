package org.neuroml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.lemsml.jlems.ResourceRoot;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.run.ConnectionError;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.ParseException;
import org.lemsml.jlems.core.sim.Sim;
import org.lemsml.jlems.core.type.BuildException;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.xml.XMLException;
import org.lemsml.jlems.io.Main;
import org.lemsml.jlems.io.logging.DefaultLogger;
import org.lemsml.jlems.io.out.FileResultWriterFactory;
import org.lemsml.jlems.io.reader.FileInclusionReader;
import org.lemsml.jlems.io.reader.JarResourceInclusionReader;
import org.lemsml.jlems.io.reader.PathInclusionReader;
import org.lemsml.jlems.io.util.FileUtil;
import org.lemsml.jlems.io.util.JUtil;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.neuroml.export.Utils;
import org.neuroml.export.brian.BrianWriter;
import org.neuroml.export.graph.GraphWriter;
import org.neuroml.export.neuron.NeuronWriter;
import org.neuroml.export.sbml.SBMLWriter;
import org.neuroml.export.xpp.XppWriter;
import org.neuroml.importer.sbml.SBMLImporter;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml1.model.util.NeuroML1Validator;
import org.sbml.jsbml.SBMLException;
import org.lemsml.jlems.viz.datadisplay.SwingDataViewerFactory;
import org.xml.sax.SAXException;


public class JNeuroML {

	public static String JNML_SCRIPT = "jnml";

	public static String JNML_VERSION = "0.2.9";

	public static String HELP_FLAG = "-help";
	public static String HELP_FLAG_SHORT = "-h";
	public static String HELP_FLAG_SHORT_Q = "-?";

	public static String NO_GUI_FLAG = "-nogui";

	public static String VALIDATE_FLAG = "-validate";
	public static String VALIDATE_V1_FLAG = "-validatev1";

	public static String XPP_EXPORT_FLAG = "-xpp";

	public static String BRIAN_EXPORT_FLAG = "-brian";

	public static String NEURON_EXPORT_FLAG = "-neuron";

	public static String SBML_IMPORT_FLAG = "-sbml-import";
	public static String SBML_EXPORT_FLAG = "-sbml";

	public static String GRAPH_FLAG = "-graph";

	static String usage = "Usage: \n\n" +
            "    "+JNML_SCRIPT+" LEMSFile.xml\n" +
            "           Load LEMSFile.xml using jLEMS, parse it and validate it as LEMS, and execute the model it contains\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+NO_GUI_FLAG+"\n" +
            "           As above, parse and execute the model and save results, but don't show GUI\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+GRAPH_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to GraphViz format\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+XPP_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to XPPAUT format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+BRIAN_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to Brian format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+SBML_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to SBML format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+NEURON_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to NEURON format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" "+SBML_IMPORT_FLAG+" SBMLFile.sbml duration dt\n" +
            "           Load SBMLFile.sbml using jSBML, and convert it to LEMS format using values for duration & dt in ms (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" "+VALIDATE_FLAG+" NMLFile.nml\n" +
            "           Validate NMLFile.nml against latest v2beta Schema & perform a number of other tests\n\n"+
            "    "+JNML_SCRIPT+" "+VALIDATE_V1_FLAG+" NMLFile.nml\n" +
            "           Validate NMLFile.nml against NeuroML v1.8.1 Schema \n\n"+
            "    "+JNML_SCRIPT+" "+HELP_FLAG+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT_Q+"\n" +
            "           Print this help information\n\n";

	public static void showUsage() {
		System.out.println(usage);
	}

	/*
	private static Lems loadLemsFile(String filename) throws ContentError, ParseError, ParseException, BuildException, XMLException {
		File lemsFile = new File(filename);

		return loadLemsFile(lemsFile);
	}*/

	private static Lems loadLemsFile(File lemsFile) throws ContentError, ParseError, ParseException, BuildException, XMLException {

		if (!lemsFile.exists()) {
			System.err.println("File does not exist: "+lemsFile.getAbsolutePath());
			showUsage();
			System.exit(1);
		}
		return Utils.loadLemsFile(lemsFile);
	}

	public static void main(String[] args) throws SBMLException, org.sbml.jsbml.text.parser.ParseException, RuntimeError {
		System.out.println(" jNeuroML v"+JNML_VERSION);
		
		

		//System.out.println("File: "+JUtil.getRelativeResource("/NeuroML2CoreTypes/Cells.xml"));
		
		/*
		String jnmlHome = System.getenv("JNML_HOME");
        if (jnmlHome!=null) {
			File nmlCoreTypesDir = new File(jnmlHome+"/../NeuroML2/NeuroML2CoreTypes");
			FileInclusionReader.addSearchPath(nmlCoreTypesDir);
        } else {
			File nmlCoreTypesDir = new File(System.getenv("HOME")+"/NeuroML2/NeuroML2CoreTypes");
			FileInclusionReader.addSearchPath(nmlCoreTypesDir);
        }*/

		try {
			if (args.length == 0) {
				System.err.println("Error, no arguments to "+JNML_SCRIPT);
				showUsage();
				System.exit(1);

		// One argument

			} else if (args.length == 1) {

				if (args[0].startsWith("-")) {
					if (args[0].equals(HELP_FLAG) || args[0].equals(HELP_FLAG_SHORT) || args[0].equals(HELP_FLAG_SHORT_Q)) {
						showUsage();
						System.exit(0);
					} else {
						System.err.println("Unrecognised argument: "+args[0]);
						showUsage();
						System.exit(1);
					}
				} else {
					File lemsFile = new File(args[0]);
					if (!lemsFile.exists()) {
						System.err.println("File does not exist: "+args[0]);
						showUsage();
						System.exit(1);
					}

					System.out.println("Loading: "+lemsFile.getAbsolutePath()+" with jLEMS...");
			    	FileResultWriterFactory.initialize();
			    	SwingDataViewerFactory.initialize();
					DefaultLogger.initialize();

					runLemsFile(lemsFile);
					//Main.main(args);

				}

		// Two arguments

			} else if (args.length == 2) {

			    ///  Run LEMS with no gui

				if  (args[1].equals(NO_GUI_FLAG)) {

					File lemsFile = new File(args[0]);
					if (!lemsFile.exists()) {
						System.err.println("File does not exist: "+args[0]);
						showUsage();
						System.exit(1);
					}

					System.out.println("Loading: "+lemsFile.getAbsolutePath()+" with jLEMS, NO GUI mode...");
			    	FileResultWriterFactory.initialize();
					DefaultLogger.initialize();

					runLemsFile(lemsFile);
					//Main.main(args);

			    ///  Validation

				} else if  (args[0].equals(VALIDATE_FLAG)) {
					File xmlFile = new File(args[1]);
					if (!xmlFile.exists()) {
						System.err.println("File does not exist: "+args[1]);
						showUsage();
						System.exit(1);
					}
					NeuroML2Validator nmlv =  new NeuroML2Validator();
					nmlv.validateWithTests(xmlFile);
				} else if (args[0].equals(VALIDATE_V1_FLAG)) {
					File xmlFile = new File(args[1]);
					if (!xmlFile.exists()) {
						System.err.println("File does not exist: "+args[1]);
						showUsage();
						System.exit(1);
					}
					NeuroML1Validator nmlv =  new NeuroML1Validator();
					nmlv.validateWithTests(xmlFile);


			///  exporting formats

				} else if (args[1].equals(SBML_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					SBMLWriter sbmlw = new SBMLWriter(lems);
			        String sbml = sbmlw.getMainScript();

			        File sbmlFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".sbml"));
			        System.out.println("Writing to: "+sbmlFile.getAbsolutePath());

			        FileUtil.writeStringToFile(sbml, sbmlFile);

				} else if (args[1].equals(XPP_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					XppWriter xppw = new XppWriter(lems);
			        String ode = xppw.getMainScript();

			        File odeFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".ode"));
			        System.out.println("Writing to: "+odeFile.getAbsolutePath());

			        FileUtil.writeStringToFile(ode, odeFile);

				} else if (args[1].equals(NEURON_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					NeuronWriter nw = new NeuronWriter(lems);
			        String nrn = nw.getMainScript();

			        File nrnFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", "_nrn.py"));
			        System.out.println("Writing to: "+nrnFile.getAbsolutePath());

			        FileUtil.writeStringToFile(nrn, nrnFile);

				} else if (args[1].equals(BRIAN_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					BrianWriter bw = new BrianWriter(lems);
			        String br = bw.getMainScript();

			        File brFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", "_brian.py"));
			        System.out.println("Writing to: "+brFile.getAbsolutePath());

			        FileUtil.writeStringToFile(br, brFile);

				} else if (args[1].equals(GRAPH_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					GraphWriter gw = new GraphWriter(lems);
			        String gv = gw.getMainScript();

			        File gvFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".gv"));
			        System.out.println("Writing to: "+gvFile.getAbsolutePath());


			        FileUtil.writeStringToFile(gv, gvFile);
			        String imgFile = gvFile.getAbsolutePath().replace(".gv", ".png");

                    String cmd = "dot -Tpng  " + gvFile.getAbsolutePath() + " -o " + imgFile;
                    String[] env = new String[]{};
                    Runtime run = Runtime.getRuntime();
                    Process pr = run.exec(cmd, env, gvFile.getParentFile());


                    try {
						pr.waitFor();

	                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
	                    String line;
	                    while ((line = buf.readLine()) != null) {
	                        System.out.println("----" + line);
	                    }

	                    System.out.println("Have successfully run command: " + cmd);

                    } catch (InterruptedException e) {

	                    System.out.println("Error running command: " + cmd);
						e.printStackTrace();
					}

				} else {
					System.err.println("Unrecognised arguments: "+args[0]+" "+args[1]);
					showUsage();
					System.exit(1);

				}
			} else if (args.length == 4) {

				///  importing formats

				if (args[0].equals(SBML_IMPORT_FLAG)) {

					File sbmlFile = new File(args[1]);
					if (!sbmlFile.exists()) {
						System.err.println("File does not exist: "+sbmlFile.getAbsolutePath());
						showUsage();
						System.exit(1);
					}
					float duration = Float.parseFloat(args[2]);
					float dt = Float.parseFloat(args[3]);
					Lems lems = SBMLImporter.convertSBMLToLEMS(sbmlFile, duration, dt);

					String newName = sbmlFile.getName().replaceAll(".xml", "_LEMS.xml");
					newName = newName.replaceAll(".sbml", "_LEMS.xml");
			        File lemsFile = new File(sbmlFile.getParentFile(),newName);

			        System.out.println("Writing to: "+lemsFile.getAbsolutePath());
			        String lemsString  = XMLSerializer.serialize(lems);

			        FileUtil.writeStringToFile(lemsString, lemsFile);

				} else {
					System.err.println("Unrecognised arguments: "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]);
					showUsage();
					System.exit(1);

				}

			} else {
				System.err.println("Unrecognised arguments! ");
				showUsage();
				System.exit(1);

			}

		} catch (ConnectionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ContentError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void runLemsFile(File f) throws ContentError, ParseError, ParseException, BuildException, XMLException, ConnectionError, RuntimeError{

		JarResourceInclusionReader.addSearchPathInJar("/NeuroML2CoreTypes");
		JarResourceInclusionReader.addSearchPath(f.getParentFile());
		
		JarResourceInclusionReader jrir = new JarResourceInclusionReader(f);
		
        Sim sim = new Sim(jrir.read());
            
        sim.readModel();
        sim.build();
    	sim.run();
    	E.info("Finished reading, building, running & displaying LEMS model");
		
	}
	

}
