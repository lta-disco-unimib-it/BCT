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
/*
 * Created on 29-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package traceReaders.normalized.traceCreation;

import java.util.Stack;
import java.util.Vector;

import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import conf.InvariantGeneratorSettings;

/**
 * This class generates normalized traces that for every monitored method list the sequences of methods it calls
 * 
 * If we have recorded the following sequence of method calls:
 * 
 * A.a()B#A.b()B#A.b()E#A.c()B#A.d()B#A.d()E#A.c()E#A.a()E#A.c()B#A.c()E#A.c()B#A.b()B#A.b()E#A.c()E#
 * 
 * it generates the following traces:
 * 
 * A.a() :  A.b()#A.c()|
 * A.b() : ||
 * A.c() : A.d()||A.b()
 * A.d() : |
 * 
 * @author Leonardo Mariani
 * @author Fabrizio Pastore
 * 
 */
public class MethodOutgoingTraceMaintainer implements TraceMaintainer {
	NormalizedInteractionTraceHandler tr;
	Stack<Vector<Token>> traceVector = new Stack<Vector<Token>>();
	Stack<Token> nameVector = new Stack<Token>();

	private static boolean traceGenericProgramPoints = true;
	
	public static boolean isTraceGenericProgramPoints() {
		return traceGenericProgramPoints;
	}

	public static void setTraceGenericProgramPoints(boolean traceProgramPoints) {
		MethodOutgoingTraceMaintainer.traceGenericProgramPoints = traceProgramPoints;
	}

	public MethodOutgoingTraceMaintainer() {
		
	}
	
	public MethodOutgoingTraceMaintainer(NormalizedInteractionTraceHandler tr) {
		this.tr=tr;
	}

	/* (non-Javadoc)
	 * @see tools.TraceMaintainer#newTrace(traceReaders.raw.Token)
	 */
	public void newTrace(Token token) {
		Vector<Token> vectorToken = new Vector<Token>();
		//currentPosition++;
		nameVector.push( token);
		traceVector.push( vectorToken );
	}
	
	/* (non-Javadoc)
	 * @see tools.TraceMaintainer#addSymbol(traceReaders.raw.Token)
	 */
	public void addSymbol(Token symbol) {
		if ( traceVector.size() > 0 )
			traceVector.peek().add(symbol);
	}
	
	/* (non-Javadoc)
	 * @see tools.TraceMaintainer#closeTrace(java.lang.String)
	 */
	public void closeTrace(String threadId) {
		try{
			String method = nameVector.pop().getTokenValue();
			String methodName = method.substring(0, method.length()-1);
			Vector<Token> trace = traceVector.pop();

			if ( trace.isEmpty() ) {
				tr.addInteractionTrace(methodName, null, threadId);
			} else {
				tr.addInteractionTrace(methodName, trace, threadId);
			}

		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see tools.TraceMaintainer#getNumberOpenTraces()
	 */
	public int getNumberOpenTraces() {
		return traceVector.size();
	}

	public void init(InvariantGeneratorSettings invariantGeneratorSettings,NormalizedInteractionTraceHandler traceHandler) {
		this.tr=traceHandler;
	}

	public void analysisEnd() {
	}

	public void programPointBegin(InteractionTrace trace, Token token) {
		
		addSymbol(token);
		newTrace(token);
		
	}
	

	public void programPointGeneric(InteractionTrace trace, Token token) {
		if ( traceGenericProgramPoints ){
			addSymbol(token);
		}
	
	}

	public void programPointEnd(InteractionTrace trace, Token token) {
		closeTrace(trace.getThreadId());
	}

	public void analysisBegin() {
		
	}

	public void sessionBegin(String sessionId) {
		
	}

	public void sessionEnd() {
		
	}

	public void traceBegin(InteractionTrace trace) {
		
	}

	public void traceEnd() {
		
	}

}
