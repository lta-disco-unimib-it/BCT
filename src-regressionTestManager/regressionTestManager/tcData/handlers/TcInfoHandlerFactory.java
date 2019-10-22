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
package regressionTestManager.tcData.handlers;

import regressionTestManager.RegressionTestManagerHandlerSettings;
import conf.EnvironmentalSetter;
import conf.InvariantGeneratorSettings;

public class TcInfoHandlerFactory {
	private static TcInfoHandler _handler = null;
	
	public static TcInfoHandler getTcInfoHandler() {
		if ( _handler == null ){
			InvariantGeneratorSettings settings = EnvironmentalSetter.getInvariantGeneratorSettings();
			RegressionTestManagerHandlerSettings manager = (RegressionTestManagerHandlerSettings) settings.getMetaDataHandlerSettings();
			Class tcInfoHandler = manager.getTestCaseInfoHandlerType();
			try {
				_handler = (TcInfoHandler) tcInfoHandler.newInstance();
				_handler.init(manager);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return _handler;
	}
	
}
