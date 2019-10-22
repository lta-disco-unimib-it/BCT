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

import java.util.List;

public class StackComparisonUtil {

	public static List<StackTraceElement> identifyMissingExitsDuringEnter(
			List<StackTraceElement> lastStack, 
			List<StackTraceElement> traceStack ){
		return identifyMissingExits(lastStack,traceStack.subList(0, traceStack.size()-1));
	}

	public static  List<StackTraceElement> identifyMissingExitsDuringExit(
			List<StackTraceElement> lastStack, 
			List<StackTraceElement> traceStack ) {
		return identifyMissingExits(lastStack,traceStack);
	}

	private static List<StackTraceElement> identifyMissingExits(
			List<StackTraceElement> lastStack, 
			List<StackTraceElement> traceStack) {
		int commonPrefixElements = numberOfCommonPrefixElements( lastStack, traceStack );

		if ( commonPrefixElements == lastStack.size() ){
			throw new IllegalStateException("An unexpected comparison between equal stacks has been made");
		}

		return lastStack.subList(commonPrefixElements, lastStack.size());

	}
	

	private static int numberOfCommonPrefixElements(
			List<StackTraceElement> lastStack,
			List<StackTraceElement> traceStack) {
		for ( int i = 0; i < lastStack.size() ; i ++ ){
			if ( i >= traceStack.size() ){
				return i;
			}

			if ( ! lastStack.get(i).equals(traceStack.get(i) ) ){
				return i;
			}
		}
		return lastStack.size();
	}
	
	
}
