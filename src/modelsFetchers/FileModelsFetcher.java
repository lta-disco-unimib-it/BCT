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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.FileIndexAppend;
import util.FileUtil;
import util.FileIndex.FileIndexException;
import automata.fsa.FiniteStateAutomaton;
import conf.ModelsFetcherSettings;

/**
 * This class returns models saved as files on the fileSystem.
 * 
 * Models are saved with the same name of the method they represent.
 * 
 * TODO: change the association between fileName-methodName, we need shorter names.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileModelsFetcher implements ModelsFetcher {
	//path to the models directories 
	private File ioModelsDir;
	private File interactionModelsDir;
	//private FilenameFilter ioModelsFilterEnter = new BCTFileFilter( "in.txt" );
	//private FilenameFilter ioModelsFilterExit = new BCTFileFilter( "out.txt" );
	private FileIndexAppend 	interactionIndex;
	private FileIndexAppend 	ioIndexEnter;
	private FileIndexAppend 	ioIndexExit;
	
	private HashMap<String,ArrayList<String>> ioCollections = new HashMap<String,ArrayList<String>>();
	private String interactionModelsFileExtension = "ser";

	public static class Options {
		public static final String modelsDir = "modelsDir";
		public static String exportFormat = "exportFormat";
	}
	
	public FileModelsFetcher(){
		
	}
	
	public FileModelsFetcher ( File ioModelsDir, File interactionModelsDir ){
		this.ioModelsDir = ioModelsDir;
		this.interactionModelsDir = interactionModelsDir;
		setupIndexesAndModelsDirs();
	}
	
	
	/**
	 * Initialization method, ModelsFetcherSettings must contain a "modelsDir" field. It indicates the directory containing the 
	 * 04-invariants BCT directory with ioInvariants and interctionInvariants.
	 * 
	 */
	public void init ( ModelsFetcherSettings options ){
		ioModelsDir = new File ( (String)options.getProperty(Options.modelsDir)+File.separator+"ioInvariants" ); 
		interactionModelsDir = new File ( (String)options.getProperty(Options.modelsDir)+File.separator+"interactionInvariants" ) ;
		String exportFormat = (String)options.getProperty(Options.exportFormat );
		if ( exportFormat != null ){
			interactionModelsFileExtension = exportFormat;
		}
		
		setupIndexesAndModelsDirs();
	}
	
	
	/**
	 * This method creates the directories for IO and Interaction models (it creates the complete path if it does not exists), and setup the file indexes
	 * @param ioModelsDir
	 * @param interactionModelsDir
	 */
	private void setupIndexesAndModelsDirs() {
		ioModelsDir.mkdirs();
		interactionModelsDir.mkdirs();
		ioIndexEnter = new FileIndexAppend( new File(ioModelsDir, "ioModelsEnter.idx" ), ".io.enter" );
		ioIndexExit = new FileIndexAppend( new File(ioModelsDir, "ioModelsExit.idx" ), ".io.exit" );
		interactionIndex = new FileIndexAppend( new File(interactionModelsDir, "interactionModels.idx" ), "."+interactionModelsFileExtension );
	}
	
	
	/*
	 * Return the FSA associated to the method passed as input.
	 * The model is searched in <modelsDir>/interactionInvariants/.
	 * Usually the model has the name of the method.
	 * 
	 * @see modelsFetchers.ModelsFetcher#getInteractionModel(java.lang.String)
	 */
	public synchronized FiniteStateAutomaton getInteractionModel(String methodSignature) throws ModelsFetcherException {

		File fsaFile = getInteractionModelFile(methodSignature);
		
		try {
			FiniteStateAutomaton fsa = FSAFileManager.loadFSA(fsaFile);
			return fsa;
		} catch ( Exception e) {
			throw new ModelsFetcherException("No model");
		} 
		
		 
	}

	protected File getIoModelEnterFile(String methodSignature) throws ModelsFetcherException{
		String file;
		if ( ! ioIndexEnter.containsName(methodSignature) ){
			try {
				file = ioIndexEnter.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e.getMessage(),e);
			}
		} else {
			try {
				file = ioIndexEnter.getId( methodSignature );
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e.getMessage(),e);
			}
		}
		return new File( ioModelsDir, file );
	}
	
	protected File getIoModelExitFile(String methodSignature) throws ModelsFetcherException{
		String file;
		if ( ! ioIndexExit.containsName(methodSignature) ){
			try {
				file = ioIndexExit.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e.getMessage(),e);
			}
		} else {
			try {
				file = ioIndexExit.getId( methodSignature );
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e);
			}
		}
		return new File( ioModelsDir, file );
	}
	
	protected File getInteractionModelFile(String methodSignature) throws ModelsFetcherException{
		
		String file;
		if ( ! interactionIndex.containsName(methodSignature) )
			try {
				file = interactionIndex.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e);
			}
		else
			try {
				file = interactionIndex.getId(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e.getMessage());
			}
		return new File( interactionModelsDir, file );
	}
	
	/**
	 * This method returns a Collection of all io expressions found
	 * 
	 * TODO: add a cache of models previously read
	 * 
	 * @param fileName	the name of the file we want to retrieve.
	 * @return the right IoExpressionCollection
	 */
	protected synchronized ArrayList<String> getIoCollection(File fileToOpen) throws ModelsFetcherException{
		if ( ioCollections.containsKey(fileToOpen.getAbsolutePath() ) )
			return ioCollections.get(fileToOpen.getAbsolutePath() );
		try {
			

			
			BufferedReader in = getBufferedReader(fileToOpen);
			ArrayList<String> ioEC = new ArrayList<String>(); 
			String expression = null;
			do {
				expression = in.readLine();
				
				if ( isValid  ( expression ) )
					ioEC.add(expression);
			} while	(expression != null);
			
			in.close();
			ioCollections.put(fileToOpen.getAbsolutePath(), ioEC);
			return ioEC;
			
		} catch (IOException e) {
			throw new ModelsFetcherException("Problem loading IO models from "+fileToOpen,e);
		}

	}
	
	protected BufferedReader getBufferedReader( File fileToOpen ) throws FileNotFoundException{
		return new BufferedReader(new FileReader(fileToOpen));
	}
	
	/**
	 * Chek if a string is a right expresion. Basically it checks if it is real expression or a white line. 
	 * 
	 * @param expression	the expression string to be checked
	 * @return			
	 */
	private static boolean isValid(String expression) {
		if ( expression == null )
			return false;
		
		StringBuffer cleanString = new StringBuffer();
		for (int i = 0; i < expression.length(); i++)
			if (Character.isWhitespace(expression.charAt(i)))
				continue;
			else
				cleanString = cleanString.append(expression.charAt(i));

		if (cleanString.toString().equals(""))
			return false;
		else
			return true;
	}
	
	public void addSerializedIoModel(String methodName, File ioModelFile ) throws ModelsFetcherException {
		File booleanExpressionsFileName = getIoModelEnterFile(methodName);
		File dest = new File( booleanExpressionsFileName.getParent(), booleanExpressionsFileName.getName()+".inv.gz" );
		
		boolean moved = ioModelFile.renameTo(dest);
		
		if ( ! moved ){
			throw new ModelsFetcherException("Cannot save "+ioModelFile.getAbsolutePath()+" as "+dest.getAbsolutePath());
		}
	}

	public void addIoModel(String methodName, IoModel model) throws ModelsFetcherException {
		try {
			BufferedWriter br = new BufferedWriter(
					new FileWriter( getIoModelEnterFile(methodName) )
					);
			Iterator it = model.preconditionsIt();
			while ( it.hasNext() ){
				br.write((String)it.next());
				br.newLine();
			}
			br.close();
			
			br = new BufferedWriter(
					new FileWriter( getIoModelExitFile(methodName) )
					);
			it = model.postconditionsIt();
			while ( it.hasNext() ){
				br.write((String)it.next());
				br.newLine();
			}
			
			br.close();

			
			System.out.println("!!!MODEL added "+getIoModelEnterFile(methodName));
			//TODO: why the code below was added???
//			try {
//				Thread.sleep(30000);
//			} catch (InterruptedException e) {
//				 TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModelsFetcherException("Cannot save IoModel for "+methodName,e);
		}
	}

	public void addInteractionModel(String methodName, FiniteStateAutomaton fsa) throws ModelsFetcherException {

		try {
			FSAFileManager.saveFSA(getInteractionModelFile(methodName), fsa);
		} catch (FileNotFoundException e) {
			throw new ModelsFetcherException("Cannot save model to file ",e);
		} catch (IOException e) {
			throw new ModelsFetcherException("Cannot save model to file ",e);
		}
		
		
	}

	public IoModelIterator getIoModelIteratorEnter(String methodSignature) throws ModelsFetcherException {
		return new CollectionIoModelIterator ( getIoCollection( getIoModelEnterFile(methodSignature)) );
	}

	public IoModelIterator getIoModelIteratorExit(String methodSignature) throws ModelsFetcherException {
		return new CollectionIoModelIterator ( getIoCollection(getIoModelExitFile(methodSignature)) );
	}

	public Set<String> getIoModelsNames() {
		return  ioIndexEnter.getNames();
	}
	
	public Set<String> getInteractionModelsNames() {
		return  interactionIndex.getNames();
	}

	public boolean interactionModelExist(String methodSignature) {
		return interactionIndex.containsName(methodSignature);
	}

	public boolean ioModelEnterExist(String methodSignature) {
		try {
			return getIoModelEnterFile(methodSignature).exists();
		} catch (ModelsFetcherException e) {
			return false;
		}
	}
	
	public boolean ioModelExitExist(String methodSignature) {
		try {
			return getIoModelExitFile(methodSignature).exists();
		} catch (ModelsFetcherException e) {
			return false;
		}
	}

	
	public void addInteractionModel(int idMethod, FiniteStateAutomaton fsa) throws ModelsFetcherException {
		throw new UnsupportedOperationException("Not supported operation when saving on filesystem");
	}

	@Override
	public List getSerializedIoModelsEnter(String methodSignature) throws ModelsFetcherException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getSerializedIoModelsExit(String methodSignature)  throws ModelsFetcherException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateIoModelsEnter(String methodName, IoModelIterator it) {
		try {
			BufferedWriter br = new BufferedWriter(
					new FileWriter( getIoModelEnterFile(methodName) )
					);
			while ( it.hasNext() ){
				br.write((String)it.next());
				br.newLine();
			}
			br.close();
		} catch ( IOException e ){
		} catch (ModelsFetcherException e) {
		}
		
	}

	@Override
	public void updateIoModelsExit(String methodName, IoModelIterator it) {
		try {
			BufferedWriter br = new BufferedWriter(
					new FileWriter( getIoModelExitFile(methodName) )
					);
			while ( it.hasNext() ){
				br.write((String)it.next());
				br.newLine();
			}
			br.close();
		} catch ( IOException e ){
		} catch (ModelsFetcherException e) {
		}
	}
}
