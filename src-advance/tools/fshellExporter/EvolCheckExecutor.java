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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import tools.fshellExporter.CBMCExecutor.ValidationResult;
import util.ProcessRunner;

public class EvolCheckExecutor extends CBMCExecutor {
	private static Logger LOGGER = Logger.getLogger(CBMCExecutor.class.getCanonicalName());
	
	public EvolCheckExecutor() {
		CBMC_EXCUTABLE = "evolcheck";
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
				int claimEnd = line.indexOf(':');
				String assertionId = line.substring(assertionStart+1, claimEnd);

				boolean bct_assertion = true;
				if ( ! line.endsWith("user supplied assertion") ){
					bct_assertion = false; 
				}
				
				line = reader.readLine().trim();
				int linePos = line.indexOf(" line ");
				int functionPos = line.indexOf(" function ");
				int filePos = line.indexOf("At: file ")+9;
				String fileName = line.substring(filePos,linePos);
				
				
				
				fileName = checkAndCorrectClaimFileLocation( fileName );

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
	
	@Override
	protected List<String> createMCcommand(File gotoCCProgram,
			String assertionId, String mainFunction) {
		List<String> command = new LinkedList<String>();
		command.add(CBMC_EXCUTABLE);
		if ( assertionId == null ){

		} else {
			command.add("--claim");
			command.add(assertionId);
		}
		
		command.add("--no-itp");
		
//		command.add("--function");
//		command.add(mainFunction);
		if ( getUnwindN() > 0 ){
			command.add("--unwind");
			command.add(""+getUnwindN());
		}

		command.add(gotoCCProgram.getAbsolutePath());
		return command;
	}
	
	protected void loadResultsFromCbmcOutput(
			List<String> cbmcOutputLines, List<CBMCClaim> claims) {

		HashMap<String,ValidationResult> res = new HashMap<String, CBMCExecutor.ValidationResult>();

		
		HashMap<String,CBMCClaim> claimsMap = new HashMap<String,CBMCClaim>();
		for ( CBMCClaim claim : claims ){
			claimsMap.put(claim.getClaimId(), claim);
		}
		
		String claimId = "";
		for ( String line : cbmcOutputLines ){
			
			
			if ( line.contains("Checking Claim #") ){
				int dash = line.indexOf("#");
				if ( dash < 0 ){
					continue;
				}
				
				claimId = line.substring(dash+1);
				int numberEnd = claimId.indexOf(" ");
				if ( numberEnd > 0 ){
					claimId = claimId.substring(0,numberEnd);
				}
				continue;
			} else if ( line.startsWith("Assertion(s) hold trivially.") ) {
				assignResultToClaim( claimsMap, claimId,  ValidationResult.VALID );
			} else if ( line.startsWith("VERIFICATION ") ){

					
					ValidationResult result;
					if ( line.startsWith("VERIFICATION SUCCESSFUL") ){
						result = ValidationResult.VALID;
					} else {
						result = ValidationResult.INVALID;
					}
					
					assignResultToClaim( claimsMap, claimId, result );
					
					
				}
			}

		
	}

	private void assignResultToClaim(HashMap<String,CBMCClaim> claimsMap, String claimId, ValidationResult result) {
		CBMCClaim claim = claimsMap.get(claimId);
		if ( claim == null ){
			LOGGER.warning("Claim not found '"+claimId+"'");
			LOGGER.info("Claims: "+claimsMap);
			return;
		}
		
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
