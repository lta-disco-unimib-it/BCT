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
package tools.invariantGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import regressionTestManager.MetaDataHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import tools.TokenMetaData;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import traceReaders.normalized.traceCreation.TraceMaintainer;
import traceReaders.normalized.traceCreation.TraceMaintainerFactory;
import traceReaders.raw.FileReaderException;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TraceReaderFactory;
import traceReaders.raw.TracesReader;
import conf.BctSettingsException;
import conf.EnvironmentalSetter;

public class RawInteractionTracesNormalizer {

	private Set<String> testsToIgnore;
	private Set<String> actionsToIgnore;
	private boolean invertFiltering = false;
	private Map<String,List<String>> methodCallIdsToSkip = new HashMap<String,List<String>>();
	private boolean metaHandlerDisabled;

	public Map<String, List<String>> getMethodCallIdsToSkip() {
		return methodCallIdsToSkip;
	}

	/**
	 * Normalize the project interaction traces
	 * 
	 * @param metaHandler
	 * @param methodsToIgnore
	 * @param methodsToInclude 
	 * @throws IOException
	 * @throws BctSettingsException
	 * @throws TraceException
	 * @throws NormalizedTraceHandlerException
	 * @throws FileReaderException 
	 */
	public void normalizeInteractionTraces( MetaDataHandler metaHandler, Set<String> methodsToIgnore, Set<String> methodsToInclude ) throws IOException, BctSettingsException, TraceException, NormalizedTraceHandlerException, FileReaderException {

		invertFiltering = EnvironmentalSetter.getInvariantGeneratorSettings().getInvertFiltering();
		System.out.println("Invert filtering: "+invertFiltering);

		TracesReader tr = TraceReaderFactory.getReader();
		System.out.println("InteractionTraceReader "+tr.getClass().getCanonicalName());

		NormalizedInteractionTraceHandler analyzedTraces;
		analyzedTraces = TraceHandlerFactory.getNormalizedInteractionTraceHandler();
		TraceMaintainer traceMaintainer = TraceMaintainerFactory.getTraceMaintainer(analyzedTraces);

		traceMaintainer.analysisBegin();

		Set<String> rsIds = tr.getRecordingSessionsIds();
		for ( String sessionId : rsIds ){
			normalizeSessionTraces(sessionId, tr, traceMaintainer, metaHandler, methodsToIgnore, methodsToInclude);
		}
	}

	/**
	 * This method normalized the races that belong to a session
	 * 
	 * @param sessionId
	 * @param tr
	 * @param traceMaintainer
	 * @param metaHandler
	 * @param methodsToIgnore
	 * @throws IOException
	 * @throws TraceException
	 * @throws NormalizedTraceHandlerException
	 */
	private void normalizeSessionTraces( String sessionId, TracesReader tr, TraceMaintainer traceMaintainer, MetaDataHandler metaHandler, Set<String> methodsToIgnore, Set<String> methodsToInclude ) throws IOException, TraceException, NormalizedTraceHandlerException{
		//TODO: query for the tests/actions of the current session
		testsToIgnore = getCleanActionMethodNames ( EnvironmentalSetter.getInvariantGeneratorSettings().getTestsToIgnore() );
		actionsToIgnore = getCleanActionMethodNames ( EnvironmentalSetter.getInvariantGeneratorSettings().getActionsToIgnore() );


		System.out.println("Tests to ignore:" + testsToIgnore);
		System.out.println("Actions to ignore:" + actionsToIgnore);

		Set<String> sessionsToIgnore = EnvironmentalSetter.getInvariantGeneratorSettings().getSessionsToIgnore();
		if ( sessionsToIgnore.contains(sessionId) ){
			System.out.println("Ignoring Session "+sessionId);
			return;
		}

		traceMaintainer.sessionBegin(sessionId);
		
		Iterator<InteractionTrace> traces = tr.getInteractionTracesForSession(sessionId);	
		int i = 0;

		if ( methodsToInclude != null && methodsToInclude.size() == 0 ){
			methodsToInclude = null;
		}

		while ( traces.hasNext() ){
			++i;
			InteractionTrace trace = traces.next();
			String threadId = trace.getThreadId();
			
			traceMaintainer.traceBegin( trace );

			System.out.println("processing interaction trace of thread \"" + trace.getThreadId() + "("+ i + ")");

			Stack<String> methodNamesStack = new Stack<String>();
			// full traces

			try {


				//TraceMaintainer traceMaintainer = new MethodOutgoingTraceMaintainer(analyzedTraces);

				int tokenPos = 0;

				Token token = trace.getNextToken();

				String metaInfo = null;

				if ( metaHandler != null && token != null ){
					try {
						metaInfo = trace.getNextMetaData();
					} catch ( TraceException e ){
						System.out.println("No MetaData");
						metaHandler = null;
						this.metaHandlerDisabled = true;
					}
				}

				while ( token != null ){


					String method = token.getTokenValue();

					tokenPos++;
					int callNumber = tokenPos-1;
					
					String methodName = method.substring(0, method.length() - 1);
					TokenMetaData metaData = null;


					System.out.println("Method "+method+" "+metaInfo);

					boolean skip = true;

					if ( methodsToInclude != null ) { //This is a really raw filtering it can deteriorate the corectness of the models

						for ( String include : methodsToInclude ){
							if ( methodName.matches(include) ){
								skip=false;
								break;
							}
						}

						if ( skip ){
							//System.out.println("Ignoring: "+methodName);
							token = trace.getNextToken();

							if ( metaHandler != null && token != null){

								metaInfo = trace.getNextMetaData();
							}

							continue;
						}
					}

					if ( methodsToIgnore != null ) { //This is a really raw filtering it can deteriorate the corectness of the models
						if ( methodsToIgnore.contains(methodName) ){
							System.out.println("Ignoring: "+methodName);
							token = trace.getNextToken();

							if ( metaHandler != null && token != null){

								metaInfo = trace.getNextMetaData();
							}

							continue;
						}
					}

					if (method.endsWith("B")) {


						//if needed add new trace


						//if needed add meta info
						if ( metaInfo != null ){
							try {
								metaData = metaHandler.handleInteractionBegin(methodName, metaInfo);
								fixTestNamesIfNeeded(metaData,methodName);
								token.setTokenMetaData( metaData );
							} catch (TcInfoHandlerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}


						if ( excludeToken ( token ) ){
							System.out.println("Ignoring according to exclusion rule: "+method);
							token = trace.getNextToken();
							if ( tr instanceof FileTracesReader ){
								String callId = FileTracesReader.getMethodCallId(sessionId, threadId, callNumber);
								addMethodCallIdToSkip( methodName, callId );
							}
							if ( metaHandler != null && token != null){

								metaInfo = trace.getNextMetaData();
							}

							continue;
						}

						methodNamesStack.push(methodName);

						//key symbol new trace
						traceMaintainer.programPointBegin(trace, token );

					} else if (method.endsWith("E")) {





						if ( metaInfo != null ){

							metaData = metaHandler.handleInteractionEnd(methodName, metaInfo);

							//Workaround for bug 166
							fixTestNamesIfNeeded(metaData,methodName);

							token.setTokenMetaData( metaData );

						}

						if ( excludeToken ( token ) ){
							System.out.println("Ignoring according to exclusion rule: "+method);
							token = trace.getNextToken();
							if ( tr instanceof FileTracesReader ){
								String callId = FileTracesReader.getMethodCallId(sessionId, threadId, callNumber);
								addMethodCallIdToSkip( methodName, callId );
							}
							if ( metaHandler != null ){

								metaInfo = trace.getNextMetaData();
							}

							continue;
						}


						String oldM = methodNamesStack.pop();

						if (!oldM.equals(methodName)){
							System.err.println("Malformed trace: found "+methodName+" expecting "+oldM + " (line "+tokenPos+", trace"+trace.getTraceId()+")");
						}

						traceMaintainer.programPointEnd(trace, token );
					} else if (method.endsWith("P")) {

						if ( metaInfo != null ){

							metaData = metaHandler.handleInteractionGenericProgramPoint(methodName, metaInfo);

							//Workaround for bug 166
							fixTestNamesIfNeeded(metaData,methodName);

							token.setTokenMetaData( metaData );

						}

						if ( excludeToken ( token ) ){
							System.out.println("Ignoring according to exclusion rule: "+method);
							token = trace.getNextToken();
							if ( tr instanceof FileTracesReader ){
								String callId = FileTracesReader.getMethodCallId(sessionId, threadId, callNumber);
								addMethodCallIdToSkip( methodName, callId );
							}
							if ( metaHandler != null ){

								metaInfo = trace.getNextMetaData();
							}

							continue;
						}

						traceMaintainer.programPointGeneric(trace, token );

					} else {
						throw new TraceException("The token "+method+" is not valid");
					}

					//FIXME bad code below
					token = trace.getNextToken();

					if ( metaHandler != null && token != null){

						metaInfo = trace.getNextMetaData();
					}


				} //end of WHILE != null

				//sanitycheck
				if (methodNamesStack.size() > 0){
					System.err.println("The structure of the input file is not valid. ");
					System.err.println("The following methods have a begin but not an end:");
					for ( String m : methodNamesStack ){
						System.err.println(m);
					}
				}
				traceMaintainer.analysisEnd();
			} catch (TraceException e) {
				e.printStackTrace();
			}

			if ( metaHandler != null ){
				try {
					metaHandler.save();
				} catch (TcInfoHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			trace.close();	

			traceMaintainer.traceEnd();

		} //end of WHILE traces.hasNext()
		traceMaintainer.sessionEnd();
	}

	public boolean isMetaHandlerDisabled() {
		return metaHandlerDisabled;
	}

	private void addMethodCallIdToSkip(String methodName, String callId) {
		List<String> callIds = methodCallIdsToSkip.get( methodName );
		if ( callIds == null ){
			callIds = new ArrayList<String>();
			methodCallIdsToSkip.put(methodName, callIds);
		}
		callIds.add( callId );
	}

	private Set<String> getCleanActionMethodNames(Set<String> tests) {
		Set<String> res = new HashSet<String>();

		for ( String test : tests ){
			res.add(getCleanActionMethodName(test));
		}

		return res;
	}


	private String getCleanActionMethodName(String test) {
		int pos = test.lastIndexOf(")");
		return test.substring(0, pos+1);

	}


	private void fixTestNamesIfNeeded(TokenMetaData metaData, String methodName) {
		List<String> tests = metaData.getCurrentTests();
		if ( tests.size() ==  1 ){
			String testName = tests.get(0);
			if ( testName.startsWith("*") ){

				String testPrefix = testName.substring(1)+"(";
				if ( methodName.startsWith(testPrefix) ){
					System.out.println("Test name incomplete "+testName+" using "+methodName);
					//We are in the case of bug 166
					tests= new ArrayList<String>();
					tests.add(methodName);
					metaData.setCurrentTests(tests);
				}
			}
		}
	}

	/**
	 * Returns true if the filtering strategy indicates that this Token should be excluded.
	 * This happens when the token belongs to one or more tests or actions that should be excluded.
	 * 
	 * @param token
	 * @return
	 */
	private boolean excludeToken(Token token) {
		return invertFiltering ^ excludeTokenInternal(token); 
	}

	private boolean excludeTokenInternal(Token token) {
		TokenMetaData metadata = token.getTokenMetaData();

		if ( metadata == null ){
			return false;
		}

		if ( testsToIgnore.size() > 0 ){
			for ( String test : metadata.getCurrentTests() ){
				test = getCleanActionMethodName(test);
				//				System.out.println("Contains test? "+test);
				if ( testsToIgnore.contains(test) ){
					return true;
				}
				//				System.out.println("NO");
			}
		}

		if ( actionsToIgnore.size() > 0 ){
			for ( String action : metadata.getCurrentActions() ){
				if ( actionsToIgnore.contains(action) ){
					return true;
				}
			}
		}

		return false;
	}


}
