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
package tools.gdbTraceParser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import tools.gdbTraceParser.BctGdbCheckingThreadTraceListener.ReturnObject;
import traceReaders.normalized.NormalizedIoTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import check.IoChecker;
import check.ioInvariantParser.IOMemoryRegistry;
import daikon.ProglangType;
import daikon.ValueTuple;
import daikon.VarInfo;
import daikon.inv.Invariant;
import daikon.inv.InvariantStatus;
import daikon.inv.unary.scalar.NonZero;
import dfmaker.core.ProgramPointDataStructures;
import dfmaker.core.Superstructure;
import dfmaker.core.SuperstructureField;
import dfmaker.core.VarTypeResolver.Types;

public class DaikonIoChecker extends IoChecker {
	
	///PROPERTIES-DESCRIPTION: Options for the checking of data properties using daikon serialized models

	///if true remove a boolean property from the list of properties to check the first time it is invalidated, i.e. check violated properties only once (default :false)
	private static final String BCT_CHECK_REMOVE_INVALID_IO = "bct.check.removeInvalidIO";

	///if true do not compare pointers and constants (default: false)
	private static final String BCT_CHECK_DONT_COMPARE_POINTERS_CONSTANTS = "bct.check.dontComparePointersAndConstants";
	
	private Logger LOGGER = Logger.getLogger(DaikonIoChecker.class.getCanonicalName());

	private Hashtable<String,List<Invariant>> ioProgramPoint = new Hashtable<String, List<Invariant>>();
	private Hashtable<String,List<Invariant>> ioModelsEnter = new Hashtable<String, List<Invariant>>();
	private Hashtable<String,List<Invariant>> ioModelsExit = new Hashtable<String, List<Invariant>>();
	private ModelsFetcher mf;

	private boolean removeComparisonBetweenPointersAndConstraints;
	
	{
		String val = System.getProperty(BCT_CHECK_DONT_COMPARE_POINTERS_CONSTANTS);
		removeComparisonBetweenPointersAndConstraints = Boolean.valueOf(val);
	}
	
	public DaikonIoChecker(){
		mf = ModelsFetcherFactoy.modelsFetcherInstance;
		
		String removeInvalidString = System.getProperty(BCT_CHECK_REMOVE_INVALID_IO);
		if ( removeInvalidString != null ){
			removeInvalid = Boolean.parseBoolean(removeInvalidString);
		}
		
	}

	private abstract class ProgramPointChecker {

		Hashtable<String,List<Invariant>> cache;
		private MethodCallType type;
		

		public ProgramPointChecker( Hashtable<String,List<Invariant>> cache, MethodCallType type ){
			this.cache = cache;
			this.type = type;
		}

		public void check( int callId, String methodSignature, Object[] parameters, Map<String, Object> localVariables ){
			try {
			NormalizedIoTraceHandler nth = TraceHandlerFactory.getNormalizedIoTraceHandler( );
			ProgramPointDataStructures ppData = nth.getProgramPointData(methodSignature);
			
			if ( ppData == null ){
				LOGGER.warning("null ppData for "+methodSignature);
//				throw new NullPointerException();
				return;
			}
			
			List<Invariant> list;
			
				Iterator<Invariant> it;
				if ( cache.containsKey(methodSignature) ){
					list = (List<Invariant>) cache.get(methodSignature);

				} else {
					list = loadInternal(methodSignature);

					if ( list != null ) {
						Superstructure es = getSuperstructure( ppData );
						removeComparisonBetweenPointersAndConstraints(list, es);
						
						cache.put(methodSignature, list);
					} else {
						list = new ArrayList();
						cache.put(methodSignature, list );
					}
				}



				
				it = list.iterator();
				while ( it.hasNext() ){
					Invariant inv = it.next();
					ValueTuple vars = prepareVariables( ppData, localVariables, inv, type );
					
					if ( ! inv.is_false() ){//process only if not false
						boolean stillValid = processInvariant( callId, inv , ppData, methodSignature, type, localVariables, vars );
						//System.out.println("STILLVALID "+stillValid +" "+inv.is_false()+" "+inv.toString() );
					}
				}
			} catch (ModelsFetcherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NormalizedTraceHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void removeComparisonBetweenPointersAndConstraints(List<Invariant> list,
				Superstructure es) {
			if ( ! removeComparisonBetweenPointersAndConstraints ){
				return;
			}
			int pos = 0;
			LinkedList<Integer> toRemove = new LinkedList<Integer>();
			for ( Invariant inv : list ){
				
				if ( inv instanceof daikon.inv.unary.scalar.SingleScalar && 
						( ! ( inv instanceof NonZero ) ) ){
					if ( containsPointerVariable( es, inv ) ){
						toRemove.add(pos);
					}
				}
				
				pos++;
			}
			
			
			while ( ! toRemove.isEmpty() ){
				int r = toRemove.pollLast();
				//LOGGER.info("Removing inv "+list.get(r));
				list.remove(r);
			}
		}





		protected abstract Superstructure getSuperstructure(ProgramPointDataStructures ppData);

		protected abstract List<Invariant> loadInternal(String methodSignature) throws ModelsFetcherException;

	}




	private class EnterChecker extends ProgramPointChecker {

		public EnterChecker() {
			super(ioModelsEnter,IoChecker.ENTER);
		}

		@Override
		protected List<Invariant> loadInternal(String methodSignature) throws ModelsFetcherException {
			if ( mf.ioModelEnterExist(methodSignature) ){
				return mf.getSerializedIoModelsEnter(methodSignature);
			}

			return null;

		}

		@Override
		protected Superstructure getSuperstructure(ProgramPointDataStructures ppData) {
			return ppData.getEntrySuperStructure();
		}


	}

	protected Superstructure structure(ProgramPointDataStructures ppData, MethodCallType callType){
		if ( callType == EXIT ){
			return ppData.getExitSuperStructure();
		}
		return ppData.getEntrySuperStructure();
	}

	public boolean containsPointerVariable(Superstructure es, Invariant inv) {
		for ( VarInfo info : inv.ppt.var_infos ){
			int i = info.value_index;

			String vName = info.name();
			
			SuperstructureField field = es.getField(vName);
			if ( field != null ){
				if ( Types.hashcodeType.equals(field.getVarType()) ){ //cannot use == because the superstructure was serialized
					return true;
				}
			} else {
				LOGGER.info("No variable named : "+vName + " in "+es.getProgramPointName());
			}
		}
		return false;
	}

	private class ExitChecker extends ProgramPointChecker {

		public ExitChecker() {
			super(ioModelsExit,IoChecker.EXIT);
		}


		@Override
		protected List<Invariant> loadInternal(String methodSignature) throws ModelsFetcherException {
			if ( mf.ioModelEnterExist(methodSignature) ){
				return mf.getSerializedIoModelsExit(methodSignature);
			}
			return null;
		}


		@Override
		protected Superstructure getSuperstructure(
				ProgramPointDataStructures ppData) {
			return ppData.getExitSuperStructure();
		}



	}


	private class PPChecker extends ProgramPointChecker {

		public PPChecker() {
			super(ioProgramPoint,IoChecker.POINT);
		}


		@Override
		protected List<Invariant> loadInternal(String methodSignature) throws ModelsFetcherException {
			if ( mf.ioModelEnterExist(methodSignature) ){
				return mf.getSerializedIoModelsEnter(methodSignature);
			}
			return null;
		}


		@Override
		protected Superstructure getSuperstructure(
				ProgramPointDataStructures ppData) {
			return ppData.getEntrySuperStructure();
		}



	}


	private EnterChecker enterChecker = new EnterChecker();
	private ExitChecker exitChecker = new ExitChecker();
	private PPChecker ppChecker = new PPChecker();

	private boolean removeInvalid = false;

	public void checkEnter( int callId, String methodSignature, Object[] parameters, Map<String, Object> localVariables ){
		LOGGER.info("Checking Enter "+ppCounter);
		
		IOMemoryRegistry.getInstance().upLevel();
		//System.out.println("ENTER "+methodSignature);

		if ( violationsFilter != null ) {
			violationsFilter.enterFunction(methodSignature);
		}

		enterChecker.check(callId, methodSignature, parameters, localVariables);


	}

	private ValueTuple prepareVariables(ProgramPointDataStructures ppData,
			Map<String, Object> localVariables, Invariant inv, MethodCallType callType) {
		Superstructure es = structure(ppData, callType);

		Collection<SuperstructureField> fields = es.varFields();

		int size = 0;
		for ( VarInfo info : inv.ppt.var_infos ){
			if ( info.value_index > size ){
				size = info.value_index;
			}
		}
		size++;


		Object[] values = new Object[size];
		int[] mods = new int[size];

		ArrayList<Integer> l = new ArrayList<Integer>();
		//		int i = 0;
		
		for ( VarInfo info : inv.ppt.var_infos ){
			int i = info.value_index;

			//			ProglangType curType = ;
			
			ProglangType curType = info.type;
			String vName = info.name();
			
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine( "Preparing variable " + vName + " "+curType);
			}
			

//			if ( info.ref_type.name().equals("hashcode") ){
//				inv.
//			}
			
			if ( localVariables.containsKey( vName ) ){
				values[i] = localVariables.get(vName);
				
				if ( LOGGER.isLoggable(Level.FINE) ){
					LOGGER.fine( "Contains variable " + vName + " "+curType);
				}	
				

				mods[i] = 1;
			} else {
				mods[i] = 2;
			}
			
//			System.err.println(vName + " "+curType);
			//FIX TYPE IF NECESSARY
			if ( curType.isIntegral() ){
				if ( values[i] == null ){
					values[i] = Long.valueOf(0);
				} else if ( values[i] instanceof Integer ){
					values[i] = new Long((Integer)values[i]);
				} else if ( values[i] instanceof BigInteger ){
					values[i] = ((BigInteger)values[i]).longValue();
				}
			}
			
			if ( curType.isScalar() || curType.isHashcode() || curType.isFloat() || curType.isPrimitive() || curType.toString().equals("int") ){
				if ( values[i] == null ){
					values[i] = Long.valueOf(0);
				}
			}

		}

		return new ValueTuple(values,mods);
	}

	
	private boolean processInvariant(int callId, Invariant _inv,
			ProgramPointDataStructures ppData, String methodSignature, MethodCallType callType,
			Map<String, Object> localVariables, ValueTuple vars) {

		Invariant inv = _inv.clone();
//		System.out.println("METHOD SIG "+methodSignature+";");
		//extract the boolean expression before adding the sample (otherwise the invariant could change)
		String booleanExpression = inv.toString();
		
		if ( booleanExpression.contains("orig(") ){
			LOGGER.info("Skipping expression with orig(...) "+booleanExpression);
			_inv.falsify();
			return false;
		}
		
		try {			
			//			System.out.println(localVariables);
			//			System.err.println("PROCESS INVARIANTS");
			//			System.err.println(localVariables);
			//			System.err.println(booleanExpression);
			//	
			//			System.err.println(ppData.getProgramPointName());


			
//			ValueTuple vt = prepareVariables( ppData, localVariables, inv, callType );
			ValueTuple vt = vars;

			//			System.err.println("GUARDING LIST "+inv.getGuardingList());		
			//			System.err.println("VT "+vt);
			//			System.err.println("varNames"+inv.ppt.varNames());
			//			System.err.println("var info len "+inv.ppt.var_infos.length);
			//			System.err.println("inv class "+inv.getClass().getCanonicalName());
			//			System.err.println("ppt class "+inv.ppt.getClass().getCanonicalName());
			//			System.err.println(inv.ppt.parent.name);
			//			System.err.println(callType);
			InvariantStatus status = inv.add_sample(vt, 1);



			if ( status != InvariantStatus.NO_CHANGE ){
				logViolation(callId, booleanExpression, new Object[0], null, methodSignature, callType, localVariables );
				if ( removeInvalid ){
					_inv.falsify();
					return false;
				}
			}

			//			System.out.println("STATUS "+status);
			//			System.out.println("NEW INV "+inv);
			//			System.out.flush();
			//			System.err.flush();
			return true;
		} catch ( Throwable t ){
			String types = "";
			for ( VarInfo info : inv.ppt.var_infos ){
				ProglangType curType = info.type;
				types += curType.toString();
			}
			LOGGER.log(Level.SEVERE, "Exception while processing "+booleanExpression+" "+t.getMessage()+" "+localVariables+" "+inv.repr()+" Types: "+types, t);
			t.printStackTrace();
		}
		return false;
	}


	public void checkExit( int callId, String methodSignature, Object[] parameters, Map<String, Object> localVariables ){
		ppCounter++;
		
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Checking Exit "+ppCounter);
		}
		
		exitChecker.check(callId, methodSignature, parameters, localVariables);


		if ( violationsFilter != null )
		{
			violationsFilter.exitFunction(methodSignature);
		}

		IOMemoryRegistry.getInstance().downLevel();
	}


	public void checkExit( int callId, String methodSignature, Object[] parameters, Object returnValue, Map<String, Object> localVariables ){
		ppCounter++;
		
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.info("Checking Exit "+ppCounter);
		}
		
		ReturnObject o =  (ReturnObject) returnValue;
		localVariables.put("returnValue.eax", o.eax);

		exitChecker.check(callId, methodSignature, parameters, localVariables);

		if ( violationsFilter != null )
		{
			violationsFilter.exitFunction(methodSignature);
		}

		IOMemoryRegistry.getInstance().downLevel();
	}

	long ppCounter = 0;
	public void checkProgramPoint(int callId, String methodSignature, Object[] parameters,
			Map<String, Object> localVariablesMap) {

		ppCounter++;
		
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Checking ProgramPoint "+ppCounter);
		}
		
		if ( violationsFilter != null )
		{
			violationsFilter.newProgramPointIoData(localVariablesMap);
		}
		
		ppChecker.check(callId, methodSignature, parameters, localVariablesMap);

	}






}
