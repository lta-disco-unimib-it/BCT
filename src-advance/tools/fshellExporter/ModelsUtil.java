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

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;

public class ModelsUtil {

	public static ModelInfo getModelInfoForOriginalVersion(ModelInfo model,
			SourceLinesMapper sourceMapper) throws SourceMapperException {
		return model;
//		int line;
//		String function;
//		
//		if ( model.isEntryExitPoint() ){
//			function = sourceMapper.getCorrespondingFunctionOriginal((model.getFileModified(), model.functionName );
//			if ( model.isEnter() ){
//				line = getFunctionBeginLine( sourceMapper.getCorrespondingFunctionData(function) );
//			} else {
//				throw new SourceMapperException("More than one 'return' instruction could be expected, invoke getModelInfoForReturnInUpdatedVersion");
////				line = sourceMapper.getCorrespondingFunctionData(function).getLastSourceLine();
//			}
//		} else {
//			line = sourceMapper.getCorrespondingLineInOriginalProject(model.getFileModified(), model.getLineModified());
//			function = sourceMapper.getCorrespondingFunctionOriginal(model.getFileModified(), model.functionName, line );
//			
//		}
//		
//		
//		return new ModelInfo(model.getBctModelName(), function, model.getFileOriginal(), model.getLineOriginal(), model.getLineModified(), model.isEntryExitPoint(), model.isEnter() );
	}
	
	
	public static ModelInfo getModelInfoForUpdatedVersion(ModelInfo model,
			SourceLinesMapper sourceMapper) throws SourceMapperException {
		return model;
//		int line;
//		String function;
//		
//		if ( model.isEntryExitPoint() ){
//			function = sourceMapper.getCorrespondingFunction((model.getFileOriginal(), model.functionName );
//			if ( model.isEnter() ){
//				line = getFunctionBeginLine( sourceMapper.getCorrespondingFunctionData(function) );
//			} else {
//				throw new SourceMapperException("More than one 'return' instruction could be expected, invoke getModelInfoForReturnInUpdatedVersion");
////				line = sourceMapper.getCorrespondingFunctionData(function).getLastSourceLine();
//			}
//		} else {
//			function = sourceMapper.getCorrespondingFunction(model.getFileOriginal(), model.functionName, model.getLineOriginal() );
//			line = sourceMapper.getCorrespondingLineInModifiedProject(model.getFileOriginal(), model.getLineOriginal());
//		}
//		
//		
//		return new ModelInfo(model.getBctModelName(), function, model.getLineOriginal(), model.getLineModified(), model.isEntryExitPoint(), model.isEnter() );
	}
	
	public static int getFunctionBeginLine( FunctionMonitoringData func ){
		return func.getFirstSourceLine()+1;
	}

}
