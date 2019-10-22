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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpp.gdb.GlobalVarsDetector.GlobalVariable;

public class GlobalVariablesMapSerializer {
	public static void store( Map<File,Set<GlobalVariable>> data, File dest ) throws FileNotFoundException, IOException{
		HashMap<File, Set<GlobalVariable>> toSave = new HashMap<File,Set<GlobalVariable>>();
		toSave.putAll(data);
		
		ObjectOutputStream os = new ObjectOutputStream( new FileOutputStream(dest));
			
		os.writeObject(toSave);
		
		os.close();

	}
	
	public static HashMap<File,Set<GlobalVariable>> load(File dest ) throws FileNotFoundException, IOException, ClassNotFoundException{
		
		ObjectInputStream is = new ObjectInputStream( new FileInputStream(dest));
		
		try {
			return (HashMap<File,Set<GlobalVariable>>) is.readObject();
		} finally {
			is.close();
		}
	}
}
