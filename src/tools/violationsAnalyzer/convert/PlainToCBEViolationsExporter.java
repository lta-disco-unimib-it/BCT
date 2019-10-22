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
package tools.violationsAnalyzer.convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;



import recorders.CBEViolationsRecorder;
import recorders.FileCBEViolationsRecorder;
import recorders.ViolationsRecorderException;



/**
 * This class given a file containing violations reported by BCT produces report files with information on correlation
 * between different violations.
 * 
 * @author Fabrizio Pastore
 *
 */
public class PlainToCBEViolationsExporter {
	private static final String failingOpt = "-failing";
	private static final String destFolderOpt = "-destFolder";
	private static final String graphLimitOpt = "-graphLimit";
	private static final String saveAllOpt = "-saveAll";
	private static final String graphRulesOpt = "-rules";
	private static final String saveDynamicCallTreeOpt = "-saveDynamicCallTree";
	
	private static class ViolationData {
		List<String> stackTrace = new ArrayList<String>();
		public String modelName;
		public String state;
		public String transition;
		public String threadId;
		public String type;
		public long creationTime = System.currentTimeMillis();
		public String violation;
		public String parameters = "";
	}
	/**
	 * This method creates the violation manager from the violations recorded in a plain text file
	 * 
	 * @param violationsFile
	 * @param failingTcs
	 * @return
	 * @throws IOException
	 */
	public static List<BctModelViolation> createViolations(File violationsFile ) throws IOException{
		BufferedReader fr = new BufferedReader ( new FileReader( violationsFile ) );
		String inpline;
		String lastLine="";
		String currentTest=null;
		int lines = 0;
		
		int outside = 0;
		
		ViolationData violation = null;
		List<BctModelViolation> violations = new ArrayList<BctModelViolation>();
		
		boolean stack = false;
		int testLine = 1;
		boolean stackPreprocessed = false;
		while ( ( inpline  = fr.readLine() ) != null ){
			String line = inpline.trim();
			lines++;
			boolean parameter = false;
			if ( isTestInfo(line) ){
				//record the last violation found before processing the new test information
				if ( violation != null ){
					addViolation(violations, violation,currentTest);
					violation = null;
					stack=false;
					parameter = false;
					currentTest = null;
				}
				
				stackPreprocessed = false;
				testLine = lines;
				currentTest = getTestName(line);
			} else if ( isInteractionViolation(line) ) {
				
				if ( violation != null ){
					addViolation(violations, violation,currentTest);
					violation = null;
					stack=false;
					parameter = false;
					currentTest = null;
				}

				fr.readLine();
				String state = fr.readLine();
				String transition = fr.readLine();
				lines+=3;
				if ( state == null || transition == null ){
					System.err.println("Line : "+lines+"Malformed log: found "+line+" "+state+" "+transition );
				}

				violation = getIntViolation(line,state,transition);

			} else if ( isIoViolation(line) ) {
				
				if ( violation != null ){
					addViolation(violations, violation,currentTest);
					violation = null;
					stack=false;
					parameter = false;
					currentTest = null;
				}

				lines++;
				String info = fr.readLine();

				violation = getIOViolation(line,info);

			} else if ( isThread( line )){
				System.out.println(violation);
				violation.threadId = line.split(":")[1].trim();
			} else if ( isParameter( line ) ){
				parameter = true;
			} else if ( isStackTrace( line ) ){
				stack = true;
			} else {
				if ( line.contains(" : ")  && stack ){
					if ( stackPreprocessed || ( !line.startsWith("java.lang.Thread") &&
							!line.startsWith("check.IoChecker") &&
							!line.startsWith("aspects.Bct") &&
							!line.startsWith("check.InteractionInvariantHandler") &&
							!line.startsWith("check.Checker") &&
							!line.startsWith("probes.") &&
							!line.startsWith("bct")
							)
					){
						if ( line.contains("Test") && currentTest == null){
							currentTest = line.split(":")[0];
						}
						stackPreprocessed = true;
						violation.stackTrace.add(line);
//						String[] lineEls = line.split(" : ");
//						callTrace.addBottom(lineEls[0], Integer.valueOf(lineEls[1]));
					}
					
				} else if ( parameter == true ){
					violation.parameters += "\n"+line;
				}
			}
		}
		
		if ( violation != null ){
			addViolation(violations, violation, currentTest);
		}
		
		System.out.println(lastLine);
		fr.close();
		
		return violations;
	}
	
	private static boolean isParameter(String line) {
		return line.trim().startsWith("Parameters :");
	}

	private static void addViolation(List<BctModelViolation> violations, ViolationData violation, String currentTest) {
		String id = createNewViolationId(violations,violation);
		BctModelViolation viol;
		
		if ( violation.type.equals(BctModelViolation.ViolatedModelsTypes.FSA.name()) ) {
		viol = new BctFSAModelViolation(id, 
				violation.modelName, 
				violation.violation, 
				violation.type, 
				violation.creationTime , 
				new String[0], 
				new String[]{currentTest}, 
				violation.stackTrace.toArray(new String[violation.stackTrace.size()]), 
				"1", 
				violation.threadId, 
				new String[]{violation.state});
		} else {
			viol = new BctIOModelViolation(id, 
					violation.modelName, 
					violation.violation, 
					violation.type, 
					violation.creationTime , 
					new String[0], 
					new String[]{currentTest}, 
					violation.stackTrace.toArray(new String[violation.stackTrace.size()]), 
					"1", 
					violation.threadId, 
					violation.parameters);	
		}
		
		violations.add(viol);
	}

	private static String createNewViolationId(List<BctModelViolation> violations, ViolationData violation) {
		return ""+violations.size();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if ( args.length < 1 ){
			printUsage();
			System.exit(-1);
		}
		File srcFile = new File(args[0]);
		File dstFile = new File(args[1]);
		
		if ( dstFile.exists() ){
			System.err.println("File "+dstFile+" exists!");
			System.exit(-1);
		}
		
		List<BctModelViolation> violations = createViolations(srcFile);
		
		CBEViolationsRecorder recorder = new FileCBEViolationsRecorder(dstFile);
		
		for( BctModelViolation v : violations  ){
			
			try {
				recorder.recordViolation(v);
			} catch (ViolationsRecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	

	private static File filterAW(File outDir,  File violationsFile) throws IOException {
		File f = new File(outDir, "violations.tmp");
		
		BufferedReader r = new BufferedReader ( new FileReader(violationsFile) );
		
		BufferedWriter w = new BufferedWriter ( new FileWriter(f) );
		
		String line;
		String adviceLine = null;
		while ( ( line = r.readLine() ) != null ){
			if ( line.contains("_AW_") ){
				
				if ( adviceLine == null ) {
					adviceLine = line.split(":")[1];
				}
			} else {
				if ( adviceLine!=null){
					String method = line.split(":")[0];
					line = method+":"+adviceLine;
					adviceLine = null;
				}
				w.write(line+"\n");
			}
		}
		w.close();
		r.close();
		return f;
	}




	private static void printUsage() {
		System.out.println("Usage: " +
				"\n"+PlainToCBEViolationsExporter.class.getCanonicalName()+" <srcPlainLog> <destCbeLog>");
	}

	public static boolean isStackTrace(String line) {
		return line.startsWith("Stack trace");
	}

	public static boolean isThread(String line) {
		return line.startsWith("Thread :");
	}

	public static ViolationData getIOViolation(String line, String info) {
		
		ViolationData vd = new ViolationData();
		
		int methodNameStart = line.indexOf("method")+7;
		int methodNameEnd = line.indexOf(" during ")-1;
		int exitStart = line.indexOf(" exit");
		int enterStart = line.indexOf(" enter");


		vd.modelName = line.substring(methodNameStart, methodNameEnd);
		if ( exitStart > 0 ){
			vd.modelName += ":::EXIT0";
		} else {
			vd.modelName += ":::ENTER";
		}
		
		vd.type = BctModelViolation.ViolatedModelsTypes.IO.name();
		vd.violation = info.trim();
		return vd;
	}

	public static ViolationData getIntViolation(String line, String state, String transition) {
		ViolationData vd = new ViolationData();
		
		int methodNameStart = line.indexOf("method")+7;
		int methodNameEnd = line.indexOf(" during ")-1;
		
		
		vd.modelName = line.substring(methodNameStart, methodNameEnd);
		vd.state = state.trim();
		vd.violation = transition.trim();
		vd.type = BctModelViolation.ViolatedModelsTypes.FSA.name();
		
		return vd;
	}

	public static boolean isInteractionViolation(String line) {
		return line.trim().startsWith("Interaction Invariant");
	}

	public static boolean isIoViolation(String line) {
		return line.trim().startsWith("Violation of");
	}
	
	public static boolean isTestEnd(String line) {
		if ( isTestInfo(line) ){
			return line.endsWith("END");
		}
		return false;
	}

	private static String getTestName(String line) {
		
		return line.split("\t")[1];
	}

	private static boolean isTestStart(String line) {
		if ( isTestInfo(line) ){
			return line.endsWith("START");
		}
		return false;
	}

	private static boolean isTestInfo(String line) {
		return line.startsWith("TEST_INFO");
	}
	
	

}
