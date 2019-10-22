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
package traceReaders.normalized.traceCreation;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tools.TokenMetaData;
import traceReaders.metaData.ExecutionTokenMetaData;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedInteractionTraceHandlerCsv;
import traceReaders.normalized.NormalizedInteractionTraceHandlerFile;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import util.componentsDeclaration.ComponentsDefinitionException;
import util.componentsDeclaration.JavaSignatureParser;
import conf.InvariantGeneratorSettings;

/**
 * Creates traces that describe how a component is used, but has several limitations:
 * *) does not correctly manage multiple threads
 * *) does not correctly manage the case in which multiple object of the same type are used
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public abstract class ClassUsageInteractionTraceMaintainer implements TraceMaintainer {



	private LinkedList<String> idStack = new LinkedList<String>();
	private LinkedList<String> classStack = new LinkedList<String>();
	
	private static class ClassTokensMap {
		private HashMap<String,List<Token>> traces = new HashMap<String,List<Token>>();
		private HashMap<String,String> classObjects = new HashMap<String, String>();
		
		
		private Map<String, List<Token>> contextTraces = new HashMap<String, List<Token>>();
		private Map<String, String> classContexts = new HashMap<String, String>();
		
		/**
		 * Add an incoming call to a method which is not a constructor.
		 * Constructor should not be traced in this way because 
		 * @param clazz
		 * @param calledMethod
		 * @param objectId
		 */
		public void addIncomingCall(String clazz,Token calledMethod,String objectId){
			addCall(clazz, calledMethod, objectId);
		}
		
		public void addCall(String clazz,Token calledMethod,String objectId){
			List<Token> trace = getTrace(clazz,objectId);
			trace.add(calledMethod);

			String caller;
			if ( calledMethod.getTokenMetaData().getContextData().size() > 0 ){
				caller = calledMethod.getTokenMetaData().getContextData().get(0);
				
			} else {
				caller = "?";
			}
			
			List<Token> contextTrace = getContextTrace(clazz, objectId);
			contextTrace.add(new Token(caller+"#"));
		}
		
		/**
		 * This method must be called for constructor calls, it shuold be called when the end of the constructor call is detected
		 *  
		 * @param clazz
		 * @param clazz 
		 * @param calledMethod
		 * @param objectId
		 */
		public void mergeConstructorCall(String constructorId, String clazz, Token calledMethod,String objectId){
			
			
			List<Token> constructorCalls = getTrace(clazz,constructorId);
			List<Token> constructorCallsContext = getContextTrace(clazz, constructorId);
			
			removeTrace(clazz,constructorId);
			removeContextTrace(clazz,constructorId);
			
			List<Token> trace = getTrace(clazz,objectId);
			List<Token> traceContext = getContextTrace(clazz,objectId);
			
			for ( int i = 0; i < constructorCalls.size(); ++i ){
				Token constructorToken = constructorCalls.get(i);
				
				//Update metadata object id
				TokenMetaData oldMetaData = constructorToken.getTokenMetaData(); 
				
				ExecutionTokenMetaData md = new ExecutionTokenMetaData();
				md.setCalledObjectId(objectId);
				md.setCurrentActions(oldMetaData.getCurrentActions());
				md.setCurrentTests(oldMetaData.getCurrentTests());
				md.setTimestamp(oldMetaData.getTimestamp());
				md.setContextData(oldMetaData.getContextData());
				
				constructorToken.setTokenMetaData(md);
				
				trace.add(i, constructorToken);
				
				
				
				Token contextToken = constructorCallsContext.get(i); 
				traceContext.add(i, contextToken );
				
			}
			
			
			
		}
		
		private void removeContextTrace(String clazz, String objectId) {
			if ( objectId.equals("")){
				objectId=clazz;
			}
			classContexts.remove(objectId);
		}

		private void removeTrace(String clazz, String objectId) {
			if ( objectId.equals("")){
				objectId=clazz;
			}
			classObjects.remove(objectId);
		}

		private List<Token> getTrace(String clazz, String objectId) {
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			if ( ! classObjects.containsKey(objectId) ){
				classObjects.put(objectId, clazz);
				LinkedList<Token> list = new LinkedList<Token>();
				traces.put(objectId, list );
				return list;
			}
			return traces.get(objectId);
		}

		
		private List<Token> getContextTrace(String clazz, String objectId) {
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			if ( ! classContexts.containsKey(objectId) ){
				classContexts.put(objectId, clazz);
				List<Token> list = new LinkedList<Token>();
				contextTraces.put(objectId, list );
				return list;
			}
			return contextTraces.get(objectId);
	
		}


		public void addOutgoingCall(String clazz,Token calledMethod,String objectId){

			addCall(clazz, calledMethod, objectId);
		}
		
	}
	
	private ClassTokensMap classTokensMap = new ClassTokensMap();
	
	public interface ConfigurationOptions{
		public static final String componentsDefinitionFile="traceMaintainer.componentsDefinitionFile";
	}


	private NormalizedInteractionTraceHandler handler;
	private int constructors;
	private NormalizedInteractionTraceHandlerCsv csvHandler;
	private boolean traceOutgoingCalls;
	private NormalizedInteractionTraceHandlerCsv contextCsvHandler;
	private NormalizedInteractionTraceHandlerCsv tmpHandler;

	
	public ClassUsageInteractionTraceMaintainer(boolean traceOutgoingCalls){
		this.traceOutgoingCalls = traceOutgoingCalls;
	}


	public void init(InvariantGeneratorSettings invariantGeneratorSettings, NormalizedInteractionTraceHandler handler) throws ComponentsDefinitionException {
		
		this.handler = handler;
		
		File dest = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteraction" );
		dest.mkdirs();
		csvHandler = new NormalizedInteractionTraceHandlerCsv(dest);
		
		
		File destCtx = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteractionContext" );
		destCtx.mkdirs();
		contextCsvHandler = new NormalizedInteractionTraceHandlerCsv(destCtx);
	}
	
	public void sessionEnd() {
		
		Comparator<Token> timestampComparator = new Comparator<Token>(){

			public int compare(Token o1, Token o2) {
				long diff = (o1.getTokenMetaData().getTimestamp()-o2.getTokenMetaData().getTimestamp());
				if (  diff == 0 ){
					return 0;
				} else if ( diff > 0 ){
					return 1;
				}
				return -1;
			}
			
		};
		
		for ( Entry<String,String> entry : classTokensMap.classObjects.entrySet() ){
			
			String objectId = entry.getKey();
			String className = entry.getValue();
			
			 
			
			List<Token> trace = classTokensMap.traces.get(objectId);
			
			Collections.sort(trace, timestampComparator );
			
			//if static manage in a different way
			if ( objectId.equals(className) ){
				className = "$"+className;
			}
			
			
			List<Token> contextTrace = classTokensMap.contextTraces.get(objectId);
			
			
			try {
				handler.addInteractionTrace(className, trace, null);
				csvHandler.addInteractionTrace(className, trace, null);
				contextCsvHandler.addInteractionTrace(className, contextTrace, null);
			} catch (NormalizedTraceHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//Reset values: bug 165
		classTokensMap = new ClassTokensMap();
	}


	public void programPointBegin(InteractionTrace rawTrace,
			Token programPointToken) {
		
		String completeMethodSignature = programPointToken.getMethodSignature();
		
		
		
		
		if ( ! idStack.isEmpty() ){
			String callerId = idStack.peek();
			String callerClass = classStack.peek();
			if ( traceOutgoingCalls ){
				classTokensMap.addOutgoingCall(callerClass, programPointToken, callerId);
			}
		}
		
		
		
		String objectId = programPointToken.getTokenMetaData().getCalledObjectId();
		
		if ( isConsructor(programPointToken) ){
			objectId = createConstructorId();
		}
		
		String calledClass = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature);
		classTokensMap.addIncomingCall(calledClass, programPointToken, objectId);
		
		idStack.push(objectId);
		classStack.push(calledClass);
	}

	private String createConstructorId() {
		constructors++;
		return "<init>"+constructors;
	}


	public void programPointEnd(InteractionTrace rawTrace,
			Token programPointToken) {
		
		String constructorId = idStack.pop();
		classStack.pop();
		
		if ( isConsructor(programPointToken) ){
			
			String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(programPointToken.getMethodSignature());
			String objectId = programPointToken.getTokenMetaData().getCalledObjectId();
			
			classTokensMap.mergeConstructorCall(constructorId, clazz, programPointToken, objectId);
		}
		
		
	}
	


	private boolean isConsructor(Token programPointToken) {
		String methodName = JavaSignatureParser.getMethodNameFromCompleteMethodSignature(programPointToken.getMethodSignature());
		//FIXME: we need to add the constructor signature returned by aspects
		if ( methodName.equals("<init>") || methodName.equals("new")){
			return true;
		}
		return false;
	}


	public void analysisBegin() {

	}


	public void sessionBegin(String sessionId) {
		
	}

	public void analysisEnd() {
		
	}


	public void traceBegin(InteractionTrace trace) {

	}


	public void traceEnd() {

	}

	
	@Override
	public void programPointGeneric(InteractionTrace trace, Token token) {
		throw new RuntimeException("Not implemented");
	}
	
}
