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
package cpp.gdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.FileUtil;


public class SourceLinesMapper {
	
	Logger LOGGER = Logger.getLogger(SourceLinesMapper.class.getCanonicalName());
	
	
	
	public interface CommonInterface{
		public int getCorrespondingLine(String fileName, int lineNumber) throws SourceMapperException;

		public String getCorrespondingFile(String fileName);
	}
	
	public class InOriginal implements CommonInterface {
		
		public int getCorrespondingLine(String fileName, int lineNumber) throws SourceMapperException{
			return getCorrespondingLineInOriginalProject(fileName, lineNumber);
		}

		@Override
		public String getCorrespondingFile(String fileName) {
			return getCorrespondingFileInOriginalProject(fileName);
		}

	}
	
	public class InModified implements CommonInterface {
		
		public int getCorrespondingLine(String fileName, int lineNumber) throws SourceMapperException{
			return getCorrespondingLineInModifiedProject(fileName, lineNumber);
		}

		@Override
		public String getCorrespondingFile(String fileName) {
			return getCorrespondingFileInModifiedProject(fileName);
		}
	}
	
	public final InModified inModified = new InModified();
	public final InOriginal inOriginal = new InOriginal();
	
	
	Map<String, List<Integer>> originalSourcesLines, modifiedSourcesLines;
	private Map<String, FunctionMonitoringData> originalSoftwareFunctions;
	private Map<String, FunctionMonitoringData> modifiedSoftwareFunctions;
	
	private String originalSourcesFolderPath;
	private String modifiedSourcesFolderPath;

	protected String getCorrespondingFileInOriginalProject(String fileName) {
		return getCorrespondingFile(fileName, modifiedSourcesFolderPath, originalSourcesFolderPath );
	}
	
	protected String getCorrespondingFileInModifiedProject(String fileName) {
		return getCorrespondingFile(fileName, originalSourcesFolderPath, modifiedSourcesFolderPath );
	}

	public String getCorrespondingFile(String fileName, String sourceFolderToRemove, String sourceFolderToAdd) {
		String prefix = "";
		
		if ( fileName.startsWith(sourceFolderToRemove) ){
			prefix = sourceFolderToAdd;
			fileName = trimToRelativePathIfNecessary(fileName, sourceFolderToRemove);
		}
		
		return prefix + fileName;
	}
	
	public Map<String, FunctionMonitoringData> getOriginalSoftwareFunctions() {
		return Collections.unmodifiableMap( originalSoftwareFunctions );
	}

	public Map<String, FunctionMonitoringData> getModifiedSoftwareFunctions() {
		return Collections.unmodifiableMap( modifiedSoftwareFunctions );
	}

	public SourceLinesMapper(File originalSourcesFolder, File modifiedSourcesFolder, List<FileChangeInfo> diffs) throws IOException{
		this(diffs,null,null);
		originalSourcesFolderPath = originalSourcesFolder.getAbsolutePath();
		modifiedSourcesFolderPath = modifiedSourcesFolder.getAbsolutePath();
	}
	
	public SourceLinesMapper(List<FileChangeInfo> diffs, Map<String, FunctionMonitoringData> originalSoftwareFunctions, Map<String, FunctionMonitoringData> modifiedSoftwareFunctions) throws IOException {
		originalSourcesLines = TraceUtils.getSourceLines(diffs, true);
		modifiedSourcesLines = TraceUtils.getSourceLines(diffs, false);

		
		removeLines(originalSourcesLines, TraceUtils.getModifiedLines(diffs, true));
		removeLines(modifiedSourcesLines, TraceUtils.getModifiedLines(diffs, false));
		
		this.originalSoftwareFunctions = originalSoftwareFunctions;
		this.modifiedSoftwareFunctions = modifiedSoftwareFunctions;
	}
	
	public boolean containOriginalFile( String fileName ){
		return originalSourcesLines.containsKey(fileName);
	}
	
	public int getCorrespondingLineInModifiedProject(String fileName, int lineNumber) throws SourceMapperException {
		fileName = trimToRelativePathIfNecessary( fileName, originalSourcesFolderPath );
		
		List<Integer> lines = getCorrespondingLines( originalSourcesLines, fileName );
		
		
		int newLinePosition = lines.indexOf(lineNumber);
		
		if ( newLinePosition == -1 ){
			throw new SourceMapperException("Line "+newLinePosition+" was modified, cannot find corresponding one");
		}
		
		List<Integer> correspondingLines = getCorrespondingLines( modifiedSourcesLines, fileName );
		
		if ( correspondingLines == null ){
			Logger.getLogger(SourceLinesMapper.class.getCanonicalName()).severe("NULL corresponding line for "+lineNumber+" "+originalSourcesLines+" "+modifiedSourcesLines);
		}
		
		return correspondingLines.get(newLinePosition);
	}

	public String trimToRelativePathIfNecessary( String fileName, String pathToCutOut ) {
		if ( pathToCutOut != null && fileName.startsWith(pathToCutOut) ){
			fileName = fileName.substring(pathToCutOut.length());//we leave the separator
		}
		return fileName;
	}
	
	private List<Integer> getCorrespondingLines(
			Map<String, List<Integer>> originalSourcesLines, String fileName) throws SourceMapperException {
		if ( EnvUtil.isWindows() ){
			fileName = fileName.replace("/", File.separator);
		}
		
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Retrieving corresponding lines for file: "+fileName);
		}
		
		List<Integer> lines = originalSourcesLines.get(fileName);
		
		
		if ( lines == null ){
			fileName= FileUtil.getCleanPath(fileName);
			if ( EnvUtil.isWindows() ){
				fileName = fileName.replace("/", File.separator);
			}
			lines = originalSourcesLines.get(fileName);
		}
		if ( lines == null ){
			if ( EnvUtil.isWindows() ){
				fileName= "\\"+fileName;
			} else {
				fileName= "/"+fileName;
			}
			lines = originalSourcesLines.get(fileName);
		}
		
		if ( lines == null ){
			if ( fileName.startsWith("\\") || fileName.startsWith("/") ){
				fileName = fileName.substring(1);
				lines = originalSourcesLines.get(fileName);
			}
		}
		
		
		if ( lines == null ){
			LOGGER.warning("Cannot find lines for "+fileName+" available "+originalSourcesLines.keySet());
			LOGGER.fine("FileName looked for "+fileName);
			LOGGER.fine("OriginalSOurcesLines: "+originalSourcesLines);
			for ( Entry<String, List<Integer>> e : originalSourcesLines.entrySet() ){
				LOGGER.fine(e.getKey()+" : "+e.getValue());
			}
			
			throw new SourceMapperException("Cannot find lines for "+fileName+" available "+originalSourcesLines.keySet());
		}
		
		return lines;
	}

	public int getCorrespondingLineInOriginalProject(String fileName, int lineNumber) throws SourceMapperException {
		fileName = trimToRelativePathIfNecessary( fileName, modifiedSourcesFolderPath );
		
		List<Integer> lines = getCorrespondingLines(modifiedSourcesLines, fileName);
		
		int oldLinePosition = lines.indexOf(lineNumber);
		
		if ( oldLinePosition == -1 ){
			throw new SourceMapperException("Line "+oldLinePosition+" was modified, cannot find corresponding one");
		}
		
		return getCorrespondingLines( originalSourcesLines, fileName ).get(oldLinePosition);
	}
	
	private void removeLines(Map<String, List<Integer>> lines, Map<String, List<Integer>> linesToRemove) {
		for (String fileName : linesToRemove.keySet()){
			List<Integer> toRemove = linesToRemove.get(fileName);
			List<Integer> allLines = lines.get(fileName);
			allLines.removeAll(toRemove);
		}
	}
	
	public Map<String, List<Integer>> getOriginalSourcesLines() {
		return originalSourcesLines;
	}
	
	public Map<String, List<Integer>> getModifiedSourcesLines() {
		return modifiedSourcesLines;
	}

	public String getCorrespondingFunctionOriginal(String fileLocation,
			String functionName ) throws SourceMapperException {
		FunctionMonitoringData func = originalSoftwareFunctions.get(functionName);
		if ( func != null ){
			return functionName;
		}
		
		func = modifiedSoftwareFunctions.get(functionName);
		if ( func == null ){
			throw new SourceMapperException("Function "+functionName+"  does not exist in original software");
		}
		
		//the function name might have been changed due to refactoring, looks for corresponding line as workaround
		
		for ( LineData line : func.getLines() ){
			for( FunctionMonitoringData  f : originalSoftwareFunctions.values() ){
				if ( fileLocation.equals( f.getSourceFileLocation() ) ){
					if ( f.containsLine(line.getLineNumber()) ){
						return f.getMangledName();
					}
				}
			}
		}
		
		throw new SourceMapperException("Function "+functionName+"  does not exist in modified software");
	}
	
	
	public String getCorrespondingFunction(String fileLocation,
			String functionName ) throws SourceMapperException {
		FunctionMonitoringData func = modifiedSoftwareFunctions.get(functionName);
		if ( func != null ){
			return functionName;
		}
		
		func = originalSoftwareFunctions.get(functionName);
		if ( func == null ){
			throw new SourceMapperException("Function "+functionName+"  does not exist in original software");
		}
		
		//the function name might have been changed due to refactoring, looks for corresponding line as workaround
		
		for ( LineData line : func.getLines() ){
			for( FunctionMonitoringData  f : modifiedSoftwareFunctions.values() ){
				if ( fileLocation.equals( f.getSourceFileLocation() ) ){
					if ( f.containsLine(line.getLineNumber()) ){
						return f.getMangledName();
					}
				}
			}
		}
		
		throw new SourceMapperException("Function "+functionName+"  does not exist in modified software");
	}
	
	public FunctionMonitoringData getCorrespondingFunctionData(String functionName) throws SourceMapperException{
		FunctionMonitoringData funcData = modifiedSoftwareFunctions.get(functionName);
		if ( funcData == null ){
			throw new SourceMapperException("Function "+functionName+"  does not exist in modified software");
		}
		return funcData;
	}
	
	public String getCorrespondingFunctionOriginal(String fileLocation,
			String functionName, int correspondingLine) throws SourceMapperException {
		FunctionMonitoringData func = originalSoftwareFunctions.get(functionName);
		if ( func != null ){
			if ( func.containsLine( correspondingLine ) ){
				return functionName;
			}
		}
		
		for( FunctionMonitoringData  f : originalSoftwareFunctions.values() ){
			if ( fileLocation.equals( f.getSourceFileLocation() ) ){
				if ( f.containsLine(correspondingLine) ){
					return f.getMangledName();
				}
			}
		}
		
		throw new SourceMapperException("Function "+functionName+"  does not exist in modified software");
	}
	
	
	public String getCorrespondingFunction(String fileLocation,
			String functionName, int correspondingLine) throws SourceMapperException {
		FunctionMonitoringData func = modifiedSoftwareFunctions.get(functionName);
		if ( func != null ){
			if ( func.containsLine( correspondingLine ) ){
				return functionName;
			}
		}
		
		for( FunctionMonitoringData  f : modifiedSoftwareFunctions.values() ){
			if ( fileLocation.equals( f.getSourceFileLocation() ) ){
				if ( f.containsLine(correspondingLine) ){
					return f.getMangledName();
				}
			}
		}
		
		throw new SourceMapperException("Function "+functionName+"  does not exist in modified software");
	}
	
	public static SourceLinesMapper createMapperFromSoftware(File originalSoftwareFolder,
			File modifiedSoftwareFolder, File originalSoftwareObjDump, File modifiedSoftwareObjDump) throws IOException {
		ArrayList<File> orig = new ArrayList<File>();
		ArrayList<File> mod = new ArrayList<File>();
		
		orig.add(originalSoftwareFolder);
		mod.add(modifiedSoftwareFolder);
		
		return createMapperFromSoftware(orig, mod, originalSoftwareObjDump, modifiedSoftwareObjDump);
	}
	
	public static SourceLinesMapper createMapperFromSoftware(ArrayList<File> originalSoftwareFolders,
			ArrayList<File> modifiedSoftwareFolders, File originalSoftwareObjDump, File modifiedSoftwareObjDump) throws IOException {
		
		Collection<String> originalFoldersAsString = new ArrayList<String>();
		for ( File folder : originalSoftwareFolders ){
			originalFoldersAsString.add(folder.getAbsolutePath());
		}

		Collection<String> modifiedFoldersAsString = new ArrayList<String>();
		for ( File folder : modifiedSoftwareFolders ){
			modifiedFoldersAsString.add(folder.getAbsolutePath());
		}
		
		
		RegressionConfigObjDumpListener listener = GdbRegressionConfigCreator.extractFunctionsData(originalSoftwareObjDump, originalFoldersAsString );

		Map<String, FunctionMonitoringData> originalSoftwareFunctions = listener.getFunctionsData();
		
		
		RegressionConfigObjDumpListener listenerMod = GdbRegressionConfigCreator.extractFunctionsData(modifiedSoftwareObjDump, modifiedFoldersAsString );
		Map<String, FunctionMonitoringData> modifiedSoftwareFunctions = listenerMod.getFunctionsData();
		
		return createMapperFromFunctionData(originalSoftwareFolders, modifiedSoftwareFolders, originalSoftwareFunctions, modifiedSoftwareFunctions);
	}
		
		
	public static SourceLinesMapper createMapperFromFunctionData(ArrayList<File> originalSoftwareFolders,
			ArrayList<File> modifiedSoftwareFolders, 
			Map<String, FunctionMonitoringData> originalSoftwareFunctions,
			Map<String, FunctionMonitoringData> modifiedSoftwareFunctions ) throws IOException {
		
		

		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		List<FileChangeInfo> diffs = mfd.extractDiffsFromMultipleSourceFolders(originalSoftwareFolders, modifiedSoftwareFolders);

		return new SourceLinesMapper( diffs, originalSoftwareFunctions, modifiedSoftwareFunctions );

	}

	public static SourceLinesMapper createMapperFromFunctionData(File originalSoftwareFolder,
			File modifiedSoftwareFolder, 
			Map<String, FunctionMonitoringData> originalFunctions,
			Map<String, FunctionMonitoringData> modifiedFunctions) throws IOException {
		
		
		ArrayList<File> orig = new ArrayList<File>();
		ArrayList<File> mod = new ArrayList<File>();
		
		orig.add(originalSoftwareFolder);
		mod.add(modifiedSoftwareFolder);
		
		
		return createMapperFromFunctionData(orig, mod, originalFunctions, modifiedFunctions);
	}
}
