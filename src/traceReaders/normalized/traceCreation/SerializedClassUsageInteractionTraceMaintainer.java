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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedInteractionTraceHandlerCsv;
import traceReaders.normalized.NormalizedInteractionTraceHandlerFile;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import util.FileIndex.FileIndexException;
import util.FileIndexAppend;
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
public abstract class SerializedClassUsageInteractionTraceMaintainer implements TraceMaintainer {
	private static final boolean DEBUG = false;
	private LinkedList<String> idStack = new LinkedList<String>();
	private LinkedList<String> classStack = new LinkedList<String>();
	
	private HashMap<String,Integer> innerConstructors = new HashMap<String, Integer>();
	
	@Override
	public void programPointGeneric(InteractionTrace trace, Token token) {
		throw new RuntimeException("Not implemented");
	}

	private void innerConstructorBegin( String objectId ){
		Integer value = innerConstructors.get(objectId);
		if ( value == null ){
			value = 0;
			
		}
		innerConstructors.put(objectId, (value+1) );
	}
	
	private void innerConstructorEnd( String objectId ){
		Integer value = innerConstructors.get(objectId);
		
		innerConstructors.put(objectId, (value-1) );
	}
	
	private int getInnerConstructors( String objectId ){
		Integer value = innerConstructors.get(objectId);
		
		if ( value == null ){
			return 0;	
		}
		
		
		if ( value == 0 ){ //keep the map clean
			innerConstructors.remove(objectId);
		}
		
		return value;
	}
	
	private static class ClassTokensMap {
		private HashMap<String,List<Token>> traces = new HashMap<String,List<Token>>();
		private HashMap<String,String> classObjects = new HashMap<String, String>();
		
		
		private Map<String, List<Token>> contextTraces = new HashMap<String, List<Token>>();
		private Map<String, String> classContexts = new HashMap<String, String>();
		private FileIndexAppend tmpRepository;
		private FileIndexAppend ctxTmpRepository;
		
		
		
		private HashMap<String,Integer> objCounters = new HashMap<String, Integer>();
		private HashMap<String,List<Token>> objStacks = new HashMap<String, List<Token> >();
		
		public ClassTokensMap(FileIndexAppend tmpRepository,
				FileIndexAppend ctxTmpRepository) {
			this.tmpRepository = tmpRepository;
			this.ctxTmpRepository = ctxTmpRepository;
		}

		/**
		 * Add an incoming call 
		 *  
		 * @param clazz
		 * @param calledMethod
		 * @param objectId
		 */
		public void addIncomingBeginCall(String clazz,Token calledMethod,String objectId){
			if ( DEBUG )
				System.out.println("BEGIN "+calledMethod.getMethodSignature()+" "+objectId);
			
			increaseObjCounter(clazz, objectId);
			
			
//			
//			During recording we cannot monitor calls to constructors of classes 
//			not instrumented.
//			For this reason we cannot put this check here.
//			
//			if ( ! traceExist(clazz, objectId) ){
//				return;
//			}
			
			//addCall(clazz, calledMethod, objectId);
		}
		
		public void addStaticConstructorIncomingBeginCall(String clazz,Token calledMethod,String objectId){
			
			
			addCall(clazz, calledMethod, objectId);
		}
		
		public void addConstructorIncomingEndCall(String clazz,Token calledMethod,String objectId){
			if ( DEBUG )
				System.out.println("END CONSTR "+calledMethod.getMethodSignature()+" "+objectId);
			
			resetCallTrace(clazz, calledMethod, objectId);
			addCall(clazz, calledMethod, objectId);
		}
		
		
		public void addCall(Token calledMethod){
			if ( DEBUG )
				System.out.println("===>     Adding call "+calledMethod.getMethodSignature() );
			
			String completeMethodSignature = calledMethod.getMethodSignature();
			
			String objectId = calledMethod.getTokenMetaData().getCalledObjectId();
			
			String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature);
			
			
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
		
		public void resetCallTrace(String clazz,Token calledMethod,String objectId){
			List<Token> trace = getTrace(clazz,objectId);
			
			trace.clear();
			
			
			
			List<Token> contextTrace = getContextTrace(clazz, objectId);
			contextTrace.clear();
			
			
		}
		
		
		public void addCall(String clazz,Token calledMethod,String objectId){
			List<Token> trace = getTrace(clazz,objectId);
			
			if ( trace == null ){
				return;
			}
			
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
//		public void mergeConstructorCall(String constructorId, String clazz, Token calledMethod,String objectId){
//			
//			
//			List<Token> constructorCalls = getTrace(clazz,constructorId);
//			List<Token> constructorCallsContext = getContextTrace(clazz, constructorId);
//			
//			removeTrace(clazz,constructorId);
//			removeContextTrace(clazz,constructorId);
//			
//			List<Token> trace = getTrace(clazz,objectId);
//			List<Token> traceContext = getContextTrace(clazz,objectId);
//			
//			for ( int i = 0; i < constructorCalls.size(); ++i ){
//				Token constructorToken = constructorCalls.get(i);
//				
//				//Update metadata object id
//				TokenMetaData oldMetaData = constructorToken.getTokenMetaData(); 
//				
//				ExecutionTokenMetaData md = new ExecutionTokenMetaData();
//				md.setCalledObjectId(objectId);
//				md.setCurrentActions(oldMetaData.getCurrentActions());
//				md.setCurrentTests(oldMetaData.getCurrentTests());
//				md.setTimestamp(oldMetaData.getTimestamp());
//				md.setContextData(oldMetaData.getContextData());
//				
//				constructorToken.setTokenMetaData(md);
//				
//				trace.add(i, constructorToken);
//				
//				
//				
//				Token contextToken = constructorCallsContext.get(i); 
//				traceContext.add(i, contextToken );
//				
//			}
//			
//			
//			
//		}
		
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

		private boolean traceExist(String clazz, String objectId) {
			return classObjects.containsKey(objectId);
		}

		
		private Integer getObjCounter(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			return objCounters.get(objectId);
		}
		
		private void resetObjCounter(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			objCounters.remove(objectId);
			
			
		}
		
		
		private void increaseObjCounter(String clazz, String objectId) {
			
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			Integer counter = objCounters.get(objectId);
			
			if ( counter == null ){
				counter = 0;
			}
			
			
			objCounters.put(objectId, counter+1);
			
			if ( DEBUG )
				System.out.println("Increased counter for "+objectId+" "+getObjCounter(clazz, objectId));
			
		}
		
		
		private void decreaseObjCounter(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			Integer counter = objCounters.get(objectId);
			
			if ( counter == null ){
				counter = 0;
			}
			
			
			objCounters.put(objectId, counter-1);
			
			if ( DEBUG )
				System.out.println("Decreased counter for "+objectId+" "+getObjCounter(clazz, objectId));
		}
		
		
		private List<Token> getObjStack(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			if ( ! objStacks.containsKey(objectId) ){
				ArrayList<Token> stack = new ArrayList<Token>();
				objStacks.put( objectId, stack );
				
				
			}
			
			return objStacks.get(objectId);
		}
		
		
		private void resetObjStack(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			if ( objStacks.containsKey(objectId) ){
				objStacks.remove( objectId );
			}
			
			
		}
		
		private List<Token> getTrace(String clazz, String objectId) {
			//System.out.println("Trace for "+clazz+" "+objectId);
			if ( objectId == null || objectId.equals("")){
				objectId=clazz;
			}
			
			if ( ! classObjects.containsKey(objectId) ){
				classObjects.put(objectId, clazz);
				
				String fileId = null;
				try {
					fileId = tmpRepository.add(objectId);
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				List<Token> list = new FileList<Token>(new File(tmpRepository.getIndexFile().getParent(),fileId));
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
				
				if ( DEBUG ){
					System.out.println( "ADDING CONTEXT TRACE for " + objectId);
				}
				
				classContexts.put(objectId, clazz);
				String fileId = null;
				try {
					fileId = ctxTmpRepository.add(objectId);
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				List<Token> list = new FileList<Token>(new File(ctxTmpRepository.getIndexFile().getParent(),fileId));
				contextTraces.put(objectId, list );
				return list;
			}
			return contextTraces.get(objectId);
	
		}


		public void addOutgoingCall(String clazz,Token calledMethod,String objectId){

			addCall(clazz, calledMethod, objectId);
		}

		public void addIncomingEndCall(String calledClass,
				Token programPointToken, String objectId) {
			if ( DEBUG )
				System.out.println("END "+programPointToken.getMethodSignature()+" "+objectId);
			 
			decreaseObjCounter(calledClass, objectId);
			
			List<Token> stack = getObjStack(calledClass, objectId);
			stack.add(programPointToken);
			
			
			if ( getObjCounter(calledClass, objectId)  == 0 ){
				for ( int i = stack.size()-1; i >= 0; i-- ){
					Token token = stack.get(i);
					addCall(token);
				}
				
				
				resetObjStack(calledClass, objectId);
				resetObjCounter(calledClass, objectId);
			}
			
		}
		
	}
	
	private ClassTokensMap classTokensMap;
	
	public interface ConfigurationOptions{
		public static final String componentsDefinitionFile="traceMaintainer.componentsDefinitionFile";
	}


	private NormalizedInteractionTraceHandler handler;
	private int constructors;
	private NormalizedInteractionTraceHandlerCsv csvHandler;
	private boolean traceOutgoingCalls;
	private NormalizedInteractionTraceHandlerCsv contextCsvHandler;
	
	private FileIndexAppend tmpRepository;
	private FileIndexAppend ctxTmpRepository;
	private boolean inConstructor;

	
	public SerializedClassUsageInteractionTraceMaintainer(boolean traceOutgoingCalls){
		this.traceOutgoingCalls = traceOutgoingCalls;
	}


	public void init(InvariantGeneratorSettings invariantGeneratorSettings, NormalizedInteractionTraceHandler handler) throws ComponentsDefinitionException {
		
		this.handler = handler;
		
		File dest = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteraction" );
		dest.mkdirs();
		csvHandler = new NormalizedInteractionTraceHandlerCsv(dest);
		
		File destTmp = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteractionTmp" );
		destTmp.mkdirs();
		tmpRepository = new FileIndexAppend(new File(destTmp,"interactions.idx"),".txt");
		
		
		File destCtxTmp = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteractionCtxTmp" );
		destCtxTmp.mkdirs();
		ctxTmpRepository = new FileIndexAppend(new File(destCtxTmp,"interactions.idx"),".txt");
		
		classTokensMap = new ClassTokensMap( tmpRepository, ctxTmpRepository);
		
		File destCtx = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteractionContext" );
		destCtx.mkdirs();
		contextCsvHandler = new NormalizedInteractionTraceHandlerCsv(destCtx);
	}
	
	public void sessionEnd() {
		
		System.out.println("Session finished ");
		
//		Comparator<Token> timestampComparator = new Comparator<Token>(){
//
//			public int compare(Token o1, Token o2) {
//				long diff = (o1.getTokenMetaData().getTimestamp()-o2.getTokenMetaData().getTimestamp());
//				if (  diff == 0 ){
//					return 0;
//				} else if ( diff > 0 ){
//					return 1;
//				}
//				return -1;
//			}
//			
//		};
		
		for ( Entry<String,String> entry : classTokensMap.classObjects.entrySet() ){
			
			String objectId = entry.getKey();
			String className = entry.getValue();
			
			 
			
			List<Token> trace = classTokensMap.traces.get(objectId);
			
			//Collections.sort(trace, timestampComparator );
			
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
		
		tmpRepository.deleteAll();
		ctxTmpRepository.deleteAll();
		
		classTokensMap = new ClassTokensMap(tmpRepository,ctxTmpRepository);
	}


	public void programPointBegin(InteractionTrace rawTrace,
			Token programPointToken) {
		
		if ( DEBUG )
			System.out.println("PP BEGIN "+programPointToken.getMethodSignature());
		
		String completeMethodSignature = programPointToken.getMethodSignature();
		
		String objectId = programPointToken.getTokenMetaData().getCalledObjectId();
		
		String calledClass = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature);
		
		
		
		
		//Static constructor
		if ( isStaticConstructor(programPointToken)  ){
			if ( DEBUG )
				System.out.println("STATIC CONSTRUCTOR");
			
			classTokensMap.addStaticConstructorIncomingBeginCall(calledClass, programPointToken, objectId);
			
			
			addCallToStack( calledClass, calledClass );

			return;
		}
		
		
		//First constructor
		if ( ( objectId == null ) && isConsructor(programPointToken)  ){
			if ( DEBUG )
				System.out.println("CONSTRUCTOR BEGIN");
			
			addCallToStack( objectId, calledClass );
			
			return;
		}
		
		//Inner constructor
		if ( isConsructor(programPointToken)  ){
			if ( DEBUG )
				System.out.println("INNER CONSTRUCTOR");
			innerConstructorBegin(objectId);
			
			addCallToStack( objectId, calledClass );
			
			return;
		}
		
		//Normal method call
		if ( DEBUG )
			System.out.println("NORMAL CALL");
		
		//static case
		if ( objectId == null ) {
			objectId = calledClass;
		}

		if ( checkCallStack(objectId ) ){
			addCallToStack( objectId, calledClass );
			
			return;
		}
		
		addCallToStack( objectId, calledClass );
				
		classTokensMap.addIncomingBeginCall(calledClass, programPointToken, objectId);
		
		
	}



	private void addCallToStack(String objectId, String calledClass) {
		idStack.push(objectId);
		classStack.push(calledClass);
	}

	private boolean isStaticConstructor(Token programPointToken) {
		String methodName = JavaSignatureParser.getMethodNameFromCompleteMethodSignature(programPointToken.getMethodSignature());
		//FIXME: we need to add the constructor signature returned by aspects
		if ( methodName.equals("<clinit>") || methodName.equals("newStatic")){
			return true;
		}
		return false;
	}


	/**
	 * Returns true if caller and callee are the same
	 * 
	 * @return
	 */
	public boolean checkCallStack(String objectId){
		
		if ( ! idStack.isEmpty() ){
			String callerId = idStack.peek();
			classStack.peek();
			
			if ( callerId == null ){ 
				//null when caller is a constructor
				//a constructor could invoke another object method
				return false;
			}
			
			if ( callerId.equals(objectId) ){
				if ( DEBUG )
					System.out.println("NORMAL CALL FORM SAME OBJECT, simply return "+callerId+" == "+objectId);
				return true;
			}
		}
		
		return false;
	}
	
	
	public void programPointEnd(InteractionTrace rawTrace,
			Token programPointToken) {
		if ( DEBUG )
			System.out.println("PP END "+programPointToken.getMethodSignature());

		String completeMethodSignature = programPointToken.getMethodSignature();
		
		String calledClass = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature);
		
		String objectId = programPointToken.getTokenMetaData().getCalledObjectId();
		
		popOutOfStack();
		
		//Normal call end
		
		if ( objectId == null ){
			objectId = calledClass;
		}
		
		
		if ( isStaticConstructor(programPointToken)  ){
			if ( DEBUG )
				System.out.println("STATIC CONSTRUCTOR END");
			
			
			return;
			
		}
		
		if ( ! isConsructor(programPointToken) ){
			if ( DEBUG )
				System.out.println("NORMAL CALL END");
			
			if ( checkCallStack(objectId) ){
				return;
			}
			
			classTokensMap.addIncomingEndCall(calledClass, programPointToken, objectId);
			
			return;
		}
		
		//Is constructor
		
		
		
		
		//Inner constructor
		
		if ( getInnerConstructors(objectId) > 0 ){
			if ( DEBUG )
				System.out.println("INNER CONSTRUCTOR END");
			
			innerConstructorEnd(objectId);
			
			
			return;
		}
		
		
		//First constructor 
		if ( DEBUG )
			System.out.println("FIRST CONSTRUCTOR END");
		
		if ( checkCallStack(objectId) ){
			return;
		}
		
		
		
		classTokensMap.addConstructorIncomingEndCall(calledClass, programPointToken, objectId);

	}
	


	private void popOutOfStack() {
		if ( ! idStack.isEmpty() ){
			idStack.pop();
			classStack.pop();	
		}
	}

	private static boolean isConsructor(Token programPointToken) {
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

}
