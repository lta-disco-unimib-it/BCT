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
package regressionTestManager.tcData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import regressionTestManager.VariableInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;

public class ProgramPointInfo implements TcInfoEntity{
	private String id;
	private ArrayList variablesIds;
	private HashMap values;
	private TcInfoHandler mapper;
	
	public ProgramPointInfo ( TcInfoHandler mapper, String id, ArrayList variablesIds ){
		this.id = id;
		this.variablesIds = variablesIds;
		this.mapper = mapper;
	}
	
	public String getId() {
		return id;
	}
	
	public Iterator getVariablesIdsIt(){
		return variablesIds.iterator();
	}

	public String getVariableValue(String programPoint, String name) {
		//System.out.println("GET "+programPoint+" "+name+" FROM "+values+" "+valueVarKey(programPoint,name));
		if ( values == null ){
			fillValues();
		}
		return (String) values.get( valueVarKey(programPoint,name));
	}

	private void fillValues() {
		//System.out.println("FILL");
		values = new HashMap();
		Iterator it = variablesIds.iterator();
		while ( it.hasNext() ){
			VariableInfo var = mapper.getVariableFromId((String)it.next());
			String value = var.getValue();
			//System.out.println(valueVarKey(var.getProgramPoint(),var.getName())+"="+value);
			values.put(valueVarKey(var.getProgramPoint(),var.getName()),value);
		}
	}

	private String valueVarKey( String programPoint, String name ){
		return programPoint+"."+name;
	}

	public void addVariable(VariableInfo varInfo) {
		variablesIds.add(varInfo.getId());
	}
	
	public String toString(){
		String msg="{ "+id;
		fillValues();
		Iterator it = values.keySet().iterator();
		msg += "[";
		while ( it.hasNext() ){
			String name = (String)it.next();
			msg+= name+ " "+values.get(name)+"; ";
		}
		msg += "] }";
		return msg;
	}
	
	public boolean equals( Object o ){
		if ( o  == this )
			return true;
		if ( ! ( o instanceof ProgramPointInfo  ) )
			return false;
		ProgramPointInfo rhs = (ProgramPointInfo) o;
		
		//mapper can be different
		
		//values are loaded only for optimizations, but they depend on variableIds, so check only them
		
		if ( ! id.equals( rhs.id ))
			return false;
		
		if ( ! variablesIds.equals(rhs.variablesIds))
			return false;
		return true;
	}

	/**
	 * Returns values for an array.
	 * Array name must end with []
	 * 
	 * @param programPoint
	 * @param name
	 * @return 
	 */
	public Collection<String> getArrayValues(String programPoint, String name) {
		if ( values == null ){
			fillValues();
		}
		String arrayName = valueVarKey(programPoint, name.substring(0,name.length()-2));
		Iterator it = values.keySet().iterator();
		TreeMap<Double,String> map = new TreeMap<Double,String>();
		int len = arrayName.length();
		while ( it.hasNext() ){
			String varName = (String) it.next();
			
			if (  name.startsWith( arrayName ) ){
				String arrayIndex = varName.substring(len+1,name.length()-1);
				
				Double index = Double.valueOf(arrayIndex);
				if ( index != null ){
					String value = (String) values.get(varName);
					map.put( index, value);
				}
			}
			
		}
		return map.values();
	}
	
}
