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
package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * This class replace all the lines of a given files according to a translation table.
 * The table indicate that if a line starts with a specified string it must be replaced with a specific string   
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class LineReplacer {

	private HashMap<String,String> replaceMap;
	
	LineReplacer( Map<String,String> map ){
		this.replaceMap = new HashMap<String,String>( map );
	}
	
	/**
	 * This method replaces all the lines of a given file and stores a backup copy of the given file.
	 * 
	 * @param original
	 * @throws IOException 
	 */
	public void doReplacement( File original ) throws IOException{
		File backup = new File(original.getAbsolutePath()+".bak");
		File destination = new File ( original.getAbsolutePath() );
		
		original.renameTo(backup);
		
		BufferedReader r = new BufferedReader( new FileReader(backup) );
		
		BufferedWriter w = new BufferedWriter( new FileWriter(destination) );
		
		String line = null;
		while ( ( line = r.readLine() ) != null ){
			
			String replacement = line;
			
			for ( String toReplaceKey : replaceMap.keySet() ){
				if ( line.startsWith(toReplaceKey) ){
					replacement = replaceMap.get(toReplaceKey);
					break;
				}
			}
			
			
			w.write(replacement);
			
			w.write("\n");
		}
		w.close();
		r.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if ( args.length < 1 ){
			printUsage();
			System.exit(-1);
		}
		
		Properties p = new Properties();
		
		try {
			p.load(new FileInputStream(args[0]));
			
			HashMap<String,String> map = new HashMap<String, String>();
			
			for ( Entry property : p.entrySet() ){
				map.put((String)property.getKey(), (String)property.getValue());
			}
			
			LineReplacer lr = new LineReplacer(map);
			
			
			File curDir = new File(System.getProperty("user.dir"));
			
			File[] metafiles = curDir.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					return pathname.isFile() && pathname.getName().endsWith(".meta");
				}}
				);
			
			for ( File file : metafiles ){
				lr.doReplacement(file);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void printUsage() {
		
	}

}
