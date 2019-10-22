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
package tools.violationsAnalyzer;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;


import org.eclipse.tptp.logging.events.cbe.FormattingException;

import tools.violationsAnalyzer.ViolationsUtil.VariableData;
import tools.violationsAnalyzer.ViolationsUtil.ViolationData;
import util.cbe.CBELogLoader;
import failureDetection.Failure;

public class CBEBctViolationsLogLoader {


	/**
	 * Load the contents of the log files passed;
	 * 
	 * @param cbeLogs logs of BCt execution in cbe format 
	 * @return
	 * @throws CBEBctViolationsLogLoaderException
	 */
	public BctViolationsLogData load(Collection<File> cbeLogs) throws CBEBctViolationsLogLoaderException{
		CBELogLoader loader = new CBELogLoader();
		
		BctViolationsLogData result = new BctViolationsLogData();

		for ( File file : cbeLogs ){


			Object[] entities;

			try {
				entities = loader.loadEntitiesFromCBEFile(file);

				for ( Object entity : entities ){
					if ( entity instanceof BctModelViolation ){
						BctModelViolation v = (BctModelViolation) entity;
						result.addViolation( v );
						
						
						result.addProcessId(v.getPid());
						for ( String action : v.getCurrentActions() ){
							result.addActionId(action);
						}
						
						for ( String test : v.getCurrentTests() ){
							result.addTestId(test);
						}
						
					} else if ( entity instanceof Failure) {
						result.addFailure((Failure) entity);
					} else if ( entity instanceof BctAnomalousCallSequence ){
						result.addAnomalousCallSequence((BctAnomalousCallSequence)entity);
					}
				}
			} catch (FormattingException e) {
				throw new CBEBctViolationsLogLoaderException(e);
			}

		}
		return result;
	}
	
	public static void main( String args[] ){
		File file = new File( args[0]);
		
		CBEBctViolationsLogLoader loader = new CBEBctViolationsLogLoader();
		ArrayList<File> list = new ArrayList<File>();
		list.add(file);
		try {
			BctViolationsLogData data = loader.load(list);
			
			__processResults(data.getViolations());
		} catch (CBEBctViolationsLogLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	public static int __processResults(List<BctModelViolation> filteredAnomalies) {
		if ( filteredAnomalies.size() > 0 ){
			System.out.println("\n\n\n\n!!!!!!!! Violations Found");

			for ( BctModelViolation violation : filteredAnomalies){

				String functionName = (violation.getViolatedModel());

				System.out.println("Monitored function: "+functionName +"\t["+violation.getViolatedModel()+"]");


				String anomaly = violation.getViolation();
				if ( violation.getViolationType().equals(BctModelViolation.ViolationType.UNEXPECTED_EVENT) ){
					anomaly = anomaly.trim();
				}
				System.out.println("Anomaly: "+ anomaly + "\t\t("+violation.getViolationType()+")");

				if ( violation instanceof BctIOModelViolation ){
					ViolationData vd = ViolationsUtil.getViolationData((BctIOModelViolation) violation);

					System.out.println("\tActual values: ");
					for ( VariableData varData : vd.getViolatedVariables() ){
						System.out.println("\t\t"+varData.getVariableName()+" "+varData.getActualValue());
					}


				} else if ( violation instanceof BctFSAModelViolation ){
					ViolationData vd = ViolationsUtil.getViolationData((BctFSAModelViolation) violation);

					System.out.println("\tCurrent state(s): ");
					for( String state : vd.getViolationStatesNames() ){
						System.out.println("\t"+state);
					}
					System.out.println("");
				}

				System.out.println("Stack trace:");
				for( String stack : violation.getStackTrace() ){
					if ( stack.startsWith(".") ){
						stack = stack.substring(1);
					}
					System.out.println("\t"+stack);
				}
			}

			return 1;
		}
		return 0;
	}
}
