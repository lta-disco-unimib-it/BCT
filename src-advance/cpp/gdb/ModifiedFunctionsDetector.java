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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import util.FileUtil;
import util.Logging;
import util.componentsDeclaration.Component;
import util.componentsDeclaration.ComponentDefinitionExporter;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;



public class ModifiedFunctionsDetector {
	
	private boolean useDemangledNames;

	public boolean isUseDemangledNames() {
		return useDemangledNames;
	}


	public void setUseDemangledNames(boolean useDemangledNames) {
		this.useDemangledNames = useDemangledNames;
	}


	@Deprecated
	public Component getChangesAsComponent(File version1, File version2, File objdumpFile){
		Logging.info("Retrieving changes between "+version1.getAbsolutePath()+" AND "+version2.getAbsolutePath());
		ModifiedFunctionsDetectorObjDumpListener listener = fillChangesListener(version1, version2, objdumpFile);
		
		return listener.getModifiedFunctionsComponent();
	}
	
	
	public Component getChangesAsComponent(File version1, File version2, File objdumpFile, File objdumpFileV2 ){
		Logging.info("Retrieving changes between "+version1.getAbsolutePath()+" AND "+version2.getAbsolutePath());
		ModifiedFunctionsDetectorObjDumpListener listener = fillChangesListener(version1, version2, objdumpFile, objdumpFileV2);
		
		return listener.getModifiedFunctionsComponent();
	}

	public ModifiedFunctionsAnalysisResult getModifiedFunctionsAnalysisResult(File version1, File version2, File objdumpFile, File objdumpFileV2 ){
		Logging.info("Retrieving changes between "+version1.getAbsolutePath()+" AND "+version2.getAbsolutePath());
		return fillChangesListener(version1, version2, objdumpFile, objdumpFileV2);
	}
	
	
	private ModifiedFunctionsDetectorObjDumpListener fillChangesListener(File version1, File version2, File objdumpFile, File objdumpFilev2){
		
		ModifiedFunctionsDetectorObjDumpListener listener1 = fillChangesListener(version1, version2, objdumpFile);
		
		ModifiedFunctionsDetectorObjDumpListener listener2 = fillChangesListener(version2, version1, objdumpFilev2);
		
		HashSet<String> allFunctionsOriginal = listener1.getAllFunctions();
		HashSet<String> allFunctionsModified = listener2.getAllFunctions();
		
		HashSet<String> modifiedFunctions = new HashSet<String>();
		HashSet<String> addedFunctions = new HashSet<String>();
		HashSet<String> deletedFunctions = new HashSet<String>();
		
		for ( String function : listener1.getModifiedFunctions() ){
			if ( ! allFunctionsModified.contains(function) ){
				deletedFunctions.add(function);
			} else {
				modifiedFunctions.add(function);
			}
		}
		
		for ( String function : listener2.getModifiedFunctions() ){
			if ( ! allFunctionsOriginal.contains(function) ){
				addedFunctions.add(function);
			} else {
				modifiedFunctions.add(function);
			}
		}
		
		listener1.setModifiedFunctions( modifiedFunctions );
		listener1.setAddedFunctions( addedFunctions );
		listener1.setDeletedFunctions(deletedFunctions);
		
		return listener1;
	}
	
	private ModifiedFunctionsDetectorObjDumpListener fillChangesListener(File version1, File version2, File objdumpFile){
		
		
		
		List<FileChangeInfo> fileChanges = extractDiffs(version1, version2);
		
		Logging.fine("List of file changes follows");
		for ( FileChangeInfo fileChange : fileChanges ){
			Logging.fine("File change: "+fileChange.toString());
		}
		
		
		
		ModifiedFunctionsDetectorObjDumpListener listener = new ModifiedFunctionsDetectorObjDumpListener(fileChanges);
		listener.setUseDemangledNames(useDemangledNames);
		
		ObjDumpParser parser = new ObjDumpParser();
		try {
			parser.parse(objdumpFile, listener);
			
			
			return listener;
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return null;
		
	}

	public List<FileChangeInfo> extractDiffsFromMultipleSourceFolders(List<File> version1,List<File> version2) {
		ArrayList<FileChangeInfo> result = new ArrayList<FileChangeInfo>();
		for ( int i = 0; i < version1.size(); i++ ){
			result.addAll(extractDiffs(version1.get(i), version2.get(i)) );
		}
		return result;
	}

	public List<FileChangeInfo> extractDiffs(File version1, File version2) {
		
		
		Set<String> commonFiles = identifyCommonSourceFiles(version1, version2);
		
		List<FileChangeInfo> fileChanges = new ArrayList<FileChangeInfo>();
		fileChanges.addAll( extractDiffs( commonFiles, version1, version2 ) );
		return fileChanges;
	}


	public static Set<String> identifyCommonSourceFiles(File version1, File version2) {
		FileFilter filter = new CSourcesFilter();
		
		List<File> filesV1 = recursivelyListAllFiles( version1, filter );
		
		List<File> filesV2 = recursivelyListAllFiles( version2, filter );
		
		List<String> filesRelativeV1 = extractRelativeNames( version1, filesV1 );
		
		List<String> filesRelativeV2 = extractRelativeNames( version2, filesV2 );
		
		Set<String> commonFiles = getCommonFiles( filesRelativeV1, filesRelativeV2 );
		return commonFiles;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		File version1 = new File(args[0]);
		
		File version2 = new File(args[1]);
		
		File objdumpFile = new File(args[2]);
		
		File objdumpFile2 = new File(args[3]);
		
		File modifiedFunctionsFile = new File(args[4]);
		
//		Set<String> commonFiles = new HashSet<String>();
//		commonFiles.add("src/pr.c");
//		List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version1, version2);
//		
//		System.out.println(diffs);
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version1, version2, objdumpFile, objdumpFile2 );
		
		System.out.println(changesComponent.toString());
		
		List<Component> components = new ArrayList<Component>();
		components.add(changesComponent);
		try {
			ComponentDefinitionExporter.export(components, modifiedFunctionsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		HashSet<String> modifiedFunctions = listener.getModifiedFunctions();
//		
//		
//		writeToFile( modifiedFunctions, modifiedFunctionsFile );
		
	}

	private static void writeToFile(HashSet<String> modifiedFunctions,
			File modifiedFunctionsFile) throws IOException {
		BufferedWriter w = null;
		
		try {
			w = new BufferedWriter( new FileWriter( modifiedFunctionsFile ) );
			
			for ( String func : modifiedFunctions ){
				w.write(func);
				w.newLine();
			}
			
		} finally {
			if ( w != null ){
				w.close();
			}
		}
		
	}

	public static List<FileChangeInfo> extractDiffs(Set<String> commonFiles, File version1,
			File version2) {
		
		List<FileChangeInfo> fileChanges = new ArrayList<FileChangeInfo>();
		
		for ( String commonFile : commonFiles ){
			try {
				
				File v1 = new File( version1.getAbsolutePath()+"/"+commonFile );
				File v2 = new File( version2.getAbsolutePath()+"/"+commonFile );
				
				if ( v1.isDirectory() && v2.isDirectory() ){
					continue;
				}
				
				if ( v2.isDirectory()  //in V2 it is a dir, is the same as if it was deleted
						|| ! v2.exists() ){
					FileChangeInfo ci = new FileChangeInfo("/"+commonFile,v1);
					ci.setRemoved(true);
					fileChanges.add(ci);
					continue;
				}
				
				
				
				List<String> linesV1 = FileUtil.getLines( v1, true );
				List<String> linesV2 = FileUtil.getLines( v2, true );
				
				Logging.fine("Diffing "+v1.getAbsolutePath()+" AND "+v2.getAbsolutePath());
				
				Patch diff = DiffUtils.diff(linesV1, linesV2);
				
				FileChangeInfo fileChange = new FileChangeInfo( commonFile,  new File ( version1.getAbsolutePath()+commonFile ),  new File ( version2.getAbsolutePath()+commonFile ));
				
				for ( Delta delta : diff.getDeltas() ){
					
//					if ( emptyLines( delta.getOriginal() ) && emptyLines( delta.getRevised() ) ) {
//						continue;
//					}
					
					int start = delta.getOriginal().getPosition();
				
					int end = delta.getOriginal().last();
					
					boolean pureAddition = false;
					boolean pureDeletion = false;
					
					if ( start > end ){ //This is the case of a pure insertion
						int aux = start;
						start = end;
						end = aux;
						pureAddition = true;
					}
					
					
					int startModified = delta.getRevised().getPosition();
					int endModified = delta.getRevised().last();
					if ( startModified > endModified ){ //This is the case of a pure deletion
						int aux = startModified;
						startModified = endModified;
						endModified = aux;
						pureDeletion = true;
					}
					
					fileChange.addChange(start+1, end+1, startModified+1, endModified+1, pureAddition, pureDeletion);
				}
				
				
				fileChanges.add(fileChange);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if ( Logging.fineIsLoggable() ){
			Logging.fine("File changes found:");
			for ( FileChangeInfo fileChange : fileChanges ){
				Logging.fine(fileChanges.toString());	
			}
			
		}
		return fileChanges;
	}

	private static boolean emptyLines(Chunk chunk) {
		for ( Object lineObject : chunk.getLines() ){
			String line = (String) lineObject;
			if ( ! line.trim().isEmpty() ){
				return false;
			}
		}
		return true;
	}

	private static List<String> getFilesRemoved(List<String> filesRelativeV1,
			List<String> filesRelativeV2) {
		HashSet<String> filesRelativeSetV1 = new HashSet<String>();
		filesRelativeSetV1.addAll(filesRelativeV1);
		
		
		filesRelativeSetV1.removeAll(filesRelativeV2);
		
		List<String> v1FilesRemoved = new ArrayList<String>();
		v1FilesRemoved.addAll(filesRelativeSetV1);
		
		return v1FilesRemoved;
	}
	
	protected static Set<String> getCommonFiles(List<String> filesRelativeV1,
			List<String> filesRelativeV2) {
		HashSet<String> filesRelativeSetV1 = new HashSet<String>();
		filesRelativeSetV1.addAll(filesRelativeV1);
		
		
		filesRelativeSetV1.retainAll(filesRelativeV2);
		

		
		return filesRelativeSetV1;
	}

	private static List<String> extractRelativeNames(File version1,
			List<File> filesV1) {
		ArrayList<String> result = new ArrayList<String>();
		
		String prefix = version1.getAbsolutePath();
		int prefixLen = prefix.length();
		
		
		for ( File f : filesV1 ){
			result.add ( f.getAbsolutePath().substring(prefixLen) );
		}
		
		return result;
	}

	public static ArrayList<File> recursivelyListAllFiles(File version1, FileFilter filter) {
		// TODO Auto-generated method stub
		ArrayList<File> result = new ArrayList<File>();
		
		recursivelyListAllFilesInternal(result, version1, filter);
		
		return result;
		
	}
	
	private static void recursivelyListAllFilesInternal(List<File> result, File version1, FileFilter filter) {

		File[] files = version1.listFiles(filter);
		
		if ( files == null ){
			return;
		}
		
		for ( File file :files ){
			result.add(file);
		}
		
		File[] dirs = version1.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.isDirectory();
			}
		});
		
		
		for ( File dir : dirs ){
			recursivelyListAllFilesInternal(result, dir, filter);
		}
	}

}
