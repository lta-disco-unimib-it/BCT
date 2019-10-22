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
import java.util.Stack;

import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;

public class InteractionTraceFormattedPrinter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileInteractionTrace ft = new FileInteractionTrace("","","1",new File(args[0]),null);

		long tokenCounter = 0;
		try {
			
			Token token = ft.getNextToken();
			
			String method = token.getTokenValue();
			Stack<String> s = new Stack<String>();
			
			while (method != null) {
				++tokenCounter;
				String methodName = method.substring(0, method.length() - 1);
				//System.out.println("Method name : "+methodName+" "+method);
							
				if (method.endsWith("B")) {
					printMethodName(s.size(),method);
					s.push(methodName);
				}else if (method.endsWith("P")) {
					printMethodName(s.size(),method);
					
				} else {
					String popped = s.pop();
					printMethodName(s.size(),method);
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
				System.err.println("Result is ok");
			}
			
			System.err.println("Total tokens "+tokenCounter);
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printMethodName(int size, String methodName) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < size; ++i ){
			sb.append("  ");
		}
		sb.append(methodName);
		System.out.println(sb.toString());
	}
	
}
