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
import java.io.IOException;

public class FileUtil {
	
	public static String getExtension(File f) {
		String name = f.getName();
		int i = name.lastIndexOf('.');
		
		if (i == -1) {
			return null; //In case of files without extension
		}
		
		return name.substring(i + 1);
	}
	
	public static String getDirectory(File f) {
		String path;
		try {
			path = f.getCanonicalPath();
			int separatorIndex = path.contains("/") ? path.lastIndexOf("/") :  path.lastIndexOf("\\");
			return path.substring(0, separatorIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
