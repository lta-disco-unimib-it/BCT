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
package executionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import conf.EnvironmentalSetter;
import conf.ExecutionContextRegistrySettings;

/**
 * This class manages the recording of state information for context registries in memory
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 * @param <T>
 */
public class ExecutionContextStateMaintainerFile<T extends ExecutionContextData> implements ExecutionContextStateMaintainer<T>{
	
	
	public static class ActionsContextState<T> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	
		private HashMap<Integer,T> actions = new HashMap<Integer, T>();
		private Set<Integer> currentActions = new HashSet<Integer>();
		
		
	}
	
	private File tmpFile;
	
	public static class Options {
		public static final String tmpFile = "tmpFile";
	}
	
	public synchronized void actionEnd(Integer actionId) throws ActionsRegistryException {
		//System.out.println("END "+actionId);
		ActionsContextState<T> state = loadState();
		if ( ! state.currentActions.contains(actionId) )
			throw new ActionsRegistryException("Action not registered:"+actionId);
		
		state.currentActions.remove(actionId);
		storeState(state);
	}

	private void storeState(ActionsContextState<T> state) throws ActionsRegistryException {
		
		
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(new FileOutputStream(tmpFile));
				
				oos.writeObject(state);
				
			} catch (FileNotFoundException e) {
				throw new ActionsRegistryException(e);
			} catch (IOException e) {
				throw new ActionsRegistryException(e);
			} finally {
				if ( oos != null ){
					try {
						oos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			
		
	}

	public synchronized Integer actionStart(T executionContextData)  throws ActionsRegistryException{
		ActionsContextState<T> state;
		
		state = loadState();
		

		Integer id = generateNewId(state);
		//System.out.println("START "+id);
		state.actions.put(id,executionContextData);
		state.currentActions.add(id);
		storeState(state);
		return id;

	}

	private Integer generateNewId(ActionsContextState<T> state) {
		return state.actions.size();
	}

	public synchronized Set<Integer> getCurrentActions() throws ActionsRegistryException {
		ActionsContextState<T> state = loadState();
		return state.currentActions;
	}

	private ActionsContextState<T> loadState() throws ActionsRegistryException  {
		
		if ( ! tmpFile.exists() ){
			System.out.println("! exists "+tmpFile.getAbsolutePath());
			return new ActionsContextState<T>();
		}
			
		ObjectInputStream  ois = null;
		
		try {
			
			ois = new ObjectInputStream ( new FileInputStream(tmpFile) );
			ActionsContextState<T> state = (ActionsContextState<T>) ois.readObject();
			
			return state;
			
		} catch (FileNotFoundException e) {
			throw new ActionsRegistryException(e);
		} catch (IOException e) {
			throw new ActionsRegistryException(e);
		} catch (ClassNotFoundException e) {
			throw new ActionsRegistryException(e);
		} finally {
			if ( ois != null ){
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void init(ExecutionContextRegistrySettings settings) {
		String tmpFileString = settings.getProperty(Options.tmpFile);

		tmpFile = new File ( tmpFileString );

		
	}

	public boolean isActionRunning(Integer actionId)
			throws ActionsRegistryException {
		ActionsContextState<T> state = loadState();
		return state.currentActions.contains(actionId);
	}

	public T getExecutionContextData(Integer actionId)
			throws ActionsRegistryException {
		ActionsContextState<T> state = loadState();
		return state.actions.get(actionId);
	}

}
