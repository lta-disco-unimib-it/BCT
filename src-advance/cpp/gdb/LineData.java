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
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class LineData implements Serializable{

	private String fileLocation;
	private int lineNumber;
	private String address;
	private boolean duplicated;
	
	public LineData(String fileLocation, int lineNumber ) {
		super();
		this.fileLocation = fileLocation;
		this.lineNumber = lineNumber;
	}


	public String getCanonicalFilePath(){
		File f = new File( fileLocation);
		try {
			String toMonitor = f.getCanonicalPath();
			System.out.println("CANONICAL PATH "+toMonitor);
			return toMonitor;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileLocation;
	}
	
	public String getFileLocation() {
		return fileLocation;
	}


	public int getLineNumber() {
		return lineNumber;
	}
	
	public String toString(){
		return fileLocation+":"+lineNumber;
	}


	@Override
	public boolean equals(Object obj) {
		if ( !(  obj instanceof LineData ) ) {
			return false;
		}
		LineData rhs = (LineData) obj;
		
		if ( fileLocation == null ){
			if ( rhs.fileLocation != null ){
				return false;
			}
		} else {
			if ( ! fileLocation.equals(rhs.fileLocation) ){
				return false;
			}
		}
		
		return rhs.lineNumber == lineNumber;
	}


	@Override
	public int hashCode() {
		return toString().hashCode();
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getAddress() {
		return address;
	}
	
	public boolean hasAddress(){
		return address != null;
	}


	public void setDuplicated(boolean value) {
		duplicated = value;
	}
	
	/**
	 * Returns true if this line appears in multiple function bodies.
	 * @return 
	 */
	public boolean isDuplicated() {
		return duplicated;
	}
}
