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
import java.io.IOException;

public class GdbMonitorAllConfigCreator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File parentFile = new File( args[0]);
		
		
		
		GdbMonitorAllConfigCreator c = new GdbMonitorAllConfigCreator();
		
		File output = new File(args[1]);
		
		c.createConfig( output, parentFile );
	}

	private void createConfig(File output, File parentFile) throws IOException {
		
		
		
//		List<FunctionMonitoringData> childrenDefs = GdbRegressionConfigCreator.extractFunctionsDefinitions( parentFile );
//		
//		BufferedWriter w = new BufferedWriter ( new FileWriter( output ) );
//		
//		
//		GdbRegressionConfigCreator.writeHeader( w );
//		
//		
//		for (FunctionMonitoringData def : childrenDefs ){
//			GdbRegressionConfigCreator.writeChildDef( w, def );
//		}
//		
//		
//		GdbRegressionConfigCreator.writeFooter(w);
//		
//		w.close();
	}






}
