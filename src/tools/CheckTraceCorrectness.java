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
package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import conf.BctSettingsException;

import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TraceReaderFactory;

public class CheckTraceCorrectness {

	/**
	 * @param args
	 * @throws BctSettingsException 
	 * @throws TraceException 
	 */
	public static void main(String[] args) throws TraceException, BctSettingsException {
		// TODO Auto-generated method stub
		
		
		Iterator<InteractionTrace> traces = getTraces(args); 

		while ( traces.hasNext() ){
			long tokenCounter = 0;
			try {
				InteractionTrace ft = traces.next();
				Token token = ft.getNextToken();

				String method = token.getTokenValue();
				Stack<String> s = new Stack<String>();

				while (method != null) {
					++tokenCounter;
					String methodName = method.substring(0, method.length() - 1);
					
					int pars = countChar ( methodName, ')');
					if ( pars > 2 ){
						System.err.println("Error, method signature not correct "+methodName);
					}
//					System.out.println("Method name : "+methodName+" "+method);

					if (method.endsWith("B")) {

						s.push(methodName);
					} else {
						String popped = s.pop();
						if ( ! popped.equals(methodName) ){
							System.err.println("Error, unexpected "+method+" expecting "+popped+" (token "+tokenCounter+")");
						}
					}
					token = ft.getNextToken();

					if ( token != null )
						method = token.getTokenValue();
					else
						method = null;
				}
				if ( s.size() != 0 ){
					System.err.println("The structure of the input file is not valid. ");
					System.err.println("Expected :");

					for ( int c = s.size()-1; c >= 0; --c )
						System.err.println(" "+s.get(c)+"E");

				} else {
					System.out.println("Result is ok");
				}

				System.out.println("Total tokens "+tokenCounter);
			} catch (TraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static int countChar(String methodName, char c) {
		int count = 0;
		for ( char ch : methodName.toCharArray() ) {
			if ( ch == c ){
				count++;		
			}
		}
		return count;
	}
	

	private static Iterator<InteractionTrace> getTraces(String[] args) throws TraceException, BctSettingsException {
		
		if ( args.length > 0 ){
			List<InteractionTrace> traces = new ArrayList<InteractionTrace>();
			for ( String arg : args ){
				FileInteractionTrace ft = new FileInteractionTrace("","X","1",new File(arg),null);
				traces.add(ft);
			}
			return traces.iterator();
		}
		
		return TraceReaderFactory.getReader().getInteractionTraces();
	}



}
