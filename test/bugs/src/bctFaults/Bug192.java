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
package bctFaults;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.Environment;
import util.componentsDeclaration.MatchingRule;
import util.componentsDeclaration.MatchingRuleInclude;

public class Bug192 {

	@Test
	public void testBug(){
		String signature = ".WorkersMap_cpp._ZN10WorkersMap9getSalaryESs()";
		
		MatchingRuleInclude rule = new MatchingRuleInclude(".*",".*","_ZN10WorkersMap9getSalaryESs\\(\\)");

		assertTrue ( rule.acceptMethod("", "WorkersMap_cpp", "_ZN10WorkersMap9getSalaryESs()") );
		
		List<MatchingRule> rules = new ArrayList<MatchingRule>();
		rules.add(rule);
		
		Component c = new Component("C", rules);
		
		
		
		List<Component> components = new ArrayList<Component>();
		components.add(c);
		Environment envComponent = new Environment("Environment", components);
		
		String envSignature = ".WorkersMap_cpp._ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE()";
		
		//This is for bug 193
		String signatureNonParenthesis = ".._ZNSsD1Ev@plt";
		
		assertTrue ( c.acceptBytecodeMethodSignature(signature) );
		assertFalse ( c.acceptBytecodeMethodSignature(envSignature) );
		
		assertFalse ( envComponent.acceptBytecodeMethodSignature(signature) );
		assertTrue ( envComponent.acceptBytecodeMethodSignature(envSignature) );
		
		assertTrue ( envComponent.acceptBytecodeMethodSignature(signatureNonParenthesis) );
	}
}
