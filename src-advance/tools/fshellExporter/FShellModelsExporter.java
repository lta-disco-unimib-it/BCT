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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import conf.EnvironmentalSetter;
import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.FunctionMonitoringDataSerializer;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;

import modelsFetchers.IoModelIterator;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import tools.fshellExporter.FShellExecutor.FShellResult;
import util.FileIndexAppend;
import util.FileUtil;



@Deprecated
public class FShellModelsExporter  {

	private static final String _FSHELL_ASSERTION_END = ")}";
	private static final String _FSHELL_ASSERTION_START = ".{ !(";
	private static final String _FSHELL_COVER_FILE_END = "')";
	private static final String _FSHELL_LINE_END = ")";
	private static final String _FSHELL_LINE = "@line(";
	private static final String _FSHELL_ENTRY = "@entry(";
	private static final String _FSHELL_COVER_FILE = "cover @file('";
	public static final String VALIDATED_MODELS_FOLDER = "FSHELL";
	private File bctHome;
	private Map<String, FunctionMonitoringData> monitoredFunctionsData;

	public class ModelsData extends ModelInfo {
		
		private String assertion;
		
		public String getAssertion() {
			return assertion;
		}

		public ModelsData(String file, int line, String assertion, boolean entryExitPoint, boolean isEnter) {
			super("",file,line,line, entryExitPoint, isEnter);
			this.assertion = assertion;
		}
		
	}

	public FShellModelsExporter(File bctHomeFile) {
		bctHome = bctHomeFile;
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		mf = ModelsFetcherFactoy.modelsFetcherInstance;
		String functionDataFilePath = bctHome.getAbsolutePath()+"/conf/files/scripts/monitoredFunctions.original.ser";
		File functionDataFile = new File( functionDataFilePath );
		if ( functionDataFile.exists() ){
			try {
				monitoredFunctionsData = FunctionMonitoringDataSerializer.load(functionDataFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}





	/**
	 * @param args
	 * @throws ModelsFetcherException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ModelsFetcherException, IOException {


		String bctHome = args[0];
		
		
		
		File bctHomeFile = new File( bctHome );
		File dest = new File ( bctHomeFile, VALIDATED_MODELS_FOLDER );
		
		System.out.println("Dest folder is: "+dest.getAbsolutePath());

		// TODO Auto-generated method stub
		FShellModelsExporter exporter = new FShellModelsExporter(bctHomeFile);

		
	
		
		exporter.exportModels( dest );



	}


	private boolean skipFunctionModels = true;
	private String lastFunction;
	private ModelInfo pendingModels;
	private Map<String, FunctionMonitoringData> monitoredFunctionsDataV1;
	private ModelsFetcher mf;

	public static File getValidatedModelsDir( File bctHome ){
		return new File ( bctHome, VALIDATED_MODELS_FOLDER );
	}
	
	public static File getValidationResultFile( File modelsDir ){
		return new File ( modelsDir, "validationResult.ser" );
	}
	
	public static File getValidModelsFile( File modelsDir ){
		return new File ( modelsDir, "validModels.txt" );
	}
	
	public static File getAllModelsFile( File modelsDir ){
		return new File ( modelsDir, "allModels.txt" );
	}
	
	public void exportInvalidModelsV1(FShellResult result,
			File destDir) throws IOException {
		File validModelsFile = getInvalidatedModelsFile(destDir);
		
		BufferedWriter w = new BufferedWriter(new FileWriter(validModelsFile));
		
		
		List<String> lines = result.getProperties();
		
		for ( Integer hoding : result.getFalseProperties() ){
			if ( hoding == lines.size()  ){
				continue;
			}
			w.write(lines.get(hoding));
			w.newLine();
		}
		
		w.close();
	}
	
	public static File getInvalidatedModelsFile(File modelsDir) {
		return new File ( modelsDir, "invalidatedModels.txt" );
	}
	
	public static File getValidModelsModifiedSoftware(File modelsDir) {
		return new File ( modelsDir, "validModels.modified.txt" );
	}

	public File exportValidModelsForUpgradedVersion ( File modelsDir, SourceLinesMapper sourceMapper ) throws IOException{
		File validModelsOriginal = getValidModelsFile(modelsDir);
		File validModelsModified = getValidModelsModifiedSoftware(modelsDir);
		
		
		List<ModelsData> modelsData = extractModelsData( validModelsOriginal, sourceMapper );
		
		exportForModifiedVersion( modelsData, validModelsModified, sourceMapper );
		
		return validModelsModified;
	}

	private void exportForModifiedVersion(List<ModelsData> modelsData,
			File validModelsModified, SourceLinesMapper sourceMapper) throws IOException {
		
		BufferedWriter w = new BufferedWriter(new FileWriter(validModelsModified) );
		
		
		for ( ModelsData model : modelsData){
			try {
				ModelInfo newData = ModelsUtil.getModelInfoForUpdatedVersion( model, sourceMapper ); 

				writeFShellExpressionForLine(newData, w, model.assertion );
			} catch ( Exception e){
				e.printStackTrace();
			}
		}
		
		w.close();
	}


	private List<ModelsData> extractModelsData(File validModelsOriginal, SourceLinesMapper sourceMapper) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(validModelsOriginal));
		List<ModelsData> modelsData = new ArrayList<ModelsData>();
		
		String line;
		try {
		while ( (line = r.readLine() ) != null ){
			if ( isLineInvariant( line ) ){
				ModelsData data = parseLineInvariant( line );
				modelsData.add(data);
			} else if ( isEntryInvariant( line ) ){
				ModelsData data;
				try {
					data = parseEntryInvariant( line, sourceMapper );
					modelsData.add(data);
				} catch (SourceMapperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		} finally {
			r.close();
		}
		
		return modelsData;
	}


	private boolean isLineInvariant(String line) {
		int coverFile = line.indexOf(_FSHELL_COVER_FILE);
		
		int lineTag = line.indexOf(_FSHELL_LINE);
		
		return coverFile >= 0 && lineTag > 0 ;
	}

	private boolean isEntryInvariant(String line) {
		int coverFile = line.indexOf(_FSHELL_COVER_FILE);
		
		int lineTag = line.indexOf(_FSHELL_ENTRY);
		
		return coverFile >= 0 && lineTag > 0 ;
	}

	public ModelsData parseLineInvariant(String line) {
		
		int fileStart = line.indexOf(_FSHELL_COVER_FILE) + _FSHELL_COVER_FILE.length();
		line = line.substring(fileStart);
		int fileEnd = line.indexOf(_FSHELL_COVER_FILE_END);
		String file = line.substring(0, fileEnd );
		
		
		line = line.substring(fileEnd);
		
		
		int assertionStart = line.indexOf(_FSHELL_ASSERTION_START)+_FSHELL_ASSERTION_START.length();
		line = line.substring(assertionStart);
		int assertionEnd = line.indexOf(_FSHELL_ASSERTION_END);
		String assertion = line.substring(0, assertionEnd );
		
		line = line.substring(assertionEnd);
		
		int lineNoStart = line.indexOf(_FSHELL_LINE) + _FSHELL_LINE.length();
		line = line.substring(lineNoStart);
		int lineNoEnd = line.indexOf(_FSHELL_LINE_END);
		
		String lineNo = line.substring(0, lineNoEnd);
		
		return new ModelsData(file, Integer.valueOf(lineNo), assertion, false, true);
	}
	
	
	
	public ModelsData parseEntryInvariant(String line, SourceLinesMapper sourceMapper) throws SourceMapperException {
		
		int fileStart = line.indexOf(_FSHELL_COVER_FILE) + _FSHELL_COVER_FILE.length();
		line = line.substring(fileStart);
		int fileEnd = line.indexOf(_FSHELL_COVER_FILE_END);
		String file = line.substring(0, fileEnd );
		
		
		line = line.substring(fileEnd);
		
		
		int assertionStart = line.indexOf(_FSHELL_ASSERTION_START)+_FSHELL_ASSERTION_START.length();
		line = line.substring(assertionStart);
		int assertionEnd = line.indexOf(_FSHELL_ASSERTION_END);
		String assertion = line.substring(0, assertionEnd );
		
		line = line.substring(assertionEnd);
		
		int lineNoStart = line.indexOf(_FSHELL_ENTRY) + _FSHELL_ENTRY.length();
		line = line.substring(lineNoStart);
		int lineNoEnd = line.indexOf(_FSHELL_LINE_END);
		
		String functionName = line.substring(0, lineNoEnd);
		String functionNew = sourceMapper.getCorrespondingFunction(file, functionName);
		FunctionMonitoringData funcData = sourceMapper.getCorrespondingFunctionData(functionNew);
		
		int functionBegin = ModelsUtil.getFunctionBeginLine(funcData);
		
		return new ModelsData(file, functionBegin, assertion, false, true);
	}


	public void exportValidModels ( FShellResult result, File destDir ) throws IOException{
		File validModelsFile = getValidModelsFile(destDir);
		
		BufferedWriter w = new BufferedWriter(new FileWriter(validModelsFile));
		
		
		List<String> lines = result.getProperties();
		
		for ( Integer hoding : result.getTrueProperties() ){
			if ( hoding == lines.size()  ){
				continue;
			}
			w.write(lines.get(hoding));
			w.newLine();
		}
		
		w.close();
		
		File validationResultFile = getValidationResultFile(destDir);
		FShellResult.store(result, validationResultFile);
		
	}
	
	public File exportModels(File dest) throws ModelsFetcherException, IOException {

		dest.mkdirs();
		
		File allModels = getAllModelsFile(dest);
		System.out.println("All models file is: "+allModels.getAbsolutePath());
		
//		new FileIndexAppend( new File( dest, "ioModelsEnter.idx" ), ".io.enter" );
//		new FileIndexAppend( new File( dest, "ioModelsExit.idx" ), ".io.enter" );
		
		FShellModelsExporterListener listener = new FShellModelsExporterListener( allModels );
		ModelsExporter exporter = new ModelsExporter(monitoredFunctionsData, monitoredFunctionsDataV1, null, mf);
		exporter.processModels(listener);
		
		
		
		return allModels;
	}




	public static void writeFShellExpressionForFunctionEnter(ModelInfo modelInfo,
			BufferedWriter bw, String model) throws IOException {
		String lineInfo = _FSHELL_ENTRY +
				modelInfo.functionName +
				_FSHELL_LINE_END;
		
		writeFShellExpression(modelInfo.getFileOriginal(), bw, model, lineInfo);
	}

	
	
	



	public static void writeFShellExpressionForLine(ModelInfo modelInfo,
			BufferedWriter bw, String model) throws IOException {
		String lineInfo = _FSHELL_LINE +
				modelInfo.getLineOriginal() +
				_FSHELL_LINE_END;
		
		writeFShellExpression(modelInfo.getFileOriginal(), bw, model, lineInfo);
		
	}


	private static void writeFShellExpression(String file,
			BufferedWriter bw, String booleanProperty, String lineInfo) throws IOException {
		
		String assertion = booleanProperty;
		if ( assertion != null ){

			//(@file(bla.c)&@line(7)).{!(myVariable > 0)}

//			1. @line(42)
//			2. invariant: before: x>5
//
//			cover NOT(@line(42)).{!(x>5)}.@line(42)
//			  passing ...assumptions...
//
//
//			41:    x=1;
//			42:    x=6; x=3;
//
//
//			{x>5}.@42
			
			String message = 
					_FSHELL_COVER_FILE + file + _FSHELL_COVER_FILE_END;
			if ( ! lineInfo.startsWith(_FSHELL_ENTRY)){
				message += "&NOT(" +lineInfo + _FSHELL_LINE_END;
			}
			message += 	_FSHELL_ASSERTION_START + assertion + _FSHELL_ASSERTION_END +
					"." + lineInfo;
			//TODO: add assumptions
			
			
			bw.append( message );
			bw.newLine();
		}
		System.err.println("Cannot parse: "+booleanProperty);
	}


	

}
