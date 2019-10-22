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
package traceReaders.normalized.traceCreation;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import util.componentsDeclaration.Component;
import util.componentsDeclaration.ComponentDefinitionImporter;
import util.componentsDeclaration.ComponentsDefinitionException;
import conf.InvariantGeneratorSettings;

/**
 * Creates traces that describe how a component is used, but has several limitations:
 * *) does not correctly manage multiple threads
 * *) does not correctly manage the case in which multiple object of the same type are used
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ComponentUsageTraceMaintainer implements TraceMaintainer {
	
	private Component defaultComponent = new Component();
	private HashMap<Component,List<Token>> traces = new HashMap<Component,List<Token>>();
	
	public interface ConfigurationOptions{
		public static final String componentsDefinitionFile="traceMaintainer.componentsDefinitionFile";
	}

	private List<Component> components;
	private NormalizedInteractionTraceHandler handler;
	
	
	
	public void addSymbol(Token symbol) {
	
	}
	
	public ComponentUsageTraceMaintainer(){
		
	}
	
	public ComponentUsageTraceMaintainer(Collection<Component> components){
		this.components = new ArrayList<Component>(components.size());
		this.components.addAll(components);
	}
	

	private List<Token> getComponentTrace(Component component) {
		List<Token> trace = traces.get(component);
		if ( trace == null ){
			trace = new LinkedList<Token>();
			traces.put(component,trace);
		}
		return trace;
	}

	private Component getOwningComponent(String methodSignature) {
		for ( Component component : components ){
			if ( component.acceptMethodSignature(methodSignature) ){
				return component;
			}
		}
		return defaultComponent;
	}

	public void init(InvariantGeneratorSettings invariantGeneratorSettings, NormalizedInteractionTraceHandler handler) throws ComponentsDefinitionException {
		String file = invariantGeneratorSettings.getProperty(ConfigurationOptions.componentsDefinitionFile);
		components = ComponentDefinitionImporter.getComponents(new File(file));
		this.handler = handler;
	}

	public void analysisEnd() {
		for ( Entry<Component,List<Token>> entry : traces.entrySet() ){
			Component component = entry.getKey();
			List<Token> trace = entry.getValue();
			try {
				handler.addInteractionTrace(component.getName(), trace, null);
			} catch (NormalizedTraceHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void programPointBegin(InteractionTrace rawTrace,
			Token programPointToken) {
		String methodSignature = programPointToken.getMethodSignature();
		
		Component component = getOwningComponent(methodSignature);
	
		//runningComponents.add(component);
		List<Token> trace = getComponentTrace(component);
		trace.add(programPointToken);
		
		//runningComponents.push(component);
	}

	public void programPointEnd(InteractionTrace rawTrace,
			Token programPointToken) {
		// TODO Auto-generated method stub
		
	}

	public void analysisBegin() {
		
	}

	public void sessionBegin(String sessionId) {
		
	}

	public void sessionEnd() {
		
	}

	public void traceBegin(InteractionTrace trace) {
		
	}

	public void traceEnd() {
		
	}

	@Override
	public void programPointGeneric(InteractionTrace trace, Token token) {
		throw new RuntimeException("Not implemented");
	}

}
