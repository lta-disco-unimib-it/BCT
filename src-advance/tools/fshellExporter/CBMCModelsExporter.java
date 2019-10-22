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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.NEW;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsViolations.BctModelViolation;


import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;

public class CBMCModelsExporter {

	private static String entrySeparator = "\t";

	

	public static String buildAssertionLineOriginal(ModelInfo modelInfo, String model) {
		return buildAssertionLine( modelInfo.getFileOriginal() , String.valueOf(modelInfo.getLineOriginal()) , model );
	}

	public static String buildAssertionLineModified(ModelInfo modelInfo, String model) {
		return buildAssertionLine( modelInfo.getFileModified() , String.valueOf(modelInfo.getLineModified()) , model );
	}
	
	public static String buildAssertionLine(String file, String position, String model) {
		return file + entrySeparator + position + entrySeparator + "assert( " + model + " );";
	}
	
	private class CBMCModelsExporterListener implements ModelsExporterListener {

		private BufferedWriter writerV0;
		private BufferedWriter writerV1;



		public CBMCModelsExporterListener(File allModelsV0, File allModelsV1) throws IOException {
			writerV0 = new BufferedWriter(new FileWriter(allModelsV0));
			writerV1 = new BufferedWriter(new FileWriter(allModelsV1));
		}


		@Override
		public void addAssertionAtLineBegin(ModelInfo modelInfo,
				String model) {
			if ( !exportLines ){
				return;
			}

			addAssertionAtLineBeginOriginal(writerV0, modelInfo, model);
			addAssertionAtLineBeginModified(writerV1, modelInfo, model);
			
			

		}

		private void addAssertionAtLineBeginOriginal(BufferedWriter writer,
				ModelInfo modelInfo,
				String model) {
			try {
				writer.append( buildAssertionLineOriginal(modelInfo, model) );
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		

		private void addAssertionAtLineBeginModified(BufferedWriter writer,
				ModelInfo modelInfo,
				String model) {
			try {
				writer.append( buildAssertionLineModified(modelInfo, model) );
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		@Override
		public void addAssertionAtFunctionEnter(ModelInfo modelInfo,
				String model) {
			if ( ! exportEntryPoints ){
				return;
			}

			addAssertionAtFunctionEnterOriginal(writerV0, modelInfo, model);
			addAssertionAtFunctionEnterModified(writerV1, modelInfo, model);
			
			

		}

		private void addAssertionAtFunctionEnterOriginal(
				BufferedWriter writer, ModelInfo modelInfo, String model) {

			addAssertionAtLineBeginOriginal(writer, modelInfo, model);

		}

		private void addAssertionAtFunctionEnterModified(
				BufferedWriter writer, ModelInfo modelInfo, String model) {

			addAssertionAtLineBeginModified(writer, modelInfo, model);

		}

		@Override
		public void addAssertionAtFunctionExit(ModelInfo modelInfo, String model) {

			if ( ! exportExitPoints ){
				return;
			}

			addAssertionAtFunctionExitOriginal(writerV0, modelInfo, model);

			addAssertionAtFunctionExitModified(writerV1, modelInfo, model);
			
			
		}


		private void addAssertionAtFunctionExitOriginal(BufferedWriter writer,
				ModelInfo modelInfo, String model) {
			try {
				writer.append( modelInfo.getFileOriginal() + entrySeparator + modelInfo.functionName + entrySeparator + "assert( " + model + " );" );
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		private void addAssertionAtFunctionExitModified(BufferedWriter writer,
				ModelInfo modelInfo, String model) {
			try {
				writer.append( modelInfo.getFileModified() + entrySeparator + modelInfo.functionName + entrySeparator + "assert( " + model + " );" );
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}


		@Override
		public void processingEnd() {
			// TODO Auto-generated method stub
			try {
				writerV0.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				writerV1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private Map<String, FunctionMonitoringData> monitoredFunctionsDataV1;
	private Map<String, FunctionMonitoringData> monitoredFunctionsDataV0;
	private SourceLinesMapper sourceLinesMapper;

	private boolean exportEntryPoints = true;
	private boolean exportExitPoints = false;
	private boolean modelNamesMatchV0 = true;

	public boolean isModelNamesMatchV0() {
		return modelNamesMatchV0;
	}


	public void setModelNamesMatchV0(boolean modelNamesMatchV0) {
		this.modelNamesMatchV0 = modelNamesMatchV0;
	}


	public boolean isExportExitPoints() {
		return exportExitPoints;
	}

	public void setExportExitPoints(boolean exportExitPoints) {
		this.exportExitPoints = exportExitPoints;
	}

	public boolean isExportEntryPoints() {
		return exportEntryPoints;
	}

	public void setExportEntryPoints(boolean exportEntryPoints) {
		this.exportEntryPoints = exportEntryPoints;
	}

	public boolean isExportLines() {
		return exportLines;
	}

	public void setExportLines(boolean exportLines) {
		this.exportLines = exportLines;
	}

	private boolean exportLines = true;
	private List<ModelFilter> additionalVariablesFilters = new ArrayList<ModelFilter>();
	private boolean unitVerification = false;
	private boolean filterRedundants;
	private ModelsFetcher mf;

	//	public ModelFilter getAdditionalVariablesFilter() {
	//		return additionalVariablesFilter;
	//	}

	public void addAdditionalVariablesFilter(ModelFilter additionalVariablesFilter) {
		if ( additionalVariablesFilter == null ){
			return;
		}
		this.additionalVariablesFilters.add( additionalVariablesFilter );
	}

	public CBMCModelsExporter ( 
			SourceLinesMapper sourceLinesMapper, ModelsFetcher mf ){
		this.monitoredFunctionsDataV0 = sourceLinesMapper.getOriginalSoftwareFunctions();
		this.monitoredFunctionsDataV1 = sourceLinesMapper.getModifiedSoftwareFunctions();
		this.sourceLinesMapper = sourceLinesMapper;
		this.mf = mf;
	}

	public void exportModels( File allModelsV0, File allModelsV1 ) throws IOException, ModelsFetcherException{
		CBMCModelsExporterListener exporterListener = new CBMCModelsExporterListener( allModelsV0, allModelsV1 ); 

		ModelsExporter exporter = new ModelsExporter(monitoredFunctionsDataV0,monitoredFunctionsDataV1,sourceLinesMapper, mf );
		exporter.setUnitVerification( unitVerification );
		exporter.setAdditionalVarFilters(additionalVariablesFilters);
		exporter.setFilterRedundants(filterRedundants);
		exporter.processModels(exporterListener);

	}

	public boolean isFilterRedundants() {
		return filterRedundants;
	}

	public void setFilterRedundants(boolean filterRedundants) {
		this.filterRedundants = filterRedundants;
	}

	public void setUnitVerification(boolean b) {
		unitVerification = b;
	}



}
