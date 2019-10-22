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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.FileUtil;

public class VariablesFilter {

	public static VariablesFilter INSTANCE = new VariablesFilter();

	private HashMap<String,Set<String>> variablesToIgnore = new HashMap<String,Set<String>>();
	private HashSet<String> variablesToIgnoreGlobally = new HashSet<String>();
	
	{
		File f = new File("bct.variablesToIgnore");
		if ( f.exists() ){
			try {
				List<String> lines = FileUtil.getLines(f);
				for( String line : lines ){
					String[] splitted = line.split(":");
					String fileName = splitted[0];
					String variableName = splitted[1];

					if ( fileName.equals("*") ){
						variablesToIgnoreGlobally.add(variableName);
					} else {

						Set<String> toIgnore = variablesToIgnore.get(fileName);
						if ( toIgnore == null ){
							toIgnore = new HashSet<String>();
							variablesToIgnore.put(fileName, toIgnore);
						}

						toIgnore.add(variableName);
					}
				}
				System.out.println("bct.variablesToIgnore:"+variablesToIgnore);
				System.out.println("bct.variablesToIgnoreGlobally:"+variablesToIgnoreGlobally);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean varsToIgnoreInFile(File file) {
		return variablesToIgnore.containsKey(file.getName()) || variablesToIgnoreGlobally.size() > 0;
	}

	public boolean ignore(File file, String name) {
		Set<String> toIgnore = variablesToIgnore.get(file.getName());
		if ( toIgnore != null ){
			if ( toIgnore.contains(name) ){
				return true;
			}
		}
		
		return variablesToIgnoreGlobally.contains(name);
	}

}
