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
package tools.violationsAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class MultiHashMap<K, V> implements Cloneable {

	private HashMap<K, List<V>> elements = new HashMap<K, List<V>>();

	public void clear() {
		elements.clear();
	}

	public Object clone() {
		MultiHashMap<K, V> newMap = new MultiHashMap<K, V>();
		newMap.elements = (HashMap<K, List<V>>) elements.clone();
		return newMap;
	}

	public boolean containsKey(Object key) {
		return elements.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return elements.containsValue(value);
	}


	public boolean equals(Object o) {
		return elements.equals(o);
	}

	public List<V> get(Object key) {
		return elements.get(key);
	}

	public int hashCode() {
		return elements.hashCode();
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public Set<K> keySet() {
		return elements.keySet();
	}

	public List<V> remove(Object key) {
		return elements.remove(key);
	}

	public int size() {
		return elements.size();
	}

	public String toString() {
		return elements.toString();
	}

	public Set<Entry<K, List<V>>> entrySet() {
		return elements.entrySet();
	}
	
	
	public List<V> put(K key, Collection<V> value) {
		List<V> list = elements.get(key);
		
		if ( list == null ){
			list = new LinkedList<V>();
			elements.put(key, list);
		}
		
		list.addAll(value);
		
		return list;
	}

	public List<V> put(K key, V value) {
		List<V> list = elements.get(key);
		
		if ( list == null ){
			list = new LinkedList<V>();
			elements.put(key, list);
		}
		
		list.add(value);
		
		return list;
	}

	public List<V> values() {
		List<V> result = new ArrayList<V>();
		for ( List<V> els : elements.values() ){
			result.addAll(els);
		}
		return result;
	}



}
