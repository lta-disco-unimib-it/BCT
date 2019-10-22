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
package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class TgfExporter {

	public static void exportToFile(DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g, File dest) throws IOException{
		BufferedWriter w = new BufferedWriter( new FileWriter(dest));
		for ( String v : g.vertexSet() ){
			w.write(v.trim()+" "+v.trim()+"\n");
		}
		w.write("#\n");
		for ( String from : g.vertexSet() ){
			for ( String to : g.vertexSet() ){
				
				DefaultWeightedEdge edge = g.getEdge(from, to);
				if ( edge != null){
					w.write(from.trim()+" "+to.trim()+"\n");
				}
			}
		}
		
		w.close();	
	}
	
	
}
