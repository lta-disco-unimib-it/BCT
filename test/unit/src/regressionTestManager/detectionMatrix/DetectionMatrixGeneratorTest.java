/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package regressionTestManager.detectionMatrix;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.DetectionMatrixGenerator.DMGException;
import regressionTestManager.ioInvariantParser.Variable;
import regressionTestManager.tcSpecifications.TcSpecificationGreaterThan;
import regressionTestManager.tcSpecifications.TcSpecificationLessThan;
import regressionTestManager.tcSpecifications.TcSpecificationPlusOne;
import testSupport.regressionTestManager.TcInfoHandlerStub;

public class DetectionMatrixGeneratorTest extends TestCase {
	private TcInfoHandlerStub infoHandler;
	
	public DetectionMatrixGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		infoHandler = new TcInfoHandlerStub();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		infoHandler = null;
	}


	public void testTestCover__ProgramPointInfo_TcSpecificationLessThan() {
		Variable var = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var2EnterId) );
		Variable varExit = new Variable( infoHandler.programPointExit, infoHandler.getVariableName(infoHandler.var2EnterId) );
		
		TcSpecificationLessThan spec = new TcSpecificationLessThan(var,Double.valueOf( infoHandler.var2EnterVal )+1 );
		TcSpecificationLessThan specExit = new TcSpecificationLessThan(varExit,Double.valueOf( infoHandler.var2EnterVal )+1 );
		
		assertTrue ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), spec ) );
		assertTrue ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), specExit ) );
		
		
		TcSpecificationLessThan specFalse = new TcSpecificationLessThan(var,Double.valueOf( infoHandler.var0EnterVal )-1 );
		assertFalse ( DetectionMatrixGenerator.testCover( infoHandler.getPp0(), specFalse ) );
		
		Variable varNotPresent = new Variable( infoHandler.programPointEnter, "parameter[0].notPresent" );
		TcSpecificationGreaterThan specNotPresent = new TcSpecificationGreaterThan(varNotPresent, 4 );
		assertFalse ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), specNotPresent ) );
	}

	public void testLess() {
		assertTrue ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "3", new Double(4) ) );
		assertFalse ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "3", new Double(2) ) );
		assertFalse ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "3", (Object)null ) );
		
		
		Variable varNumTrue = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var2EnterId) );
		Variable varNumFalse = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var0EnterId) );
		Variable varStringFalse = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var1EnterId) );
		
		assertFalse ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "5", varNumFalse ) );
		
		assertTrue ( DetectionMatrixGenerator.less( infoHandler.getPp1(), "5", varNumTrue ) );
		
		assertFalse ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "5", varStringFalse ) );
		
		//FIXME: check String ordering operators in daikon
		//assertTrue ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "\"Casa\"", new String("Pippo") ) );
		
	}

	public void testGreat() {
		assertTrue ( DetectionMatrixGenerator.great( infoHandler.getPp0(), "5", new Double(4) ) );
		assertFalse ( DetectionMatrixGenerator.great( infoHandler.getPp0(), "1", new Double(2) ) );
		assertFalse ( DetectionMatrixGenerator.great( infoHandler.getPp0(), "3", (Object)null ) );
		
		
		Variable varNumFalse = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var2EnterId) );
		Variable varNumTrue = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var0EnterId) );
		Variable varStringFalse = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var1EnterId) );
		
		assertFalse ( DetectionMatrixGenerator.great( infoHandler.getPp1(), "5", varNumFalse ) );
		
		assertTrue ( DetectionMatrixGenerator.great( infoHandler.getPp0(), "5", varNumTrue ) );
		
		assertFalse ( DetectionMatrixGenerator.great( infoHandler.getPp0(), "5", varStringFalse ) );
		
		//FIXME: check String ordering operators in daikon
		//assertTrue ( DetectionMatrixGenerator.less( infoHandler.getPp0(), "\"Casa\"", new String("Pippo") ) );
				
	}

	public void testTestCover__ProgramPointInfo_TcSpecificationGreaterThan() {
		Variable var = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var2EnterId) );
		
		TcSpecificationGreaterThan spec = new TcSpecificationGreaterThan(var,Double.valueOf( infoHandler.var2EnterVal )-1 );
		
		assertTrue ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), spec ) );
		
		
		
		TcSpecificationGreaterThan specFalse = new TcSpecificationGreaterThan(var,Double.valueOf( infoHandler.var2EnterVal )+1 );
		assertFalse ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), specFalse ) );
		
		Variable varNotPresent = new Variable( infoHandler.programPointEnter, "parameter[0].notPresent" );
		TcSpecificationGreaterThan specNotPresent = new TcSpecificationGreaterThan(varNotPresent, 4 );
		assertFalse ( DetectionMatrixGenerator.testCover( infoHandler.getPp1(), specNotPresent ) );
		
	}
	
	public void testEquals__ProgramPointInfo_String_Object() {
		Double value = new Double(3);
		String sValue = ""+value.intValue();
		
		assertTrue ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), "null", (Object)null) );
		
		assertFalse ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), "2", (Object)null) );
		
		assertFalse ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), "null", (Object)new Double(7)) );
		
		assertTrue ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), sValue, (Object)new Double(value)) );
		
		assertFalse ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), sValue, (Object)new Double(value+1)) );
		
		assertTrue ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), "\"Pippo pippo\"", (Object)infoHandler.var1EnterVal) );
		
		assertFalse ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), "\"Pi\"", (Object)infoHandler.var1EnterVal) );

	}
	
	public void testGetValue__ProgramPointInfo_TcSpecificationPlusOne() throws DMGException {
		TcSpecificationPlusOne spec = 
			new TcSpecificationPlusOne( new Variable( infoHandler.programPointExit, infoHandler.getVariableName( infoHandler.var0ExitId) ) );
		
		Double value = DetectionMatrixGenerator.getValue(infoHandler.getPp0(), spec );
		assertEquals( Double.valueOf( infoHandler.var0ExitVal)+1, value );
	}

	/**
	 * Test getValue.
	 * Two cases:	Variable enter
	 * 				Variable exit
	 * @throws DMGException 
	 * @throws DMGException 
	 *
	 */
	public void testGetValue__ProgramPointInfo_Variable() throws DMGException{
		
		Double value = DetectionMatrixGenerator.getValue( infoHandler.getPp0(), new Variable(infoHandler.programPointEnter, infoHandler.var0Name ) );
		assertEquals ( Double.valueOf(infoHandler.var0EnterVal), value);
		
		value = DetectionMatrixGenerator.getValue( infoHandler.getPp0(), new Variable(infoHandler.programPointExit, infoHandler.var0Name ) );
		assertEquals ( Double.valueOf(infoHandler.var0ExitVal), value);
		
	}

	public void testEquals__ProgramPointInfo_String_TcSpecificationPlusOne() {
		TcSpecificationPlusOne spec = 
			new TcSpecificationPlusOne( new Variable( infoHandler.programPointEnter, infoHandler.getVariableName( infoHandler.var0EnterId) ) );
		
		String expectedTrue = ""+(Double.valueOf(infoHandler.var0EnterVal).intValue()+1);
		System.out.println(expectedTrue);
		boolean result = DetectionMatrixGenerator.equals( infoHandler.getPp0(), expectedTrue, spec);
		
		assertTrue( result );
		
		String expectedFalse = ""+(Double.valueOf(infoHandler.var0EnterVal).intValue());
		assertFalse( DetectionMatrixGenerator.equals( infoHandler.getPp0(), expectedFalse, spec) );
	}

	public void testEquals__ProgramPointInfo_String_Number() {
		Double value = new Double(3);
		String sValue = ""+value.intValue();
		
		assertTrue ( DetectionMatrixGenerator.equals( sValue, value) );
		
		assertFalse ( DetectionMatrixGenerator.equals( sValue, value+1) );

	}

	public void testEquals__ProgramPointInfo_String_String() {
		
		assertTrue ( DetectionMatrixGenerator.equals( "\"Pippo pippo\"", infoHandler.var1EnterVal) );
		
		assertFalse ( DetectionMatrixGenerator.equals( "\"Pi\"", infoHandler.var1EnterVal) );
		
	}

	public void testEqualsProgramPointInfoStringVariable() {
		
		Variable var = new Variable( infoHandler.programPointEnter, infoHandler.getVariableName(infoHandler.var1EnterId) );
		
		assertTrue ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), infoHandler.var1EnterVal, var ) );
		
		assertFalse ( DetectionMatrixGenerator.equals( infoHandler.getPp0(), infoHandler.var1ExitVal, var ) );
		
	}

}
