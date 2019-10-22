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

import conf.SettingsException;

public interface MetaDataHandlerSettings {

	/**
	 * Initialize the meta data handler settings.
	 * This method receives a Property table with only its properties.
	 * The properties are set in the InvariantGeneratorConfigFile as SpecificMetaDataHandlerSettings.propertyName = value.
	 * This method receives the properties in the format propertyName = value, the prefix is removed by the framework
	 * @param properties
	 * @throws SettingsException 
	 */
	public void init(Properties properties) throws SettingsException;

	/**
	 * Returns the type of handler this settings refers to
	 * 
	 * @return
	 */
	public Class<? extends MetaDataHandler> getMetaDataHandlerType();
}
