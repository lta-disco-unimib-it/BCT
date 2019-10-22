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
package modelsFetchers;

import conf.EnvironmentalSetter;
import conf.ModelsFetcherSettings;

/**
 * The aim of this class is to provide the ModelsFetchers indicated by the EnvironmentalSetter
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ModelsFetcherFactoy {
	
	public final static ModelsFetcher modelsFetcherInstance = createModelsFetcher();
	
	/**
	 * Returns the model fetcher indicated by the EnvironmentalSetter.
	 * The fetcher type can be indicated in the ModelsFetcher configuration file specified in the "modelsFetcherSettings" field
	 * in BCT.properties configuration file.
	 * 
	 * The modelFetcher instance is savede in a private attribute, so after the first instance is retrieved any change to
	 * EnvironmentalSetter's ModelsFetcherSettings will have no effect.
	 * 
	 * This method is synchronized because we want only a models fetcher instance for all threads
	 * 
	 * @return the model fetcher indicated by EnvironmentalSetter
	 * 
	 */
	protected static ModelsFetcher createModelsFetcher(){

		ModelsFetcherSettings mfs = EnvironmentalSetter.getModelsFetcherSettings();
		ModelsFetcher _fetcher = null;
		try {
			_fetcher = (ModelsFetcher)mfs.getType().newInstance();
			_fetcher.init( mfs );
		} catch ( Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 

		return _fetcher;
	}
}