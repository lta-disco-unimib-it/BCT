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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import tools.violationsAnalyzer.ViolationsUtil;
import traceReaders.normalized.NormalizedIoTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import check.ViolationsFilter;
import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.TraceUtils;
import dfmaker.core.ProgramPointDataStructures;
import dfmaker.core.SuperstructureField;
import dfmaker.core.VarTypeResolver;

public class BctCheckingFilter implements ViolationsFilter {

	private static final String DELIMITERS = "\t .,;+-=/*!~|[]{}():<>?";
	
	private Map<String, FunctionMonitoringData> functionsMap;


	public BctCheckingFilter(Map<String, FunctionMonitoringData> functionsMap) {
		this.functionsMap = functionsMap;
	}

	@Override
	public boolean acceptIoViolation(int callId, String signature,
			String expression, boolean result, Object[] argumentValues,
			Object returnValue, StackTraceElement[] stack,
			Map<String, Object> localVariables) {

		List<String> anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression, localVariables );
		
//		if ( excludeBecauseSpuriousPointerModel(signature, expression,
//				anomalousVariables) ){
//			return false;
//		}
		
		HashSet<String> variableNames = extractVariableNames(anomalousVariables);
		
		
		
		
		signature = signature.trim();
		
		signature = TraceUtils.extractFunctionSignatureFromGenericProgramPointName(signature);
		
		FunctionMonitoringData function = functionsMap.get(signature);
		//TODO: if using demanged names then signature is demangled, but functionsmap are mangled.
		
		if ( function == null ){
			System.out.println("No function");
			return false;
		}
		
		int firstLine = function.getFirstSourceLine();
		if ( firstLine == -1 ){
			System.out.println("No first line");
			return false;
		}
		
		if ( isViolationPresentInPreviousLine( signature, expression, localVariables, anomalousVariables ) ){
			System.out.println("Present in previous");
			return false;
		}
		
		
		boolean allUsed = checkIfAllVariablesUsed( variableNames, function.getAbsoluteFile(), firstLine, function.getLastSourceLine() );
		
		if ( ! allUsed ){
			return false;
		}
		
		return true;
	}

	public boolean excludeBecauseSpuriousPointerModel(String signature,
			String expression, List<String> anomalousVariables) {
		try {
//			NOT USEFUL FOR NOW, POINTERS ARE SAVED WITH INT TYPE 
//			if ( allowPointerComparison ){
//				return false;
//			}
			
			NormalizedIoTraceHandler nth = TraceHandlerFactory.getNormalizedIoTraceHandler( );
			ProgramPointDataStructures ppData = nth.getProgramPointData(signature);
			for( String var : anomalousVariables ){
				SuperstructureField field = ppData.getEntrySuperStructure().getField(var);
				if ( VarTypeResolver.Types.hashcodeType.equals(field.getVarType()) ){
					//is a pointer, consider only models like ptr != null
					if( ! expression.endsWith(" != null") ){
						return true;
					}
				}
			}
		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void enterFunction( String function ){
		violFlowMapStack.push(new HashMap<String, Map<String,Object>>());
	}
	
	@Override
	public void exitFunction( String function ){
		violFlowMapStack.pop();
	}
	
	@Override
	public void newProgramPointIoData( Map<String, Object> localVariables ){
		HashMap<String, Map<String, Object>> violFlowMap = violFlowMapStack.peek();
		
		if ( violFlowMap == null ){
			//this happens if we are monitoring only program points and not function entry/exit points
			return;
		}
		
		List<String> toRemove = new ArrayList<String>();
		for ( Entry<String,Map<String, Object>> e : violFlowMap.entrySet() ){
			String expression = e.getKey();
			Map<String, Object> violatedVars = e.getValue();
			
			for ( String var : violatedVars.keySet() ){
				Object val = violatedVars.get(var);
				Object newVal = localVariables.get(var);
				if ( val == newVal ){
					continue;
				}
				if ( val == null || ! val.equals(newVal) ){ //They are different 
					toRemove.add(expression); //remove from map;
					break;
				}
			}
			
			
		}
		
		for( String key : toRemove ){
			violFlowMap.remove(key);
		}
	}
	
	/**
	 * @deprecated("Does not distinguish violations occurring in different method calls")
	 * Filter out violations that occur multiple times in different lines
	 * @param localVariables 
	 * @param expression 
	 * @param expression 
	 * @param anomalousVariables 
	 * @param viol
	 * @return
	 */
	protected boolean isViolationPresentInPreviousLine(String signature, String expression, Map<String, Object> localVariables, List<String> anomalousVariables) {
		
		String violKey = createViolationKey( signature, expression );
		
		HashMap<String, Map<String, Object>> violFlowMap = violFlowMapStack.peek();
		
		if ( violFlowMap == null ){
			//useful when entry/exit not monitored
			return false;
		}
		
		Map<String, Object> oldViolation = violFlowMap.get(violKey);
		
		
		
		if ( oldViolation != null ){
			System.out.println("VIOLATION ALREADY REPORTED FOR PREVIOUS LINES(S)");
			return true;
		}
		
		Map<String, Object> violatedVariables = new HashMap<String, Object>();
		for ( String var : anomalousVariables ){
			if ( localVariables.containsKey(var) ){
				violatedVariables.put( var, localVariables.get(var) );
			}
		}
		
		violFlowMap.put(violKey, violatedVariables);
		
		return false;
	}

	private String createViolationKey(String signature, String expression) {
		return expression;
	}

	private LinkedList<HashMap<String, Map<String, Object>>> violFlowMapStack = new LinkedList<HashMap<String, Map<String, Object>>>();
	
	
	/**
	 * @deprecated Use {@link TraceUtils#extractFunctionSignatureFromGenericProgramPointName(String)} instead
	 */
	private static String extractFunctionSignatureFromGenericProgramPointName(
			String signature) {
				return TraceUtils
						.extractFunctionSignatureFromGenericProgramPointName(signature);
			}



	private HashMap<String,VariablesUsageInfo> variablesUsedCache = new HashMap<String,VariablesUsageInfo>();

//	private boolean allowPointerComparison;
	
	private boolean checkIfAllVariablesUsed(Collection<String> _variables, File file, int firstLine, int lastSourceLine) {
		System.out.println("CHECKING IF THESE VARIABLES ARE USED: "+_variables);
		System.out.println("FILE "+file.getAbsolutePath()+" lines "+firstLine+" to "+lastSourceLine);
		
		HashSet<String> variables = createSetWithVariableNames(_variables);
		
		
		
		VariablesUsageInfo usageInfo = getUsedVariablesCache( file, firstLine, lastSourceLine );
		
		
		if ( usageInfo.removeNotUsed(variables) ){
			System.out.println("SOME VARIABLES NOT USED");
			return false;
		}
		
		variables.removeAll(usageInfo.used);	
		if ( variables.size() == 0 ){
			return true;
		}
		
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			System.out.println("NOT FOUND "+file.getAbsolutePath()+" CANNOT DETERMINE IF VARIABLES ARE USED.");
			return false;
		}
			
		String line;
		int counter = 0;
		try {
			while( ( line = r.readLine() ) != null ){
				counter++;
				if ( counter < firstLine ) {
					continue;
				}
				if ( counter > lastSourceLine ){
					break;
				}
				
				HashSet<String> used = checkVariablesUsedInLine( variables, line );
				variables.removeAll(used);
				usageInfo.addUsed(used);
				
				if ( variables.size() == 0 ){
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		usageInfo.addNotUsed(variables);
		System.out.println("NOT USED "+variables);
		return false;
	}

	private static class VariablesUsageInfo {
		Set<String> used = new HashSet<String>();
		Set<String> notUsed = new HashSet<String>();
		
		public void addUsed( Collection<String> vars ){
			used.addAll(vars);
		}
		

		public boolean removeNotUsed(HashSet<String> variables) {
			return variables.removeAll(notUsed);
		}


		public void addNotUsed( Collection<String> vars ){
			notUsed.addAll(vars);
		}
	}
	
	private VariablesUsageInfo getUsedVariablesCache(File file, int firstLine, int lastSourceLine) {
		String key = file.getAbsolutePath()+"-"+firstLine+"-"+lastSourceLine;
		VariablesUsageInfo cache = variablesUsedCache.get( key );
		if ( cache == null ){
			cache = new VariablesUsageInfo();
			variablesUsedCache.put(key, cache);
		}
		return cache;
	}

	public HashSet<String> createSetWithVariableNames(
			Collection<String> _variables) {
		HashSet<String> variables = new HashSet<String>();
		for ( String _variable : _variables ){
			int arrayStart = _variable.indexOf('[');
			if ( arrayStart > 0 ){
				_variable = _variable.substring(0,arrayStart);
			}
			if ( _variable.startsWith("&") ){
				_variable = _variable.substring(1);
			}
			variables.add(_variable);
		}
		
		variables.remove("this"); //this do not need to be explicitely written to be available... if we monitor it it means that it is reachable
		variables.remove("*this");
		return variables;
	}

	private HashSet<String> checkVariablesUsedInLine(Collection<String> variables,
			String line) {
		
		HashSet<String> used = new HashSet<String>();
		
		StringTokenizer tokenizer = new StringTokenizer(line,DELIMITERS);
		while( tokenizer.hasMoreTokens() ){
			String token = tokenizer.nextToken();
			
			for ( String variable : variables ){
				if ( variable.equals(token) ){
					used.add(variable);
				}
			}
		}
		
		return used;
	}

	private HashSet<String> extractVariableNames(List<String> localVariables) {
		HashSet<String> variableNames = new HashSet<String>();
		
		for ( String var : localVariables ){
			
			if ( var.startsWith("returnValue") ){
				continue;
			}
			
			int pos = var.lastIndexOf('.');
			
			String variableName;
			
			if ( pos >= 0 ) {
				variableName = var.substring(pos+1);
			} else {
				variableName = var;
			}
			
			variableNames.add(variableName);
		}
		
		return variableNames;
	}

//	public void setAllowPointerComparison(boolean b) {
//		this.allowPointerComparison = b;
//	}

	
	
}
