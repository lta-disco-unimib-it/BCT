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
package check.ioInvariantParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import sjm.utensil.PubliclyCloneable;

public class Target implements PubliclyCloneable {

	private ArrayList queue = new ArrayList();

	//public static HashMap memory = new HashMap();

	private Object[] parameters = new Object[0];
	private Map<String,Object> localVariables;
	private Object returnValue;

	private boolean blocked;

	private String expression;

	public Target(Object[] parameters,Object returnValue, Map<String,Object> localVariables, String expression) {
		this.parameters = parameters;
		this.returnValue = returnValue;
		this.localVariables = localVariables;
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	public void setLocalVariables( Map<String,Object> localVariables ){
		this.localVariables = localVariables;
	}


	public Object[] getParameters() {
		return parameters;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public Object top() {
		return queue.get(queue.size() - 1);
	}
	

	public Object pop()  {
//		StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
//		System.out.println("Caller "+stack.getClassName()+" "+stack.getLineNumber());

		//  System.out.println("Queue :\n"+queue.toString());
//		System.out.println("pop queue : "+queue.toString());
		//System.out.flush();
		Object o = queue.get(queue.size() - 1);
		queue.remove(queue.size() - 1);
		return o;
	}


	public int queueSize(){
		return queue.size();
	}

	public void put(Object key,Object value) {
		//  System.out.println("#Target.put: key: "+key+" : "+value+" a "+memory.keySet() );
		//memory.put(key,value);

		IOMemoryRegistry.getInstance().getCurrentMethodsMap().put(key,value);
	}

	/**
	 * Returns the original value of an inspector.
	 * This method is called when evaluating an orig(..) operator during exit from a method.
	 * 
	 * @param key
	 * @return
	 */
	public Object getOrig(Object key) {
		//System.out.println("#Target.get: key: "+key+" "+IOMemoryRegistry.getInstance().getCurrentMethodsMap().get(key) );
		//System.out.println(memory);
		//return memory.get(key);
		return IOMemoryRegistry.getInstance().getCurrentMethodsMap().get(key);
	}

	public Object remove(Object key) {
		//System.out.println("#Target.remove: key: "+key );
		//System.out.println(IOMemoryRegistry.getInstance().getCurrentMethodsMap().containsKey(key) );
		//return memory.remove(key);
		return IOMemoryRegistry.getInstance().getCurrentMethodsMap().remove(key);
	}



	public void push(Object o) {
		//FIXME: throw an exception?

		if ( ClassTypeChecker.getInstance().isIgnoredType(o) ){
			block();

			return;
			//throw new RuntimeException("ClassToIgnore");
		}
//		System.out.println("PUSHED "+o);
		queue.add(o);
//		System.out.println("push queue : "+queue.toString());
		//System.out.flush();
	}

	public void pushForce(Object o) {
		queue.add(o);
	}

	public Object clone() {
		try {
			Target t = (Target)super.clone();
			t.queue = (ArrayList)queue.clone();
			t.blocked = blocked;
			t.localVariables = localVariables;//this does not change, we do not need to clone it
			return t;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public boolean contains( Object key ){
		return IOMemoryRegistry.getInstance().getCurrentMethodsMap().containsKey(key);
	}


	public String toString(){
		String res = super.toString();
		res += "queue: "+queue.toString();
		return res;
	}

	/**
	 * Block target queue, from this moment queue will be void an no push can be made.
	 * It is used when a NonExistentMethod is called.
	 *
	 */
	public void block(){
		//System.out.println("BLOCK");
		blocked = true;
	}


	public boolean isBlocked(){
		//System.out.println("BLOCKED "+blocked);
		return blocked;
	}

	public Set getOrigKeys(){
		return IOMemoryRegistry.getInstance().getCurrentMethodsMap().keySet();
	}

	public Object getLocalVariable(String variableName) throws NoSuchElementException {
		if ( localVariables == null ){
			throw new NoSuchElementException("Variable "+variableName+" is absent ");
		}

		if ( ! localVariables.containsKey(variableName) ){

			List<String> fields = findChildrenOfThisVariable( variableName );

			if ( fields.size() > 0 ){
				return new LocalVariablesWrapper( variableName, fields, this);
			}

			throw new NoSuchElementException("Variable "+variableName+" is absent ");
		}
		return localVariables.get(variableName);
	}

	private List<String> findChildrenOfThisVariable(String variableName) {
		List<String> childVariables = new ArrayList<String>();
		for ( String key : localVariables.keySet() ){
			if ( key.startsWith(variableName) ){
				childVariables.add(variableName);
			}
		}
		return childVariables;
	}


}
