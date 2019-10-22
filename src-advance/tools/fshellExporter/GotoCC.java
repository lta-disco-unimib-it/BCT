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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.io.Files;

import util.FileUtil;
import util.JavaRunner;
import util.ProcessRunner;
import cpp.gdb.CSourcesFilter;
import cpp.gdb.EnvUtil;
import cpp.gdb.GccCompileAndFix;
import cpp.gdb.GotoCompileAndFix;

public class GotoCC {
	///PROPERTIES-DESCRIPTION: Options for the execution of goto-cc
	
	///if false redefines some functions implemented by libc that create problem with goto-cc, otherwise skips this workaround (dafault false)
//	private static final String BCT_GOTOCC_SKIP_FUNC_DEFS_WORKAROUND = "bct.gotocc.skipFuncDefsWorkaround";

	//	private ArrayList<String> additionalDefs;
	private String gotoProgram;
	private File srcFolder;
	
	private String gotoCCexecutable = EnvUtil.getGotoCCProgram();
	private String gotoInstrumentExecutable = EnvUtil.getGotoInstrumentProgram();
	
	public String getGotoCCexecutable() {
		return gotoCCexecutable;
	}

	public void setGotoCCexecutable(String gotoCCexecutable) {
		this.gotoCCexecutable = gotoCCexecutable;
	}

	public String getGotoProgram() {
		return gotoProgram;
	}

	public File getSrcFolder() {
		return srcFolder;
	}

	private String compileCommand;
	
	
	

	public GotoCC(String compileCommand, File gotoCCsrcFolder, String gotoProgram) {
		this.srcFolder = gotoCCsrcFolder;
		this.gotoProgram = gotoProgram;
		this.compileCommand =  compileCommand;
	}

	public String compileWithGotoCC() throws FileNotFoundException, IOException {
		
		buildKnownFunctionsDefinitions();

			File[] sources = srcFolder.listFiles(new CSourcesFilter());

//			if ( additionalDefs.size() > 0 ) {
//
//				for ( File src : sources ) {
//					File injSrc = src;
//					injectAdditionalDefsInFile(additionalDefs, injSrc);	
//				}
//			}

			
			
			boolean makefile = false; //indicate if the compilation was executed by automaticlaly identifying the presence of a makefile

			
			
			List<String> command = new ArrayList<String>();
			if ( compileCommand != null ){
				for ( String commandArg : compileCommand.split(" ") ){
					command.add(commandArg);
				}
			} else {
				File makeFile = new File( srcFolder, "Makefile" );
				if ( makeFile.exists() ){
					makefile = true;
					command.add("make");
					command.add("CC="+gotoCCexecutable+" "+knownFunctionsDefinitionsString() );
					
				} else {
					command.add(gotoCCexecutable);
					for ( File src : sources){
						command.add(src.getAbsolutePath());
					}

					command.add("-o");
					command.add(gotoProgram);
					
					for ( String def : knownFunctionsDefinitions() ){
						command.add(def);
					}
				}
				
				
			}
			try {
				System.out.println("Sleeping 5 secs...");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			Appendable buf = new StringBuffer();
			int result = ProcessRunner.run(command, buf, buf, 0, srcFolder);
			if ( result != 0 ){
				ArrayList<String> cmd = new ArrayList<String>();
				cmd.add("make");
				
				List<String> paths = new ArrayList<String>();
				String jar = EnvUtil.getBctJarPath();
				if ( jar != null ){
					paths.add( jar );
				}
				JavaRunner.runMainInClass(GccCompileAndFix.class, null, cmd, 0, paths, true, null, null, srcFolder );
				
				JavaRunner.runMainInClass(GotoCompileAndFix.class, null, command, 0, paths, true, null, null, srcFolder );
			}
			System.out.println(buf);
			
			if ( makefile ){
				File[] _files = srcFolder.listFiles();
				List<File> files = Arrays.asList(_files);
				java.util.Comparator<? super File> c = new java.util.Comparator<File>() {

					@Override
					public int compare(File o1, File o2) {
						if (o2.lastModified() > o1.lastModified()){
							return 1;
						}
						return -1;
					}
				};
				Collections.sort(files, c);
				for ( File f : files ){
					System.out.println("FILE-SORTED: "+f.lastModified());
				}
				gotoProgram = files.get(0).getAbsolutePath();
			}
			
			
			makeStaticNonDeterministic( gotoProgram );
			
			return gotoProgram;
		}
	
//	public void injectAdditionalDefsInFile(List<String> additionalDefs,
//			File injSrc) throws FileNotFoundException, IOException {
//		List<String> lines = FileUtil.getLines(injSrc);				
//		for ( String additionalDef : additionalDefs ){
//			lines.add(0, additionalDef);
//		}
//		FileUtil.writeToTextFile(lines, injSrc);
//	}
	
	private void makeStaticNonDeterministic(String gotoProgram) throws IOException {
		File gotoProgramInstrumented = new File( gotoProgram );
		File gotoProgramOriginal = new File( gotoProgram+".orig" );
		Files.copy(gotoProgramInstrumented, gotoProgramOriginal);
		
		
		ArrayList<String> command = new ArrayList<String>();
		command.add(gotoInstrumentExecutable);
		command.add("--nondet-static");
		command.add(gotoProgramOriginal.getAbsolutePath());
		command.add(gotoProgramInstrumented.getAbsolutePath());
		
		ProcessRunner.run(command, null, null, 0, srcFolder, null);
	}

	private String knownFunctionsDefinitionsString() {
		StringBuffer sb = new StringBuffer();
		for ( String def : knownFunctionsDefinitions() ){
			sb.append(" '"+def+"'");
		}
		return sb.toString();
	}

	private List<String> knownFunctionsDefinitionsList;
	private boolean redefineMathFuncs;

	private List<String> knownFunctionsDefinitions() {
		return knownFunctionsDefinitionsList;
	}
	
	private static String[] knownPositiveFunctions = new String[]{"sqrt","abs","fabsf","absf","fabs"};
	private void buildKnownFunctionsDefinitions() {
		
		knownFunctionsDefinitionsList = new ArrayList<String>();
		
//		boolean skipKnownDefinitionsWorkaround = Boolean.parseBoolean(System.getProperty(BCT_GOTOCC_SKIP_FUNC_DEFS_WORKAROUND));
//		if ( skipKnownDefinitionsWorkaround ){
//			return;
//		}
		if ( ! redefineMathFuncs ){
			return;
		}
		
		String positiveMacro = "(x)=((x)<0?-(x):(x))";
		
		for ( String f : knownPositiveFunctions ){
			knownFunctionsDefinitionsList.add("-D"+f+positiveMacro);
		}
		
	}

	public void setRedefineMathFuncs(boolean redefineMathFuncs) {
		this.redefineMathFuncs = redefineMathFuncs;
	}
}
