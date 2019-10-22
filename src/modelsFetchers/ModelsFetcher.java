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
import java.util.List;
import java.util.Set;

import automata.fsa.FiniteStateAutomaton;
import conf.ModelsFetcherSettings;


/**
 * 
 * Interface for all the fetchers of models for methods
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public interface ModelsFetcher {
	
	/**
	 * Return an FSA given the method name.
	 * If an FSA for the method does not exists or if there is a problem during read throws a ModelsFetcherException
	 * 
	 * @param methodSignature	the method for which we want the model
	 * @return	the FSA
	 * 
	 * @throws ModelsFetcherException
	 */
	public FiniteStateAutomaton getInteractionModel(String methodSignature) throws ModelsFetcherException;
	
	/**
	 * Returns if there is an Interaction model for the give method.
	 * 
	 * @param methodSignature
	 * @return
	 */
	public boolean interactionModelExist( String methodSignature ) throws ModelsFetcherException;
	
	/**
	 * Returns if there is an IO model for the give method.
	 * 
	 * @param methodSignature
	 * @return
	 * @throws ModelsFetcherException 
	 */
	public boolean ioModelEnterExist( String methodSignature ) throws ModelsFetcherException;
	
	/**
	 * Returns if there is an IO model for the give method.
	 * 
	 * @param methodSignature
	 * @return
	 */
	public boolean ioModelExitExist( String methodSignature ) throws ModelsFetcherException;
	
	/**
	 * Return an iterator over the expressions of a method model 
	 * If a model for the method does not exists or if there is a problem during read throws a ModelsFetcherException
	 * 
	 * @param methodSignature	the method for which we want the model
	 * @return	the ioExpressionCollection
	 * 
	 * @throws ModelsFetcherException
	 */
	public IoModelIterator getIoModelIteratorEnter(String methodSignature) throws ModelsFetcherException;
	
	/**
	 * Return an iterator over the expressions of a method model
	 * If a model for the method does not exists or if there is a problem during read throws a FileModelFetcherException
	 * 
	 * @param methodSignature	the method for which we want the model
	 * @return	the ioExpressionCollection
	 * 
	 * @throws ModelsFetcherException
	 */
	public IoModelIterator getIoModelIteratorExit(String methodSignature) throws ModelsFetcherException;


	/**
	 * Returns an ArrayList with the names of the methods for wich we have an IO model
	 *  
	 * @return
	 */
	public Set<String> getIoModelsNames() throws ModelsFetcherException;
	
	/**
	 * This method is common to all ModelsFetcher it is called by the ModelsFetcherFactory after istantiating the object.
	 * ModelsFetcherSettings is not passed to the constructor because Class.newInstance does not permit it 
	 * (and reflection costs too much).
	 * 
	 * @param mfs	ModelsFetcherSetting, settings of the models fetcher.
	 */
	public void init(ModelsFetcherSettings mfs) throws ModelsFetcherException;
	
	/**
	 * Add the IO model for the given method
	 * 
	 * @param methodName
	 * @param model
	 * @throws ModelsFetcherException
	 */
	public void addIoModel( String methodName, IoModel model )  throws ModelsFetcherException;
	
	/**
	 * Add the interaction model for the given method
	 * 
	 * @param methodName
	 * @param fsa
	 * @throws ModelsFetcherException
	 */
	public void addInteractionModel( String methodName, FiniteStateAutomaton fsa ) throws ModelsFetcherException;
	
	/**
	 * Add an interaction model to the ioRepository, used by GK-Tail
	 * 
	 * @param method
	 * @param output
	 */
	public void addInteractionModel(int method, FiniteStateAutomaton fsa) throws ModelsFetcherException;

	/**
	 * Return a list of objects representing the enter invariants for the given program point.
	 * If daikon is used returns a list of Invariants objects.
	 * 
	 * @param methodSignature
	 * @return
	 */
	public List getSerializedIoModelsEnter(String methodSignature)  throws ModelsFetcherException;

	/**
	 * Return a list of objects representing the exit invariants for the given program point.
	 * If daikon is used returns a list of Invariants objects.
	 * 
	 * @param methodSignature
	 * @return
	 * @throws ModelsFetcherException 
	 */
	public List getSerializedIoModelsExit(String methodSignature) throws ModelsFetcherException;
	
	/**
	 * Saves the serialized form of an IO model.
	 * 
	 * @param methodName
	 * @param ioModelFile the file that contains the serialized IO model
	 * @throws ModelsFetcherException
	 */
	public void addSerializedIoModel(String methodName, File ioModelFile ) throws ModelsFetcherException;

	/**
	 * Replace current IO models enter with the given models
	 * @param methodSignature
	 * @param it
	 */
	public void updateIoModelsEnter(String methodSignature, IoModelIterator it);
	
	/**
	 * Replace current IO models exit with the given models
	 * @param methodSignature
	 * @param it
	 */
	public void updateIoModelsExit(String methodSignature, IoModelIterator it);
}
