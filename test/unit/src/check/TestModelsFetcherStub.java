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
package check;


import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import modelsFetchers.IoModel;
import modelsFetchers.IoModelIterator;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;

import org.junit.After;
import org.junit.Before;

import automata.State;
import automata.Transition;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;
import conf.ModelsFetcherSettings;
import daikon.inv.Invariant;

public class TestModelsFetcherStub implements ModelsFetcher {
	
	public static final String SIMPLE_MAIN_SIGNATURE= "Simple.main()";
	
	private HashMap<String,FiniteStateAutomaton> models = new HashMap<String, FiniteStateAutomaton>(); 
	
	public TestModelsFetcherStub(){
		createSimpleAutomata();
		
		
	}
	
	private FiniteStateAutomaton createEmptyAutomata() {
		FiniteStateAutomaton emptyFsa = new FiniteStateAutomaton();
		State initialState = emptyFsa.createState(new Point(0,0));
		emptyFsa.setInitialState(initialState);
		emptyFsa.addFinalState(initialState);
		return emptyFsa;
	}

	public void createSimpleAutomata(){
		FiniteStateAutomaton fsa = new FiniteStateAutomaton();
		
		
		State previous = fsa.createState(new Point(0,0));
		fsa.setInitialState(previous);
		
		for ( int i = 1; i < 100; i++ ){
			State state = fsa.createState(new Point(0,0));
			String method = "Simple.method"+i+"()";
			FSATransition t = new FSATransition(previous,state,method);
			fsa.addTransition(t);
			
			
			
			models.put(method, createEmptyAutomata());
			
			previous = state;
		}
		
		System.out.println("TOTAL: "+fsa.getTransitions().length);
		
		fsa.addFinalState(previous);
		
		models.put(SIMPLE_MAIN_SIGNATURE,fsa);
	}

	public void addInteractionModel(String methodName, FiniteStateAutomaton fsa)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void addInteractionModel(int method, FiniteStateAutomaton fsa)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void addIoModel(String methodName, IoModel model)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public FiniteStateAutomaton getInteractionModel(String methodSignature)
			throws ModelsFetcherException {
		
		FiniteStateAutomaton fsa = models.get(methodSignature);
		if ( fsa == null ){
			fsa = createEmptyAutomata();
		}
		
		return fsa;
	}

	public IoModelIterator getIoModelIteratorEnter(String methodSignature)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public IoModelIterator getIoModelIteratorExit(String methodSignature)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public Set<String> getIoModelsNames() {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void init(ModelsFetcherSettings mfs) {
		
	}

	public boolean interactionModelExist(String methodSignature) {
		return true;
	}

	public boolean ioModelEnterExist(String methodSignature) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public boolean ioModelExitExist(String methodSignature) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	@Override
	public List getSerializedIoModelsEnter(String methodSignature)
			throws ModelsFetcherException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Invariant> getSerializedIoModelsExit(String methodSignature)
			throws ModelsFetcherException {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateIoModelsExit(String methodSignature, IoModelIterator it) {
		// TODO Auto-generated method stub
		
	}





}
