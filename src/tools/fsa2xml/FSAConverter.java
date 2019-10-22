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
package tools.fsa2xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;

import automata.fsa.FiniteStateAutomaton;

public class FSAConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File orig = new File( args[0] );
		File dest = new File( args[1] );
		
		FiniteStateAutomaton origFSA;
		Map<String,String> namesMapping = null;
		try {
			
			if ( args.length > 2 ){
				File mappingFile = new File( args[2] );
				Properties p = new Properties();
				FileInputStream is = new FileInputStream(mappingFile);
				try {
					p.load(is);
					
					namesMapping = new HashMap<String, String>();
					
					for ( Entry<Object,Object> entry : p.entrySet() ){
						namesMapping.put((String)entry.getKey(), (String)entry.getValue());
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			origFSA = LazyFSALoader.loadFSA(orig.getAbsolutePath(), namesMapping );
			LazyFSALoader.storeFSA(origFSA, dest);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LazyFSALoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		

	}

}
