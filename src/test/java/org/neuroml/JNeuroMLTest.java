package org.neuroml;

import java.io.File;
import java.io.IOException;

import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.xml.XMLElementReader;
import org.lemsml.jlems.io.util.FileUtil;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class JNeuroMLTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public JNeuroMLTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( JNeuroMLTest.class );
    }


    public void testVersions() throws IOException
    {
    	System.out.println("Running a test on version usage, making all references to versions are: v"+JNeuroML.JNML_VERSION+"...");

    	String jnmlScript = FileUtil.readStringFromFile(new File("jnml"));
    	
    	assert(jnmlScript.contains("JNML_VERSION="+JNeuroML.JNML_VERSION));
    	
    	String jnmlBat = FileUtil.readStringFromFile(new File("jnml.bat"));
    	
    	assert(jnmlBat.contains("JNML_VERSION="+JNeuroML.JNML_VERSION));
    	
    	String jnmlPom = FileUtil.readStringFromFile(new File("pom.xml"));

    	XMLElementReader xer = new XMLElementReader(jnmlPom);
    	assertEquals(JNeuroML.JNML_VERSION, xer.getRootElement().getElement("version").getBody());
    	
    }
    
    public void testReadingFile() throws SBMLException, RuntimeError, ParseException
    {
    	//File lemsFile = (new File("/home/finnk/Desktop/Sielegans/LEMS2HDL/git/jNeuroML/test/A1_iafCell.xml")).getCanonicalFile();
        String[] args = {"/home/finnk/Desktop/Sielegans/LEMS2HDL/git/jNeuroML/test/A1_iafCell.xml","-vhdl"};
    	JNeuroML.main(args);
    	
    }
}
