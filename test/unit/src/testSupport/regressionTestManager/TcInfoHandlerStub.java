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
package testSupport.regressionTestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.VariableInfo;
import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TcInfoEntity;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import conf.InvariantGeneratorSettings;

public class TcInfoHandlerStub implements TcInfoHandler {
	private HashMap<String,VariableInfo> variables= new HashMap<String,VariableInfo>();
	private HashMap<String,VarNameInfo> varNames= new HashMap<String,VarNameInfo>();
	private HashMap<String, ProgramPointInfo> programPoints = new HashMap<String, ProgramPointInfo>();
	
	private static final String ENTER = ":::ENTER";
	private static final String EXIT = ":::EXIT1";
	
	public static final String programPoint = "mypackage.MyClass.myMethod(I)";
	public static final String programPointEnter = programPoint+ENTER;
	public static final String programPointExit = programPoint+EXIT;
	
	public static final String var0Name = "parameter[0].num";
	public static final String var0EnterVal = "1";
	public static final String var0EnterId = "1";
	public static final String var0ExitVal = "2";
	public static final String var0ExitId = "2";
	
	public static final String var1Name = "parameter[0].name";
	public static final String var1EnterVal = "\"Pippo pippo\"";
	public static final String var1EnterId = "3";
	public static final String var1ExitVal = "\"Pippo\"";
	public static final String var1ExitId = "4";
	
	public static final String var2Name = "parameter[0].num";
	public static final String var2EnterVal = "7";
	public static final String var2EnterId = "5";
	public static final String var2ExitVal = "7";
	public static final String var2ExitId = "6";
	
	public static final String pp0Id = "0";
	private static ProgramPointInfo pp0 = null;
	
	public static final String pp1Id = "1";
	private static ProgramPointInfo pp1 = null;
	
	
	private class VarNameInfo{
		String programPoint;
		String name;
		String value;
		
		VarNameInfo( String programPoint, String name, String value ){
			this.programPoint = programPoint;
			this.name = name;
			this.value = value;
		}
		
		private String getFullName() {
			return programPoint+"#"+name+"#"+value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getProgramPoint() {
			return programPoint;
		}

		public void setProgramPoint(String programPoint) {
			this.programPoint = programPoint;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		public String toString(){
			return getFullName();
		}
		
	}
	
	public TcInfoHandlerStub(){
		
		addVariableEnter( var0EnterId, var0Name, var0EnterVal );
		addVariableExit( var0ExitId, var0Name, var0ExitVal );
		
		addVariableEnter( var1EnterId, var1Name, var1EnterVal );
		addVariableExit( var1ExitId, var1Name, var1ExitVal );

		addVariableEnter( var2EnterId, var2Name, var2EnterVal );
		addVariableExit( var2ExitId, var2Name, var2ExitVal );
		
		ArrayList pp0Variables = new ArrayList();
		pp0Variables.add( var0EnterId );
		pp0Variables.add( var0ExitId );
		pp0Variables.add( var1EnterId );
		pp0Variables.add( var1ExitId );
		
		pp0 = new ProgramPointInfo(this, pp0Id, pp0Variables );
		programPoints.put(pp0Id, pp0);
		
		ArrayList pp1Variables = new ArrayList();
		pp1Variables.add( var2EnterId );
		pp1Variables.add( var2ExitId );
		
		pp1 = new ProgramPointInfo(this, pp1Id, pp1Variables );
		programPoints.put(pp1Id, pp1);
	}
	
	
	/**
	 * Add an ener variable
	 * 
	 * @param id	Id of the variable
	 * @param name	Name of the variable, like parameter[0].name
	 * @param val	String representation of the value of teh variable
	 * 
	 */
	private void addVariableEnter( String id, String name, String val ) {
		variables.put( id, new VariableInfo(this, id ) );
		varNames.put( id, new VarNameInfo(programPointEnter, name, val)  );
	}
	
	/**
	 * Add an exit variable
	 * 
	 * @param id	Id of the variable
	 * @param name	Name of the variable, like parameter[0].name
	 * @param val	String representation of the value of teh variable
	 * 
	 */
	private void addVariableExit( String id, String name, String val ) {
		variables.put( id, new VariableInfo(this, id ) );
		varNames.put( id, new VarNameInfo(programPointExit, name, val)  );
	}



	public ProgramPointInfo createProgramPointInfo() {
		throw new NotImplementedException();
	}

	public MethodInfo getMethodInfo(String methodName) {
		throw new NotImplementedException();
	}

	public String getMethodName(String id) {
		throw new NotImplementedException();
	}

	public Set<String> getMethodsIds() {
		throw new NotImplementedException();
	}

	public Set<String> getProgramPointIds() {
		throw new NotImplementedException();
	}

	public ProgramPointInfo getProgramPointInfo(String string) {
		return programPoints.get(string);
	}

	public TestCaseInfo getTestCaseInfo(String testCaseName) {
		throw new NotImplementedException();
	}

	public TestCaseInfo getTestCaseInfoFromId(String id) {
		throw new NotImplementedException();
	}

	public Set<String> getTestCasesIds() {
		throw new NotImplementedException();
	}

	public VariableInfo getVariableFromId(String string) {
		return variables.get(string);
	}

	public Set<String> getVariableIds() {
		return variables.keySet();
	}

	public VariableInfo getVariableInfo(String programPointName, String name,
			String value) {
		throw new NotImplementedException();
	}

	public String getVariableName(String id) {
		return varNames.get(id).getName();
	}

	public String getVariableProgramPoint(String id) {
		return varNames.get(id).getProgramPoint();
	}

	public String getVariableValue(String id) {
		return varNames.get(id).getValue();
	}

	public void init(InvariantGeneratorSettings settings) {
	}

	public void save() throws TcInfoHandlerException {
	}

	public void saveTestCaseInfo(TestCaseInfo info){
	}

	public void setModified(TcInfoEntity tcInfoElement) {
	}



	public static ProgramPointInfo getPp0() {
		return pp0;
	}



	public static void setPp0(ProgramPointInfo pp0) {
		TcInfoHandlerStub.pp0 = pp0;
	}


	public static ProgramPointInfo getPp1() {
		return pp1;
	}


	public void init(RegressionTestManagerHandlerSettings settings) {
		// TODO Auto-generated method stub
		
	}

}
