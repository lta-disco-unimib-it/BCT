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
package tools;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import check.Checker;

import conf.BctSettingsException;
import conf.EnvironmentalSetter;

import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TraceReaderFactory;
import traceReaders.raw.TracesReader;


public class InteractionSimulator {

	public static void replay() throws TraceException, BctSettingsException, NormalizedTraceHandlerException{
		TracesReader tr;

		tr = TraceReaderFactory.getReader();


		Iterator traces = tr.getInteractionTraces();
		int i = 0;
		
		while ( traces.hasNext() ){
			++i;
			InteractionTrace trace = (InteractionTrace) traces.next();
			
			System.out.println("processing interaction trace of thread:" + trace.getThreadId() + ".");
			
			Token methodT = trace.getNextToken();
			
			long tid = Long.valueOf(trace.getThreadId());
			
			String fileName = EnvironmentalSetter.getViolationsRecorderSettings().getProperty("violationsFile");
			File file = new File(fileName);
			Stack<String> activeStack = new Stack<String>();
			
			try {
				String method = methodT.getTokenValue();
				String methodName = methodT.getMethodSignature(); 
				
				while (methodT != null) {
					 
					
					FileWriter writer = new FileWriter( file, true );
					writer.write("#"+method);
					if (method.endsWith("B")) {
						activeStack.push(methodName);
						writer.write(getStackSequence(activeStack));
						writer.close();
						Checker.checkInteractionEnter(methodName, tid );
					} else {
						writer.write(getStackSequence(activeStack));
						writer.close();
						Checker.checkInteractionExit(methodName, tid );
						activeStack.pop();
					}
					methodT = trace.getNextToken();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static String getStackSequence(Stack<String> activeStack) {
		StringBuffer sb = new StringBuffer();
		sb.append("Stack : ");
		for ( String method : activeStack ){
			sb.append(method);
			sb.append("#");
		}
		return sb.toString();
	}

	public static void main( String[] args ){
		try {
			replay();
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BctSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}