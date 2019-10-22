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

public class Node {

	private int id;
	private String function, sourceCode;
	private Location location;
	private List<Edge> inEdges;

	public Node(int id, String function, String sourceCode, Location location) {
		this.id = id;
		this.function = function;
		this.sourceCode = sourceCode;
		this.location = location;
		inEdges = new ArrayList<Edge>();
	}

	public int getId() {
		return id;
	}

	public String getFunction() {
		return function;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Edge> getInEdges() {
		return inEdges;
	}

	public void addInEdge(Edge edge) {
		inEdges.add(edge);
	}

	@Override
	public String toString() {
		String str = "";
		
		str += "ID: " + id + "\n";
		str += "Function: " + function + "\n";
		str += "Location: " + location + "\n";
		str += "Source: " + sourceCode + "\n";
		for (Edge e : inEdges) {
			String type = null;
			if (e instanceof AddressDependencyEdge) { 
				type = "address";
			} else if(e instanceof ControlDependencyEdge) {
				type = "control";
			} else if (e instanceof DataDependencyEdge) {
				type = "data";
			}
			str += "<- " + e.getFrom().getId() + " (" + type + ")\n";
		}
		
		return str;
	}
}
