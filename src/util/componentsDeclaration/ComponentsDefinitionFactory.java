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



import java.io.File;
import java.util.List;

/**
 * This class reads a component definition file and returns a list of components defined in the file.
 *  
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ComponentsDefinitionFactory {

	public static List<Component> getComponents(File file) throws ComponentsDefinitionException {
		return ComponentDefinitionImporter.getComponents(file);
	}

	
	
}
