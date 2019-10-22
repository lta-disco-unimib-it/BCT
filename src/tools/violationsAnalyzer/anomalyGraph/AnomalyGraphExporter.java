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
package tools.violationsAnalyzer.anomalyGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



public class AnomalyGraphExporter {

	public static void serialize ( AnomalyGraph graph, File dest ) throws FileNotFoundException, IOException{
		ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(dest)); 
		
		try{
			oos.writeObject(graph);
		} finally {
			oos.close();
		}
		
	}
	
	public static AnomalyGraph deserialize ( File dest ) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream( new FileInputStream(dest) );
		
		try {
			return (AnomalyGraph) ois.readObject();
		} finally {
			ois.close();
		}
	}
}
