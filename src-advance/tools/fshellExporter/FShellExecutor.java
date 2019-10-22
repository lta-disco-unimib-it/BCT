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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import tools.fshellExporter.FShellExecutor.FShellResult;
import util.FileUtil;
import util.ProcessRunner;

public class FShellExecutor {
	
	private String fshellPath = "fshell";
	private int unwind = 3;
	private String fshellOptions;
	
	public static class FShellResult implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private List<Integer> trueProperties = new ArrayList<Integer>();
		private List<Integer> falseProperties = new ArrayList<Integer>();
		private List<Integer> errorProperties = new ArrayList<Integer>();

		private List<String> properties;
		
		public List<String> getProperties() {
			return properties;
		}

		public FShellResult(List<String> properties) {
			this.properties = properties;
		}

		public void addTrueProperty( int x ){
			getTrueProperties().add(x);
		}
		
		public void addFalseProperty( int x ){
			getFalseProperties().add(x);
		}
		
		public void addErrorProperty( int x ){
			errorProperties.add(x);
		}

		public List<Integer> getFalseProperties() {
			return falseProperties;
		}

		public void setFalseProperties(List<Integer> falseProperties) {
			this.falseProperties = falseProperties;
		}

		public List<Integer> getTrueProperties() {
			return trueProperties;
		}

		public void setTrueProperties(List<Integer> trueProperties) {
			this.trueProperties = trueProperties;
		}
		
		public static void store( FShellResult result, File dest ) throws IOException{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dest));
			
			oos.writeObject(result);
		}
		
		public static FShellResult load( File src ) throws IOException, ClassNotFoundException{
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(src));
			
			return (FShellResult) oos.readObject();
		}
	}
	
	public FShellResult execute(File queryFile, File srcFolder, String args) throws IOException{
		List<File> files = new ArrayList<File>();
		for ( String arg : args.trim().split(" ") ){
			files.add(new File(arg));
		}
		
		return execute(queryFile, srcFolder, files);
	}
	
	public FShellResult execute(File queryFile, File srcFolder, List<File> filesToCheck) throws IOException{
		
		StringBuffer errorBuffer = new StringBuffer();
		StringBuffer outputBuffer = new StringBuffer();
		
		List<String> command = new ArrayList<String>();
		
		command.add(fshellPath);
		
		if ( fshellOptions != null) {
			for ( String opt : fshellOptions.split(" ") ){
				command.add(opt);
			}
		} else {
			command.add("--unwind");
			command.add( ""+unwind );
		}
		
		command.add("--query-file");
		command.add(queryFile.getAbsolutePath());
		
		for ( File f : filesToCheck ){
			command.add(f.getPath());
		}
		
		ProcessRunner.run(command , outputBuffer, outputBuffer, 0, srcFolder);
		
		System.err.println("FSHELL ouput:");
		System.err.println(outputBuffer.toString());

		System.err.println(errorBuffer.toString());
		
		System.out.println("FSHELL-parsing:");
		return generateResults( FileUtil.getLines(queryFile), outputBuffer.toString() );
	}

	enum State { VALID, INVALID, ERROR }
	
	private FShellResult generateResults(List<String> properties, String outputBuffer) throws IOException {

		State state = null;
		
		BufferedReader br = new BufferedReader(new StringReader(outputBuffer));

		FShellResult fr = new FShellResult(properties);

		String line;

		int q = -2;
		//Starting from -2 because the fist time Starting Bounded Model Checking appears
		
		int counter = 0;
		boolean empty = false;
		boolean emptyTargetNext = false;
		while ( ( line = br.readLine() ) != null ) {
			counter++;
			System.out.println("EXEC: "+line);
			if ( line.startsWith("Starting Bounded Model Checking") ){

				System.out.println("SBMC "+q+" "+counter);
				if ( q >= 0 ){			
					saveResult(state, fr, q, empty);
				}
				state=null;
				q++;

				empty=emptyTargetNext;
				emptyTargetNext=false;
				continue;
			} 
			
			if ( line.trim().endsWith("evaluates to empty target graph") ){
				emptyTargetNext=true;
				continue;
			}
			
			if ( line.startsWith("Test cases: 0" ) ){
				System.out.println("!!!-VALID?");
				if ( state == null ){
					System.out.println("!!!-VALID");
					state = State.VALID;
				}
				continue;
			}
			
			if ( line.startsWith("Test cases:") ){
				System.out.println("!!!-INVALID");
				state = State.INVALID;
				continue;
			}
		}
		
		System.out.println("FShellExecutor read "+counter+" lines");
		
		saveResult(state, fr, q, emptyTargetNext);
		
		return fr;

	}

	private void saveResult(State state, FShellResult fr, int q, boolean empty) {
		System.out.println("EXEC: SAVE "+q+" "+state);
		if ( state == null ){
			return;
		}
		
		if ( empty ){
			fr.addErrorProperty(q);
			return;
		}
		
		switch ( state ){
		case VALID:
			fr.addTrueProperty(q);
			break;
		case INVALID:
			fr.addFalseProperty(q);
			break;
		}
	}

	
	
	public static void main(String args[]) throws IOException{
		
		FShellExecutor ex = new FShellExecutor();
		
		File queryFile = new File (args[0]);
		
		File srcFolder = new File (args[1]);
		
		String filesToCheck = "";
		for ( int i = 2; i < args.length; i++ ){
			filesToCheck += " "+args[i]; 
		}
		
		if ( filesToCheck.contains(" -- ") ){
			int fshellOptsStart = filesToCheck.indexOf(" -- ");
			String options = filesToCheck.substring(fshellOptsStart+4);
			ex.setFshellOptions(options);
			
			filesToCheck = filesToCheck.substring(0,fshellOptsStart);
		}
		
		FShellResult result = ex.execute(queryFile, srcFolder, filesToCheck);
		
		List<String> lines = FileUtil.getLines(queryFile);
		
		System.out.println(result.getTrueProperties());
		
		System.out.println("Models that hold: ");
		for ( Integer hoding : result.getTrueProperties() ){
			System.out.println("\t"+lines.get(hoding));
		}
		
		System.out.println("Models that do not hold: ");
		for ( Integer hoding : result.getFalseProperties() ){
			System.out.println("\t"+lines.get(hoding));
		}
		
	}



	public String getFshellOptions() {
		return fshellOptions;
	}

	public void setFshellOptions(String fshellOptions) {
		this.fshellOptions = fshellOptions;
	}

	public void setFShellPath(String fshellCommand) {
		fshellPath=fshellCommand;
	}
	
	
	
}
