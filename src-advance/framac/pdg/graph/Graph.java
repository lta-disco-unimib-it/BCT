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
package framac.pdg.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	
	private List<Node> nodes;

	public Graph() {
		nodes = new ArrayList<Node>();
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}
	
	public Node getNode(int id) {
		for (Node node : nodes) {
			if (node.getId() == id) {
				return node;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		String str = "";
		
		for (Node n : nodes) {
			str = str + "\n" + n;
		}
		return str;
	}
}
