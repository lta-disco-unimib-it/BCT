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
package modelsFetchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import tools.violationsAnalyzer.ViolationsUtil;

public class IoModel {
	private HashSet<String> preconditions = new HashSet<String>();
	private HashSet<String> postconditions = new HashSet<String>();	
	
	public void addPreconditions(ArrayList preconditions) {
		this.preconditions.addAll(preconditions);
	}

	public void addPrecondition(String line) {
		preconditions.add(line);
	}

	public void addPostconditions(ArrayList postconditions) {
		this.postconditions.addAll(postconditions);
	}
	
	public void addPostcondition(String line) {
		postconditions.add(line);
	}
	
	public Iterator preconditionsIt(){
		return preconditions.iterator();
	}

	public Iterator postconditionsIt(){
		return postconditions.iterator();
	}

	public void expandEquivalences() {
		{
			HashSet<String> conds = doEquivalenceExapansion(preconditions);
			preconditions = new HashSet<String>(conds.size());
			preconditions.addAll(conds);
		}
		
		{
			HashSet<String> conds = doEquivalenceExapansion(postconditions);
			postconditions = new HashSet<String>(conds.size());
			postconditions.addAll(conds);
		}

	}

	public HashSet<String> doEquivalenceExapansion(Collection<String> preconditions) {
		
		
		
		HashSet<String> conds = new HashSet<String>();
		HashMap<String, String> mappings = getEquivalenceMappings(preconditions);
//		ArrayList<Entry<String,String>> mapArray = new ArrayList<Map.Entry<String,String>>(mappings.size());
		
		
		
		for ( String cond : preconditions ){
			conds.add( cond );
			cond = " "+cond+" ";
			
//			int size = mapArray.size();
			
//			Set<String> vars = ViolationsUtil.extractVariables(cond);
//			
//			
//			int end = size;
//			
//			for ( int begin = 0; begin < end; begin++ ){
//				
//			}
			for ( Entry<String,String> e : mappings.entrySet() ){
				String left = " "+e.getKey()+" ";
				String right = " "+e.getValue()+" ";
				
				conds.add( cond.replace(left, right).trim() );
			}
		}
		return conds;
	}

	public HashMap<String, String> getEquivalenceMappings(Collection<String> preconditions) {
		HashMap<String,String> mappings = new HashMap<String,String>();
		for ( String cond : preconditions ){
			String[] splittedCond = cond.split(" +");
			if ( splittedCond.length != 3 ){
				continue;
			}
			if ( ! splittedCond[1].equals("==") ){
				continue;
			}
			
			mappings.put( splittedCond[0], splittedCond[2] );
			
			if ( splittedCond[2].startsWith("\"") ){
				continue;
			}
			
			try {
				Double.parseDouble( splittedCond[2] );
				continue;
			} catch ( NumberFormatException  e ){ };
			
			mappings.put( splittedCond[2], splittedCond[0] );
		}
		return mappings;
	}
}
