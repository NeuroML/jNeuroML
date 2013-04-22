package org.neuroml;

import java.io.File;
import java.io.IOException;

import org.lemsml.jlems.io.util.FileUtil;

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
    	
    	assert(jnmlPom.contains("<version>"+JNeuroML.JNML_VERSION+"</version>"));
    	
    }
}
