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
package tools.fsa2xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import tools.fsa2xml.codec.api.FSACodec;
import tools.fsa2xml.codec.factory.FSA2FileFactory;
import tools.fsa2xml.codec.factory.FSA2FileFactoryException;
import automata.fsa.FiniteStateAutomaton;
import file.ParseException;

public class LazyFSALoader {
	
	public static class LazyFSALoaderException extends Exception {

		public LazyFSALoaderException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
		
	}

	public static FiniteStateAutomaton loadFSA( String path ) throws LazyFSALoaderException, FileNotFoundException {
		return loadFSA(path, null);
	}
	
	
	public static FiniteStateAutomaton loadFSA( String path, Map<String,String> namesMapping ) throws LazyFSALoaderException, FileNotFoundException {	
		
		
		//try to load with another codec
		for ( FSACodec codec : FSA2FileFactory.getAllCodecs() ){
			try {
				return loadFSA(codec,path,namesMapping);
			} catch (ClassNotFoundException e) {
				//This exception is generated when the file does not match the codecs format
				//Thus it is not necessary to print it
				//e.printStackTrace();
			} catch ( FileNotFoundException e) {
				//This exception is generated when the file does not match the codecs format
				//Thus it is not necessary to print it
				throw e;

			} catch ( ParseException e) {
				//This exception is generated when the file does not match the codecs format
				//Thus it is not necessary to print it
				//e.printStackTrace();
			} catch (Exception e) {
				//This exception is generated when the file does not match the codecs format
				//Thus it is not necessary to print it
				//e.printStackTrace();
			}
		}
		throw new LazyFSALoaderException("No codec available to load fsa "+path);
		
	}
	
	private static FiniteStateAutomaton loadFSA( FSACodec codec, String path ) throws IOException, ClassNotFoundException{
		return loadFSA(codec, path, null);		
	}
	
	private static FiniteStateAutomaton loadFSA( FSACodec codec, String path, Map<String,String> namesMapping ) throws IOException, ClassNotFoundException{
		FiniteStateAutomaton fsa = codec.loadFSA(path, namesMapping);
		return fsa;
	}

	public  static void storeFSA ( FiniteStateAutomaton fsa, File dest ) {
		String name = dest.getName();
		int dotIdx = name.lastIndexOf('.');
		
		String extension = name.substring(dotIdx+1);
		
		FSACodec codec;
		try {
			codec = FSA2FileFactory.getCodecForFileExtension(extension);
			
			codec.saveFSA(fsa, dest);
			
		} catch (FSA2FileFactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
