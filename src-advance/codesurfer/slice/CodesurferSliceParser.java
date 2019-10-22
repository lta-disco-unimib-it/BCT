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
package codesurfer.slice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import cpp.gdb.coverage.FileNameAndCoverageKey;

public class CodesurferSliceParser {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new InvalidParameterException("The parser must be invoked with the following parameters: " 
					+ "\n- file path");
		}
		
		try {
			CodesurferSliceParser.load(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Set<FileNameAndCoverageKey<Integer>> load(String sliceFilePath) throws IOException {
		Set<FileNameAndCoverageKey<Integer>> lines = new HashSet<FileNameAndCoverageKey<Integer>>();
		
		File file = new File(sliceFilePath);
		if (!file.exists()) {
			throw new IOException("File " + sliceFilePath + " not found!");
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(":");
			lines.add(new FileNameAndCoverageKey<Integer>(splitted[0], Integer.valueOf(splitted[1])));
		}
		
		reader.close();
		
		//System.out.println(lines);
		
		return lines;
	}
}
