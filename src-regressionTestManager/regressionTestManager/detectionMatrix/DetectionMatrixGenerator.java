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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import regressionTestManager.ioInvariantParser.TcSpecificationArrayContains;
import regressionTestManager.ioInvariantParser.Variable;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcSpecifications.TcSpecification;
import regressionTestManager.tcSpecifications.TcSpecificationAnd;
import regressionTestManager.tcSpecifications.TcSpecificationEquals;
import regressionTestManager.tcSpecifications.TcSpecificationEqualsNull;
import regressionTestManager.tcSpecifications.TcSpecificationGreaterThan;
import regressionTestManager.tcSpecifications.TcSpecificationLessThan;
import regressionTestManager.tcSpecifications.TcSpecificationNot;
import regressionTestManager.tcSpecifications.TcSpecificationNotEqualsNull;
import regressionTestManager.tcSpecifications.TcSpecificationOr;
import regressionTestManager.tcSpecifications.TcSpecificationPlusOne;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class creates the detection matrixs to be used to select test cases for regression test.
 * 
 * A detection matrix is made of several Vectors, each vector represent the elements covered by a test case.
 * 
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class DetectionMatrixGenerator {
	
	public static class DMGException extends Exception{
		
	}
	
	public static interface Operator {
		boolean operate( ProgramPointInfo ppInfo, String left, Object rhs );
	}
	
	public static class OperatorLess implements Operator {

		public boolean operate(ProgramPointInfo ppInfo, String left, Object right) {
			return less(ppInfo, left, right);
		}
		
	}
	
	public static class OperatorGreat implements Operator {
		public boolean operate(ProgramPointInfo ppInfo, String left, Object right) {
			return great(ppInfo, left, right);
		}
	}
	
	public static class OperatorEqual implements Operator {
		public boolean operate(ProgramPointInfo ppInfo, String left, Object right) {
			return DetectionMatrixGenerator.equals(ppInfo, left, right);
		}
	}
	
	private static final OperatorLess LESS = new OperatorLess();
	private static final OperatorLess GREAT = new OperatorLess();
	private static final OperatorEqual EQUAL = new OperatorEqual();
	private static final String nullValue = "null";
	
	/**
	 * This method creates an Interaction matrix that indicate which method executions are covered by the diferent test cases.
	 * 
	 * @param infoHandler
	 * @return
	 */
	public static InteractionTcDetectionMatrix createInteractionMatrix( TcInfoHandler infoHandler ){
		InteractionTcDetectionMatrix dm = new InteractionTcDetectionMatrix();
		Set<String> tcs = infoHandler.getTestCasesIds();
		Set<String> mts = infoHandler.getMethodsIds();
		
		//retireve and set method names as matrix elements
		ArrayList<String> methodNames = new ArrayList<String>();
		for ( String methodId : mts ){
			methodNames.add( infoHandler.getMethodName(methodId) );
		}
		dm.setElementsToCover( methodNames );

		
		Iterator tcIt = tcs.iterator();
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = infoHandler.getTestCaseInfoFromId((String) tcIt.next());
			Iterator mIt = mts.iterator();
			Boolean[] elements = new Boolean[mts.size()];
			int c = 0;
			while ( mIt.hasNext() ){
				
				if ( tcInfo.getMethodOccurrencies((String) mIt.next()) > 0 ) {
					elements[c++] = new Boolean(true);
				} else {
					elements[c++] = new Boolean(false);
				}
				
			}
			dm.addTestVector(tcInfo,elements);
			
		}
		
		return dm;
	}
	
	/**
	 * This method creates an IOMatrix that specifies for each test cases the variables covered
	 * 
	 * @param infoHandler
	 * @param tcSpecifications
	 * @return
	 */
	public static IoTcDetectionMatrix createIOMatrix( TcInfoHandler infoHandler, ArrayList<TcSpecification> tcSpecifications ){
		IoTcDetectionMatrix dm = new IoTcDetectionMatrix();
		Set<String> tcs = infoHandler.getTestCasesIds();
		
		dm.setElementsToCover( tcSpecifications );
		
		Iterator<String> tcIt = tcs.iterator();
		
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = infoHandler.getTestCaseInfoFromId((String) tcIt.next());
			
			Boolean[] elements = new Boolean[tcSpecifications.size()];
			
			int i = 0;
			
			Iterator<TcSpecification> specIt = tcSpecifications.iterator();
			
			while ( specIt.hasNext() ){
				if ( testCover( tcInfo, (TcSpecification)specIt.next() ) ){
					elements[i++] = Boolean.TRUE;
				} else {
					elements[i++] = Boolean.FALSE;
				}
			}
			
			dm.addTestVector(tcInfo, elements);
		}
		
		return dm;

	}

	/**
	 * Return if an executed test case cover the specified TestCase Specifications.
	 * 
	 * Every test case has some associated program points informations.
	 * A test case cover a specification if it is covered in one of its program points.
	 * 
	 * FIXME: not implemented the case of OneOf
	 * 
	 * @param tcInfo
	 * @param specification
	 * @return
	 */
	static boolean testCover(TestCaseInfo tcInfo, TcSpecification specification) {
		Iterator ppIt = tcInfo.getProgramPointsIterator();
		while ( ppIt.hasNext() ){
			
			ProgramPointInfo ppInfo = (ProgramPointInfo) ppIt.next(); 
				
			if ( testCover( ppInfo, specification ) )
				return true;
		}
		return false;
	}

	static boolean testCover(ProgramPointInfo ppInfo, TcSpecification specification) {
		if ( specification instanceof TcSpecificationAnd ){
			return testCoverAnd( ppInfo, (TcSpecificationAnd) specification );
		} else if ( specification instanceof TcSpecificationNot ){
			return testCoverNot( ppInfo, (TcSpecificationNot) specification );
		} else if ( specification instanceof TcSpecificationOr ){
			return testCoverOr( ppInfo, (TcSpecificationOr) specification );
		} else if ( specification instanceof TcSpecificationEquals ){
			return testCoverEquals( ppInfo, (TcSpecificationEquals) specification );
		} else if ( specification instanceof TcSpecificationLessThan ){
			return testCoverLessThan( ppInfo, (TcSpecificationLessThan) specification );
		} else if ( specification instanceof TcSpecificationGreaterThan ){
			return testCoverGreaterThan( ppInfo, (TcSpecificationGreaterThan) specification );
		} else if ( specification instanceof TcSpecificationEqualsNull ){
			return testCoverEqualsNull( ppInfo, (TcSpecificationEqualsNull) specification );
		} else if ( specification instanceof TcSpecificationNotEqualsNull ){
			return testCoverNotEqualsNull( ppInfo, (TcSpecificationNotEqualsNull) specification );
		} else if ( specification instanceof TcSpecificationArrayContains ){
			return testCoverArrayContains( ppInfo, (TcSpecificationArrayContains) specification );
		}
		
		
		return false;
	}
	
	private static boolean testCoverNotEqualsNull(ProgramPointInfo ppInfo, TcSpecificationNotEqualsNull specification) {
		Variable var = specification.getVariable();
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		return ! equals( ppInfo, value , nullValue );
	}

	private static boolean testCoverEqualsNull(ProgramPointInfo ppInfo, TcSpecificationEqualsNull specification) {
		Variable var = specification.getVariable();
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		
		boolean res = equals( ppInfo, value , nullValue );
		return res;		
	}

	static boolean testCoverAnd( ProgramPointInfo ppInfo, TcSpecificationAnd specification) {
		if ( ! testCover ( ppInfo, specification.getLeftSide() ) )
				return false;
		return testCover ( ppInfo, specification.getRigthSide() );
	}
	
	static boolean testCoverNot( ProgramPointInfo ppInfo, TcSpecificationNot specification) {
		return ! testCover ( ppInfo, specification.getElement() );
	}
	
	static boolean testCoverOr( ProgramPointInfo ppInfo, TcSpecificationOr specification) {
		Iterator it = specification.getElementsIterator();
		while( it.hasNext() ){
			if ( testCover( ppInfo, specification ) )
				return true;
		}
		return false;
	}
	
	static boolean testCoverEquals( ProgramPointInfo ppInfo, TcSpecificationEquals specification) {
		
		Variable var = specification.getVariable();
		Object rhs = specification.getRightSide();
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		
		
		
		boolean res = equals( ppInfo, value , rhs );
		return res;		
	}
	
	static boolean testCoverArrayContains( ProgramPointInfo ppInfo, TcSpecificationArrayContains specification) {
		throw new NotImplementedException();	
	}

	static boolean testCoverLessThan( ProgramPointInfo ppInfo, TcSpecificationLessThan specification) {
		Variable var = specification.getVariable();
		String name = var.getName();
		Object rhs = specification.getRightSide();
		return less ( ppInfo, var, rhs );
	}
	
	static boolean less(ProgramPointInfo ppInfo, Variable var, Object rhs) {
		
		if ( var.isArray() ){
			return lessArray(ppInfo,var,rhs);
		}
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		return less ( ppInfo, value, rhs );
		
	}

	private static boolean lessArray(ProgramPointInfo ppInfo, Variable var, Object rhs) {
		return operatorArray(ppInfo, var, rhs, LESS);
	}
	
	private static boolean greatArray(ProgramPointInfo ppInfo, Variable var, Object rhs) {
		return operatorArray(ppInfo, var, rhs, GREAT);
	}
	
	private static boolean equalsArray(ProgramPointInfo ppInfo, Variable var, Object rhs) {
		return operatorArray(ppInfo, var, rhs, EQUAL);
	}

	static boolean less(ProgramPointInfo ppInfo, String value, Object rhs) {
		try {
			Double leftValue = Double.valueOf(value); 
			if ( rhs instanceof Number ){

				return (  leftValue < ((Number)rhs).doubleValue() );

			} else if ( rhs instanceof TcSpecificationPlusOne ) {

				return ( leftValue < getValue(ppInfo, (TcSpecificationPlusOne) rhs));

			} else if ( rhs instanceof Variable ) {
				return ( leftValue < getValue(ppInfo, (Variable)rhs));
			}
		} catch ( Exception e ) {

		}
		return false;
	}

	private static boolean operatorArray(ProgramPointInfo ppInfo, Variable var, Object rhs, Operator op ) {
		if ( rhs instanceof Variable ){
			
			Collection<String> lvalues = ppInfo.getArrayValues(var.getProgramPoint(),var.getName());
			Collection<String> rvalues = ppInfo.getArrayValues(((Variable)rhs).getProgramPoint(),((Variable)rhs).getName());
			if ( lvalues.size() != rvalues.size() )
				return false;
			Iterator<String> rit = rvalues.iterator();
			Iterator<String> lit = lvalues.iterator();
			while ( rit.hasNext() ){
				String left = lit.next();
				String right = rit.next();
				if ( ! op.operate(ppInfo, left, right) )
					return false;
			}
			
		} else {
			Collection<String> lvalues = ppInfo.getArrayValues(var.getProgramPoint(),var.getName());
			Iterator<String> lit = lvalues.iterator();
			while ( lit.hasNext() ){
				String left = lit.next();
				if ( ! op.operate(ppInfo, left, rhs ) )
					return false;
			}
		}
		return true;
	}

	static boolean great(ProgramPointInfo ppInfo, String value, Object rhs) {
		try {
			Double leftValue = Double.valueOf(value); 
			if ( rhs instanceof Number ){

				return (  leftValue > ((Number)rhs).doubleValue() );

			} else if ( rhs instanceof TcSpecificationPlusOne ) {

				return ( leftValue > getValue(ppInfo, (TcSpecificationPlusOne) rhs));

			} else if ( rhs instanceof Variable ) {
				return ( leftValue > getValue(ppInfo, (Variable)rhs));
			}
		} catch ( Exception e ) {

		}
		return false;

	}


	static boolean testCoverGreaterThan( ProgramPointInfo ppInfo, TcSpecificationGreaterThan specification) {
		Variable var = specification.getVariable();
		String name = var.getName();
		Object rhs = specification.getRightSide();
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		return great( ppInfo, value , rhs );
		
	}
	
	static boolean equals( ProgramPointInfo ppInfo, String lhsValue, Object rhs) {
		if ( lhsValue == null )
			return false;
		if ( lhsValue.equals("null") )
			return (rhs == null);
		if ( rhs instanceof Number ){
			return equals( lhsValue, (Number)rhs);
		} else if (rhs instanceof String ) {
			return equals( lhsValue, (String)rhs);
		} else if (rhs instanceof Variable ) {
			return equals( ppInfo, lhsValue, (Variable)rhs);
		} else if (rhs instanceof TcSpecificationPlusOne ) {
			return equals( ppInfo, lhsValue, (TcSpecificationPlusOne)rhs);
		}
		
		return false;
	}
	
	static Double getValue( ProgramPointInfo ppInfo, TcSpecificationPlusOne rhs ) throws DMGException {
		return getValue(ppInfo,rhs.getVariable())+1;
	}
	
	static Double getValue( ProgramPointInfo ppInfo, Variable var ) throws DMGException {
		String name = var.getName();
		String value = ppInfo.getVariableValue( var.getProgramPoint(), var.getName() );
		//System.out.println("VAR"+var.getProgramPoint()+var.getName()+" VALUE "+value);
		try {
			Double res = Double.valueOf(value);
			return res;
		} catch ( Exception e ) {
			throw new DMGException();
		}
	}
	
	
	
	static boolean equals( ProgramPointInfo ppInfo, String lhsValue, TcSpecificationPlusOne rhs) {
	
		try{
			Double leftValue = Double.valueOf(lhsValue);
			
			return ( leftValue.equals( getValue( ppInfo, rhs ) ) );
		} catch ( Exception e ) {
			return false;
		}
		
	}
	
	static boolean equals( String lhsValue, Number rhs) {
		try {
			Double lvalue = Double.valueOf(lhsValue);
			return lvalue.equals(rhs);
		} catch ( Exception e ) {
			return false;	
		}
	}
	
	static boolean equals( String lhsValue, String rhs) {
		if ( lhsValue == null )
			return false;
		String rhsvalue = "\""+rhs+"\"";
		return lhsValue.equals(rhs);
	}
	
	static boolean equals( ProgramPointInfo ppInfo, String lhsValue, Variable rhs) {
		
		if ( lhsValue == null )
			return false;
		String value = ppInfo.getVariableValue(rhs.getProgramPoint(),rhs.getName());
		return lhsValue.equals(value);
	}
}
