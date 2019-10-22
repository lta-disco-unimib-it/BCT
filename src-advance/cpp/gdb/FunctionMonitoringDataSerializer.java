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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FunctionMonitoringDataSerializer {
	private static final Logger LOGGER = Logger.getLogger(FunctionMonitoringDataSerializer.class.getCanonicalName());
	
	public static void store( Collection<FunctionMonitoringData> data, File dest ) throws FileNotFoundException, IOException{
		ObjectOutputStream os = new ObjectOutputStream( new FileOutputStream(dest));
		
		Map<String,FunctionMonitoringData> map = new HashMap<String,FunctionMonitoringData>();
		
		for ( FunctionMonitoringData func : data ){
			map.put(func.getMangledName(), func);
		}
		
		os.writeObject(map);
		
		os.close();

	}
	
	public static Map<String,FunctionMonitoringData> load(File dest ) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream is = new ObjectInputStream( new FileInputStream(dest));

		try {
			Map<String, FunctionMonitoringData> loadedData = (Map<String, FunctionMonitoringData>) is.readObject();
			LOGGER.fine("Data loaded from "+dest.getAbsolutePath()+" : "+loadedData);

			Collection<FunctionMonitoringData> coll = loadedData.values();
			for ( FunctionMonitoringData func : coll ){
				func.resetCallersAndCallees(coll);
			}
			
			return loadedData;
		} finally {
			is.close();
		}

		
	}

	public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException{
		List<FunctionMonitoringData> l = new ArrayList<FunctionMonitoringData>();
		
		FunctionMonitoringData f = new FunctionMonitoringData("func");
		f.addLine(new LineData("aaa", 13));
		f.addCallee(f);
		f.addCaller(f, "0x08383");
		l.add(f);
		File file = new File("removeMe.ser");
		store(l, file );
		
		Map<String, FunctionMonitoringData> map = load(file);
		
		System.out.println(map);
	}
}
