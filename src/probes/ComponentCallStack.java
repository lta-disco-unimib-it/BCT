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
package probes;


import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * This class is used by probes and aspects to check (during logging and checking phases) if a component
 * is calling itself or is calling another element.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ComponentCallStack extends Stack<Class>{

	//private static final Map<Long,ComponentCallStack> stacks = Collections.synchronizedMap(  new TreeMap<Long,ComponentCallStack>() );  
	public static final ThreadLocal<ComponentCallStack> INSTANCE = new ThreadLocal<ComponentCallStack>(){

		@Override
		protected ComponentCallStack initialValue() {
			ComponentCallStack newInstance = new ComponentCallStack();
			newInstance.push(null);
			return newInstance;
		}
		
	};
	
	
	/**
	 * We need only one instance per Thread
	 *
	 */
	private ComponentCallStack(){
		
	}
	
	/**
	 * Return ComponentCallStack instance for current thread.
	 * Every thread has its own instance.
	 *  
	 * @return CallStack for current thread
	 * 
	 */
	public static ComponentCallStack getInstance(){
		return INSTANCE.get();
//		ComponentCallStack instance = stacks.get(Thread.currentThread().getId());
//		if ( instance == null ){
//			instance = new ComponentCallStack();
//			instance.push(null);
//			
//			stacks.put(Thread.currentThread().getId(), instance );
//		}
//		return instance;
	}
	
}
