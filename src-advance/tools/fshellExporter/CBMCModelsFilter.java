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
package tools.fshellExporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cpp.gdb.EnvUtil;
import cpp.gdb.FunctionMonitoringData;

import modelsViolations.BctIOModelViolation;
import modelsViolations.BctIOModelViolation.Position;
import modelsViolations.BctModelViolation;

import tools.fshellExporter.CBMCExecutor.ValidationResult;
import tools.violationsAnalyzer.ViolationsUtil;
import tools.violationsAnalyzer.ViolationsUtil.ViolationData;
import util.FileUtil;

public class CBMCModelsFilter {

	private static Logger LOGGER = Logger.getLogger(CBMCModelsFilter.class.getCanonicalName());
	
//	public static void retrieveLineInInjectedSource( String claimString, File modelsInjectedF, File modelLinesF ){
//		try {
//			List<String> modelsInjected = FileUtil.getLines(modelsInjectedF);
//			modelLinesF = FileUtil.getLines(modelLinesF);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public static void filterTrueProperties(File allModelsV0, File validated, File allModelsV1, File allModelsV1Filtered, File allModelsV0Filtered) throws IOException {
		
		List<String> modelsV0 = FileUtil.getLines(allModelsV0);
		HashMap<String, Integer> linesMap = new HashMap<String,Integer>();
		int counter=0;
		for ( String line : modelsV0 ){
			linesMap.put(line, counter++);
		}
		
		List<String> validatedLines = FileUtil.getLines(validated);
		List<String> modelLines = FileUtil.getLines(allModelsV1);
		List<String> toKeep = new ArrayList<String>();
		List<String> toKeepV0 = new ArrayList<String>();
		
		for( int i = 0; i < validatedLines.size(); i++ ){
			String validatedLine = validatedLines.get(i);
			String[] tokens = validatedLine.split("\t");
			if ( ValidationResult.valueOf(tokens[3]) == ValidationResult.VALID ){
				String originalModelLine = tokens[0]+"\t"+tokens[1]+"\t"+tokens[2];
				Integer originalLine = linesMap.get(originalModelLine);
				toKeep.add(modelLines.get(originalLine));
				
				toKeepV0.add(originalModelLine);
			}
		}
		
		
		FileUtil.writeToTextFile(toKeep, allModelsV1Filtered);
		FileUtil.writeToTextFile(toKeepV0, allModelsV0Filtered);
	}

	public static void filterIntendedAnomalies(File allModelsV1TrueProperties,
			File allModelsV1Filtered, List<BctModelViolation> validViolations, Map<String, FunctionMonitoringData> modifiedlFunctions, File allModelsV0TrueProperties,
			File allModelsV0Filtered, File allModelsV1Outdated, File allModelsV0Outdated) {
		
		HashSet<String> assertionsIntentionallyChanged = new HashSet<String>();
		
		for ( BctModelViolation  viol : validViolations  ){
			if ( viol instanceof BctIOModelViolation ){
				
				BctIOModelViolation v = (BctIOModelViolation) viol;
				ViolationData vd = ViolationsUtil.getViolationData(v);
				
				String model = v.getViolatedModel(); 
				String violatedExpression = v.getViolation();
				String assertion = ModelsExporter.processExpression(violatedExpression);
				
				String function = getFunction( model );
				
				FunctionMonitoringData functionData = modifiedlFunctions.get(function);
				String position;
				if ( v.getPosition() == Position.EXIT ){
					position = function;
				} else {
					int line = getLine(function);
					if ( line == -1 ){
						position = ""+functionData.getFirstSourceLine();
					} else {
						position = String.valueOf(line);
					}
				}
				
				String location = functionData.getSourceFileLocationClean();
				
				if ( EnvUtil.isCygWin() ){
					LOGGER.info("Renaming location "+location);
					location = location.replace('/', '\\');
				}
				
				String assertionLine = CBMCModelsExporter.buildAssertionLine(location, position, assertion);
				
				assertionsIntentionallyChanged.add(assertionLine);
				
			}
			
			
		}
		
		System.out.println("Assertions intentionally changed:");
		for ( String assertion : assertionsIntentionallyChanged ){
			System.out.println("\t "+assertion);
		}
		
		
		try {
			List<String> trueProperties = FileUtil.getLines(allModelsV1TrueProperties);
			List<String> truePropertiesV0 = FileUtil.getLines(allModelsV0TrueProperties);

			BufferedWriter outdatedW = new BufferedWriter(new FileWriter(allModelsV1Outdated) );
			BufferedWriter outdatedWV0 = new BufferedWriter(new FileWriter(allModelsV0Outdated) );
			
			BufferedWriter w = new BufferedWriter(new FileWriter(allModelsV1Filtered) );
			BufferedWriter wV0 = new BufferedWriter(new FileWriter(allModelsV0Filtered) );
			try {
				int truePropertiesSize = trueProperties.size();
				for ( int i = 0; i < truePropertiesSize; i++  ){
					String tp = trueProperties.get(i);
					if ( ! assertionsIntentionallyChanged.contains(tp) ){
						w.write(tp);
						w.newLine();
						
						//Keeping track also for V0 (necessary for EvolCheck)
						String tpV0 = truePropertiesV0.get(i);
						wV0.write(tpV0);
						wV0.newLine();
						
					} else {
						System.out.println("Assertion intentionally changed: "+tp);
						
						outdatedW.write(tp+"\t"+ValidationResult.OUTDATED+"\t-1");
						outdatedW.newLine();
						
						//Keeping track also for V0 (necessary for EvolCheck)
						String tpV0 = truePropertiesV0.get(i);
						
						outdatedWV0.write(tpV0+"\t"+ValidationResult.OUTDATED+"\t-1");
						outdatedWV0.newLine();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				w.close();
				wV0.close();
				
				outdatedW.close();
				outdatedWV0.close();
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getLine(String model) {
		int spacePos = model.indexOf(' ');
		String function;
		if ( spacePos == -1 ){
			function = model;
		} else {
			function = model.substring(0, spacePos);
		}
		
		int colonPos = function.indexOf(':');
		if ( colonPos < 0 ){
			return -1;
		}
		
		return Integer.valueOf( function.substring(colonPos) );
	}
	
	private static String getFunction(String model) {
		int spacePos = model.indexOf(' ');
		String function = model.substring(0, spacePos);
		
		int colonPos = function.indexOf(':');
		if ( colonPos < 0 ){
			return function;
		}
		
		return function.substring(0,colonPos);
	}

}
