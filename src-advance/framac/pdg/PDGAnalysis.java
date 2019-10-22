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
package framac.pdg;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class PDGAnalysis {
	
	public static void main(String[] args) {
		if (args.length != 5) {
			throw new InvalidParameterException("PDG analysis must be invoked with the following parameters: " 
					+ "\n- frama-c command\n- options list\n- working directory paht\n- output file name\n- list of source to analyze");
		}
	
		
		List<String> options = Arrays.asList(args[1].split(" "));
		
		FramaCExecutor framaCExecutor = new FramaCExecutor(args[0], options);
		
		framaCExecutor.runFramaC(args[2], args[3], args[4]);
	}
}
