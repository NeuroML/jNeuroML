package org.neuroml;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.neuroml.model.util.NeuroML2Validator;
import org.xml.sax.SAXException;

/**
 * Hello world!
 * 
 */
public class JNeuroML {
	
	static String mainScript = "jnml";
	
	static String usage = "USAGE: \n\n" +
			              "     "+mainScript+" -validate NMLFile.nml\n" +
			              "          Validate NMLFile.nml against latest Schema & perform a number of other tests\n" +
			              "\n";

	public static void showUsage() {
		System.out.println(usage);
	}

	public static void main(String[] args) throws SAXException, IOException, JAXBException {
		if (args.length == 0) {
			System.err.println("Error, no arguments to "+mainScript);
			showUsage();
			System.exit(1);
		}
		
		if (args.length == 2 && args[0].equals("-validate")) {
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
