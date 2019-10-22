package test.system;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;
import tools.RegressionInvariantGenerator;

public class RegressionInvariantGeneratorTestT6 extends TestCase {

	/**
	 * P1 t0 *
	 * P2 t1 -
	 * P3 t1 *
	 * P2 t2 -
	 * P4 t2 -
	 * P1 t2 *
	 * P3 t3 
	 * P4 t3 -
	 * @throws IOException
	 */
	public void testT6() throws IOException{
		
		RegressionInvariantGeneratorTest.setTestHome("T6");
		
		String args[]={"-default","-skipLP"};
		RegressionInvariantGenerator.main(args);
		
		
		//Check interaction matrix
		
		
		
		//
		//Check IO matrix
		//
		
		FileInputStream is = new FileInputStream(RegressionInvariantGeneratorUtil.ioMatrixFile);
		
		GenericTcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 4, dm.getTestCasesNumber() );
		
		
		Iterator<TestCaseInfo> testIt = dm.getTestCasesIterator();
		TestCaseInfo testInfo = testIt.next();
		
		assertEquals("",RegressionInvariantGeneratorUtil.test3Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test2Name, testInfo.getName());
		
		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test0Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test1Name, testInfo.getName());
		
		
		
		

		//Check covered elements names
		assertEquals ( 5,dm.getElementsToCover().size() );
		
		Iterator<String> elementsIt = dm.getElementsToCoverIterator();
		String el = elementsIt.next();
		el = elementsIt.next();
		el = elementsIt.next();
		el = elementsIt.next();
		el = elementsIt.next();
		assertFalse(elementsIt.hasNext());
		
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementA0, el );
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementB0, el );
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementC0, el );
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementD0, el );
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementD1, el );
		
		
		
		//check coverage info
		
		
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,false,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test0Name)));
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,false,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test1Name)));
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,false,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test2Name)));
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,false,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test3Name)));		
		
		is.close();
		
		RegressionInvariantGeneratorUtil.clean("T6");
	}
}
