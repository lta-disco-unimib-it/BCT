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

import java.util.ArrayList;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.MatchingRule;
import util.componentsDeclaration.MatchingRuleExclude;
import util.componentsDeclaration.MatchingRuleInclude;
import junit.framework.TestCase;

public class Bug143Test extends TestCase {
	
	public void test1Bug(){
	
		ArrayList<MatchingRule> rules = new ArrayList<MatchingRule>();
		
		MatchingRule rule;
		
		rule = new MatchingRuleExclude("org.apache.catalina.core.*","StandardContext","b.*");
		rules.add(rule);
		
		rule = new MatchingRuleInclude("org.apache.catalina.core.*","StandardContext",".*");
		rules.add(rule);
		
		rule = new MatchingRuleInclude("org.apache.catalina.*",".*",".*");
		rules.add(rule);
		
		Component c = new Component("Catalina", rules);
		
		assertTrue(c.acceptMethodSignature("org.apache.catalina.connector.Connector.setProperty((Ljava.lang.String)V)"));
		
	}
	
	public void test2Bug(){
		
		ArrayList<MatchingRule> rules = new ArrayList<MatchingRule>();
		
		MatchingRule rule;
		
		rule = new MatchingRuleExclude("org.apache.catalina.core.*","StandardContext","b.*");
		rules.add(rule);
		
		rule = new MatchingRuleInclude("org.apache.catalina.*",".*",".*");
		rules.add(rule);
		
		Component c = new Component("Catalina", rules);
		
		assertTrue(c.acceptMethodSignature("org.apache.catalina.connector.Connector.setProperty((Ljava.lang.String)V)"));
		
	}

}
