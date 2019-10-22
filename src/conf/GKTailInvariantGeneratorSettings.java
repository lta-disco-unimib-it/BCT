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

import regressionTestManager.RegressionTestManagerHandlerSettings;

public class GKTailInvariantGeneratorSettings extends ConfigurationSettings {
	private File temporaryDir;
	
	private Class normalizedTraceHandlerType;
	private Class traceReaderType;
	private File distilledDir;

	private String fsaEngine;

	private String daikonConfig;

	private RegressionTestManagerHandlerSettings regressionTestManagerHandler = null; 
	
	public interface Options{
		public final String temporaryDir = "temporaryDir";
		public final String normalizedTraceHandlerType = "normalizedTraceHandler.type";
		//public final String fsaEngine = "FSAEngine";
		public final String daikonConfig = "daikonConfig";
	}
	
	public GKTailInvariantGeneratorSettings(Class type, Properties p) throws SettingsException {
		super(type, p);
		
		temporaryDir = new File(p.getProperty(Options.temporaryDir));
		temporaryDir.mkdirs();
		if ( ! temporaryDir.exists() )
			throw new SettingsException("Cannot set temporary dir "+temporaryDir);
		
		String typeN = (String) p.getProperty(Options.normalizedTraceHandlerType);
		try {
			normalizedTraceHandlerType = Class.forName( typeN );
		} catch (ClassNotFoundException e) {
			throw new SettingsException("Wrong normalizedTraceHandlerType : "+typeN);
		}
	
		//fsaEngine = (String) p.getProperty(Options.fsaEngine);
		
		daikonConfig = (String) p.getProperty(Options.daikonConfig);
		
		distilledDir = new File ( temporaryDir, "distilled");
		distilledDir.mkdirs();
		
		
	}

	public File getTemporaryDir() {
		return temporaryDir;
	}

	public Class getNormalizedTraceHandlerType() {
		return normalizedTraceHandlerType;
	}

	public File getDistilledDir() {
		return distilledDir;
	}

	public String getFSAEngine() {
		return fsaEngine;
	}

	public String getDaikonConfig() {
		return daikonConfig;
	}

	public RegressionTestManagerHandlerSettings getRegressionTestManagerHandler() {
		return regressionTestManagerHandler;
	}
}
