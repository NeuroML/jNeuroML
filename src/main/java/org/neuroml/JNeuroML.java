package org.neuroml;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.run.ConnectionError;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.ParseException;
import org.lemsml.jlems.core.type.BuildException;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.xml.XMLException;
import org.lemsml.jlems.io.Main;
import org.lemsml.jlems.io.logging.DefaultLogger;
import org.lemsml.jlems.io.out.FileResultWriterFactory;
import org.lemsml.jlems.io.reader.FileInclusionReader;
import org.lemsml.jlems.io.util.FileUtil;
import org.neuroml.export.Utils;
import org.neuroml.export.xpp.XppWriter;
import org.neuroml.model.util.NeuroML2Validator;
import org.lemsml.jlems.viz.datadisplay.SwingDataViewerFactory;
import org.xml.sax.SAXException;

/**
 * Hello world!
 * 
 */
public class JNeuroML {

	public static String JNML_SCRIPT = "jnml";
	
	public static String HELP_FLAG = "-help";
	public static String HELP_FLAG_SHORT = "-h";
	public static String HELP_FLAG_SHORT_Q = "-?";

	public static String VALIDATE_FLAG = "-validate";
	
	public static String XPP_FLAG = "-xpp";
	
	static String usage = "Usage: \n\n" +
            "    "+JNML_SCRIPT+" LEMSFile.xml\n" +
            "           Load LEMSFile.xml using jLEMS, parse it and validate it as LEMS, and execute the model it contains\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml "+XPP_FLAG+"\n" +
            "           Load LEMSFile.xml using jLEMS, and convert it to XPP format\n\n"+
            "    "+JNML_SCRIPT+" "+VALIDATE_FLAG+" NMLFile.nml\n" +
            "           Validate NMLFile.nml against latest v2beta Schema & perform a number of other tests\n\n"+
            "    "+JNML_SCRIPT+" "+HELP_FLAG+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT_Q+"\n" +
            "           Print this help information\n\n";

	public static void showUsage() {
		System.out.println(usage);
	}

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.err.println("Error, no arguments to "+JNML_SCRIPT);
				showUsage();
				System.exit(1);
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
					
					//TODO: add from jar instead!
					String jnmlHome = System.getenv("JNML_HOME");
			        if (jnmlHome!=null) {
						File nmlCoreTypesDir = new File(jnmlHome+"/../NeuroML2/NeuroML2CoreTypes");
						FileInclusionReader.addSearchPath(nmlCoreTypesDir);
			        } else {
						File nmlCoreTypesDir = new File(System.getenv("HOME")+"/NeuroML2/NeuroML2CoreTypes");
						FileInclusionReader.addSearchPath(nmlCoreTypesDir);
			        }
			        
					
					Main.main(args);
					
				}
				
			} else if (args.length == 2) {
				if  (args[0].equals(VALIDATE_FLAG)) {
					File xmlFile = new File(args[1]);
					if (!xmlFile.exists()) {
						System.err.println("File does not exist: "+args[1]);
						showUsage();
						System.exit(1);
					}
					NeuroML2Validator nmlv =  new NeuroML2Validator();
					nmlv.validateWithTests(xmlFile);
				} else if (args[1].equals(XPP_FLAG)) {
					File lemsFile = new File(args[0]);
					if (!lemsFile.exists()) {
						System.err.println("File does not exist: "+args[0]);
						showUsage();
						System.exit(1);
					}
			        
					Lems lems = Utils.loadLemsFile(lemsFile);
	
					XppWriter xppw = new XppWriter(lems);
			        String ode = xppw.getMainScript();
	
			        File odeFile = new File(lemsFile.getParentFile(),lemsFile.getName().replaceAll(".xml", ".ode"));
			        System.out.println("Writing to: "+odeFile.getAbsolutePath());
			        
			        FileUtil.writeStringToFile(ode, odeFile);
					
				} else {
					System.err.println("Unrecognised arguments: "+args[0]+" "+args[1]);
					showUsage();
					System.exit(1);
					
				}
			}
		
		} catch (ConnectionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ContentError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeError e) {
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
		} 
	}
	
}
