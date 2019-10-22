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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * An index for method name, that associate a method to each id
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileIndex {
	
	
	public class FileIndexException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FileIndexException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public FileIndexException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public FileIndexException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public FileIndexException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	/* Data are recorded as Name = ID*/
	private Properties index = new Properties();
	/*Reverse index, useful to get name from an id*/
	private HashMap<String,String> reverseIndex = new HashMap<String,String>();
	
	private String suffix;
	
	/**
	 * Create a file index
	 * 
	 * @param file
	 */
	public FileIndex ( File file ){
		this( file, "" );
	}
	
	/**
	 * Create a file index that creates ids that end with suffix
	 * 
	 * @param file
	 * @param suffix
	 */
	public FileIndex ( File file, String suffix ){
		this.suffix = suffix;
		
		try {
			if ( ! file.exists() ){
				//System.out.println("FileIndex NOT EXIST");
				file.createNewFile();
				return;
			}
			InputStream fileStream = new FileInputStream(file);
			loadDataFromStream(fileStream, suffix);
			
			//System.out.println("LOADED "+index.toString());
			
			fileStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FileIndex ( InputStream inputStream, String suffix ){
		loadDataFromStream(inputStream,suffix);
	}
	
	/**
	 * Loads data from the stream. It does not close the stream.
	 * @param inputStream
	 * @param suffix
	 */
	private void loadDataFromStream( InputStream inputStream, String suffix ){
		try {
			

			index.load(inputStream);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator it = index.keySet().iterator();
		while ( it.hasNext() ){
			String key = (String) it.next();
			reverseIndex.put( (String) index.get(key), key );
		}
	}

	/**
	 * Given a method name return an id
	 * 
	 * @param modelName
	 * @return
	 * @throws FileIndexException 
	 */
	public String getId(String name) throws FileIndexException {
		if ( ! containsName(name) )
			throw new FileIndexException("Method not present");
		return index.getProperty(name);
	}
	
	/**
	 * Given an id return the correspnding metgod name
	 * @param id
	 * @return
	 * @throws FileIndexException
	 */
	public String getNameFromId(String id) throws FileIndexException {
		//System.out.println("FILE INDEX request for id "+id+" "+reverseIndex.get(id));
		if ( ! containsId( id ) )
			throw new FileIndexException("Method not present");
		return (String) reverseIndex.get(id);
	}

	/**
	 * Returns true or false weathe ror not this index contains a name associated to the given id
	 *  
	 * @param id
	 * @return
	 */
	public boolean containsId( String id ){
		return reverseIndex.containsKey(id);
	}
	
	/**
	 * Returns true or false weathe ror not this index contains a name associated to the given id
	 *  
	 * @param id
	 * @return
	 */
	public boolean containsName( String name ){
		return index.containsKey(name);
	}
	
	/**
	 * Add a method to this list and returns an id
	 * 
	 * @param methodName
	 * @return
	 */
	public String add(String methodName) throws FileIndexException {
		if ( index.containsKey(methodName) )
			return index.getProperty(methodName);
		String id = ""+index.size()+suffix;
		index.put(methodName, id);
		reverseIndex.put(id, methodName);
		return id;
	}
	
	
	public void save( File file ) throws IOException {
			FileOutputStream fos = new FileOutputStream( file );
			index.store(fos, null);
			fos.close();
	}

	/**
	 * Return a List of all the IDs
	 * 
	 * @return
	 */
	public Set<String> getIds() {
		return reverseIndex.keySet();
	}
	
	public String toString() {
		String string = "FIleIndex : { ";
		Iterator it = index.keySet().iterator();
		while ( it.hasNext() ){
			String id = (String) it.next();
			string +=  id+ " = "+index.getProperty(id); 
		}
		return string + "}";
	}

	public int size() {
		return index.size();
	}
	
	public Set<String> getNames() {
		Set<String> names = new TreeSet<String>();
		
		Iterator<Object> keysIt = index.keySet().iterator();
		while ( keysIt.hasNext() ){
			names.add( (String) keysIt.next() );
		}
		
		return names;
	}
	
	public void clear(){
		index.clear();
		reverseIndex.clear();
	}

	protected String getSuffix() {
		return suffix;
	}
	
	protected void setSuffix(String s) {
		suffix=s;
	}
	
}
