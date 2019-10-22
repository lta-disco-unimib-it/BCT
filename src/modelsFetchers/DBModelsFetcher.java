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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;
import automata.fsa.FiniteStateAutomaton;
import conf.ModelsFetcherSettings;
import database.DataLayerException;
import database.Fsa;
import flattener.writers.DBWriter;

/**
 * This class returns models saved on DB.
 * 
 * Models are saved with the same name of the method they represent.
 * 
 * FIXME: controllare metodi per il check
 */
public class DBModelsFetcher implements ModelsFetcher {
	//path to the models directories 
	private File ioModelsDir;
	private File interactionModelsDir;
	//private FilenameFilter ioModelsFilterEnter = new BCTFileFilter( "in.txt" );
	//private FilenameFilter ioModelsFilterExit = new BCTFileFilter( "out.txt" );
	private FileIndexAppend 	interactionIndex;
	private FileIndexAppend 	ioIndexEnter;
	private FileIndexAppend 	ioIndexExit;
	
	/**
	 * Initialization method, ModelsFetcherSettings must contain a "modelsDir" field. It indicates the directory containing the 
	 * 04-invariants BCT directory with ioInvariants and interctionInvariants.
	 * 
	 */
	public void init ( ModelsFetcherSettings options ){
		ioModelsDir = new File ( (String)options.getProperty("modelsDir")+File.separator+"ioInvariants" ); 
		interactionModelsDir = new File ( (String)options.getProperty("modelsDir")+File.separator+"interactionInvariants" ) ;
		ioModelsDir.mkdirs();
		interactionModelsDir.mkdirs();
		ioIndexEnter = new FileIndexAppend( new File(ioModelsDir, "ioModelsEnter.idx" ), ".io.enter" );
		ioIndexExit = new FileIndexAppend( new File(ioModelsDir, "ioModelsExit.idx" ), ".io.exit" );
		interactionIndex = new FileIndexAppend( new File(interactionModelsDir, "interactionModels.idx" ), ".ser" );
	}
	
	/*
	 * Return the FSA associated to the method passed as input.
	 * The model is searched in <modelsDir>/interactionInvariants/.
	 * Usually the model has the name of the method.
	 * 
	 * @see modelsFetchers.ModelsFetcher#getInteractionModel(java.lang.String)
	 */
	public FiniteStateAutomaton getInteractionModel(String methodSignature) throws ModelsFetcherException {
		
		File fsaFile = getInteractionModelFile(methodSignature);
		ObjectInputStream in = null;
		
		try {
			FiniteStateAutomaton fsa ;
			in = new ObjectInputStream(new BufferedInputStream(	new FileInputStream( fsaFile )));
			fsa = (FiniteStateAutomaton) in.readObject();
			in.close();
			return fsa;
		} catch ( Exception e) {
			throw new ModelsFetcherException("No model");
		} 
		
		 
	}
	///////////////////////////////////
	private File getIoModelEnterFile(String methodSignature) throws ModelsFetcherException {
		String file = null;
		try {
			file = ioIndexEnter.getId( methodSignature );
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( file == null ){
			try {
				file = ioIndexEnter.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e);
			}
		}
		return new File( ioModelsDir, file );
	}
	///////////////////////////////
	//////////////////////////////////
	private File getIoModelExitFile(String methodSignature) throws ModelsFetcherException{
		String file = null;
		try {
			file = ioIndexExit.getId( methodSignature );
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( file == null ){
			try {
				file = ioIndexExit.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e);
			}
		}
		return new File( ioModelsDir, file );
	}
	////////////////////////////////////////////
	///////////////////////////////////////
	private File getInteractionModelFile(String methodSignature) throws ModelsFetcherException{
		String file = null;
		try {
			file = interactionIndex.getId(methodSignature);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( file == null )
			try {
				file = interactionIndex.add(methodSignature);
			} catch (FileIndexException e) {
				throw new ModelsFetcherException(e);
			}
		
		return new File( interactionModelsDir, file );
	}
	///////////////////////////////////////////
	/**
	 * This method returns a Collection of all io expressions found
	 * 
	 * TODO: add a cache of models previously read
	 * 
	 * @param fileName	the name of the file we want to retrieve.
	 * @return the right IoExpressionCollection
	 */
	//////////////////////////////////////////////
	private ArrayList<String> getIoCollection(File fileToOpen) throws ModelsFetcherException{
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileToOpen));
			ArrayList<String> ioEC = new ArrayList<String>(); 
			String expression = null;
			do {
				expression = in.readLine();
				if ( isValid  ( expression ) )
					ioEC.add(expression);
			} while	(expression != null);
			
			in.close();
			return ioEC;
			
		} catch (IOException e) {
			throw new ModelsFetcherException("Problem loading IO models from "+fileToOpen);
		}

	}
	/////////////////////////////////////////
	/**
	 * Chek if a string is a right expresion. Basically it checks if it is real expression or a white line. 
	 * 
	 * @param expression	the expression string to be checked
	 * @return			
	 */
	//////////////////////////////////////
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
	////////////////////////////////////////////
	public void addIoModel(String methodName, IoModel model) throws ModelsFetcherException {
		try {
			String dbTable = "Invariants";
			DBWriter writer = new DBWriter(methodName, dbTable);

			Iterator it = model.preconditionsIt();
			while ( it.hasNext() ){
				writer.write((String)it.next());
				writer.write("\n");
			}
			//these lines need to separate preconditions from postcondiction in the DB 
			writer.write("====");
			writer.write("\n");
			
			it = model.postconditionsIt();
			while ( it.hasNext() ){
				writer.write((String)it.next());
				writer.write("\n");
			}
			writer.close();
			
		} 
		catch (IOException e) {
			throw new ModelsFetcherException("Cannot save IoModel for " + methodName);
		}
	}

	public void addInteractionModel(String methodName, FiniteStateAutomaton fsa) throws ModelsFetcherException {

		
		try {
			
			Fsa.insert(methodName, fsa);

		
		} catch (DataLayerException e) {
			throw new ModelsFetcherException("Cannot save fsa for "+methodName);
		}
		
	}

	public IoModelIterator getIoModelIteratorEnter(String methodSignature) throws ModelsFetcherException {
		return new CollectionIoModelIterator ( getIoCollection( getIoModelEnterFile(methodSignature)) );
	}

	public IoModelIterator getIoModelIteratorExit(String methodSignature) throws ModelsFetcherException {
		return new CollectionIoModelIterator ( getIoCollection(getIoModelExitFile(methodSignature)) ) ;
	}

	public Set<String> getIoModelsNames() {
		return  ioIndexEnter.getNames();
	}

	public boolean interactionModelExist(String methodSignature) throws ModelsFetcherException {
		return getInteractionModelFile(methodSignature).exists();
		
	}

	public boolean ioModelEnterExist(String methodSignature) throws ModelsFetcherException {
		return getIoModelEnterFile(methodSignature).exists();
	}
	
	public boolean ioModelExitExist(String methodSignature) throws ModelsFetcherException {
		return getIoModelExitFile(methodSignature).exists();
	}




	public void addInteractionModel(int method, FiniteStateAutomaton fsa)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List getSerializedIoModelsEnter(String methodSignature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getSerializedIoModelsExit(String methodSignature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSerializedIoModel(String methodName, File ioModelFile)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateIoModelsEnter(String methodSignature, IoModelIterator it) {

	}

	@Override
	public void updateIoModelsExit(String methodSignature, IoModelIterator it) {

	}
}
