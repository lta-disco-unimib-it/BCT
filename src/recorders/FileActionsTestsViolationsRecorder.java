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
package recorders;

import java.io.PrintWriter;
import java.util.Set;

import executionContext.ActionsRegistryException;
import executionContext.TestCaseData;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;

public class FileActionsTestsViolationsRecorder extends
		FileActionsViolationsRecorder {

	@Override
	protected void writeAdditionalInfo(PrintWriter bw) {
		
		super.writeAdditionalInfo(bw);
		writeTestCaseInfo(bw);
	}

	private void writeTestCaseInfo(PrintWriter bw) {
		TestCasesRegistry registry = TestCasesRegistryFactory.getExecutionContextRegistry();
		String message;
		try {
			Set<Integer> actions = registry.getCurrentActions();
			
			StringBuffer sb = new StringBuffer();
			
			boolean first = true; 
			for ( Integer action : actions ){
				
				if ( ! first ){
					sb.append("|");
				} else {
					first = false;
				}
				TestCaseData data = registry.getExecutionContextData(action);
				sb.append(data.getTestCaseName());
			}
			message=sb.toString();
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message="null";
		}
		
		bw.write("TESTS:\t");
		bw.write(message);
		bw.write("\n");
	}

}
