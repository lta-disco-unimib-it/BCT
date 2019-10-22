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

import java.util.ArrayList;
import java.util.List;


public class Component implements SystemElement {
	
	protected List<MatchingRule> rules = new ArrayList<MatchingRule>();
	private String name;
	private SignatureParser signatureParser = new JavaSignatureParser();
	
	public SignatureParser getSignatureParser() {
		return signatureParser;
	}

	public void setSignatureParser(SignatureParser signatureParser) {
		this.signatureParser = signatureParser;
	}

	public Component( String name, List<MatchingRule> rules ){
		this( name );
		this.rules.addAll(rules);
	}

	public Component( String name ) {
		this.name = name;
	}
	
	public Component()
	{
		
	}
	

	
	public void addRule(MatchingRule rule) {
		rules.add(rule);
	}
	
	public boolean acceptClass( String packageName, String className ){
		System.out.println("Component.acceptClass : "+packageName+" "+className);
		for ( MatchingRule rule : rules ){
			if ( rule.acceptClass(packageName, className) ){
				return true;
			} else if ( rule.rejectClass(packageName, className) ) {
				return false;
			}
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public List<MatchingRule> getRules() {
		return rules;
	}

	public boolean acceptFunction(String packageName, String functionName) {
		return acceptMethod( packageName, "", functionName );
	}

	public boolean acceptMethod(String packageName, String className, String methodSignature) {
		for ( MatchingRule rule : rules ){
			if ( rule.acceptMethod(packageName, className, methodSignature) ){
				return true;
			} else if ( rule.rejectMethod(packageName, className, methodSignature ) ) {
				return false;
			}
		}
		return false;
	}
	
	public boolean acceptPackage(String packageName) {
		for (MatchingRule rule : rules) {
			if ( rule.acceptPackage(packageName) )
				return true;
		}
		return false;
	}
	
	public void setRules(List<MatchingRule> matchingRules) {
		rules = new ArrayList<MatchingRule>( matchingRules );
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns true if the method with the given complete signature is part of the component
	 * @param methodSignature
	 * @return
	 */
	public boolean acceptMethodSignature(String completeMethodSignature) {
		
		
		String className = signatureParser.getClassNameFromCompleteMethodSignature(completeMethodSignature);
		String packageName = signatureParser.getPackageNameFromCompleteMethodSignature(completeMethodSignature);
		String methodSignature = signatureParser.getMethodSignatureFromCompleteMethodSignature(completeMethodSignature);
		
		return acceptMethod(packageName, className, methodSignature);
	}
	
	/**
	 * Returns true if the method with the given bytecode signature is part of the component
	 * 
	 * We define the bytecode method signature as the method signature with return value information
	 * 
	 * eg:
	 * 
	 * package.MyClass.method((III)Ljava.lang.Object);
	 * 
	 * package.MyClass.method((int, int, int) java.lang.Object);
	 * 
	 * @param bytecodeMethodSignature
	 * @return
	 */
	public boolean acceptBytecodeMethodSignature(String bytecodeMethodSignature) {
		return acceptMethodSignature( 
				signatureParser.getCompleteMethodSignatureFromBytecodeMethodSignature(bytecodeMethodSignature)
				);
	}
	
	
	public String toString(){
		String s = "Component :"+name;
		for ( MatchingRule rule : rules){
			s = s+(rule.toString()+" ");
		}
		return s;
	}
}
