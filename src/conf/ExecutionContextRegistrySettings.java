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
package conf;

import java.io.File;
import java.util.Properties;

import executionContext.ExecutionContextStateMaintainer;

public class ExecutionContextRegistrySettings extends ConfigurationSettings {

	public static class Options {
		public static final String stateRecorderType = "stateRecorderType";
		
	}

	private Class<?> recorderClass;
	
	
	public ExecutionContextRegistrySettings(Class type, Properties p) {
		super(type, p);
		
		String recorderType = p.getProperty(Options.stateRecorderType);
		
		if ( recorderType != null ){
			try {
				recorderClass = Class.forName(recorderType);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	/**
	 * Return a new instance of the  configured recorder
	 * 
	 * @return
	 */
	public ExecutionContextStateMaintainer newMaintainerRecorderInstance() {
		try {
			ExecutionContextStateMaintainer instance = (ExecutionContextStateMaintainer) recorderClass.newInstance();
			instance.init(this);
			return instance;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	

}
