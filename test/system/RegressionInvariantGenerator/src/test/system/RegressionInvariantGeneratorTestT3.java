package test.system;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;
import tools.RegressionInvariantGenerator;

public class RegressionInvariantGeneratorTestT3 extends TestCase {

	public void testT3() throws IOException{
		
		RegressionInvariantGeneratorTest.setTestHome("T3");
		
		String args[]={"-default","-skipLP"};
		RegressionInvariantGenerator.main(args);
		
		
		//Check interaction matrix
		
		FileInputStream is = new FileInputStream(RegressionInvariantGeneratorUtil.interactionMatrixFile);
		
		GenericTcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 2, dm.getTestCasesNumber() );
		
		
		Iterator<TestCaseInfo> testIt = dm.getTestCasesIterator();
		
		TestCaseInfo testInfo;
		
		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test0Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test1Name, testInfo.getName());
		
		
		//Check covered elements
		assertEquals ( 3,dm.getElementsToCover().size() );
		
		Iterator<String> elementsIt = dm.getElementsToCoverIterator();
		String el;
		
		el = elementsIt.next();
		
		System.out.println(RegressionInvariantGeneratorUtil.methodCCm);
		
		assertEquals ( RegressionInvariantGeneratorUtil.methodCCm, el );

		
		el = elementsIt.next();
		
		assertEquals ( RegressionInvariantGeneratorUtil.methodAAm, el );
		
		
		
		el = elementsIt.next();
		
		assertEquals ( RegressionInvariantGeneratorUtil.methodBBm, el );

		
		assertFalse(elementsIt.hasNext());
		
		
		//Check coverage
		Iterator<Boolean[]> it = dm.coverageIterator();
		Boolean[] vector = it.next();
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,true,true},vector));
		
		
		
		vector = it.next();
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{true,false,true},vector));
		
		
		//
		//Check IO matrix
		//
		
		is = new FileInputStream(RegressionInvariantGeneratorUtil.ioMatrixFile);
		
		dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 2, dm.getTestCasesNumber() );
		
		
		
		

		//Check covered elements names
		assertEquals ( 3,dm.getElementsToCover().size() );
		
		elementsIt = dm.getElementsToCoverIterator();
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementA0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementB0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementC0, el );
		
		assertFalse(elementsIt.hasNext());
		
		//check coverage info
		it = dm.coverageIterator();
		
		
		
		vector = it.next();
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{true,true,false},vector));
		

		vector = it.next();
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,true,true},vector));
		
		
		
		
		is.close();
		
		RegressionInvariantGeneratorUtil.clean("T3");
	}
}
