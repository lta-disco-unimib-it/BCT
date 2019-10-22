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
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;

import cpp.gdb.FunctionMonitoringData.ReturnData;
import daikon.inv.unary.scalar.IsPointer;

import util.FileUtil;

public class FunctionMonitoringData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(FunctionMonitoringData.class.getCanonicalName());

	@Override
	public String toString() {
		return "[FUNCTION "+getMangledName()+

				" location:"+getSourceFileLocation()+":"+getFirstSourceLine()+":"+getLastSourceLine()+

				" target:"+isTargetFunction()

				+" implementedInProject:"+isImplementedWithinProject()

				+" calledByTarget:"+isCalledByTargetFunction()

				+" callerOfTarget:"+isCallerOfTargetFunction()
				+" calledByParentOfTarget:"+isCalledByParentOfTargetFunction()
				+"]";
	}

	public void resetCallersAndCallees(Collection<FunctionMonitoringData> data ){
		callees = new HashSet<FunctionMonitoringData>();
		resetCallersAndCallees(callees, calleeNames, data );

		callers = new HashSet<FunctionMonitoringData>();
		resetCallersAndCallees(callers, callerNames, data );
	}

	private void resetCallersAndCallees(Set<FunctionMonitoringData> callees,
			Set<String> names,
			Collection<FunctionMonitoringData> data) {
		for ( FunctionMonitoringData function : data ){
			if ( names.contains(function.getMangledName()) ){
				callees.add(function);
			}
		}
	}

	public File getSourceFileClean() {

		return new File( getSourceFileLocationClean() );

	}

	public String getSourceFileLocationClean() {

		return FileUtil.getCleanPath(sourceFileLocation);



	}

	public File getSourceFile() {
		return new File ( sourceFileLocation );
	}

	public String getSourceFileLocation() {
		return sourceFileLocation;
	}

	public boolean implementedWithinProject;


	public boolean isImplementedWithinProject() {
		return implementedWithinProject;
	}

	public void setImplementedWithinProject(boolean implementedWithinProject) {
		this.implementedWithinProject = implementedWithinProject;
	}



	private String mangledName;
	private String demangledName;
	private String addressEnter;
	private Set<String> addressesExit = new HashSet<String>();
	private Set<String> callpoints = new HashSet<String>();
	private Set<String> addressesAfterCallPoints = new HashSet<String>();

	public boolean isCallerOfTargetFunction() {
		return callerOfTargetFunction;
	}

	public Set<String> getAddressesAfterCallPoints() {
		return addressesAfterCallPoints;
	}

	public Set<String> getCallpoints() {
		return callpoints;
	}

	private Set<String> callerNames = new HashSet<String>();
	private Set<String> calleeNames = new HashSet<String>();

	private transient Set<FunctionMonitoringData> callers = new HashSet<FunctionMonitoringData>();
	private transient Set<FunctionMonitoringData> callees = new HashSet<FunctionMonitoringData>();

	///SourceFileLocation is the location of the file relative to the source folder
	private String sourceFileLocation;

	//	public void resetCallersAndCallees( List<FunctionMonitoringData> functionMonitoringData ){
	//		
	//	}

	public static class IntComparator implements Comparator<LineData>, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(LineData arg0, LineData arg1) {
			return arg0.getLineNumber()-arg1.getLineNumber();
		}
	};
	private IntComparator myComp = new IntComparator();
	private TreeSet<LineData> lines = new TreeSet<LineData>(myComp);
	private File absoluteFile;

	private ArrayList<String> jmpToAddresses = new ArrayList<String>(); //the address is raw, without leading zeroes
	private ArrayList<LineData> jumpingLines = new ArrayList<LineData>();
	private ArrayList<String> jumpingAddresses = new ArrayList<String>();
	private String addressOfFirstInstructionOfLastLine;
	private boolean lastLineContainsOnlyReturn = false;
	private String returnType;
	private String lastFldlArg;

	private transient LineData lastLine;
	public boolean isCalledByTargetFunction() {
		return calledByTargetFunction;
	}

	public void setCalledByTargetFunction(boolean calledByTargetFunction) {
		this.calledByTargetFunction = calledByTargetFunction;
	}

	private Boolean enterExit = null;
	private boolean targetFunction;
	private boolean callerOfTargetFunction;
	private boolean calledByTargetFunction;

	private Integer parNum = null;
	private boolean calledByParentOfTargetFunction;
	//	private boolean calleeOfTargetFunction;
	private boolean cppUnitTestCase;
	private int parametersNumber = -1;

	private boolean isInline;

	private boolean isStatic;


	public boolean isCalledByParentOfTargetFunction() {
		return calledByParentOfTargetFunction;
	}

	public boolean isTargetFunction() {
		return targetFunction;
	}

	public void addCaller( FunctionMonitoringData caller, String address ){
		callers.add( caller);
		callpoints.add(address);
		callerNames.add(caller.getMangledName());
	}

	public void addCallee( FunctionMonitoringData callee ){
		callees.add( callee);
		calleeNames.add(callee.getMangledName());
	}

	public FunctionMonitoringData(String mangledName, String demangledName,
			String addressEnter ) {
		super();
		System.out.println("!!!FunctionMonitoringData: "+mangledName+" "+demangledName+" "+addressEnter);
		this.mangledName = mangledName;
		this.demangledName = demangledName;
		if ( addressEnter == null ){
			throw new IllegalArgumentException("Enter adress null for "+mangledName+"( "+demangledName+" )");
		}
		this.addressEnter = addressEnter;
	}


	public FunctionMonitoringData(String mangledName ) {
		super();
		this.mangledName = mangledName;
	}

	public boolean isStatic(){
		return StaticFunctionsFinderFactory.getInstance().isStatic( this );
	}

	public boolean isConsructor(){
		//		return isConstructorDestructor('C');
		return isConstructorDestructor(true);
	}

	public boolean isDestructor(){
		//		return isConstructorDestructor('D');
		return isConstructorDestructor(false);
	}

	public boolean isConstructorDestructor(boolean constructor){
		FunctionMonitoringDataTransformerDemangled tr = new FunctionMonitoringDataTransformerDemangled();

		String functionName = tr.getMethodName(this);
		String className = tr.getClassNameNoTemplates(this);

		//		System.out.println(functionName+" "+className);
		LOGGER.fine("isCOnstructorDestructor "+className+" "+functionName);
		if ( ! constructor ){
			if ( ! functionName.startsWith("~") ){
				return false;
			}
			functionName = functionName.substring(1);
		}

		return className.equals(functionName);
	}
	//		public boolean isConstructorDestructor(char id){
	//		if ( functionName.)
	//		
	//		int parStart = mangledName.lastIndexOf('E');
	//		
	//		if ( parStart < 0 ){
	//			System.out.println("isConstructorDestructor cannot find E in mangledName "+mangledName);
	//			return true;
	//		}
	//		
	//		//check for not empty parameter list
	//		if ( mangledName.length() == ( parStart + 1 ) || ( ! ( mangledName.charAt(parStart+1) == 'v' ) ) ) { 
	//				String subName = mangledName.substring(0, parStart);
	//				parStart = subName.lastIndexOf('E');
	//		}
	//		
	//		int idx = 0;
	//		for ( idx = parStart-1; mangledName.charAt(idx) >= '0' && mangledName.charAt(idx) <= '9' ; idx-- );
	//		
	//		
	//		if ( mangledName.charAt(idx) == id ){
	//			return true;
	//		}
	//		
	//		return false;
	//	}

	public void addAddressExit( String address ){
		this.addressesExit.add( address );
	}

	public String getMangledName() {
		return mangledName;
	}
	public void setMangledName(String mangledName) {
		this.mangledName = mangledName;
	}
	public String getDemangledName() {
		LOGGER.fine("demangled name for "+this.mangledName+" "+demangledName);
		if ( demangledName == null ){
			int hatPos = mangledName.indexOf('@');
			String mangledNoHat;
			if ( hatPos > 0 ){
				mangledNoHat = mangledName.substring(0,hatPos);
			} else {
				mangledNoHat = mangledName;
			}
			demangledName = Demangler.INSTANCE.demangle(mangledNoHat);
		}
		return demangledName;
	}

	public String getAddressEnter() {
		return addressEnter;
	}

	public void setEnterAddress(String address) {
		addressEnter = address;
	}

	public Set<String> getExitAddresses() {
		return addressesExit;
	}

	public Set<FunctionMonitoringData> getCallees() {
		return callees;
	}

	public Set<FunctionMonitoringData> getCallers() {
		return callers;
	}

	public void setSourceFileLocation(String fileLocation) {
		sourceFileLocation = fileLocation;
	}


	public String getSourceFileName() {

		if ( sourceFileLocation == null ){
			return null;
		}

		int lastDir = sourceFileLocation.indexOf(File.separator);
		return sourceFileLocation.substring(lastDir+1);
	}

	public String getSourceFolder() {

		if ( sourceFileLocation == null ){
			return null;
		}


		int lastDir = sourceFileLocation.indexOf(File.separator);
		if ( lastDir == -1 ){
			return "";
		}

		return sourceFileLocation.substring(0, lastDir);
	}

	public void addAddressAfterCall(String address) {
		addressesAfterCallPoints.add( address );
	}

	public void addLine(LineData lineData) {
		lines.add(lineData);
		lastLine = lineData;
	}

	public TreeSet<LineData> getLines() {
		return lines;
	}

	public LineData getFirstSourceLineData() {
		if ( lines.size() == 0 ){
			return null;
		}
		return lines.iterator().next();
	}

	public int getFirstSourceLine() {
		if ( lines.size() == 0 ){
			return -1;
		}
		return lines.iterator().next().getLineNumber();
	}

	public void setAbsoluteSourceFileLocation(String fileLocation) {
		absoluteFile = new File( fileLocation );
	}

	public File getAbsoluteFile() {
		return absoluteFile;
	}

	public void setAbsoluteFile(File absoluteFile) {
		this.absoluteFile = absoluteFile;
	}

	public int getLastSourceLine() {
		if ( lines.size() == 0 ){
			return -1;
		}
		return lines.last().getLineNumber();
	}

	public LineData getLastSourceLineData() {
		if ( lines.size() == 0 ){
			return null;
		}
		return lines.last();
	}

	public boolean containsLine(int correspondingLine) {
		return correspondingLine >= getFirstSourceLine() && correspondingLine <= getLastSourceLine();
	}

	public void addJmpToAddress(String address, String jmpToAddress) {
		jmpToAddresses.add( jmpToAddress );

		if ( lines.size() > 0 ){
			jumpingLines.add( lines.last() );
		} else {
			jumpingLines.add( null );
		}

		jumpingAddresses.add( address );
	}

	/**
	 * Adds the address of the first instruction that follows the last line of the method.
	 * This address is used in case of multiple return instructions.
	 * In fact it is the address where jmp instructions corresponding to "return" are going to
	 * 
	 * @param addressOfInstructionFollowingLastLocation
	 */
	public void addAddressOfFirstInstrcutionOfLastLine(
			String addressOfInstructionFollowingLastLocation) {
		addressOfFirstInstructionOfLastLine = addressOfInstructionFollowingLastLocation; 
	}

	public static class ReturnData{

		private String jmpingAddress;
		private LineData jmpingLine;

		public ReturnData(String jmpingAddress, LineData jmpingLine) {
			this.jmpingAddress = jmpingAddress;
			this.jmpingLine = jmpingLine;
		}

		public String getJmpingAddress() {
			return jmpingAddress;
		}

		public LineData getJmpingLine() {
			return jmpingLine;
		}

	}

	public List<ReturnData> getAdditionalReturns(){
		List<ReturnData> additionalReturns = new ArrayList<ReturnData>();
		if ( addressOfFirstInstructionOfLastLine == null ){
			return additionalReturns;
		}

		for ( int i = 0; i < jmpToAddresses.size(); i++ ){
			if ( addressOfFirstInstructionOfLastLine.equals( jmpToAddresses.get(i) ) ){
				String jmpingAddress = jumpingAddresses.get(i);
				LineData jmpingLine = jumpingLines.get(i);
				ReturnData returnData = new ReturnData(jmpingAddress, jmpingLine);
				additionalReturns.add(returnData);
			}
		}

		return additionalReturns;
	}

	public boolean hasValidAddresses() {
		if ( ! isHexAddress(addressEnter) ){
			return false;
		}
		for ( String exit : addressesExit ){
			if ( ! isHexAddress( exit ) ){
				return false;
			}
		}
		return true;
	}

	public boolean isHexAddress(String addressEnter) {
		if ( addressEnter == null ){
			return false;
		}
		return addressEnter.matches("\\p{XDigit}+");
	}

	public void setLastLineContainsOnlyReturn(boolean b) {
		lastLineContainsOnlyReturn = b;
	}

	public boolean isLastLineContainsOnlyReturn() {
		return lastLineContainsOnlyReturn;
	}

	public int getParametersNumber() {
		if ( parNum == null ){
			parNum = ReturnTypesRegistry.getParameters( this );
			if ( parNum == null || parNum == -1 ){

				parNum = parametersNumber;

			}
		}
		return parNum;
	}

	public Set<String> getPointerArgs() {
		return ReturnTypesRegistry.getPointerArgs( this );
	}

	public List<Parameter> getAllArgs() {
		return ReturnTypesRegistry.getAllArgs( this );
	}

	public Set<String> getReferenceArgs() {
		return ReturnTypesRegistry.getReferenceArgs( this );
	}

	public Set<String> getScalarArgs() {
		return ReturnTypesRegistry.getPointerArgs( this );
	}

	public String getReturnType() {
		if ( returnType == null ){
			String rt = ReturnTypesRegistry.getReturnType( this );
			processReturnTypeString(rt);
		}

		if ( returnType == null ){
			try {
				List<String> fileLines = FileUtil.getLines(getAbsoluteFile());
				String line = fileLines.get(getFirstSourceLine()-1);
				String rt = extractReturnTypeFromDeclaration( line );
				processReturnTypeString(rt);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return returnType;
	}

	public void processReturnTypeString(String rt) {
		if ( rt != null ){
			String[] splitted = rt.split("\\s");
			returnType = "";
			for( String s : splitted ){
				if ( s.equals("inline") ){
					isInline=true;
				} else if ( s.equals("static") ){
					isStatic=true;
				} else {
					returnType+=s;
				}
			}
		}
	}

	private String extractReturnTypeFromDeclaration(String line) {
		String[] tokens = line.split(" ");
		return tokens[0];
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public void setLastFldlArg(String jmpToAddress) {
		lastFldlArg = jmpToAddress;
	}

	public String getLastFldlArg() {
		return lastFldlArg;
	}

	public void setAddressOfLastLine(String address) {
		lastLine.setAddress( address );
	}

	public boolean hasExitAddresses() {
		return addressesExit.size() > 0;
	}

	public boolean isEnterExit() {
		if ( enterExit == null ){
			for ( String exitAdress  : addressesExit  ){
				if ( exitAdress.equals(addressEnter) ){
					enterExit = Boolean.TRUE;
				}
			}
			if ( enterExit == null ){
				enterExit = Boolean.FALSE;
			}
		}
		return enterExit;
	}

	public void setTargetFunction(boolean b) {
		targetFunction = b;
	}

	public void setCallerOfTargetFunction(boolean b) {
		callerOfTargetFunction = b;
	}

	public List<LocalVariableDeclaration> getLocalVariableDeclarations() {
		return ReturnTypesRegistry.getLocalVariableDeclarations( this );
	}

	public void setCalledByParentOfTargetFunction(boolean b) {
		calledByParentOfTargetFunction = b;
	}

	public boolean isCalleeOfMonitoredFunction(){
		return calledByParentOfTargetFunction || calledByTargetFunction;
	}

	public void setCppUnitTestCase(boolean b) {
		cppUnitTestCase = b;
	}

	public boolean isCppUnitTestCase() {
		return cppUnitTestCase;
	}

	public void setParametersNumber(int size) {
		parametersNumber = size;
	}

	public boolean hasParametersNumber() {
		return parametersNumber > -1;
	}

	public List<FunctionMonitoringData> getInvokedTargets() {
		List<FunctionMonitoringData> targets = new ArrayList<FunctionMonitoringData>();
		for ( FunctionMonitoringData callee : callees ){
			if ( callee.isTargetFunction() ){
				targets.add(callee);
			}
		}
		return targets;
	}

	public boolean returnsInLine(LineData lineData) {
		for ( ReturnData ret : getAdditionalReturns() ){
			if ( ret.getJmpingLine().equals(lineData) ){
				return true;
			}
		}
		return false;
	}

	public boolean isCpp() {
		return getDemangledName().contains("::");
	}

	public Parameter getReturnParameter() {
		String returnType = getReturnType(); //this.returnType might not have been initialized yet
		int stars = returnType.indexOf('*');
		String rt;
		boolean isPointer = false;
		int pointersN = 0;;
		if ( stars >= 0 ){
			pointersN  = returnType.substring(stars).trim().length();
			rt = returnType.substring(0, stars);
			isPointer = true;
		} else {
			rt = returnType;
		}


		return new Parameter(rt, "BCT_return", isPointer, false, pointersN);
	}

	public boolean isVoidReturn() {
		if ( returnType == null ){
			return true;
		}

		if ( "void".equals(returnType) && !returnType.contains("*") ){
			return true;
		}


		return returnType.isEmpty();
	}


	public List<String> getSourceCodeLines() throws FileNotFoundException{
		List<String> srcLines = FileUtil.getLines(getAbsoluteFile());
		return srcLines.subList(getFirstSourceLine(), getLastSourceLine()-1);
	}
	
	public String getSourceCode() throws FileNotFoundException{
		StringBuffer sb = new StringBuffer();
		
		for( String line : getSourceCodeLines() ){
			sb.append(line);
			sb.append("\n");
		}
		
		return sb.toString();
	}

}
