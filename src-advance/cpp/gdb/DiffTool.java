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
package cpp.gdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import util.FileUtil;
import util.Logging;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class DiffTool {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		File v1 = new File( args[0] );
		File v2 = new File( args[1] );
		
		List<String> linesV1 = FileUtil.getLines( v1 );
		List<String> linesV2 = FileUtil.getLines( v2 );
		
		Logging.fine("Diffing "+v1.getAbsolutePath()+" AND "+v2.getAbsolutePath());
		
		Patch diff = DiffUtils.diff(linesV1, linesV2);
		
		for ( Delta delta : diff.getDeltas() ){
			int start = delta.getOriginal().getPosition();
			
			int end = delta.getOriginal().last();
			
			int startR = delta.getRevised().getPosition();
			
			int endR = delta.getRevised().last();
			
			System.out.println(delta.getType()+" "+start+" "+end+" "+startR+" "+endR);
		}
		
	}

}
