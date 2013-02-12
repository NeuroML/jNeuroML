package org.neuroml;

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

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	System.out.println("Running a simple test..");
        assertTrue( true );
    }
}
