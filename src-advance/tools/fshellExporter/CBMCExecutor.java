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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import tools.fshellExporter.CBMCExecutor.ValidationResult;
import util.FileUtil;
import util.ProcessRunner;

import com.google.common.base.Preconditions;

public class CBMCExecutor {
	///PROPERTIES-DESCRIPTION: Options that manage the execution of CBMC
	
	///if true rename all the references to source files ending with .c or .cc to source files ending with .i or .ii
	private static final String BCT_CBMC_MAP_CTO_I = "bct.cbmc.mapCtoI";

	///path to the CBMC executable
	private static final String BCT_CBMC_EXEC = "bct.cbmc.exec";

	protected String CBMC_EXCUTABLE = "cbmc";

	private static Logger LOGGER = Logger.getLogger(CBMCExecutor.class.getCanonicalName());
	
	///if true executes the model checker to check all the claims alltogether (default true)
	public static final String BCT_CBMC_ALL_CLAIMS = "bct.cbmc.allClaims";

	///used when goto-cc program is compiled relatively to a folder different from the source folder indicated to BCT
	private static final String BCT_CBMC_SOURCE_FOLDER = "bct.cbmc.sourceFolder";

	private int unwindN = 5;
	private List<String> cbmcOutputLines;
	private boolean executeCBMCOnceOnAllClaims = true;
	private File sourceFolder;
	
	private File gotoCCSourceFolder;
	
	public File getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(File sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	private boolean mapCtoI;
	{
		String allClaimsString = System.getProperty(BCT_CBMC_ALL_CLAIMS);
		if ( allClaimsString != null ) {
			executeCBMCOnceOnAllClaims = Boolean.parseBoolean(allClaimsString); 
		}
		
		
		String sourceFolderString = System.getProperty(BCT_CBMC_SOURCE_FOLDER);
		if ( sourceFolderString != null ){
			sourceFolder = new File( sourceFolderString );
		}
		
		String cbmcExecutableString = System.getProperty(BCT_CBMC_EXEC);
		if ( cbmcExecutableString != null ){
			CBMC_EXCUTABLE = cbmcExecutableString;
		}
		
		String mapCtoIString = System.getProperty(BCT_CBMC_MAP_CTO_I);
		mapCtoI = Boolean.parseBoolean(mapCtoIString);
	}

	public List<CBMCClaim> validateInterna( GotoCC gotoCompiler, File gotoCCProgram, String mainFunction ) throws IOException{
		
		List<CBMCClaim> claims = retrieveCCProgramClaims(gotoCCProgram);

		generateResultsExecutingCBMC(gotoCompiler, gotoCCProgram, mainFunction, claims);

		
		List<CBMCClaim> result = new ArrayList<CBMCClaim>();
		for (CBMCClaim claim : claims ){
			if ( claim.isBctAssertion() ){
				result.add(claim);
			}
		}
		
		return result;
	}

	public List<CBMCClaim> retrieveCCProgramClaims(File gotoCCProgram)
			throws IOException {
		Appendable _outputBuffer = new StringWriter();
		Appendable _errorBuffer = new StringWriter();
		List<String> command = new LinkedList<String>();
		command.add(CBMC_EXCUTABLE);
		command.add("--show-claims");
		command.add(gotoCCProgram.getAbsolutePath());
		ProcessRunner.run(command, _outputBuffer, _errorBuffer, 0, null);


		System.out.println(_outputBuffer.toString());
		System.out.println(_errorBuffer.toString());

		BufferedReader reader = new BufferedReader( new StringReader(_outputBuffer.toString() ) );
		String line;

		//		HashMap<String,TreeMap<Integer, String>> assertionIds = new HashMap<String,TreeMap<Integer,String>>();
		List<CBMCClaim> claims = new ArrayList<CBMCClaim>();

		while ( (line = reader.readLine() ) != null ){
			//			System.out.println("!"+line);
			if ( line.startsWith("Claim ") || line.startsWith("Property ") ){
				line = line.trim();
				
				int assertionStart = line.indexOf(' ');
				String assertionId = line.substring(assertionStart+1, line.length()-1);

				line = reader.readLine().trim();
				int linePos = line.indexOf(" line ");
				int functionPos = line.indexOf(" function ");
				String fileName = line.substring(5,linePos);
				
				boolean bct_assertion = true;
				String assertionLine = reader.readLine().trim();
				if ( ! assertionLine.startsWith("assertion") ){
					bct_assertion = false; //if it does not start with the term "assertion" it means that is an assertion automatically generated by CBMC/GOTO-CC
				}
				
				fileName = checkAndCorrectClaimFileLocation( fileName );
				//				System.out.println("FIELNAME "+fileName);
				//				TreeMap<Integer, String> treeMap = assertionIds.get(fileName);
				//				if ( treeMap == null ){
				//					treeMap = new TreeMap<Integer, String>();
				//					assertionIds.put(fileName, treeMap);
				//				}



				if ( functionPos == -1 ){
					functionPos = line.length();
				}
				String lineNoString = line.substring(linePos+6, functionPos);
				//				System.out.println("LINENO "+lineNoString);

				Integer lineNo = Integer.valueOf(lineNoString);

				CBMCClaim claim = new CBMCClaim(lineNo,assertionId,fileName);
				claim.setBctAssertion ( bct_assertion );
				LOGGER.fine("Adding Claim "+claim);
				claims.add(claim);

				

			}
		}

		reader.close();
		return claims;
	}

	private HashMap<String,String> fileNames = new HashMap<String,String>();

	private boolean singleAssertionsHiding = true;
	/**
	 * This methods act as a workaround.
	 * Depending on the make file, CBMC clams may point to fileName only, without having a proper relative path.
	 * This method tries to fix this issue.
	 * 
	 * @param fileName
	 * @return
	 */

	
	protected String checkAndCorrectClaimFileLocation(String fileName) {
		if ( mapCtoI ){
			fileName = fileName.replace(".cc", ".ii");
			fileName = fileName.replace(".c", ".i");
			LOGGER.fine("REPLACED extension: "+fileName);
		}
		
		
		
		String correctName = fileNames.get(fileName);
		if ( correctName != null ){
			LOGGER.fine(fileName + " in cache returning "+correctName);
			return correctName;
		}
		
		if ( sourceFolder == null ){
			if ( gotoCCSourceFolder == null ){
				LOGGER.fine("sourceFolder not set and gotoCCSourceFolder null, returning "+fileName);
				
				return fileName;
			}
			
			if ( fileName.startsWith(gotoCCSourceFolder.getAbsolutePath()+File.separator) ){
				correctName = fileName.substring(gotoCCSourceFolder.getAbsolutePath().length()+1);


				LOGGER.fine("sourceFolder not set and absolute path found, returning "+correctName);
				
				return correctName;
			} else {
				sourceFolder = gotoCCSourceFolder;
			}
		}
		
		File file = new File( sourceFolder.getAbsolutePath()+File.separator+fileName );
		if ( ! file.exists() ){

			file = FileUtil.find( sourceFolder, fileName );
			if ( file == null ){
				LOGGER.fine(fileName + " not found, returning "+fileName);
				fileNames.put(fileName, fileName );
				return fileName;
			}
		}
		
		//if file exists we still have to retrieve its  path relative to the root folder of the project (sourceFolder)  
		
		
		
		
		try {
			correctName = FileUtil.getRelativePath( sourceFolder, file, File.separator );
		} catch (IOException e) {
			LOGGER.fine(fileName + " relative path not found, returning "+fileName);
			fileNames.put(fileName, fileName );
			return fileName;
		}
		
		File correctFile = new File ( sourceFolder.getAbsolutePath()+File.separator+correctName );
		
		
		try {
			LOGGER.fine("Calculating path relative to "+gotoCCSourceFolder+" for file "+ correctFile);
			correctName = FileUtil.getRelativePath( gotoCCSourceFolder, correctFile, File.separator );
		} catch (IOException e) {
			LOGGER.fine(fileName + " correct file relative path not found, returning "+fileName);
			fileNames.put(fileName, fileName );
			return fileName;
		}
		

		
		LOGGER.fine("Correct name for "+fileName + " is "+correctName);
		fileNames.put(fileName, correctName );
		
		return correctName;
	}

	public File getGotoCCSourceFolder() {
		return gotoCCSourceFolder;
	}

	public void setGotoCCSourceFolder(File gotoCCSourceFolder) {
		this.gotoCCSourceFolder = gotoCCSourceFolder;
	}

	public void generateResultsExecutingCBMC(
			GotoCC gotoCompiler, File gotoCCProgram, String mainFunction,
			List<CBMCClaim> claims)
					throws IOException {
		if ( executeCBMCOnceOnAllClaims ){
			executeCBMCOnceOnAllClaims(gotoCCProgram, mainFunction,
					claims);	
			return;
		}
		executeCBMCOnSingleAssertions(gotoCompiler, gotoCCProgram, mainFunction,
				claims);
	}

	public void executeCBMCOnceOnAllClaims(
			File gotoCCProgram, String mainFunction,
			List<CBMCClaim> claims)
					throws IOException {
		LOGGER.info("Executing CBMC on all claims");
		
		List<String> cbmcResult;
		
		if ( cbmcOutputLines != null ){
			LOGGER.info("Getting results from manual CBMC execution");
			cbmcResult = cbmcOutputLines;	
		} else {
			Appendable _outputBuffer = executeCBMC(gotoCCProgram, null,
					mainFunction);

			BufferedReader r = new BufferedReader(new StringReader(_outputBuffer.toString()));
			
			cbmcResult = FileUtil.getLines(r);
		}
		

		

		loadResultsFromCbmcOutput(cbmcResult, claims);

	}

	public void executeCBMCOnSingleAssertions(
			GotoCC gotoCompiler, File gotoCCProgram, String mainFunction,
			List<CBMCClaim> claims)
					throws IOException {


		LOGGER.info("Executing CBMC on single assertions");
		if ( singleAssertionsHiding  ){
			executeCBMCOnSingleAssertions_hiding(gotoCompiler, gotoCCProgram, mainFunction, claims);
		} else {
			executeCBMCOnSingleAssertions_noHiding(gotoCompiler, gotoCCProgram, mainFunction, claims);
		}
		

	}
	
	public void executeCBMCOnSingleAssertions_noHiding(
			GotoCC gotoCompiler, File gotoCCProgram, String mainFunction,
			List<CBMCClaim> claims)
					throws IOException {


		LOGGER.info("Executing CBMC on single assertions wthout assertions hiding");
		
		
		for ( CBMCClaim claim : claims ){
			
			System.out.println("EVALUATING: "+claim.getClaimId());
			
			ValidationResult result = evaluate( gotoCCProgram, claim.getClaimId(), mainFunction );
			System.out.println("RESULT: "+result.toString());
			claim.setResult ( result );
		}


	}
	
	public void executeCBMCOnSingleAssertions_hiding(
			GotoCC gotoCompiler, File gotoCCProgram, String mainFunction,
			List<CBMCClaim> claims)
					throws IOException {


		LOGGER.info("Executing CBMC on single assertions hiding other assertions");
		
		File srcFolder = gotoCompiler.getSrcFolder();
		hideAll( srcFolder, claims );
		
		CBMCClaim oldClaim = null;
		
		for ( CBMCClaim claim : claims ){
			
			if ( ! claim.isBctAssertion() ){
				LOGGER.info("SKIPPING Claim "+claim.getClaimId()+" : NOT GENERATED BY BCT");
				continue;
			}
			
			System.out.println("EVALUATING: "+claim.getClaimId());
			hideClaim( srcFolder, oldClaim );
			unHideClaim( srcFolder, claim );
			oldClaim = claim;
			
			gotoCompiler.compileWithGotoCC();
			
			List<CBMCClaim> newClaims = retrieveCCProgramClaims(gotoCCProgram);
			
			CBMCClaim found = null;
			for ( CBMCClaim newClaim : newClaims ){
				if ( newClaim.getFileName().equals( claim.getFileName() ) && ( newClaim.getLine() ==  claim.getLine() ) ){
					found = newClaim;
					break;
				}
			}
			
			if ( found == null ){
				LOGGER.warning("Claim not found: "+claim.getClaimId());
				continue;
			}
			
			ValidationResult result = evaluate( gotoCCProgram, found.getClaimId(), mainFunction );
			System.out.println("RESULT: "+result.toString());
			claim.setResult ( result );
		}


	}

	private void unHideClaim(File srcFolder, CBMCClaim claim) {
		hideUnhideClaim(srcFolder, claim, false);
	}
	
	private void hideClaim(File srcFolder, CBMCClaim claim) {
		hideUnhideClaim(srcFolder, claim, true);
	}
	
	private void hideUnhideClaim(File srcFolder, CBMCClaim claim, boolean hide) {
		if ( claim == null ){
			return;
		}
		
		String fileName = claim.getFileName();
		File file = new File ( srcFolder, fileName );
		List<String> lines;
		try {
			lines = FileUtil.getLines(file);
			

			int lineNo = claim.getLine()-1;
			String claimLineContent = lines.get( lineNo );
			
			if ( hide ){
				if ( ! claimLineContent.startsWith("//") ) {
					lines.set(lineNo, "//"+claimLineContent );
				} else {
					LOGGER.warning("CANNOT HIDE: the claim "+claim.getClaimId()+" was already hidden ");
				}
			} else {
				if ( claimLineContent.startsWith("//") ) {
					lines.set(lineNo, claimLineContent.substring(2) );
				} else {
					LOGGER.warning("CANNOT UNHIDE: the claim "+claim.getClaimId()+" was not hidden ");
				}
			}
			
			
			FileUtil.writeToTextFile(lines, file);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private void hideAll( File sourceFolder, List<CBMCClaim> claims) {
		HashMap<String, List<CBMCClaim>> claimsPerFile = groubClaimsByFile(claims);
		
		for ( Entry<String, List<CBMCClaim>> fileClaims : claimsPerFile.entrySet() ){
			String fileName = fileClaims.getKey();
			File file = new File ( sourceFolder, fileName );
			
			hideClaimsInFile( file, fileClaims.getValue() );
		}
		
		
	}

	private void hideClaimsInFile(File file, List<CBMCClaim> claims) {
		List<String> lines;
		try {
			lines = FileUtil.getLines(file);
			
			for ( CBMCClaim claim : claims ){
				int lineNo = claim.getLine()-1;
				String claimLineContent = lines.get( lineNo );
				lines.set(lineNo, "//"+claimLineContent);
			}
			
			FileUtil.writeToTextFile(lines, file);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void loadResultsFromCbmcOutput(
			List<String> cbmcOutputLines, List<CBMCClaim> claims) {

		HashMap<String,ValidationResult> res = new HashMap<String, CBMCExecutor.ValidationResult>();

		boolean results=false;
		int i = -1;
		
		HashMap<String,CBMCClaim> claimsMap = new HashMap<String,CBMCClaim>();
		for ( CBMCClaim claim : claims ){
			claimsMap.put(claim.getClaimId(), claim);
		}
		
		for ( String line : cbmcOutputLines ){
			
			
			if ( line.startsWith("** Results") || line.startsWith("**Results") ){
				results = true;
				continue;
			}
			if ( results ){
				if ( line.isEmpty() ){
					results = false;
				} else {
					i++;
					
					int assertStart;
					int assertEnd;
					if ( line.startsWith("[")){
						assertStart = 1;
						assertEnd = line.indexOf(']');
					} else {
						assertStart = 0;
						assertEnd = line.indexOf(':');
					}

					String claimName = CBMCUtils.parseClaim(line.substring(assertStart,assertEnd));
					int colonsPos = line.indexOf(':');
					String resultString = line.substring(colonsPos+1).trim();
					
					if ( resultString.indexOf(' ') > 0 ){
						LOGGER.warning("Skipping claim '"+claimName+"' looks like a CBMC generated claim");
						continue;
					}
					
					ValidationResult result;
					if ( resultString.equals("OK") ){
						result = ValidationResult.VALID;
					} else {
						result = ValidationResult.INVALID;
					}
					
					
					
					CBMCClaim claim = claimsMap.get(claimName.trim());
					if ( claim == null ){
						LOGGER.warning("Claim not found '"+claimName+"'");
						LOGGER.info("Claims: "+claimsMap);
						continue;
					}
					
//					CBMCClaim claim = claims.get(i);
//					if ( ! claim.getClaimId().equals(claimName.trim()) ){
//						new IllegalStateException("Expecting "+claim.getClaimId()+" found "+claimName);
//					}
					
					LOGGER.info("Result for Claim "+claim.getClaimId() +" "+ result);
					ValidationResult oldResult = claim.getResult();
					if ( oldResult != ValidationResult.INVALID ){
						LOGGER.fine("Old result was "+oldResult+" setting to "+result);
						claim.setResult(result);
					} else {
						LOGGER.fine("Claim already set as INVALID");
					}
				}
			}
		}

		
	}

	private ValidationResult evaluate(File gotoCCProgram, String assertionId, String mainFunction) throws IOException {

		System.out.println("EVALUATING "+assertionId);

		Appendable _outputBuffer = executeCBMC(gotoCCProgram, assertionId,
				mainFunction);

		System.out.println(_outputBuffer.toString());


		String lastLine = retrieveLastLine( _outputBuffer.toString() );
		if ( lastLine.equals("VERIFICATION SUCCESSFUL") ){
			return ValidationResult.VALID;
		}
		return ValidationResult.INVALID;
	}

	public Appendable executeCBMC(File gotoCCProgram, String assertionId,
			String mainFunction) throws IOException {
		Appendable _outputBuffer = new StringWriter();
		List<String> command = createMCcommand(gotoCCProgram, assertionId, mainFunction);
		LOGGER.info("Executing CBMC "+command);
		ProcessRunner.run(command, _outputBuffer, null, 0, null);
		return _outputBuffer;
	}

	protected List<String> createMCcommand(File gotoCCProgram,
			String assertionId, String mainFunction) {
		List<String> command = new LinkedList<String>();
		command.add(CBMC_EXCUTABLE);
		if ( assertionId == null ){
			command.add("--all-claims");
		} else {
			command.add("--claim");
			command.add(assertionId);
		}
		command.add("--function");
		command.add(mainFunction);
		if ( getUnwindN() > 0 ){
			command.add("--no-unwinding-assertions");
			command.add("--unwind");
			command.add(""+getUnwindN());
		}

		command.add(gotoCCProgram.getAbsolutePath());
		return command;
	}

	private String retrieveLastLine(String output) {
		BufferedReader reader = new BufferedReader( new StringReader(output ) );

		String lastLine = null;;
		String line;
		try {
			while( ( line = reader.readLine() ) != null ){
				lastLine = line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lastLine;
	}

	public enum ValidationResult { VALID, INVALID, UNKNOWN, UNREACHABLE, OUTDATED };

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		File gotoProgram = new File( args[0] );
		File modelsFile = new File( args[1] );
		String mainFunction = args[2];

		CBMCExecutor executor = new CBMCExecutor();
		if ( args.length > 3 ){
			Integer unwind = Integer.valueOf(args[3]);
			System.out.println("setting unwind to "+unwind);
			executor.setUnwindN(unwind);
		}
	
		executor.validate(gotoProgram, mainFunction, modelsFile);
	}
	
	private void validate(File gotoProgram, String mainFunction, File modelsFile) {
		validate(gotoProgram, mainFunction, modelsFile);
	}

	public Map<String,CBMCClaim> validate( GotoCC gotoCompiler, File gotoProgram, String mainFunction, File modelsFile ) throws IOException{
		List<CBMCClaim> result = validateInterna( gotoCompiler, gotoProgram, mainFunction );
		LOGGER.info("Number of claims verified by CBMC "+result.size());
		Map<String, CBMCClaim> map = filterValidationResults( modelsFile, result );
		writeValidationResults( map, modelsFile );
		return map;
	}

	public static Map<String, CBMCClaim> writeValidationResults(Map<String, CBMCClaim> map, File modelsFile) throws IOException {
		File resultFile = new  File(modelsFile.getAbsoluteFile() + ".validated" );
		//write in the same order as the models file
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
		try {

			List<String> assertions = FileUtil.getLines(modelsFile);
			for ( String assertion : assertions ){
				CBMCClaim mapClaim = map.get(assertion);
				
				if ( mapClaim == null ){
					LOGGER.warning("Cannot find: "+assertion);
					writer.write(assertion+"\t"+ValidationResult.UNKNOWN+"\t"+"-1");
				} else  {
					writer.write(assertion+"\t"+mapClaim.getResult()+"\t"+mapClaim.getLine());
				}
				writer.newLine();
			}

		} finally {
			writer.close();
		}
		
		return map;		
	}
	
	private static Map<String, CBMCClaim> filterValidationResults(File modelsFile,
			List<CBMCClaim> claims) throws IOException {
	
		List<ProcessedAssertion> assertionsInjected = ProcessedAssertionsLoader.loadFromTextFiles(modelsFile);
		
		//if there are manual assertions in files where we did not inject any automatic assertions the number of assertions could be different
//		checkSameSizeOfAssertions( assertionsInjected, claims ); 


		HashMap<String, List<CBMCClaim>> claimsPerFile = groubClaimsByFile(claims);
		HashMap<String, List<ProcessedAssertion>> assertionsPerFile = groupProcessedAssertionsByFile(assertionsInjected);
		
		

		//assuming modelsFile is sorted by fileName and then by line number

		LOGGER.fine("File with claims : "+claimsPerFile.size()+ " "+ claimsPerFile.keySet());
		for( Entry<String,List<CBMCClaim>> entry : claimsPerFile.entrySet() ){
			LOGGER.info("FILE,CLAIMS: "+entry.getKey()+ "  "+entry.getValue());
		}
		
		LOGGER.fine("File with assertions : "+assertionsPerFile.size() + " "+assertionsPerFile.keySet());

		Map<String, CBMCClaim> map = asociateValidationResultToInjectedAssertions(
				claimsPerFile, assertionsPerFile);

		LOGGER.info("Got validation results for "+map.size()+" assertions.");
		
//		for( Entry<String, ValidationResult> eny : map.entrySet() ){
//			LOGGER.info("MAP: "+eny.getKey()+ "  "+eny.getValue());
//		}
		
		File resultFile = new  File(modelsFile.getAbsoluteFile() + ".validated" );
		//write in the same order as the models file
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
		try {

			List<String> assertions = FileUtil.getLines(modelsFile);
			for ( String assertion : assertions ){
				ValidationResult result = null;
				
				CBMCClaim mapClaim = map.get(assertion);
				if ( mapClaim != null ){
					result = mapClaim.getResult();
				}
				
				if( result == null ){
					LOGGER.info("Assertion not verified: "+assertion);
					map.remove(assertion);
					continue;
				}
				//			if ( result != ValidationResult.VALID ){
				//				result = ValidationResult.INVALID;
				//			}

				writer.write(assertion+"\t"+result.toString()+"\t"+mapClaim.getLine());
				writer.newLine();
			}

		} finally {
			writer.close();
		}
		
		return map;
	}

	private static HashMap<String, List<ProcessedAssertion>> groupProcessedAssertionsByFile(
			List<ProcessedAssertion> assertionsInjected) {
		HashMap<String, List<ProcessedAssertion>> claimsPerFile = new HashMap<String,List<ProcessedAssertion>>();
		
		for ( ProcessedAssertion assertion: assertionsInjected ){
			String file = assertion.getRelativePath();
			List<ProcessedAssertion> fileClaims = claimsPerFile.get(file);
			
			if ( fileClaims == null ){
				fileClaims = new ArrayList<ProcessedAssertion>();
				claimsPerFile.put(file, fileClaims);
			}
			
			LOGGER.fine("PUTTING ASSERTION : "+assertion +" ["+file+"]");
			fileClaims.add(assertion);
		}
		return claimsPerFile;
	}

	public static Map<String, CBMCClaim> asociateValidationResultToInjectedAssertions(
			HashMap<String, List<CBMCClaim>> claimsPerFile,
			HashMap<String,List<ProcessedAssertion>> assertionsPerFile) {
		Map<String,CBMCClaim> map = new HashMap<String, CBMCClaim>();

		int assertionIndex = -1;
		for( Entry<String, List<ProcessedAssertion>> entry : assertionsPerFile.entrySet() ){
			String fileName = entry.getKey();
			List<ProcessedAssertion> injectedAssrtions = entry.getValue();
			
			List<CBMCClaim> claimsWithResults = claimsPerFile.get(fileName);
			
			if ( claimsWithResults == null ){
				claimsWithResults = claimsPerFile.get(fileName.replace('\\', '/'));
			}
			
			if ( claimsWithResults == null ){
				
				if ( injectedAssrtions.size() > 0 ){
					LOGGER.info("Missing evaluated assertions for file "+fileName+". It could be the case that the function with the assertions is never called. Assertions injected: "+injectedAssrtions);
				}
				//
				continue;
			}
			
			
			Collections.sort(claimsWithResults, new Comparator<CBMCClaim>() {
				@Override
				public int compare(CBMCClaim o1, CBMCClaim o2) {
					return o1.getLine()-o2.getLine();
				}
			});
			
			HashMap<Integer, ProcessedAssertion> assertionsLineMap = new HashMap<Integer, ProcessedAssertion>();
			for ( ProcessedAssertion injectedAssrtion : injectedAssrtions ){
				assertionsLineMap.put(injectedAssrtion.getSourcelineNo(), injectedAssrtion);
			}
			
//			if ( claimsWithResults.size() != injectedAssrtions.size() ){
//				LOGGER.warning("File "+fileName+": Different size for evaluated claims ("+claimsWithResults.size()+") and injected assertions ("+injectedAssrtions.size()+")");
//				LOGGER.warning("Claims: "+claimsWithResults );
//				LOGGER.warning("Assertions: "+injectedAssrtions );
//				
//				helpDebuggingDifferentClaimsAndAssertions( claimsWithResults, injectedAssrtions );
//				
//				throw new IllegalStateException("Different size for injected assertions and evaluated ones for file "+fileName);
//			}
			
			for ( CBMCClaim claim : claimsWithResults ){
				if ( ! claim.isBctAssertion() ){
					LOGGER.warning("Claim not generated by BCT "+claim.getClaimId()+" "+claim.getFileName()+" "+claim.getLine());
					continue;
				}
				
				ProcessedAssertion correspondingAssertion = assertionsLineMap.get(claim.getLine());
				
				if ( correspondingAssertion == null ){
					LOGGER.warning("Corresponding assertion missing for claim "+claim.getClaimId()+" "+claim.getFileName()+" "+claim.getLine());
					continue;
				}
				
				String stringAssertion = correspondingAssertion.toStringWithFunctionNameOrLine();
				ValidationResult res = null;
				CBMCClaim mapClaim = map.get(stringAssertion);
				if ( mapClaim != null ){
					res = mapClaim.getResult();
				}
				if ( res == null || res == ValidationResult.VALID ){
					//this way INVALID assertions involving return values are 
					//considered INVALID if they are VALID at some point but not in other points 
					System.out.println("PUT: "+stringAssertion+" "+claim.getResult()+" "+claim.getClaimId());
					map.put(stringAssertion, claim);	
				}
			}
			
//			for ( String injectedAssrtion : injectedAssrtions ){
//				CBMCClaim claim = claimIt.next();
////				claim.
//				ValidationResult res = map.get(injectedAssrtion);
//				if ( res == null || res == ValidationResult.VALID ){
//					//this way INVALID assertions involving return values are 
//					//considered INVALID if they are VALID at some point but not in other points 
//					System.out.println("PUT: "+injectedAssrtion+" "+claim.getResult()+" "+claim.getClaimId());
//					map.put(injectedAssrtion, claim.getResult());	
//				}
//				
//			}

		}
		return map;
	}

	private static void helpDebuggingDifferentClaimsAndAssertions(
			List<CBMCClaim> claimsWithResults, List<String> injectedAssrtions) {
		// TODO Auto-generated method stub
		Iterator<CBMCClaim> claimIt = claimsWithResults.iterator();
		for ( String injectedAssrtion : injectedAssrtions ){
			CBMCClaim claim = claimIt.next();
		
			String line = injectedAssrtion.split("\t")[1];
			Integer assertionLineNumber = Integer.valueOf(line); 
			
			if ( ! assertionLineNumber.equals(claim.getLine()) ){
				LOGGER.warning("Mismatch for claim "+ claim + " and assertion " + injectedAssrtion );
				break;
			}
		}
		
		if ( claimIt.hasNext() ){
			LOGGER.warning("Claims not injected by BCT: " );
			while ( claimIt.hasNext() ){
				LOGGER.warning("Claim not injected by BCT: "+claimIt.next() );
			}
		}
	}

	private static HashMap<String, List<String>> groupAssertionsByFile(
			List<String> assertionsInjected) {
		HashMap<String, List<String>> claimsPerFile = new HashMap<String,List<String>>();
		
		for ( String assertion : assertionsInjected ){
			String file = assertion.split("\t")[0];
			List<String> fileClaims = claimsPerFile.get(file);
			
			if ( fileClaims == null ){
				fileClaims = new ArrayList<String>();
				claimsPerFile.put(file, fileClaims);
			}
			
			LOGGER.fine("PUTTING ASSERTION : "+assertion +" ["+file+"]");
			fileClaims.add(assertion);
		}
		return claimsPerFile;
		
	}

	public static HashMap<String, List<CBMCClaim>> groubClaimsByFile(
			List<CBMCClaim> claims) {
		HashMap<String, List<CBMCClaim>> claimsPerFile = new HashMap<String,List<CBMCClaim>>();
		for ( CBMCClaim claim : claims ){
			List<CBMCClaim> fileClaims = claimsPerFile.get(claim.getFileName());
			
			if ( fileClaims == null ){
				fileClaims = new ArrayList<CBMCClaim>();
				claimsPerFile.put(claim.getFileName(), fileClaims);
			}
			
			LOGGER.fine("PUTTING CLAIM "+claim.getFileName()+" "+claim.getClaimId()+" "+claim.getResult());
			fileClaims.add(claim);
		}
		return claimsPerFile;
	}

	private static void checkSameSizeOfAssertions(List<String> assertions,
			List<CBMCClaim> result) {
		int size =result.size();

		Preconditions.checkArgument(assertions.size() == size, "The number of models and the number of validated assertions should be the same, assertions="+assertions.size()+" validatedAssertion="+size);

	}

	public int getUnwindN() {
		return unwindN;
	}

	public void setUnwindN(int unwindN) {
		this.unwindN = unwindN;
	}

	public void setCBMCOutput(List<String> cbmcOutputLines) {
		this.cbmcOutputLines = cbmcOutputLines;
	}

}
