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
package modelsViolations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelsViolations.BctIOModelViolation.Position;


/**
 * This class represent the violation of a model inferred and monitored by BCT
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BctIOModelViolation extends BctModelViolation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public void setViolatedMethod(String violatedMethod) {
		this.violatedMethod = violatedMethod;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public enum Position {ENTER,EXIT};
	private String parameters;
	private String violatedMethod;
	private Position position;

	public BctIOModelViolation(){
		
	}
	
	/**
	 * Renoves data about parameters, to use only to save memory...
	 */
	public void deleteParameters(){
		parameters = "";
	}
	
	public BctIOModelViolation(String id, String violatedModel,
			String violation, String violationType, long creationTime,
			String[] currentActions, String[] currentTests,
			String[] stackTrace, String pid, String threadId,String parameters) {
		super(id, violatedModel, violation, violationType, creationTime,
				currentActions, currentTests, stackTrace, pid, threadId);
		this.parameters = parameters;
	}

	/**
	 * Return a string with the state of the parameters as flattened by the object flattener
	 * 
	 * @return
	 */
	public String getParameters() {
		return parameters;
	}

	public HashMap<String, String> getParametersMap() {
		HashMap<String,String> map = new HashMap<String, String>();
		
		
		int i = 0;
		String pName = null;
		BufferedReader reader = new BufferedReader( new StringReader(parameters) );
		String line;
		try {
			while ( ( line = reader.readLine() ) != null ){
//				System.out.println("PLINE "+line);
				if ( ++i % 3 == 2 ){
//					System.out.println("PUT "+pName+" "+line);
					map.put(pName,line);
				}
				pName = line; 
			}
		} catch (IOException e) {
			
		}
		return map;
	}
	
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public BctIOModelViolation(String id, String violatedModel,
			Position position, String expression, long creationTime, String[] currentActions,
			String[] currentTests, String[] stackTrace, String pid,
			String threadId, String parameters) {
		this(id, violatedModel+":::"+position.toString(), expression, "NotValid", creationTime, currentActions, currentTests,
				stackTrace, pid, threadId,parameters);
	}
	
	public Position getPosition() {
		if ( position == null ){
			String methodAndPosition[] = extractMethodAndPositionFromViolation();
			position=Position.valueOf(methodAndPosition[1]);
		}
		return position;
	}

	public String getViolatedMethod(){
		if ( violatedMethod == null ){
			String methodAndPosition[] = extractMethodAndPositionFromViolation();
			violatedMethod=methodAndPosition[0];
		}
		return violatedMethod;
	}

	private String[] extractMethodAndPositionFromViolation() {

		String violation = getViolatedModel();
		int separatorPos = violation.lastIndexOf(":::");
		String method = violation.substring(0, separatorPos );
		String positionName = violation.substring(separatorPos+3);
		
		return new String[]{method,positionName};
	}

	@Override
	public ViolatedModelsTypes getViolatedModelType() {
		return BctModelViolation.ViolatedModelsTypes.IO;
	}

	public boolean equals(Object o){
		if ( ! ( o instanceof BctIOModelViolation ) ){
			return false;
		}
		
		BctIOModelViolation rhs = (BctIOModelViolation) o; 
		if ( ! super.equals(o)){
			return false;
		}
	
		return parameters.equals(rhs.parameters);
	}

	@Override
	public String getDescriptiveKey() {
		return getViolatedModel()+" : " +getViolation();
	}

	
}
