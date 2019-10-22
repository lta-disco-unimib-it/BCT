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
package flattener.handlers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import flattener.core.Handler;


public class RawHandler implements Handler {
	protected HashMap<String,String> nodes = new HashMap<String,String>();
	//private HashMap	 references = new HashMap();
	private String root;
	
	
	public RawHandler(String rootName) {
		root = rootName;
	}

	/**
	 * 
	 * @param name
	 * @param object
	 */
	public void addNode( String name, Object object ){
		String value = getValue ( object );
		nodes.put(root+name, value);
	}
	
	/**
	 * Add a parsed value for a node
	 * 
	 * @param name
	 * @param parsedValue
	 */
	public void addNodeValue( String name, String parsedValue ){
		nodes.put(root+name, parsedValue);
	}

	private String getValue(Object object) {
		if ( object == null )
			return "null";
		
		if ( object.getClass() == Boolean.class ){
			if ( (Boolean)object )
				return "1";
			return"0";
		}
		
		if ( object.getClass() == String.class ){
			String s = (String) object;
			if ( s.contains("\n") ){
				s = s.replace("\n", "\\n");
			}
			if ( s.contains("\r") ){
				s = s.replace("\r", "\\r");
			}
			return "\""+s+"\"";
		}
		
		if ( object.getClass() == Character.class )
			return "\""+object+"\"";
		
		return object.toString();
	}

	public void addNodeRef(String referencingName, String referencedName) {
		nodes.put(root+referencingName, "@"+root+referencedName);
	}

	public void add(Object object) {
		throw new NotImplementedException();
	}

	public Object getData() {
		return this;
	}

	public void goDown(Method method) {
		throw new NotImplementedException();
	}

	public void goDown(Field field) {
		throw new NotImplementedException();
	}

	public void goDownArray() {
		throw new NotImplementedException();
	}

	public void goDownArray(int i) {
		throw new NotImplementedException();
	}

	public void goUp() {
		throw new NotImplementedException();
	}

	public void goUpArray() {
		throw new NotImplementedException();
	}

	public Iterator<String> getNodeNamesIt() {
		return nodes.keySet().iterator();
	}

	public String getNodeValue( String nodeName ){
		return nodes.get(nodeName);
	}

	public String getRootName() {
		return root;
	}
	
	public int getNodesNumber(){
		return nodes.size();
	}

	public void addNotNull(String inspectorFullName) {
		nodes.put(root+inspectorFullName, "!NULL");
	}

	public Set<Entry<String, String>> nodesEntrySet() {
		return nodes.entrySet();
	}

	public void addArrayNode(String inspectorFullName, Object array) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		int len = Array.getLength(array);
		
		for ( int i = 0; i < len; i++ ){
			if ( i > 0 ) {
				sb.append(" ");
			}
			sb.append(getValue(Array.get(array, i)));
		}
		
		sb.append("]");
		nodes.put(root+inspectorFullName+"[..]", sb.toString());
	}
	
}
