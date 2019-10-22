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
package modelsFetchers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import tools.fsa2xml.codec.api.FSACodec;
import tools.fsa2xml.codec.factory.FSA2FileFactory;
import util.FileUtil;
import automata.fsa.FiniteStateAutomaton;

/**
 * This class is a facade that permit to save/load finite state automaton to file system in different formats.
 * 
 * The format depend on the extension of the file:
 * 
 * fsa is used for bct xml FSA
 * jflap is used for jflap xml files
 * ser is used for jflap serialized objects
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FSAFileManager {

	public static class Extensions{
		public static final String SER = "ser";
		public static final String FSA = "fsa";
		public static final String JFLAP = "jff";
	}
	
	public static void saveFSA(File fsaFile, FiniteStateAutomaton fsa) throws ModelsFetcherException, FileNotFoundException, IOException {
		
		String extension = FileUtil.getExtension(fsaFile);
		
		if ( extension == null ){
			throw new ModelsFetcherException("Cannot save fsa to "+fsaFile+" : file extension missing");
		}
		
		if ( extension.toLowerCase().equals(Extensions.SER) ){
			saveSerFormat(fsaFile, fsa);
		} else if ( extension.toLowerCase().equals(Extensions.FSA) ){
			saveBCTFSAFormat(fsaFile, fsa);
		} else if ( extension.toLowerCase().equals(Extensions.JFLAP) ){
			saveJFlapFormat(fsaFile, fsa);
		} else {
			throw new ModelsFetcherException("Cannot save fsa to "+fsaFile+" : file extension not accepted, "+extension);
		}
	}
	
	private static void saveBCTFSAFormat(File fsaFile, FiniteStateAutomaton fsa) throws FileNotFoundException, IOException {
		FSACodec fsaCodec = FSA2FileFactory.getFSABctXml();
		
		fsaCodec.saveFSA(fsa, fsaFile.getAbsolutePath());
		
	}

	private static void saveJFlapFormat(File fsaFile, FiniteStateAutomaton fsa) throws FileNotFoundException, IOException {
		FSACodec fsaCodec = FSA2FileFactory.getFSAXml();
		
		fsaCodec.saveFSA(fsa, fsaFile.getAbsolutePath());
		
	}

	private static void saveSerFormat(File fsaFile, FiniteStateAutomaton fsa) throws ModelsFetcherException, FileNotFoundException, IOException{
		
		serializeFSA(fsa, fsaFile);

		
		
	}


	public static void serializeFSA(FiniteStateAutomaton fsa, File fsaFile) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = null;

		oos = new ObjectOutputStream(new FileOutputStream(fsaFile));

		oos.writeObject(fsa);
		oos.close();

	}

	/**
	 * Load an FSA from the given file. The FSA can be stored in one of the supported formats. The file must have the extension corresponding 
	 * to its format.
	 * 
	 * @param fsaFile file to load
	 * @return the automaton
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ModelsFetcherException 
	 */
	public static FiniteStateAutomaton loadFSA(File fsaFile) throws IOException, ClassNotFoundException, ModelsFetcherException {
		String extension = FileUtil.getExtension(fsaFile);
		
		if ( extension == null ){
			throw new ModelsFetcherException("Cannot load fsa from "+fsaFile+" : file extension missing");
		}
		
		if ( extension.toLowerCase().equals(Extensions.SER) ){
			return loadSerFormat(fsaFile);
		} else if ( extension.toLowerCase().equals(Extensions.FSA) ){
			return loadBCTFSAFormat(fsaFile);
		} else if ( extension.toLowerCase().equals(Extensions.JFLAP) ){
			return loadJFlapFormat(fsaFile);
		}
		
		throw new ModelsFetcherException("Cannot save fsa to "+fsaFile+" : file extension not accepted, "+extension);
		
	}

	private static FiniteStateAutomaton loadBCTFSAFormat(File fsaFile) throws IOException, ClassNotFoundException {
		FSACodec decoder = FSA2FileFactory.getFSABctXml();
		return decoder.loadFSA(fsaFile);
	}

	private static FiniteStateAutomaton loadJFlapFormat(File fsaFile) throws IOException, ClassNotFoundException {
		FSACodec decoder = FSA2FileFactory.getFSAXml();
		return decoder.loadFSA(fsaFile);
	}

	private static FiniteStateAutomaton loadSerFormat(File fsaFile) throws IOException, ClassNotFoundException {
		FSACodec decoder = FSA2FileFactory.getFSASer();
		
		return decoder.loadFSA(fsaFile);
		
		
	}
}
