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
package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ZipFileIndexAppend extends FileIndex {
	
	
	public ZipFileIndexAppend(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
		this(zipFile,zipEntry,"");
	}
	/**
	 * @throws IOException 
	 * @see FileIndex
	 */
	public ZipFileIndexAppend(ZipFile zipFile, ZipEntry zipEntry, String suffix) throws IOException {
		super(zipFile.getInputStream(zipEntry),suffix);
		
	}

	public String add(String name){
		throw new NotImplementedException();
	}

	
}
