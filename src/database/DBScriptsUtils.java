/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DBScriptsUtils {

	/**
	 * Returns the content of a given db script
	 * 
	 * @param scriptResourceName the name of the script, must be a resource in the classpath
	 * @throws DataLayerException 
	 * 
	 */
	public static String getDBScriptContent(String scriptResourceName) throws DataLayerException {
		InputStream stream = ClassLoader.getSystemResourceAsStream(scriptResourceName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer buf = new StringBuffer();
		String line = null;
		try {
			while ( ( line = reader.readLine() ) != null){
				buf.append(line);
				buf.append("\n");
			}
		} catch (IOException e) {
			throw new DataLayerException(e);
		}
		
		return buf.toString();
		
	}

}
