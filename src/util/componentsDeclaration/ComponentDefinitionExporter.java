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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;


public class ComponentDefinitionExporter {
	
	public static void export(List<Component> components, File file) throws IOException{
		BufferedWriter w = new BufferedWriter( new FileWriter(file));
		for ( Component component : components ){
			export(w,component);
		}
		w.close();
	}
	
	private static void export(Writer w, Component component) throws IOException{
		
		w.write("COMPONENT\t"+component.getName()+"\n");
		for ( MatchingRule rule : component.getRules() ){
			String inclusion;
			if ( rule instanceof MatchingRuleInclude ){
				inclusion = "INCLUDE";
			} else {
				inclusion = "EXCLUDE";
			}
			w.write(inclusion);
			w.write("\t");
			w.write(rule.getPackageExpr());
			w.write("\t");
			w.write(rule.getClassExpr());
			w.write("\t");
			w.write(rule.getMethodExpr());
			w.write("\n");
		}
		
	}
}
