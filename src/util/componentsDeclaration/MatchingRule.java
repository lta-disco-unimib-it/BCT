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


public abstract class MatchingRule {
	private String packageExpr;
	private String classExpr;
	private String methodExpr;
	
	protected MatchingRule( ){
	
	}
	
	protected MatchingRule( String packageExpr, String classExpr, String methodExpr ){
		this.packageExpr = packageExpr;
		this.classExpr = classExpr;
		this.methodExpr = methodExpr;
	}

	/**
	 * Returns true if according to this rule the method must be accepted
	 * 
	 * @param packageName
	 * @param className
	 * @param methodName
	 */
	public abstract boolean acceptMethod( String packageName, String className, String methodName );
	
	
	/**
	 * Returns true if according to this rule the method must be rejected
	 * 
	 * @param packageName
	 * @param className
	 * @param methodName
	 */
	public abstract boolean rejectMethod( String packageName, String className, String methodName );
	
	/**
	 * Returns true if according to this rule the class must be accepted
	 * 
	 * @param packageName
	 * @param className
	 * @param modelName
	 */
	public abstract boolean acceptClass( String packageName, String className );
	
	/**
	 * Returns true if according to this rule the class must be rejected
	 * @param packageName
	 * @param className
	 * @return
	 */
	public abstract boolean rejectClass(String packageName, String className);
	
	/**
	 * Returns true if according to this rule the package must be accepted
	 * @param packageName
	 * @return
	 */
	public abstract boolean acceptPackage(String packageName);
	
	public String getClassExpr() {
		return classExpr;
	}

	public void setClassExpr(String classExpr) {
		this.classExpr = classExpr;
	}

	public String getMethodExpr() {
		return methodExpr;
	}

	public void setMethodExpr(String methodExpr) {
		this.methodExpr = methodExpr;
	}

	public String getPackageExpr() {
		return packageExpr;
	}

	public void setPackageExpr(String packageExpr) {
		this.packageExpr = packageExpr;
	}
	
	
	/**
	 * Returns wether a method is matched by the rule defined in this object
	 * 
	 * @param packageName
	 * @param className
	 * @param methodName
	 * @return
	 */
	protected boolean matchMethod( String packageName, String className, String methodName ){
		if ( ! matchClass(packageName,className))
			return false;
		
		if ( methodName == null ){
			methodName = "";
		}
		
		return methodName.matches(methodExpr);
	}

	
	/**
	 * Returns wether the rule defined in this object matches the passed class
	 * 
	 * @param packageName
	 * @param className
	 * @return
	 */
	protected boolean matchClass(String packageName, String className) {
		if ( ! matchPackage(packageName) )
			return false;
		
		if ( className == null ){
			className = "";
		}
		return className.matches(classExpr);
	}

	/**
	 * Returns true if the rule match a method
	 * @return
	 */
	public boolean isMethodRule(){
		return ( classExpr.length() > 0 );
	}
	
	/**
	 * Returns true if the rule match a function
	 * @return
	 */
	public boolean isFunctionRule(){
		return ( classExpr.length() == 0 );
	}

	/**
	 * Returns whether or not the rule matches the passed package name
	 * 
	 * @param packageName
	 * @return
	 */
	protected boolean matchPackage(String packageName) {
		if ( packageName == null ){
			packageName = "";
		}
		return packageName.matches(packageExpr);
	}

	public String toString(){
		return "["+this.getClass().getName()+":"+packageExpr+","+classExpr+","+methodExpr+"]";
	}
	
		//java.util.ArrayList<[[util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMapC2Ev], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMapC1Ev], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap9getSalaryESs], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap8isWorkerESs], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE]]>
		//java.util.ArrayList<[[util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMapC2Ev], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMapC1Ev], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap9getSalaryESs], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap8isWorkerESs], [util.componentsDeclaration.MatchingRuleInclude:.*,.*,_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE]]>
	



	@Override
	public boolean equals(Object obj) {
		
		if ( obj == null ){
			return false;
		}
		
		if ( ! ( obj instanceof MatchingRule ) ){
			return false;
		}
		
		return toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	
}
