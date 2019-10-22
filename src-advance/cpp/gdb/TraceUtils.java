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
package cpp.gdb;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tools.gdbTraceParser.BctCNamesUtil;

import cpp.gdb.FileChangeInfo.Delta;


public class TraceUtils {
	
	private static Logger logger = CppGdbLogger.INSTANCE;

	public static int getLine(String signature) {
		return Integer.valueOf(getLineOrFunction(signature, true));
	}
	
	public static String getFunctionName(String signature) {
		return getLineOrFunction(signature, false);
	}
	
	public static String getLineOrFunction(String signature, boolean line ){
		int end = signature.length() - 1;
		
		if ( signature.charAt(end) == BctCNamesUtil.ADDITIONAL_CHAR_FOR_NONDETERMINISTIC_NAMES ){
			end--;
		}
		
		int lastChar = end;
		
		for ( ; lastChar > 0; lastChar--) {
			if ( ! Character.isDigit(signature.charAt(lastChar) ) ) {
				break;
			}
		}
		
		
		
		if ( line ){
			return signature.substring(lastChar+1, end+1);
		}
		
		if ( signature.charAt(lastChar) == ':' ){
			--lastChar; // Needed to remove ':'
		}
		return signature.substring(0, lastChar+1);
	}
 
	
	public static Map<String, List<Integer>> getModifiedLines(List<FileChangeInfo> diffs, boolean original) {
		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
		
		
		for (FileChangeInfo diff : diffs) {
			List<Integer> lines = new ArrayList<Integer>();
			
			
			
			for (Delta delta : diff.getDeltas()) {
				int start = original ? delta.getStart() : delta.getStartModified();
				int end = original ? delta.getEnd() : delta.getEndModified();
				
				if ( original && delta.isPureAddition() ){
					continue;
				}
				
				if ( ( original == false ) && delta.isPureDeletion() ){
					continue;
				}
				
				for (int i = start; i <= end; i++){
					lines.add(i);
				}
			}
			
			logModifiedLines(diff, lines );
			
			map.put(diff.getRelativeName(), lines);
		}
		return map;
	}
	
	private static void logModifiedLines(FileChangeInfo diff,
			List<Integer> lines) {
		if ( logger.isLoggable(Level.FINE) ){
			
			String linesMsg = "";
			for ( Integer line : lines ){
				linesMsg += line +", ";
			}
			
			logger.fine("Modified lines for "+diff.getFile().getName()+" : "+linesMsg);
		}
	}

	public static Map<String, List<Integer>> getSourceLines(List<FileChangeInfo> diffs, boolean originals) throws IOException {
		
		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
		
		for (FileChangeInfo diff : diffs) {
			File file = originals ? diff.getFile() : diff.getModifiedFile();
			String keyName = diff.getRelativeName();
			if (map.get(keyName) == null) {
				List<Integer> lineNumbers = new ArrayList<Integer>();
				LineNumberReader  lnr = new LineNumberReader(new FileReader(file));
				try {
					lnr.skip(Long.MAX_VALUE);
					for (int i = 1; i <= lnr.getLineNumber(); i++) {
						lineNumbers.add(i);
					}
					map.put( keyName, lineNumbers);
				} finally {
					lnr.close();
				}
			}
		}
		return map;
	}

	public static String extractFunctionSignatureFromGenericProgramPointName(
			String signature) {
		int lastColons = signature.lastIndexOf(':');
		int lastPar = signature.lastIndexOf(')');
		
		if ( lastColons > 0 ){
			if ( lastPar < lastColons ){
				//signature is like mathod:53
				signature = signature.substring(0,lastColons);
			}
		}
		return signature;
	}

	public static boolean isLineProgramPoint(
			String signature) {
		int lastColons = signature.lastIndexOf(':');
		int lastPar = signature.lastIndexOf(')');
		
		if ( lastColons > 0 ){
			if ( lastPar < lastColons ){
				return true;
			}
		}
		return false;
	}

	
	
	
	
	/*public static Delta getDelta(MonitoringConfiguration mc, MethodCallPoint callPoint, boolean originalTrace) throws ConfigurationFilesManagerException, IOException {
		//System.out.println("Getting delta for call point: " + callPoint);
		
		if (callPoint.isEnter() || callPoint.isExit())
			return null;
		
		String signature = TraceUtils.getFunctionName(callPoint.getMethod().getSignature());
		int line = TraceUtils.getLine(callPoint.getMethod().getSignature());
		if (line < 0)
			return null;
		
		CRegressionConfiguration conf = (CRegressionConfiguration) mc.getAdditionalConfiguration(CRegressionConfiguration.class);
		Map<String, FunctionMonitoringData> functionsMap = CRegressionAnalysisUtil.getFunctionsDeclaredInOriginalSoftware(mc, conf);
			
		
		 * 1. Get file name from callPoint (callPoint -> function name -> file name)
		 * 2. Get diff for the specific file name
		 * 3. Looking for delta related to callPoint
		 
		FunctionMonitoringData function = functionsMap.get(signature);
		if (function != null) {
			String fileName = function.getSourceFile().getName();
			List<String> list = new ArrayList<String>();
			list.add(fileName);
			List<FileChangeInfo> diffs = TraceUtils.getDiffs(mc, list);
			FileChangeInfo fileChangeInfo = diffs.get(0);
			
			for (Delta delta : fileChangeInfo.getDeltas()) {
				if (originalTrace) {
					if (line >= delta.getStart() && line <= delta.getEnd())
						return delta;
				} else {
					if (line >= delta.getStartModified() && line <= delta.getEndModified())
						return delta;
				}
			}
		}
		
		return null;
	}*/

	/*private static List<String> getFunctionNames(List<MethodCallPoint> trace) {
		Set<String> functionNames = new HashSet<String>();
		
		for (MethodCallPoint callPoint : trace) {
			if (callPoint.isEnter() || callPoint.isExit())
				continue;
			functionNames.add(TraceUtils.getFunctionName(callPoint.getMethod().getSignature()));
		} 
	
		return new ArrayList<String>(functionNames);
	}*/

	/*public static List<FileChangeInfo> getDiffs(MonitoringConfiguration mc, List<String> fileNames) throws ConfigurationFilesManagerException, IOException {
	List<FileChangeInfo> filesDiffs = new ArrayList<FileChangeInfo>(); 
	
	CRegressionConfiguration conf = (CRegressionConfiguration) mc.getAdditionalConfiguration(CRegressionConfiguration.class);
	List<FileChangeInfo> diffs = CRegressionAnalysisUtil.getDiffs(mc, conf);
	
	for (FileChangeInfo diff : diffs) {
		if(fileNames.contains(diff.getFile().getName()))
			filesDiffs.add(diff);
	}
	
	return filesDiffs;
}*/

/*public static List<String> getFileNames(MonitoringConfiguration mc, List<String> functionNames) throws ConfigurationFilesManagerException, IOException {
	Set<String> fileNames = new HashSet<String>();
	
	CRegressionConfiguration conf = (CRegressionConfiguration) mc.getAdditionalConfiguration(CRegressionConfiguration.class);
	Map<String, FunctionMonitoringData> functionsMap = CRegressionAnalysisUtil.getFunctionsDeclaredInOriginalSoftware(mc, conf);
	
	for (String functionName : functionNames) {
		FunctionMonitoringData function = functionsMap.get(functionName);
		if (function != null)
			fileNames.add(function.getSourceFileName());
	}
	
	return new ArrayList<String>(fileNames);
}*/

/*public static List<String> getFunctionNames(List<MethodCallPoint> trace1, List<MethodCallPoint> trace2) {
	Set<String> functionNames = new HashSet<String>();
	
	functionNames.addAll(TraceUtils.getFunctionNames(trace1));
	functionNames.addAll(TraceUtils.getFunctionNames(trace2));
	
	return new ArrayList<String>(functionNames);
}*/
}
