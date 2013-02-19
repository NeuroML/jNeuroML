package org.neuroml;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.lemsml.jlems.expression.ParseError;
import org.lemsml.jlems.run.ConnectionError;
import org.lemsml.jlems.run.RuntimeError;
import org.lemsml.jlems.sim.ContentError;
import org.lemsml.jlems.sim.ParseException;
import org.lemsml.jlems.type.BuildException;
import org.lemsml.jlems.xml.XMLException;
import org.lemsml.jlemsio.Main;
import org.lemsml.jlemsio.logging.DefaultLogger;
import org.lemsml.jlemsio.out.FileResultWriterFactory;
import org.neuroml.model.util.NeuroML2Validator;
import org.lemsml.jlemsviz.datadisplay.SwingDataViewerFactory;
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
	
	static String usage = "Usage: \n\n" +
            "    "+JNML_SCRIPT+" "+VALIDATE_FLAG+" NMLFile.nml\n" +
            "           Validate NMLFile.nml against latest v2beta Schema & perform a number of other tests\n\n"+
            "    "+JNML_SCRIPT+" LEMSFile.xml\n" +
            "           Load LEMSFile.xml using jLEMS, parse it and validate it as LEMS, and execute the model it contains\n\n"+
            "    "+JNML_SCRIPT+" "+HELP_FLAG+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT+"\n" +
            "    "+JNML_SCRIPT+" "+HELP_FLAG_SHORT_Q+"\n" +
            "           Print this help information\n\n";

	public static void showUsage() {
		System.out.println(usage);
	}

	public static void main(String[] args) throws SAXException, IOException, JAXBException {
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
				
				try {
					Main.main(args);
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
				}
				
			}
			
		} else if (args.length == 2) {
			if  (args[0].equals(VALIDATE_FLAG))
			{
				File xmlFile = new File(args[1]);
				if (!xmlFile.exists()) {
					System.err.println("File does not exist: "+args[1]);
					showUsage();
					System.exit(1);
				}
				NeuroML2Validator nmlv =  new NeuroML2Validator();
				nmlv.validateWithTests(xmlFile);
			}
		}
	}
}
