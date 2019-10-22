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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import modelsFetchers.IoModelIterator;

import util.FileIndexAppend;

public class FShellModelsExporterListener implements ModelsExporterListener {

	private BufferedWriter bw;


	public FShellModelsExporterListener(File allModels) throws IOException {
		bw = new BufferedWriter(new FileWriter(allModels));


	}

	
	public void processingEnd(){
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Override
	public void addAssertionAtLineBegin(ModelInfo modelInfo,
			String model) {
		try {
			FShellModelsExporter.writeFShellExpressionForLine(modelInfo, bw, model );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void addAssertionAtFunctionEnter(ModelInfo modelInfo, String model) {
		try {
			FShellModelsExporter.writeFShellExpressionForFunctionEnter(modelInfo, bw, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void addAssertionAtFunctionExit(ModelInfo modelInfo, String model) {
		
	}





	

}
