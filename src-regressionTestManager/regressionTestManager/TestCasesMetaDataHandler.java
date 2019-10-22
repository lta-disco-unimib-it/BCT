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
package regressionTestManager;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import regressionTestManager.tcData.handlers.TcInfoHandlerFactory;
import tools.TokenMetaData;
import traceReaders.metaData.ExecutionMetaDataHandler;
import dfmaker.core.Variable;

/**
 * This class manages the recording of data associated to method calls about tets case execution, in particular this class is used to make association
 * between test cases methods and component method calls. The class keep track of which method call occurred during the execution of a test case, and also
 * record which program points (i.e. which was the state of the parameters exchanged during the calls) are causedby a specific test cases.
 *    
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class TestCasesMetaDataHandler implements MetaDataHandler {
	
	private static enum ExecPoint {ENTER,EXIT};
	//private Stack<ProgramPointInfo> ppInfos = new Stack<ProgramPointInfo>();
	private Stack<Vector<Variable>> readPoints = new Stack<Vector<Variable>>();
	
	private ProcessedPoints processedPointsCache = new ProcessedPoints();
	private HashMap<Long, ProgramPointInfo> processedPointsHashCache = new HashMap<Long, ProgramPointInfo>();
	private String programPointNameEnter = null;

	private Stack<Integer> ppHashes = new Stack<Integer>();

	private TcInfoHandler tcInfoHandler;
	
	/**
	 * Default name to use for cases in which te test name has not been recorded
	 */
	public static final String defaultTestName = "default";
	
	/**
	 * This class maitain an association between program points and Variable in a Normalized Point, it is used to maintain trace of all the processed points to save time in case
	 * program points contain the same values.
	 * 
	 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
	 *
	 */
	public class ProcessedPoints {
		HashMap<Vector<Variable>, HashMap<Vector<Variable>,ProgramPointInfo> > points = new HashMap<Vector<Variable>, HashMap<Vector<Variable>,ProgramPointInfo>>();
		
		public void put ( Vector<Variable> normalizedPointEnter, Vector<Variable> normalizedPointExit, ProgramPointInfo ppInfo ){
			HashMap<Vector<Variable>, ProgramPointInfo> innerMap = points.get(normalizedPointEnter);
			
			if ( innerMap == null ){
				innerMap = new HashMap<Vector<Variable>, ProgramPointInfo>();
				points.put(normalizedPointEnter, innerMap);
			}
			
			innerMap.put( normalizedPointExit, ppInfo );
			
		}
		
		public ProgramPointInfo get ( Vector<Variable> normalizedPointEnter, Vector<Variable> normalizedPointExit ){
			HashMap<Vector<Variable>, ProgramPointInfo> innerMap = points.get(normalizedPointEnter);
			
			if ( innerMap == null ){
				return null;
			}
			
			ProgramPointInfo ppInfo = innerMap.get(normalizedPointExit);
			
			return ppInfo;
		}

		public void clear() {
			//System.out.println("CLEAR");
			//points.clear();
			points = new HashMap<Vector<Variable>, HashMap<Vector<Variable>,ProgramPointInfo>>();
		}
		
		public String toString(){
			return points.toString();
		}
	}


	/**
	 * Parses the metaData written on file to get te test case name
	 * 
	 * @param tokenMetaData
	 * @return
	 */
	protected String getTestCaseName(TokenMetaData tokenMetaData) {
		String rawName;
		if ( tokenMetaData.getCurrentTests().size() == 0 ){
			rawName = "default";
		} else {
			rawName = tokenMetaData.getCurrentTests().get(0);
		}
		
		if ( rawName.length() == 0 ){
			return "default";
		}
		
		return rawName;
		
	}

	public TestCasesMetaDataHandler(){
		tcInfoHandler = TcInfoHandlerFactory.getTcInfoHandler();
	}
	
	private void addVariableInfos(String programPointName, ProgramPointInfo ppInfo, Vector<Variable> normalizedPoint, TcInfoHandler tcInfoHandler) {

		for ( int i = 0; i< normalizedPoint.size(); ++i){
			Variable var = (Variable) normalizedPoint.get(i);
			
			VariableInfo varInfo = tcInfoHandler.getVariableInfo( programPointName, var.getName(), var.getValue());
			
			ppInfo.addVariable(varInfo);
			
		}
	}

	public TokenMetaData handleInteractionBegin(String methodName, String metaData) throws TcInfoHandlerException {
		TokenMetaData tokenMetaData = ExecutionMetaDataHandler.parseBasicMetaData(metaData);
		
		String testCaseName = getTestCaseName ( tokenMetaData );
		
		
		
		TestCaseInfo tcInfo = tcInfoHandler.getTestCaseInfo( testCaseName );
		MethodInfo methInfo = tcInfoHandler.getMethodInfo( methodName );
		tcInfo.addMethod(methInfo);
		
		//tcInfoHandler.save();
		return tokenMetaData;
	}

	public TokenMetaData handleIOEnter(String programPointName, Vector<Variable> normalizedPoint, String metaData) throws TcInfoHandlerException {
		return handleIOEnter(programPointName, normalizedPoint, metaData, null);
	}

	public TokenMetaData handleIOExit(String programPointName, Vector<Variable> normalizedPoint, String metaData) throws TcInfoHandlerException {
		return handleIOExit(programPointName, normalizedPoint, metaData, null);
	}


	public TokenMetaData handleIOEnter(String programPointName, Vector<Variable> normalizedPoint, String metaData, Integer pphash) throws TcInfoHandlerException {
		//System.out.println("ENTER "+programPointName+" "+pphash+" "+ppHashes.size());
		//if the trace has changed we have a program point which is different from the previous
		//in case the trace has changed we need to clear the processed points cache
		if ( !programPointName.equals(programPointNameEnter ) ){
			processedPointsCache.clear();
			ppHashes = new Stack<Integer>();
			readPoints = new Stack<Vector<Variable>>();
			if ( pphash != null ){
				processedPointsHashCache = new HashMap<Long, ProgramPointInfo>();
			}
			programPointNameEnter = programPointName;
		}
		
		if ( pphash != null ){
			ppHashes.push(pphash);
		} else {
			if ( ppHashes.size()>0 ){
				throw new TcInfoHandlerException("Expecting program point hashcode");
			}
		}
		
		
		readPoints.push(normalizedPoint);
		return ExecutionMetaDataHandler.parseBasicMetaData(metaData);
		
	}

	public TokenMetaData handleIOExit(String programPointName, Vector<Variable> normalizedPoint, String metaData, Integer pphash) throws TcInfoHandlerException {
		//System.out.println("EXIT "+programPointName+" "+pphash+" "+ppHashes.size());
		//Check if the caller is passing the program point hashcode, in this case we are supposed to do optimization
		Long programPointCoupleHash = null;
		if ( pphash != null ){
			Integer ppHashEnter = ppHashes.pop();
			programPointCoupleHash = Long.rotateLeft(ppHashEnter.longValue(), 32);
			programPointCoupleHash=programPointCoupleHash|pphash.longValue();
		} else {
			if ( ppHashes.size()>0 ){
				throw new TcInfoHandlerException("Expecting program point hashcode");
			}
		}
		
		TokenMetaData metaDataToken = ExecutionMetaDataHandler.parseBasicMetaData(metaData);

		String testCaseName = getTestCaseName(metaDataToken);
		
		Vector<Variable> normalizedPointEnter = readPoints.pop();
		
		ProgramPointInfo ppInfo = null;
		
		//if we are doing the optimization check in the cache
		if ( programPointCoupleHash != null ){
			ppInfo = processedPointsHashCache.get(programPointCoupleHash);
		} else {
			//not optimizing, check in the other cache
			ppInfo = processedPointsCache.get(normalizedPointEnter, normalizedPoint);
		}
		
		TcInfoHandler tcInfoHandler = TcInfoHandlerFactory.getTcInfoHandler();

		TestCaseInfo tcInfo = tcInfoHandler.getTestCaseInfo( testCaseName );


		if ( ppInfo == null ){ //if this program point has never been observed
			//System.out.println("NOT exists "+programPointCoupleHash);
			ppInfo = tcInfoHandler.createProgramPointInfo();

			addVariableInfos( programPointNameEnter, ppInfo, normalizedPointEnter, tcInfoHandler );
			addVariableInfos( programPointName, ppInfo, normalizedPoint, tcInfoHandler );
			
			//if we are not optimizing save in one cache
			if ( programPointCoupleHash==null ) {
				processedPointsCache.put(normalizedPointEnter, normalizedPoint, ppInfo);
			} else { //otherwise save in the other
				processedPointsHashCache.put(programPointCoupleHash,ppInfo);
			}
		}
		
		tcInfo.addProgramPoint(ppInfo);
		//tcInfoHandler.save();

		return metaDataToken;
	}

	public void save() throws TcInfoHandlerException {
		// TODO Auto-generated method stub
		tcInfoHandler.save();
	}

	public TokenMetaData handleInteractionEnd(String methodName, String metaInfo) {
		return ExecutionMetaDataHandler.parseBasicMetaData(metaInfo);
	}

	public void init(MetaDataHandlerSettings settings) {
		
	}

	@Override
	public TokenMetaData handleInteractionGenericProgramPoint(
			String methodName, String metaInfo) {
		return ExecutionMetaDataHandler.parseBasicMetaData(metaInfo);
	}

}

