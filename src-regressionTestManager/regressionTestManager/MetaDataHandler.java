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

import java.util.Vector;

import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import tools.TokenMetaData;
import dfmaker.core.Variable;

/**
 * Interface for Classes that handles metaData associated with Io and Interaction Traces
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public interface MetaDataHandler {

	/**
	 * Tell the handler that the given metadata is associated to the passed method call begin
	 * 
	 * @param methodName
	 * @param metaData
	 * @throws TcInfoHandlerException
	 */
	public TokenMetaData handleInteractionBegin(String methodName, String metaData) throws TcInfoHandlerException;

	/**
	 * Tell the handler that the given meta data is associated to the given normalized porgram point
	 * 
	 * @param exitPoint
	 * @param normalizedPoint
	 * @param metaData
	 * @throws TcInfoHandlerException
	 */
	public TokenMetaData handleIOExit(String exitPoint, Vector<Variable> normalizedPoint, String metaData) throws TcInfoHandlerException;

	/**
	 * Tell the handler that the given meta data is associated to the given normalized porgram point
	 * @param entryPoint
	 * @param normalizedPoint
	 * @param metaData
	 * @throws TcInfoHandlerException
	 */
	public TokenMetaData handleIOEnter(String entryPoint, Vector<Variable> normalizedPoint, String metaData) throws TcInfoHandlerException;

	/**
	 * This method is the optimized version of the handleIOEnter(String entryPoint, Vector<Variable> normalizedPoint, String metaData):
	 * before analyzing te given program point is should consider the hashcode of the program point o identify cases previously occurrd and save computation time 
	 * @param programPointName
	 * @param normalizedPoint
	 * @param metaData
	 * @param pphashcode
	 * @throws TcInfoHandlerException
	 */
	public TokenMetaData handleIOEnter(String programPointName, Vector<Variable> normalizedPoint, String metaData, Integer pphashcode) throws TcInfoHandlerException;

	/**
	 * This method is the optimized version of the handleIOExit(String exitPoint, Vector<Variable> normalizedPoint, String metaData):
	 * before analyzing te given program point is should consider the hashcode of the program point o identify cases previously occurrd and save computation time
	 * @param programPointName
	 * @param normalizedPoint
	 * @param metaData
	 * @param pphashcode
	 * @throws TcInfoHandlerException
	 */
	public TokenMetaData handleIOExit(String programPointName, Vector<Variable> normalizedPoint, String metaData, Integer pphashcode) throws TcInfoHandlerException;

	/**
	 * This method is used to force the saving of information on filesystem
	 * @throws TcInfoHandlerException 
	 *
	 */
	public void save() throws TcInfoHandlerException;

	/**
	 * This method indicate that the given metadata is associated to the passed method call end
	 * 
	 * @param methodName
	 * @param metaInfo
	 */
	public TokenMetaData handleInteractionEnd(String methodName, String metaInfo);

	/**
	 * Initialize the handler
	 * 
	 * @param settings
	 */
	public void init(MetaDataHandlerSettings settings);

	public TokenMetaData handleInteractionGenericProgramPoint(String methodName,
			String metaInfo);
}
