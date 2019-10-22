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

import java.io.Serializable;

public class FileNameAndCoverageKey<T extends Comparable<T>> implements Serializable, Comparable<FileNameAndCoverageKey<T>> {

	private static final long serialVersionUID = -4773214370649085883L;

	private String filePath;
	private T lineNumber;

	public FileNameAndCoverageKey(String fileName, T lineNumber) {
		this.filePath = fileName;
		this.lineNumber = lineNumber;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public T getLineNumber() {
		return lineNumber;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileNameAndCoverageKey) {
			FileNameAndCoverageKey casted = (FileNameAndCoverageKey) obj;
			String fileNameToCompare = casted.getFilePath();
			Comparable lineNumberToCompare = casted.getLineNumber();
			return filePath.equals(fileNameToCompare) && lineNumber.equals( lineNumberToCompare  );
		}
		return false;
	}
	@Override
	public String toString() {
		return filePath + ":" + lineNumber.toString();
	}

	@Override
	public int compareTo(FileNameAndCoverageKey<T> o2) {
		int cmp = getFilePath().compareTo(o2.getFilePath());
		if ( cmp != 0 ){
			return cmp;
		}
		
		return getLineNumber().compareTo(o2.getLineNumber());
	}

	
	
}
