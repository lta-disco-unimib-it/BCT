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
package cpp.gdb.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class GCovParser<T extends Comparable<T>> {
	
	private String sourcesFolderPath, destinationFilePath;
	protected Hashtable<FileNameAndCoverageKey<T>, Integer> map;
	private static final String GCOV_EXTENSION = "gcov";
	
//	public static void main(String[] args) {
//		if (args.length != 2) {
//			throw new InvalidParameterException("The parser must be invoked with the following parameters: " 
//					+ "\n- source folder path\n- destination file path");
//		}
//		
//		GCovParser parser = new GCovParser(args[0], args[1]);
//		parser.parseAll();		
//		parser.save();
//	}

	public static void main(String args[]){
		

			Hashtable<FileNameAndCoverageKey<Integer>, Integer> loaded = GCovParser.<Integer>load(args[0]);

			for (Entry<FileNameAndCoverageKey<Integer>,Integer> e : loaded.entrySet()  ){
				System.out.println(e.getKey()+" "+e.getValue());
			}
		
	}
	
	public GCovParser(String sourcesFolderPath, String destinationFilePath) {
		this.sourcesFolderPath = sourcesFolderPath;
		this.destinationFilePath = destinationFilePath;
		File f = new File( destinationFilePath );
		if ( ! f.exists() ){
		map = new Hashtable<FileNameAndCoverageKey<T>, Integer>();	
		} else {
		map = load(destinationFilePath);
		}
	}
	
	public Hashtable<FileNameAndCoverageKey<T>, Integer> getMap() {
		return map;
	}
	
	public void parseAll() {
		List<File> fileToParse = new ArrayList<File>();
		
		searchGCovFiles(new File(sourcesFolderPath), fileToParse);		
		for (File file: fileToParse) {
			parseFile(file);
			file.delete();
		}
	}
	
	private void searchGCovFiles(File folder, List<File> fileToParse) {
		File[] sources = folder.listFiles();
		for (int i = 0; i < sources.length; i++) {
			File file = sources[i];
			
			if (!file.isDirectory()) {
				String extension = getExtension(file);
				if (extension != null && extension.equals(GCOV_EXTENSION)) {
					fileToParse.add(file);
				}
			} else {
				searchGCovFiles(file, fileToParse);
			}
		}
	}
	
	
	abstract protected void parseFile(File file);
	
	

	protected int getCoverageValueToAdd(int lineExecutionCount) {
		return lineExecutionCount;
	}

	public void save() {
		storeCoverageMap(map, destinationFilePath);
	}

	public static <T extends Comparable<T>> void storeCoverageMap( Hashtable<FileNameAndCoverageKey<T>, Integer> mapToStore, String _destinationFilePath) {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream(_destinationFilePath));
			stream.writeObject(mapToStore);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Set<String> getCoveredSourceFiles(Hashtable<FileNameAndCoverageKey, Integer> map) {
		Set<String> coveredFiles = new HashSet<String>();
		
		for (FileNameAndCoverageKey key : map.keySet()) {
			String filePath = key.getFilePath();
			coveredFiles.add(filePath);
		}
		
		return coveredFiles;
	}
	
	private static Map<String, FileNameAndCoverageKey> flyweightCached = new HashMap<String, FileNameAndCoverageKey>();
	public static <T extends Comparable<T>> Hashtable<FileNameAndCoverageKey<T>, Integer> load(String filePath) {
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filePath));
			Hashtable<FileNameAndCoverageKey<T>, Integer> loadedMap = (Hashtable<FileNameAndCoverageKey<T>, Integer>) stream.readObject();
			stream.close();
			
//			Hashtable<FileNameAndCoverageKey<T>, Integer> newMap = new Hashtable<FileNameAndCoverageKey<T>, Integer>();
//			for ( Entry<FileNameAndCoverageKey<T>, Integer> entry : loadedMap.entrySet() ){
//				FileNameAndCoverageKey<T> branch = entry.getKey();
//				String flyKey = branch.toString();
////				System.err.println("KEY "+flyKey);
//				FileNameAndCoverageKey<T> cached = flyweightCached.get(flyKey);
//				if ( cached == null ){
//					cached = new FastFileNameAndCoverageKey(branch.getFilePath(), branch.getLineNumber() );
//					flyweightCached.put(flyKey, cached);
//				}
//				newMap.put(cached, entry.getValue());
//			}
			
			return loadedMap;
			
//			return loadedMap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String getExtension(File f) {
		return FileUtil.getExtension(f);
	}
	
	protected String removeGCovExtension(String name) {
		int i = name.lastIndexOf('.');
		
		if (i == -1) {
			return name; //In case of files without extension
		}
		
		return name.substring(0, i);
	}

	protected void addToCoverageMap(FileNameAndCoverageKey<T> key, int valueToAdd) {
		
		Integer currentValue = map.get(key);
		if ( currentValue != null ){
			valueToAdd+=currentValue;
		}
		System.err.println("Adding "+key+" "+valueToAdd);
		map.put(key , valueToAdd );
	}

}
