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
package util.componentsDeclaration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages import of components defined in files
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ComponentDefinitionImporter {
	
	public static List<Component> getComponents(File file) throws ComponentsDefinitionException {
		return getComponents(file, null);
	}
	
	public static List<Component> getComponents(File file, SignatureParser signatureParser) throws ComponentsDefinitionException {
		InputStream is;
		try {
			is = new FileInputStream ( file );

			InputStreamReader reader = new InputStreamReader ( is );

			BufferedReader br = new BufferedReader(reader);

			String line;
			Component currentComponent = null;
			String componentName = null;


			List<Component> components = new ArrayList<Component>();

			while ( ( line = br.readLine() ) != null ){
				if ( line.startsWith("COMPONENT")){
					String[] els = line.split("\t");
					componentName = els[1];
					currentComponent = new Component(componentName);
					if ( signatureParser != null ){
						currentComponent.setSignatureParser(signatureParser);
					}
					components.add(currentComponent);
				} else if ( line.startsWith("INCLUDE") || line.startsWith("EXCLUDE")){
					if ( currentComponent == null )
						throw new ComponentsDefinitionException("Missing COMPONENT");
					MatchingRule rule = getRule( line );
					currentComponent.addRule(rule);
				} else if ( line.startsWith("#")) {
					//is a comment
				} else {
					throw new ComponentsDefinitionException("Wrong definition "+line);
				}
			}


			br.close();


			return components;
		} catch (FileNotFoundException e) {
			throw new ComponentsDefinitionException(e.getMessage());
		} catch (IOException e) {
			throw new ComponentsDefinitionException(e.getMessage());
		}
	}



	private static MatchingRule getRule(String line) throws ComponentsDefinitionException {
		String[] els = line.split("\t");
		if ( els.length != 4 )
			throw new ComponentsDefinitionException("Wrong definition "+line);
		if ( els[0].equals("INCLUDE") ){
			return new MatchingRuleInclude(els[1],els[2],els[3]);
		} else if ( els[0].equals("EXCLUDE") ) {
			return new MatchingRuleExclude(els[1],els[2],els[3]);
		} else {
			throw new ComponentsDefinitionException("Wrong definition "+line);
		}
		
	}
}
