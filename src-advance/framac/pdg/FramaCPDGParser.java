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
package framac.pdg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import framac.pdg.graph.ControlDependencyEdge;
import framac.pdg.graph.DataDependencyEdge;
import framac.pdg.graph.Graph;
import framac.pdg.graph.Location;
import framac.pdg.graph.Node;

public class FramaCPDGParser {

	private String fileToParse;
	private Graph graph;
	private Map<Integer, ArrayList<Integer>> controlDependencies;
	private Map<Integer, ArrayList<Integer>> dataDependencies;
	private Map<Integer, ArrayList<Integer>> addressDependencies;

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new InvalidParameterException(
					"The parser must be invoked with the following parameter: "
							+ "\n- path of the file to parse");
		}

		FramaCPDGParser parser = new FramaCPDGParser(args[0]);
		parser.parse();
	}

	public FramaCPDGParser(String fileToParse) {
		this.fileToParse = fileToParse;
		this.graph = new Graph();
		
		this.controlDependencies = new HashMap<Integer, ArrayList<Integer>>();
		this.dataDependencies = new HashMap<Integer, ArrayList<Integer>>();
		this.addressDependencies = new HashMap<Integer, ArrayList<Integer>>();
	}

	public void parse() {
		File file = new File(fileToParse);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line, functionName = null;
			while ((line = reader.readLine()) != null) {

				if (line.startsWith("[pdg] RESULT for")) {
					functionName = getFunctionName(line);
				} else {
					line = line.trim();
					if (line.contains(" | [Elem]") || line.startsWith("[Elem]")) {
						Node node = parseBlock(functionName, line);
	
						reader.mark(20000); //FIXME: this fixed limit can be a problem and leads to a failure (java.io.IOException: Mark invalid)
						while ((line = reader.readLine()).trim().startsWith("-")) {
							parseDependency(line, node);
						} 
						reader.reset();
					}
				}
			}

			reader.close();
			System.out.println(graph);
			
			addDependencies();
			
			System.out.println(graph);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addDependencies() {
		for (int nodeId : controlDependencies.keySet()) {
			Node node = graph.getNode(nodeId);
			ArrayList<Integer> deps = controlDependencies.get(nodeId);
			for (int dep : deps) {
				node.addInEdge(new ControlDependencyEdge(graph.getNode(dep), node));
			}
		}
		
		for (int nodeId : dataDependencies.keySet()) {
			Node node = graph.getNode(nodeId);
			ArrayList<Integer> deps = dataDependencies.get(nodeId);
			for (int dep : deps) {
				node.addInEdge(new DataDependencyEdge(graph.getNode(dep), node));
			}
		}
	}

	private void parseDependency(String line, Node node) { // Es. -[-c-]-> 1
		int index = line.indexOf("->");
		String flags = line.substring(0, index);
		String dependsOn = line.substring(index + 2, line.length());
		int id = Integer.valueOf(dependsOn.trim());

		if (flags.contains("a")) {
			ArrayList<Integer> deps = addressDependencies.get(node.getId());
			deps.add(id);
		}
		if (flags.contains("c")) {
			ArrayList<Integer> deps = controlDependencies.get(node.getId());
			deps.add(id);
		}
		if (flags.contains("d")) {
			ArrayList<Integer> deps = dataDependencies.get(node.getId());
			deps.add(id);
		}
	}

	private Node parseBlock(String functionName, String line) {
		line = line.trim();
		if (line.contains(" | [Elem]")) { // Es. lib/exclude.c:417 | [Elem] 19 : (unsigned int)seg->type
			int index = line.indexOf("|");
			Location loc = getLocation(line.substring(0, index));
			line = line.substring(index + 1, line.length() - 1);

			Node node = createNode(line, functionName);
			node.setLocation(loc);
			graph.addNode(node);
			return node;
		} else if (line.startsWith("[Elem]")) { // Es. [Elem] 7 : VarDecl : excluded
			Node node = createNode(line, functionName);
			graph.addNode(node);
			return node;
		}
		return null;
	}

	private Node createNode(String line, String functionName) {
		String[] tokens = line.split(":");
		int id = getElementId(tokens[0]);
		String source = tokens[1];
		
		controlDependencies.put(id, new ArrayList<Integer>());
		dataDependencies.put(id, new ArrayList<Integer>());
		addressDependencies.put(id, new ArrayList<Integer>());

		return new Node(id, functionName, source, null);
	}

	private Location getLocation(String line) {
		line = line.trim();
		String[] tokens = line.split(":");
		return new Location(tokens[0], Integer.valueOf(tokens[1]));
	}

	private int getElementId(String token) {
		token = token.trim();
		return Integer.valueOf(token.split(" ")[1]);
	}

	private String getFunctionName(String line) {
		/*
		 * Lines starting function blocks are in the format: [pdg] RESULT for
		 * function_name
		 */
		int index = line.lastIndexOf(' ');
		return line.substring(index, line.length() - 1).trim();
	}
}
