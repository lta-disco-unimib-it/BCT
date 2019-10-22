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

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.MatchingRule;
import util.componentsDeclaration.MatchingRuleInclude;

public class Bug142 extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBug142(){
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("main",".*",".*");
		mrList.add(mr);
		
		c.addRule(mr);
		
		String signature = "main.Riunione.main(([Ljava.lang.String;)V)";
		
		//acceptMethodSignature is not the right method to call but the defined matching rule should permit the component to accept it
		assertTrue ( c.acceptMethodSignature(signature) );
		
		
		assertTrue ( c.acceptBytecodeMethodSignature(signature) );
	}
}
