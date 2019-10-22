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
import java.io.Writer;

import util.FileIndex.FileIndexException;

public class FileIndexAppend extends FileIndex {
	private File indexFile;
	private boolean gzipped;
	
	public File getIndexFile() {
		return indexFile;
	}
	public FileIndexAppend(File file) {
		this(file,"");
	}
	/**
	 * @see FileIndex
	 */
	public FileIndexAppend(File file,String suffix) {
		super(file,suffix);
		this.indexFile = file;
	}

	public synchronized String add(String name) throws FileIndexException {
		boolean contains = super.containsName(name);
		
		String res = super.add(name);

		if ( ! contains ){
			try {
				Writer w = new FileWriter(indexFile,true);
				w.append(encode(name)+"="+res+"\n");
				w.close();
			} catch (IOException e) {
				throw new FileIndexException(e);
			}
		}
		
		return res;
	}
	
	private String encode(String name) {
		return name
				.replace("=", "\\=")
				.replace(":", "\\:")
				.replace(" ", "\\ ");
	}
	
	public void deleteAll() {
		String parent = indexFile.getParent();
		for ( String id : getIds() ){
			FileUtil.deleteRecursively( new File(parent,id) );
		}
		
		FileUtil.deleteRecursively(indexFile);
		
		clear();
	}
	
	public void setGZipped() {
		if ( gzipped ){
			return;
		}
		gzipped=true;
		String newSuffix = super.getSuffix()+".gz";
		super.setSuffix(newSuffix);
	}

	
}
