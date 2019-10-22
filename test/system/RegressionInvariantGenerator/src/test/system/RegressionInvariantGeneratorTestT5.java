package test.system;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;
import tools.RegressionInvariantGenerator;

public class RegressionInvariantGeneratorTestT5 extends TestCase {

	public void testT3() throws IOException{
		
		RegressionInvariantGeneratorTest.setTestHome("T5");
		
		String args[]={"-default","-skipLP"};
		RegressionInvariantGenerator.main(args);
		
		
		//Check interaction matrix
		
		FileInputStream is = new FileInputStream(RegressionInvariantGeneratorUtil.interactionMatrixFile);
		
		GenericTcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 3, dm.getTestCasesNumber() );
		
		
		Iterator<TestCaseInfo> testIt = dm.getTestCasesIterator();
		
		TestCaseInfo testInfo;
		

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test2Name, testInfo.getName());
		
		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test0Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test1Name, testInfo.getName());
		
		
		
		
		//Check covered elements
		assertEquals ( 4,dm.getElementsToCover().size() );
		
		Iterator<String> elementsIt = dm.getElementsToCoverIterator();
		String el;

		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.methodDDm, el );
		
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.methodCCm, el );


		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.methodAAm, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.methodBBm, el );

		
		assertFalse(elementsIt.hasNext());
		
		
		//Check coverage
		Iterator<Boolean[]> it = dm.coverageIterator();
		Boolean[] vector = it.next();
		it.next();
		it.next();
		assertFalse(it.hasNext());
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{true,false,false,false},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test2Name)));		
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,true,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test0Name)));
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{true,true,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test1Name)));
		
		
		
		
		//
		//Check IO matrix
		//
		
		is = new FileInputStream(RegressionInvariantGeneratorUtil.ioMatrixFile);
		
		dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 3, dm.getTestCasesNumber() );
		
		
		testIt = dm.getTestCasesIterator();
		
		

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test2Name, testInfo.getName());
		
		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test0Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test1Name, testInfo.getName());
		
		
		
		

		//Check covered elements names
		assertEquals ( 5,dm.getElementsToCover().size() );
		
		elementsIt = dm.getElementsToCoverIterator();
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementA0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementB0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementC0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementD0, el );
		
		el = elementsIt.next();
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementD1, el );
		
		assertFalse(it.hasNext());
		
		//check coverage info
		it = dm.coverageIterator();
		
		vector = it.next();
		vector = it.next();
		vector = it.next();
		assertFalse(it.hasNext());
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,false,false,false,true},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test2Name)));
		
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{true,true,false,false,false},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test0Name)));
		
		
		assertTrue(RegressionInvariantGeneratorTest.vectorsEqual(new boolean[]{false,true,true,true,false},dm.getCoverageVectorForTest(RegressionInvariantGeneratorUtil.test1Name)));
		
		
		
		is.close();
		
		RegressionInvariantGeneratorUtil.clean("T5");
	}
}
