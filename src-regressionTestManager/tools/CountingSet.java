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
package tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class CountingSet<T> implements Set<T> {

	private HashMap<T,Integer> elements = new HashMap<T, Integer>();
	
	public boolean add(T o) {
		Integer el = elements.get(o);
		if ( el == null ){
			el = 1;
		} else {
			el = el +1;
		}
		elements.put(o, el);
		return true;
	}

	public boolean addAll(Collection<? extends T> c) {
		for ( T el : c ){
			add(el);
		}
		return true;
	}

	public void clear() {
		elements.clear();
	}

	public boolean contains(Object o) {
		return elements.containsKey(o);
	}

	public boolean containsAll(Collection<?> c) {
		for ( Object o : c ){
			boolean res = elements.containsKey(o);
			if ( ! res ){
				return false;
			}
		}
		return true;
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public Iterator<T> iterator() {
		return elements.keySet().iterator();
	}

	public boolean remove(Object o) {
		Integer val = elements.get(o);
		if ( val == null ){
			return false;
		} else if (  val == 1){
			elements.remove(o);
		} else {
			val = val - 1;
			elements.put((T) o,val);
		}
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for ( Object o : c ){
			res |= remove(o);
		}
		return res;
	}

	public boolean retainAll(Collection<?> c) {
		boolean res = false;
		for ( T o : elements.keySet() ){
			if ( ! c.contains(o) ){
				res |= remove(o);
			}
		}
		return res;
	}

	public int size() {
		return elements.size();
	}

	public Object[] toArray() {
		return elements.keySet().toArray();
	}

	public <T> T[] toArray(T[] a) {
		return elements.keySet().toArray(a);
	}

	public Integer getOccurrecies(T o){
		Integer occ = elements.get(o);
		if ( occ == null ){
			return 0;
		}
		return occ;
	}
	
	public HashMap<T,Integer> getOccurrenciesMap(){
		HashMap<T, Integer> res = new HashMap<T, Integer>();
		res.putAll(elements);
		return res;
	}
}
