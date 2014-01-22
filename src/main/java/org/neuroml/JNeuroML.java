package org.neuroml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
//vhdl
import org.lemsml.export.vhdl.VHDLWriter;
//vhdl
import org.lemsml.export.matlab.MatlabWriter;
import org.lemsml.export.modelica.ModelicaWriter;
import org.lemsml.export.sedml.SEDMLWriter;
import org.lemsml.export.som.SOMWriter;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.logging.MinimalMessageHandler;
import org.lemsml.jlems.core.run.ConnectionError;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.ParseException;
import org.lemsml.jlems.core.sim.Sim;
import org.lemsml.jlems.core.type.BuildException;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.xml.XMLException;
import org.lemsml.jlems.io.IOUtil;
import org.lemsml.jlems.io.logging.DefaultLogger;
import org.lemsml.jlems.io.out.FileResultWriterFactory;
import org.lemsml.jlems.io.util.FileUtil;
import org.lemsml.jlems.viz.datadisplay.SwingDataViewerFactory;
import org.neuroml.export.Utils;
import org.neuroml.export.brian.BrianWriter;
import org.neuroml.export.graph.GraphWriter;
import org.neuroml.export.neuron.NeuronWriter;
import org.neuroml.export.sbml.SBMLWriter;
import org.neuroml.export.svg.SVGWriter;
import org.neuroml.export.xpp.XppWriter;
import org.neuroml.importer.sbml.SBMLImporter;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml1.model.util.NeuroML1Validator;
import org.sbml.jsbml.SBMLException;
import org.xml.sax.SAXException;


public class JNeuroML {

	public static String JNML_SCRIPT = "jnml";

	public static String JNML_VERSION = "0.3.5";

	public static String HELP_FLAG = "-help";
	public static String HELP_FLAG_SHORT = "-h";
	public static String HELP_FLAG_SHORT_Q = "-?";

	public static String NO_GUI_FLAG = "-nogui";
	
	public static String NO_RUN_FLAG = "-norun";

	public static String VALIDATE_FLAG = "-validate";
	public static String VALIDATE_V1_FLAG = "-validatev1";

	public static String XPP_EXPORT_FLAG = "-xpp";

	public static String BRIAN_EXPORT_FLAG = "-brian";
	
	//vhdl
	public static String VHDL_EXPORT_FLAG = "-vhdl";
	//end vhdl
	
	public static String MATLAB_EXPORT_FLAG = "-matlab";
	public static String MATLAB_EULER_EXPORT_FLAG = "-matlab-euler";

	public static String MODELICA_EXPORT_FLAG = "-modelica";
	
	public static String SOM_EXPORT_FLAG = "-som";   // Subject to change/removal without notice!!

	public static String SEDML_EXPORT_FLAG = "-sedml";

	public static String NEURON_EXPORT_FLAG = "-neuron";

	public static String SBML_IMPORT_FLAG = "-sbml-import";
	public static String SBML_EXPORT_FLAG = "-sbml";

	public static String GRAPH_FLAG = "-graph";
	
	public static String SVG_FLAG = "-svg";

	static String usage = "Usage: \n\n" +
            "    "+JNML_SCRIPT+" LEMSFile.xml\n" +
            "           Load LEMSFile.xml using jLEMS, parse it and validate it as LEMS, and execute the model it contains\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+NO_GUI_FLAG+"\n" +
            "           As above, parse and execute the model and save results, but don't show GUI\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+NO_RUN_FLAG+"\n" +
            "           Parse the LEMS file, but don't run the simulation\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+GRAPH_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to GraphViz format\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+SEDML_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to SED-ML format\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+XPP_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to XPPAUT format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+BRIAN_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to Brian format (**EXPERIMENTAL**)\n\n"+
			//vhdl
            "    "+JNML_SCRIPT+" LEMSFile.xml "+VHDL_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to VHDL format (**EXPERIMENTAL**)\n\n"+
			//vhdl
			//vhdl
            "    "+JNML_SCRIPT+" LEMSFile.xml "+SBML_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to SBML format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+NEURON_EXPORT_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to NEURON format (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" "+SBML_IMPORT_FLAG+" SBMLFile.sbml duration dt\n" +
            "           Load SBMLFile.sbml using jSBML, and convert it to LEMS format using values for duration & dt in ms (**EXPERIMENTAL**)\n\n"+
            "    "+JNML_SCRIPT+" NMLFile.nml "+SVG_FLAG+"\n" +
            "           Load NMLFile.nml and convert cell(s) to SVG image format (**EXPERIMENTAL**)\n\n"+
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

	private static Lems loadLemsFile(File lemsFile) throws ContentError, ParseError, ParseException, BuildException, XMLException, ConnectionError, RuntimeError {

		if (!lemsFile.exists()) {
			System.err.println("File does not exist: "+lemsFile.getAbsolutePath());
			showUsage();
			System.exit(1);
		}
		return Utils.readLemsNeuroMLFile(lemsFile).getLems();
	}

	public static void main(String[] args) throws SBMLException, org.sbml.jsbml.text.parser.ParseException, RuntimeError {

		MinimalMessageHandler.setVeryMinimal(true);
		E.setDebug(false);
		
		System.out.println(" jNeuroML v"+JNML_VERSION);

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
				}

			///  Parse LEMS & exit

				else if  (args[1].equals(NO_RUN_FLAG)) {

					File lemsFile = new File(args[0]);
					if (!lemsFile.exists()) {
						System.err.println("File does not exist: "+args[0]);
						showUsage();
						System.exit(1);
					}

					System.out.println("Loading: "+lemsFile.getAbsolutePath()+" with jLEMS, NO RUN mode...");

					loadLemsFile(lemsFile, false);

				} else if  (args[0].equals(VALIDATE_FLAG)) {
					File xmlFile = new File(args[1]);
					if (!xmlFile.exists()) {
						System.err.println("File does not exist: "+args[1]);
						showUsage();
						System.exit(1);
					}
					NeuroML2Validator nmlv =  new NeuroML2Validator();
					nmlv.validateWithTests(xmlFile);
					if (nmlv.isValid() && !nmlv.hasWarnings()) {
				        System.out.println(nmlv.getValidity());
				        System.out.println(nmlv.getWarnings());
					} else {
				        System.err.println(nmlv.getValidity());
				        System.err.println(nmlv.getWarnings());
						System.exit(1);
					}
						
					
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

				} else if (args[1].equals(SEDML_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					SEDMLWriter sedw = new SEDMLWriter(lems, lemsFile.getAbsolutePath(), SEDMLWriter.ModelFormat.NEUROML2);
			        String sed = sedw.getMainScript();

			        File sedFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".sedml"));
			        System.out.println("Writing to: "+sedFile.getAbsolutePath());

			        FileUtil.writeStringToFile(sed, sedFile);


				} else if (args[1].equals(MATLAB_EXPORT_FLAG) || args[1].equals(MATLAB_EULER_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					MatlabWriter matlabw = new MatlabWriter(lems);
					if (args[1].equals(MATLAB_EULER_EXPORT_FLAG))
					{
						matlabw.setMethod(MatlabWriter.Method.EULER);
					}
			        String matlab = matlabw.getMainScript();

			        String filename = lemsFile.getName().replaceAll("-", "_").replaceAll(".xml", ".m");
			        
			        if (!Character.isLetter(filename.charAt(0)))
			        	filename = "M_"+filename;
			        
			        File matlabFile = new File(lemsFile.getParentFile(),filename);
			        
			        System.out.println("Writing to: "+matlabFile.getAbsolutePath());

			        FileUtil.writeStringToFile(matlab, matlabFile);


				} else if (args[1].equals(MODELICA_EXPORT_FLAG)) {  // Subject to change/removal without notice!!

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					ModelicaWriter modw = new ModelicaWriter(lems);
					File tgtDir = lemsFile.getAbsoluteFile().getParentFile();
					
			        System.out.println("Converting "+lemsFile+" to Modelica to: "+tgtDir);
			        
					String main = modw.generateMainScriptAndCompFiles(tgtDir);
					
			        System.out.println(main);
					for (File genFile: modw.allGeneratedFiles)
			        {
				        System.out.println("Writing to: "+genFile.getAbsolutePath());
			        }


				} else if (args[1].equals(SOM_EXPORT_FLAG)) {  // Subject to change/removal without notice!!

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					SOMWriter somw = new SOMWriter(lems);
			        String som = somw.getMainScript();

			        File somFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".json"));
			        System.out.println("Writing to: "+somFile.getAbsolutePath());

			        FileUtil.writeStringToFile(som, somFile);
			        
				} else if (args[1].equals(NEURON_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					NeuronWriter nw = new NeuronWriter(lems);
			        String nrn = nw.getMainScript();

			        File nrnFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", "_nrn.py"));
			        System.out.println("Writing to: "+nrnFile.getAbsolutePath());

			        FileUtil.writeStringToFile(nrn, nrnFile);

				}
				//vhdl
				else if (args[1].equals(VHDL_EXPORT_FLAG)) {

					File lemsFile = new File(args[0]);
					Lems lems = loadLemsFile(lemsFile);

					VHDLWriter vw = new VHDLWriter(lems);
			        Map<String,String> componentScripts = vw.getComponentScripts();
					String testbenchScript = vw.getMainScript();
					for (Map.Entry<String, String> entry : componentScripts.entrySet()) {
						String key = entry.getKey();
						String val = entry.getValue();
						File vwFile = new File(lemsFile.getParentFile(),  "/" + key + ".vhdl");
						FileUtil.writeStringToFile(val, vwFile);
						System.out.println("Writing to: "+vwFile.getAbsolutePath());
					}
					File vwFile = new File(lemsFile.getParentFile(),  "/testbench.vhdl");
					FileUtil.writeStringToFile(testbenchScript, vwFile);
					System.out.println("Writing to: "+vwFile.getAbsolutePath());
					System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"); 

				} 
				//end vhdl
				else if (args[1].equals(GRAPH_FLAG)) {

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
						System.exit(1);
					}
				} else if (args[1].equals(SVG_FLAG)) {

    					File nmlFile = new File(args[0]);
    					
    					NeuroMLConverter nmlc = new NeuroMLConverter();
    			    	NeuroMLDocument nmlDocument = nmlc.loadNeuroML(nmlFile);

    			    	SVGWriter svgw = new SVGWriter(nmlDocument, nmlFile.getAbsolutePath());
    			        String svg = svgw.getMainScript();
    			        
    			        String newName = nmlFile.getName().replaceAll(".nml", ".svg");
    			        newName = newName.replaceAll(".xml", ".svg");
    			        
    			        File svgFile = new File(nmlFile.getParentFile(),newName);
    			        System.out.println("Writing to: "+svgFile.getAbsolutePath());

    			        FileUtil.writeStringToFile(svg, svgFile);


				} else {
					System.err.println("Unrecognised 2 arguments: "+args[0]+" "+args[1]);
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
			
			        File lemsFile = SBMLImporter.convertSBMLToLEMSFile(sbmlFile, duration, dt, true);

			        System.out.println("Written to: "+lemsFile.getAbsolutePath());

				} else {
					System.err.println("Unrecognised 4 arguments: "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]);
					showUsage();
					System.exit(1);

				}

			} else {
				System.err.println("Unrecognised arguments! ");
				showUsage();
				System.exit(1);

			}

		} catch (ConnectionError e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ContentError e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseError e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (BuildException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (XMLException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



	public static void runLemsFile(File f) throws ContentError, ParseError, ParseException, BuildException, XMLException, ConnectionError, RuntimeError {
		loadLemsFile(f, true);
	}


	public static void loadLemsFile(File f, boolean run) throws ContentError, ParseError, ParseException, BuildException, XMLException, ConnectionError, RuntimeError {

        Sim sim = Utils.readLemsNeuroMLFile(f);
        sim.build();
        
    	if (run) {
    		sim.run();
    		IOUtil.saveReportAndTimesFile(sim);
    		E.info("Finished reading, building, running and displaying LEMS model");
    	} else {
    		E.info("Finished reading and building LEMS model");
    	}
		
	}
	

}
