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
package traceReaders.metaData;

import conf.EnvironmentalSetter;
import regressionTestManager.MetaDataHandler;
import regressionTestManager.MetaDataHandlerSettings;

public class MetaDataHandlerFactory {

	private static MetaDataHandler instance = null;
	
	public static MetaDataHandler getMetaDataHandler() {
		if ( instance != null ){
			return instance;
		}
		
		MetaDataHandlerSettings ms = EnvironmentalSetter.getInvariantGeneratorSettings().getMetaDataHandlerSettings();
		if ( ms == null ){
			return null;
		}
		try {
			instance = ms.getMetaDataHandlerType().newInstance();
			instance.init(ms);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return instance;
	}
}
