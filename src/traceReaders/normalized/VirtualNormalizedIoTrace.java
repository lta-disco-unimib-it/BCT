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
package traceReaders.normalized;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import traceReaders.raw.TraceException;
import dfmaker.core.Superstructure;
import dfmaker.core.Variable;
import dfmaker.core.DaikonDeclarationMaker.DaikonComparisonCriterion;

public class VirtualNormalizedIoTrace implements NormalizedIoTrace {
	private HashMap<String,List<Vector<Variable>>> genericPoints = new HashMap<String, List<Vector<Variable>>>();
	private HashMap<String,List<Vector<Variable>>> entryPoints = new HashMap<String, List<Vector<Variable>>>();
	private HashMap<String,List<Vector<Variable>>> exitPoints = new HashMap<String, List<Vector<Variable>>>();
	private HashMap<String,List<Vector<Variable>>> objPoints = new HashMap<String, List<Vector<Variable>>>();
	private Superstructure entrySuperStructure;
	private Superstructure exitSuperStructure;
	
	public void addEntryPoint(String entryPoint,
			Vector<Variable> normalizedPoint) {
		addPoint ( entryPoint, entryPoints, normalizedPoint );
	}
	
	public void addGenericPoint(String entryPoint,
			Vector<Variable> normalizedPoint) {
		addPoint ( entryPoint, genericPoints, normalizedPoint );
	}

	private void addPoint(String entryPoint,
			HashMap<String, List<Vector<Variable>>> pointsMap,
			Vector<Variable> normalizedPoint) {
		List<Vector<Variable>> list = pointsMap.get(entryPoint);
		if ( list == null ){
			list = new LinkedList<Vector<Variable>>();
			pointsMap.put(entryPoint,list);
		}
		
		list.add(normalizedPoint);
	}

	public void addExitPoint(String exitPoint, Vector<Variable> normalizedPoint) {
		addPoint ( exitPoint, exitPoints, normalizedPoint );
	}

	public void addObjectPoint(String objectPoint,
			Vector<Variable> normalizedPoint) {
		// TODO Auto-generated method stub
		addPoint ( objectPoint, objPoints, normalizedPoint );
	}

	public void commit(DaikonComparisonCriterion  comparisonCriterion ) throws TraceException {
		// TODO Auto-generated method stub

	}

	public File getDaikonDeclFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getDaikonTraceFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setEntrySuperStructure(Superstructure entrySuperStructure) {
		this.entrySuperStructure = entrySuperStructure;
	}

	public void setExitSuperStructure(Superstructure exitSuperStructure) {
		this.exitSuperStructure = exitSuperStructure;
	}

	public Map<String,List<Vector<Variable>>> getEntryPoints() {
		return entryPoints;
	}

	public Map<String,List<Vector<Variable>>> getExitPoints() {
		return exitPoints;
	}

	public Map<String,List<Vector<Variable>>> getObjPoints() {
		return objPoints;
	}

	public Superstructure getEntrySuperStructure() {
		return entrySuperStructure;
	}

	public Superstructure getExitSuperStructure() {
		return exitSuperStructure;
	}

}
