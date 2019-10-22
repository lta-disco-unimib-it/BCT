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

import java.util.Vector;

import dfmaker.core.Variable;
import regressionTestManager.MetaDataHandler;
import regressionTestManager.MetaDataHandlerSettings;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import tools.TokenMetaData;

public class ExecutionMetaDataHandler implements MetaDataHandler{

	public TokenMetaData handleIOEnter(String entryPoint,
			Vector<Variable> normalizedPoint, String metaData)
			throws TcInfoHandlerException {
		return parseBasicMetaData(metaData);
	}

	public static TokenMetaData parseBasicMetaData(String metaDataLine) {
		return ExecutionTokenMetaData.loadFromString( metaDataLine );
	}

	public TokenMetaData handleIOEnter(String programPointName,
			Vector<Variable> normalizedPoint, String metaData,
			Integer pphashcode) throws TcInfoHandlerException {
		return parseBasicMetaData(metaData);
	}

	public TokenMetaData handleIOExit(String exitPoint,
			Vector<Variable> normalizedPoint, String metaData)
			throws TcInfoHandlerException {
		return parseBasicMetaData(metaData);
	}

	public TokenMetaData handleIOExit(String programPointName,
			Vector<Variable> normalizedPoint, String metaData,
			Integer pphashcode) throws TcInfoHandlerException {
		return parseBasicMetaData(metaData);
	}

	public TokenMetaData handleInteractionBegin(String methodName,
			String metaData) throws TcInfoHandlerException {
		return parseBasicMetaData(metaData);
	}

	public TokenMetaData handleInteractionEnd(String methodName, String metaInfo) {
		return parseBasicMetaData(metaInfo);
	}

	public void save() throws TcInfoHandlerException {
		
	}

	public void init(MetaDataHandlerSettings settings) {
		
	}

	@Override
	public TokenMetaData handleInteractionGenericProgramPoint(
			String methodName, String metaInfo) {
		// TODO Auto-generated method stub
		return parseBasicMetaData(metaInfo);
	}

}
