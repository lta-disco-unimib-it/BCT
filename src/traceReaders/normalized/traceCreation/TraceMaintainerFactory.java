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
package traceReaders.normalized.traceCreation;

import traceReaders.normalized.NormalizedInteractionTraceHandler;
import util.componentsDeclaration.ComponentsDefinitionException;
import conf.EnvironmentalSetter;

public class TraceMaintainerFactory {
	private static TraceMaintainer traceMaintainer;
	
	public static TraceMaintainer getTraceMaintainer(NormalizedInteractionTraceHandler traceHandler) {
		
		
		if ( traceMaintainer == null ){
			Class<? extends TraceMaintainer> type = EnvironmentalSetter.getInvariantGeneratorSettings().getTraceMaintainerType();
			try {
				traceMaintainer = type.newInstance();
				traceMaintainer.init(EnvironmentalSetter.getInvariantGeneratorSettings(),traceHandler);
				
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ComponentsDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return traceMaintainer;
	}

}
