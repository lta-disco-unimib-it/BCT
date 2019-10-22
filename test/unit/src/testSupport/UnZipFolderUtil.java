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
package testSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *	This class mangaes the extraction of zip files in a given folder.
 * All the given zip files are extracted to a given folder and then can be al deleted with a call to a specific method.
 *  
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class UnZipFolderUtil {

	public static class ZipUtilException extends Exception {

		public ZipUtilException(String string) {
			super(string);
		}
		
	}

	private File destinationFolder;
	private LinkedList<File> createdFiles = new LinkedList<File>();
	private static final int bufLen = 256;
	
	/**
	 * Create a UnZipUtil that will extract all the files in the given working directory
	 * 
	 * @param workingDir
	 * @throws ZipUtilException
	 */
	public UnZipFolderUtil ( File workingDir ) throws ZipUtilException{
		if ( ! workingDir.isDirectory() ){
			throw new ZipUtilException(workingDir+" is not a directory");
		}
		this.destinationFolder = workingDir;
	}
	
	/**
	 * Decompress the given file in the working directory of the UnZip util.
	 * The decompressed files can be later deleted by a call to deleteCreatedFiles
	 * @param origin
	 * @throws ZipException
	 * @throws IOException
	 */
	public void unzip( File origin ) throws ZipException, IOException{
		
		ZipFile zip = new ZipFile(origin);
		Enumeration<? extends ZipEntry> e = zip.entries();
		while ( e.hasMoreElements() ){
			ZipEntry entry = e.nextElement();
			File dest = new File ( destinationFolder, entry.getName());
			
			InputStream zipis=null;
			OutputStream os=null;
			try {
				
				zipis = zip.getInputStream(entry);
			os = new FileOutputStream(dest);
			byte buffer[]=new byte[bufLen];
			int readed;
			do {
				readed = zipis.read(buffer);
				if ( readed > 0 )
					os.write(buffer, 0, readed);
			} while ( readed > 0 );
			} finally {
				if ( zipis != null )
					zipis.close();
				if ( os != null )
					os.close();
			}
			createdFiles .add(dest);
		}
	}
	
	/**
	 * Delete all thefiles that have been created with this ZipUtil
	 * 
	 * @throws ZipException if error occurr while trying to remove the files 
	 *
	 */
	public void deleteCreatedFiles() throws ZipException{
		LinkedList<File> notRemoved = new LinkedList<File>();
		
		for ( File file : createdFiles ){
			if ( file.delete() == false ){
				notRemoved.add(file);
			}
		}
		
		if ( notRemoved.size() > 0 ){
			StringBuffer msgB = new StringBuffer();
			msgB.append("It was not possible to remove these files: ");
			for ( File file : notRemoved ){
				msgB.append(file.getAbsolutePath());
			}
			throw new ZipException(msgB.toString());
		}
			
	}
}
