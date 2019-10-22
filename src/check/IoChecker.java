/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;



import modelsFetchers.CollectionIoModelIterator;
import modelsFetchers.IoModelIterator;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import check.ioInvariantParser.IOMemoryRegistry;
import check.ioInvariantParser.InvariantParseException;
import check.ioInvariantParser.IoInvariantParser;

public class IoChecker {	
	
	private Hashtable<String,IoModelIterator> ioProgramPoint = new Hashtable<String, IoModelIterator>();
	private Hashtable<String,IoModelIterator> ioModelsEnter = new Hashtable<String, IoModelIterator>();
	private Hashtable<String,IoModelIterator> ioModelsExit = new Hashtable<String, IoModelIterator>();
	protected ViolationsFilter violationsFilter;
	private boolean deleteInvalidModels;
	
	{
		deleteInvalidModels = Boolean.getBoolean("bct.deleteInvalidModels");
	}
	
	public ViolationsFilter getViolationsFilter() {
		return violationsFilter;
	}


	public void setViolationsFilter(ViolationsFilter violationsFilter) {
		this.violationsFilter = violationsFilter;
	}

	protected interface MethodCallType {
		
	}
	
	protected static class EnterMethod implements MethodCallType{

	}

	protected static class ExitMethod implements MethodCallType {

	}	

	protected static class ProgramPoint implements MethodCallType {

	}	

	protected static final MethodCallType POINT = new ProgramPoint();
	protected static final MethodCallType ENTER = new EnterMethod();
	protected static final MethodCallType EXIT = new ExitMethod();
	

	public void checkEnter( int callId, String methodSignature, Object[] parameters, Map<String, Object> localVariables ){
		
		IOMemoryRegistry.getInstance().upLevel();
		//System.out.println("ENTER "+methodSignature);
		
		if ( violationsFilter != null )
		{
			violationsFilter.enterFunction(methodSignature);
		}
		
		IoModelIterator it;

		try {
			if ( ioModelsEnter.containsKey(methodSignature) ){
				it = ioModelsEnter.get(methodSignature);
				it.reset();
			} else {
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				if ( mf.ioModelEnterExist(methodSignature) ){
					it = mf.getIoModelIteratorEnter(methodSignature);
					ioModelsEnter.put(methodSignature, it);
				}else {
					it = new CollectionIoModelIterator(new ArrayList<String>(0) );
					ioModelsEnter.put(methodSignature, it );
				}
			}

			boolean updateModels = false;
			
			while ( it.hasNext() ){
				boolean result = ___processExpression( callId, (String)it.next(), parameters, null, methodSignature, ENTER, localVariables );
				
				if ( deleteInvalidModels && result ){
					it.remove();
					updateModels=true;
				}
			}
			
			if( updateModels ){
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				it.reset();
				mf.updateIoModelsEnter( methodSignature, it );
			}
		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		
	}
	

	public void checkExit( int callId, String methodSignature, Object[] parameters, Map<String, Object> localVariables ){

		IoModelIterator it;
		//System.out.println("EXIT "+methodSignature);
		try {
			if ( ioModelsExit.containsKey(methodSignature) ){
				it = ioModelsExit.get(methodSignature);
				it.reset();
			} else {
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				if ( mf.ioModelExitExist(methodSignature) ){
					it = mf.getIoModelIteratorExit(methodSignature);
					ioModelsExit.put(methodSignature, it);
				}else {
					it = new CollectionIoModelIterator(new ArrayList<String>(0) );
					ioModelsExit.put(methodSignature, it );
				}
			}

			boolean updateModels = false;
			while ( it.hasNext() ) {
				boolean result = ___processExpression( callId, (String)it.next(), parameters, null, methodSignature, EXIT, localVariables );
				
				if ( deleteInvalidModels && result ){
					it.remove();
					updateModels=true;
				}
			}
			
			if( updateModels ){
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				it.reset();
				mf.updateIoModelsExit( methodSignature, it );
			}

		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if ( violationsFilter != null )
		{
			violationsFilter.exitFunction(methodSignature);
		}
		
		IOMemoryRegistry.getInstance().downLevel();
	}


	public void checkExit( int callId, String methodSignature, Object[] parameters, Object returnValue, Map<String, Object> localVariables ){
		//System.out.println("EXIT_ "+methodSignature);
		IoModelIterator it;
		
		try {
			if ( ioModelsExit.containsKey(methodSignature) ){
				it = ioModelsExit.get(methodSignature);
				it.reset();
			} else {
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				if ( mf.ioModelExitExist(methodSignature) ){
					it = mf.getIoModelIteratorExit(methodSignature);
					ioModelsExit.put(methodSignature, it);
				}else {
					it = new CollectionIoModelIterator(new ArrayList<String>(0) );
					ioModelsExit.put(methodSignature, it );
				}
			}

			boolean updateModels = false;
			while ( it.hasNext() ) {
				boolean result = ___processExpression( callId, (String)it.next(), parameters, returnValue, methodSignature, EXIT, localVariables );
				
				if ( deleteInvalidModels && result ){
					it.remove();
					updateModels=true;
				}
			}
			
			if( updateModels ){
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				it.reset();
				mf.updateIoModelsExit( methodSignature, it );
			}
		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( violationsFilter != null )
		{
			violationsFilter.exitFunction(methodSignature);
		}
		
		IOMemoryRegistry.getInstance().downLevel();
	}

	

	private boolean ___processExpression(int callId, String expression,
			Object[] argumentValues, Object returnValue, String signature, MethodCallType callType, Map<String, Object> localVariables ) {
		//EvaluationRuntimeErrors.log("Processing : "+expression);
		expression = expression.trim();
		if ( expression.length() == 0 ){
			return false;
		}
		
//		System.out.println("CHECKING "+expression);
		
		try {
			
			boolean result = evaluateExpression(expression,
					argumentValues, returnValue, localVariables);
			if (!result) {
				logViolation(callId, expression, argumentValues, returnValue, signature, callType, localVariables );
				return true;
			}
		
		} catch ( Throwable e ){
			System.err.println("Excpetion thrown while evaluating "+expression);
			e.printStackTrace();
			//if ( ! e.getMessage().equals("ClassToIgnore"))
			//	throw e;
		}
		return false;
	}


	public void logViolation(int callId, String expression,
			Object[] argumentValues, Object returnValue, String signature,
			MethodCallType callType, Map<String, Object> localVariables) {
		ViolationsRecorder violationRecorder = RecorderFactory.getViolationsRecorder();
		System.out.println("VIOLATION "+signature+" "+expression);
//				EvaluationRuntimeErrors.log("VIOLATION "+signature+" "+expression);

		System.out.println(Checker.simulatedStackTrace);
		
		StackTraceElement[] stack;
		if ( Checker.simulatedStackTrace != null ){
			stack = Checker.simulatedStackTrace;
		} else {
			stack = Thread.currentThread().getStackTrace();
		}
		
		if ( violationsFilter != null )
		{
			if ( ! violationsFilter.acceptIoViolation( callId, signature, expression, false, argumentValues, returnValue, stack, localVariables ) ) {
				System.out.println("FILTERED OUT");
				return;
			}
		}
		
		try {
			if ( callType.equals(ENTER) || callType.equals(POINT) ) {
				violationRecorder.recordIoViolationEnter( callId, signature, expression, false, argumentValues, returnValue, stack, localVariables );
			} else {
//						get the original values
				HashMap origValues = IOMemoryRegistry.getInstance().getCurrentMethodsMap();
				violationRecorder.recordIoViolationExit( callId, signature, expression, false, argumentValues, returnValue, stack, localVariables, origValues );
			}

		} catch (RecorderException e) {
			e.printStackTrace();
		}
	}
	
	

	public boolean evaluateExpression(String expression,
			Object[] argumentValues, Object returnValue,
			Map<String, Object> localVariables) {
		// TODO Auto-generated method stub
		try {
			return IoInvariantParser.evaluateExpression(expression, argumentValues, returnValue, localVariables);
		} catch (InvariantParseException ipe) {
			//FIXME: always exception thrown because the parser searches for all the possibilities, need to implement IO invariants as objects 
			//ipe.printStackTrace();
			//System.err.println("Error parsing expression " + expression+" "+ipe.getMessage());
		}
		return true;
	}

	public void checkProgramPoint(int callId, String methodSignature, Object[] parameters,
			Map<String, Object> localVariablesMap) {
		IoModelIterator it;

		if ( violationsFilter != null )
		{
			violationsFilter.newProgramPointIoData(localVariablesMap);
		}
		
		try {
			if ( ioProgramPoint.containsKey(methodSignature) ){
				it = ioProgramPoint.get(methodSignature);
				it.reset();
			} else {
				ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
				if ( mf.ioModelEnterExist(methodSignature) ){
					it = mf.getIoModelIteratorEnter(methodSignature);
					ioProgramPoint.put(methodSignature, it);
				}else {
					it = new CollectionIoModelIterator(new ArrayList<String>(0) );
					ioProgramPoint.put(methodSignature, it );
				}
			}

			while ( it.hasNext() ){
				___processExpression( callId, (String)it.next(), parameters, null, methodSignature, POINT, localVariablesMap );
			}
		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	

}
