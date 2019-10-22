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
package tools.fshellExporter;

import java.util.Map;
import java.util.logging.Logger;

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;

public class ModelInfoBuilder {
	private Logger LOGGER = Logger.getLogger(ModelInfoBuilder.class.getCanonicalName());
	
	private Map<String, FunctionMonitoringData> monitoredFunctionsData;
	private Map<String, FunctionMonitoringData> monitoredFunctionsDataV1;
	private SourceLinesMapper sourceLinesMapper;
	private boolean usingUpdatedNames = true;
	
	public ModelInfoBuilder(Map<String, FunctionMonitoringData> monitoredFunctionsData, Map<String, FunctionMonitoringData> monitoredFunctionsDataV1, SourceLinesMapper sourceLinesMapper ){
		this.monitoredFunctionsData = monitoredFunctionsData;
		this.monitoredFunctionsDataV1 = monitoredFunctionsDataV1;
		this.sourceLinesMapper = sourceLinesMapper;
	}

	public int getLineEnter(FunctionMonitoringData functionData) {
		return ModelsUtil.getFunctionBeginLine(functionData);
	}

	public int getLineExit(FunctionMonitoringData functionData) {
		//in the case of exit we set return a list of lines 
		return functionData.getLastSourceLine();
	}
	
	
	public ModelInfo createModelInfo(String modelName, boolean isEnter) throws ModelInfoBuilderException{
		LOGGER.info("Creating model info for "+modelName);
		String bctModelName = modelName;
		
		modelName = modelName.trim();
		
		if ( modelName.equals("_GLOBAL__I_65535_0_initialize") ){
			throw new ModelInfoBuilderException("Skipping function: "+modelName);
		}
		
		int sep = modelName.indexOf(":");
		
		if ( sep < 0 ){
			
			FunctionMonitoringData functionV0 = monitoredFunctionsData.get(modelName);
			FunctionMonitoringData functionV1 = monitoredFunctionsDataV1.get(modelName);
			
			if ( functionV0 == null ){
				LOGGER.severe("Function missing in original version : "+functionV0 );
				return null;
			}
			
			if ( functionV1 == null ){
				LOGGER.severe("Function missing in updated version : "+functionV1 );
				return null;
			}
			
			int lineModified;
			int lineOriginal;
			if ( isEnter ){
				lineOriginal = getLineEnter(functionV0);
				lineModified = getLineEnter(functionV1);
			} else {
				lineOriginal = getLineExit(functionV0);
				lineModified = getLineExit(functionV1);
			}
			
			
			return new ModelInfo(bctModelName, functionV0, functionV1, modelName, lineOriginal, lineModified, true, isEnter);
		}
		
		
		
		
		String monitoredFunction = modelName.substring(0,sep);
		
		
		
		FunctionMonitoringData	functionV0 = monitoredFunctionsData.get(monitoredFunction);
		FunctionMonitoringData	functionV1 = monitoredFunctionsDataV1.get(monitoredFunction);
		
		
		if ( functionV0 == null ){
			LOGGER.severe("Function missing in original version : "+monitoredFunction );
			return null;
		}
		
		if ( functionV1 == null ){
			LOGGER.severe("Function missing in updated version : "+monitoredFunction );
			return null;
		}
		
		int lineModel = Integer.valueOf(modelName.substring(sep+1));
		int lineUpdated = -1;
		int lineOriginal = -1;
		if ( usingUpdatedNames ){
			try {
				lineOriginal = sourceLinesMapper.getCorrespondingLineInOriginalProject(functionV0.getSourceFileLocationClean(), lineModel);
			} catch (SourceMapperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lineUpdated  = lineModel;	
		} else {
			try {
				lineUpdated = sourceLinesMapper.getCorrespondingLineInModifiedProject(functionV1.getSourceFileLocationClean(), lineModel);
			} catch (SourceMapperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lineOriginal = lineModel;
		}
		
		
		return new ModelInfo(bctModelName,functionV0, functionV1, monitoredFunction, lineOriginal, lineUpdated, false, isEnter);
	}


}
