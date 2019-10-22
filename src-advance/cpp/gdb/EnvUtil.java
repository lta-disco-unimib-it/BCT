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
package cpp.gdb;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import conf.management.ConfigurationFilesManager;

import util.JavaRunner;
import util.ProcessRunner;

public class EnvUtil {
	///PROPERTIES-DESCRIPTION: Options that control the execution environment
	
	///indicates the path of the cygwin folder on windows
	public static final String CYGWIN_FOLDER = "bct.cygwin";
	
	///indicate the max heap size to use (e.g. 1024M)
	private static final String BCT_MAX_HEAP = "bct.maxHeap";

	///indicate the default drive to be used by BCT on windows
	private static final String BCT_DRIVE = "bct.drive";
	
	private static final String customDrive;
	private static final boolean isWindows;
	private static final boolean isOsX;
	private static boolean is64Arch;

	private static boolean isLinux;


	static {
		customDrive = System.getProperty(BCT_DRIVE,"C")+":\\\\";
		isWindows = checkIsWindows();
		is64Arch = checkIs64();
		isOsX = checkIsOsX();
		isLinux = checkIsLinux();
	}

	public static boolean isLinux() {
		return isLinux;
	}
	
	public static boolean isOsX() {
		return isOsX;
	}

	private static boolean checkIsLinux() {
		String osName = System.getProperty("os.name");
		if ( osName.startsWith("Linux") ){
			return true;
		}
		return false;
	}

	public static String getTmpFolderPath(){
		return getTmpFolder().getAbsolutePath();
	}
	
	public static File getTmpFolder(){
		String f;
		if ( isWindows() ){
			f = getBctDefaultDrive()+"\\tmp";
		} else {
			f = "/tmp";
		}
		
		File folder = new File(f);
		if (! folder.exists() ){
			folder.mkdirs();
		}
		
		return folder;
	}
	
	public static String getBctDefaultDrive(){
		return customDrive;
	}

	public static boolean isWindows() {
		return isWindows;
	}
	
	public static boolean checkIsOsX() {
		String osName = System.getProperty("os.name");
		if ( osName.startsWith("Mac OS X") ){
			return true;
		}
		return false;
	}
	
	public static boolean checkIsWindows() {
		String osName = System.getProperty("os.name");
		if ( osName.contains("Windows") ){
			return true;
		}
		return false;
	}
	
	public static boolean checkIs64() {
		String arch = System.getProperty("os.arch");
		if ( checkIsWindows() ){
			String parch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

			arch = parch.endsWith("64")
			                  || wow64Arch != null && wow64Arch.endsWith("64")
			                      ? "64" : "32";
		}
		
		return arch.endsWith("64");
	}

	

	private static HashMap<String,String> cygPathMap = new HashMap<String,String>();

	private static String bctJarPath;
	public static void setBctJarPath(String bctJarPath) {
		EnvUtil.bctJarPath = bctJarPath;
	}

	public static String getWinPathFromCygWinPath(String cygWinPath) {
		String winPath = cygPathMap.get(cygWinPath); 
		if ( winPath != null ){
			return winPath;
		}
		ArrayList<String> cygPathCommand = new ArrayList<String>();
		cygPathCommand.add("cygpath");
		cygPathCommand.add("-w");
		cygPathCommand.add(cygWinPath);
		
		Appendable outputBuffer = new StringBuffer();
		Appendable errorBuffer = new StringBuffer();
		try {
			ProcessRunner.run(cygPathCommand, outputBuffer, errorBuffer, 15);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		winPath = outputBuffer.toString().trim();
		cygPathMap.put(cygWinPath, winPath);
		return winPath;
	}

	
	




	public static String getCppFiltPath() {
		if ( isWindows() ){
			return getCygwinBinFolderPath()+"\\c++filt.exe";
		} else {
			return "/usr/bin/c++filt";
		}
	}
	
	public static String getCygWinFolderPath() {
		String cygfolder = System.getProperty(EnvUtil.CYGWIN_FOLDER);
		if ( cygfolder == null ){
			cygfolder = "c:\\cygwin";
		}
		return cygfolder;
	}

	public static String getCygwinBinFolderPath() {
		return getCygWinFolderPath()+"\\bin";
	}


	public static String getGdbExecutablePath() {
		String gdb;
		if ( EnvUtil.isWindows() ){
			
			gdb = getCygwinBinFolderPath()+"\\gdb.exe";
		} else {
			gdb = "/usr/bin/gdb";
		}
		return gdb;
	}

	public static String getMakeExecutablePath() {
		String make;
		if ( EnvUtil.isWindows() ){
			
			make = getCygwinBinFolderPath()+"\\make.exe";
		} else {
			make = "/usr/bin/make";
		}
		return make;
	}


	public static String getMaxAvailableHeap() {
		String maxHeap = System.getProperty(BCT_MAX_HEAP);
		if ( maxHeap != null ){
			return "-Xmx"+maxHeap;
		}
		
		if ( EnvUtil.isWindows() ){
			return "-Xmx"+Runtime.getRuntime().maxMemory();
		}
		return "-Xmx2048M";
	}




	public static boolean isCygWin() {
		return isWindows();
	}


	public static String getOSAbsolutePath(String path) {
		if ( EnvUtil.isWindows() ){
			path = path.replace('/', '\\');
			path = getBctDefaultDrive()+path;
		}
		return path;
	}


	public static String getGotoCCProgram() {
		if ( EnvUtil.isWindows() ){
			return "goto-cl";
		}
		return "goto-cc";
	}
	
	public static String getGotoInstrumentProgram() {
		return "goto-instrument";
	}

	public static String getOSScriptPath(String logginfFilePath) {
		if ( !isWindows() ){
			return logginfFilePath;
		}
		
		logginfFilePath = logginfFilePath.replace("\\", "\\\\");
		return logginfFilePath;
	}

	public static String addTrailingSeparator(String sourcePrefixToRemove) {
		sourcePrefixToRemove = sourcePrefixToRemove + File.separator;
		if ( isWindows ){
			return getOSScriptPath(sourcePrefixToRemove);
		}
		return sourcePrefixToRemove;
	}

	public static boolean is64Arch() {
		return is64Arch;
	}

	public static String getShellPathToCopy(File file) {
		if ( isCygWin() ){
			return file.getAbsolutePath().replace("\\", "\\\\");
		}
		return file.getAbsolutePath();
	}

	public static String getPID() {
		String pname = ManagementFactory.getRuntimeMXBean().getName();
		int hatPos = pname.indexOf('@');
		if ( hatPos > 1 ){
			pname = pname.substring(0, hatPos);
		}
		return pname;
	}

	public static String getBctJarPath() {
		if ( bctJarPath == null ){
			return null;
		}
		return bctJarPath;
	}

	

//	public static String getBCTjar() {
//		String classpath = JavaRunner.getCurrentClassPath();
//		String cpEntries[];
//		if ( isWindows() ){
//			cpEntries = classpath.split(";");
//		} else {
//			cpEntries = classpath.split(":");
//		}
//		
//		for ( String cpString : cpEntries ){
//			if ( cpString.startsWith("bct") && cpString.endsWith(".jar") ){
//				return cpString;
//			}
//		}
//		
//		return null;
//	}





}
