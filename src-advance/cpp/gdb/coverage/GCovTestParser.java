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
package cpp.gdb.coverage;

import java.util.Hashtable;
import java.util.Map.Entry;

public class GCovTestParser extends GCovLineCoverageParser {

	public GCovTestParser(String sourcesFolderPath, String destinationFilePath) {
		super(sourcesFolderPath, destinationFilePath);
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]){
		
		if ( args.length == 2 ){
			GCovTestParser p = new GCovTestParser(args[0],args[1]);
			p.parseAll();
		} else {

			Hashtable<FileNameAndCoverageKey<Integer>, Integer> loaded = GCovParser.<Integer>load(args[0]);

			for (Entry<FileNameAndCoverageKey<Integer>,Integer> e : loaded.entrySet()  ){
				System.out.println(e.getKey()+" "+e.getValue());
			}
		}
	}
	
	@Override
	protected int getCoverageValueToAdd(int lineExecutionCount) {
		if ( lineExecutionCount > 0 ){
			return 1;
		}
		return 0;
	}

	
}
