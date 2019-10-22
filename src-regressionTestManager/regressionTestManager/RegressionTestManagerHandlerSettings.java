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
package regressionTestManager;

import java.util.Properties;

import regressionTestManager.detectionMatrix.launchers.MatrixLauncher;
import regressionTestManager.detectionMatrix.launchers.MatrixLauncherBB;
import regressionTestManager.detectionMatrix.launchers.MatrixLauncherGreedy;
import conf.SettingsException;


public class RegressionTestManagerHandlerSettings implements MetaDataHandlerSettings {
	private Properties properties = new Properties();
	private MatrixLauncher linearLauncher;
	private Class testCaseInfoHandler;
	
	public interface Options {
		public final String testCaseInfoHandler = "testCaseInfoHandler.type";
		public final String linerAlgorithm = "linearAlgorithm";
	}
	
	public void init( Properties p ) throws SettingsException{
		
		properties.putAll(p);
		System.out.println(properties);
		String typeIH = (String) properties.getProperty(Options.testCaseInfoHandler);
		
		try {
			
			if ( typeIH != null )
				testCaseInfoHandler = Class.forName( typeIH );
		} catch (ClassNotFoundException e) {
			throw new SettingsException("Wrong testCaseInfoHandler");
		}
		
		
		String alg = properties.getProperty( Options.linerAlgorithm );
		if ( alg != null && alg.equals("branchbound") )
			linearLauncher = new MatrixLauncherBB();
		else
			linearLauncher = new MatrixLauncherGreedy();
		
		
		
	}
	
	public MatrixLauncher getLinearLauncher(){
		return linearLauncher;
	}

	public Class getTestCaseInfoHandlerType() {
		return testCaseInfoHandler;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Class<? extends MetaDataHandler> getMetaDataHandlerType() {
		return TestCasesMetaDataHandler.class;
	}
	
}
