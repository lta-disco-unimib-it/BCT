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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.MatchingRule;
import util.componentsDeclaration.MatchingRuleInclude;

public class ModifiedFunctionsDetectorObjDumpListener implements
		ObjDumpParserListener, ModifiedFunctionsAnalysisResult {

	public static final String DELETED_FUNCTIONS_COMPONENT_NAME = "DeletedFunctions";
	public static final String ADDED_FUNCTIONS_COMPONENT_NAME = "AddedFunctions";
	public static final String MODIFIED_FUNCTIONS_COMPONENT_NAME = "ModifiedFunctions";
	
	@Override
	public Component getModifiedFunctionsComponent() {
		Component modifiedFunctionsComponent = new Component(MODIFIED_FUNCTIONS_COMPONENT_NAME);
		includeFunctionsInComponent(modifiedFunctionsComponent, modifiedFunctions);
		return modifiedFunctionsComponent;
	}

	@Override
	public Component getAddedFunctionsComponent() {
		Component modifiedFunctionsComponent = new Component(ADDED_FUNCTIONS_COMPONENT_NAME);
		includeFunctionsInComponent(modifiedFunctionsComponent, addedFunctions);
		return modifiedFunctionsComponent;
	}
	
	@Override
	public Component getDeletedFunctionsComponent() {
		Component modifiedFunctionsComponent = new Component(DELETED_FUNCTIONS_COMPONENT_NAME);
		includeFunctionsInComponent(modifiedFunctionsComponent, deletedFunctions);
		return modifiedFunctionsComponent;
	}

	private void includeFunctionsInComponent(
			Component modifiedFunctionsComponent,
			HashSet<String> functions) {
		for ( String function : functions ){
			includeFunctionInComponent(modifiedFunctionsComponent, function);
		}
	}


	/* (non-Javadoc)
	 * @see cpp.gdb.ModifiedFunctionsAnalysisResult#getModifiedFunctions()
	 */
	@Override
	public HashSet<String> getModifiedFunctions() {
		return modifiedFunctions;
	}

	private String curFunc;
	private HashMap<File, FileChangeInfo> fileChangesMap = new HashMap<File,FileChangeInfo>();
	private HashSet<String> modifiedFunctions = new HashSet<String>();
	private HashSet<String> allFunctions = new HashSet<String>();


	
	private boolean useDemangledNames = false;
	public boolean isUseDemangledNames() {
		return useDemangledNames;
	}

	public HashSet<String> getAllFunctions() {
		return allFunctions;
	}
	
	public void setUseDemangledNames(boolean useDemangledNames) {
		this.useDemangledNames = useDemangledNames;
	}

	private int lastPositionInCurrentFunction = -1;
	private int begin;
	private int end;
	private String curFuncLocation;
	private HashSet<String> addedFunctions;
	private HashSet<String> deletedFunctions;
	
	/* (non-Javadoc)
	 * @see cpp.gdb.ModifiedFunctionsAnalysisResult#getDeletedFunctions()
	 */
	@Override
	public HashSet<String> getDeletedFunctions() {
		return deletedFunctions;
	}


	public void setDeletedFunctions(HashSet<String> deletedFunctions) {
		this.deletedFunctions = deletedFunctions;
	}


	public ModifiedFunctionsDetectorObjDumpListener(
			List<FileChangeInfo> fileChanges) {
		for ( FileChangeInfo fileChange : fileChanges ){
			fileChangesMap.put(fileChange.getFile(),fileChange);
		}
	}

	@Override
	public void callInstruction(String address, String calleeName) {

	}

	@Override
	public void newFunction(String curFunc, String address) {
		System.out.println(curFunc);
		checkLastFUnction();
		
		this.curFunc = curFunc;
		lastPositionInCurrentFunction=-1;
		
		allFunctions.add(curFunc);
		resetFunctionLocationValues();
	}

	private void resetFunctionLocationValues() {
		begin=-1;
		end=-1;
		curFuncLocation=null;
	}

	private void checkLastFUnction() {
		if ( curFunc == null ){
			return;
		}
		
		if ( modifiedFunctions.contains(curFunc) ){
			return;
		}
		
		if ( curFuncLocation == null ){
			return;
		}
		
		File srcFile;
		try {
			srcFile = new File( new File(curFuncLocation).getCanonicalPath() );
		} catch (IOException e) {
			return;
		}
		
		FileChangeInfo changeInfo = fileChangesMap.get( srcFile );
		
		if ( changeInfo == null ){
			return;
		}
		
		for ( int line = begin; line <= end; line++ ){
			checkCurrentLine( changeInfo, line );
		}
		
	}

	@Override
	public void newSourceLocation(String string, int position) {
		curFuncLocation=string;
		if ( begin == -1 || position < begin ){
			begin=position;
		}
		if ( end == -1 || position > end ){
			end = position;
		}
		
		// TODO Auto-generated method stub
		File srcFile;
		try {
			srcFile = new File( new File(string).getCanonicalPath() );
		} catch (IOException e) {
			return;
		}
		FileChangeInfo changeInfo = fileChangesMap.get( srcFile );
		if ( changeInfo == null ){
			return;
		}
		
		int folderLast = string.lastIndexOf(File.separator);
		
//		String namespace = string.substring(0, folderLast);
//		String className = string.substring(folderLast+1);
		if ( modifiedFunctions.contains(curFunc) ){
			return;
		}
		
		if ( lastPositionInCurrentFunction == -1 ){
			lastPositionInCurrentFunction =position;
		}
		
		for ( int line = lastPositionInCurrentFunction; line <= position; line++ ){
			checkCurrentLine( changeInfo, line );
		}
		
		lastPositionInCurrentFunction =position;
		
	}

	private void checkCurrentLine(FileChangeInfo changeInfo, int line) {
		if ( changeInfo.isLineChanged(line) ){
			
			String methodName = curFunc;
			
			
			boolean newModifiedFunction = modifiedFunctions.add( methodName );
			
			if ( ! newModifiedFunction ){
				return;
			}
			
			
			
		}
	}


	private void includeFunctionInComponent(Component modifiedFunctionsComponent, String methodName) {
		if ( useDemangledNames ){
			FunctionMonitoringDataTransformerDemangled trs = new FunctionMonitoringDataTransformerDemangled();
			String methodNameDemangled = Demangler.INSTANCE.demangle(methodName); 
			String className = trs.getClassName(methodNameDemangled, this.curFuncLocation);
			String methodSignature = trs.getMethodSignature(methodNameDemangled, curFuncLocation);
			String packageName = trs.getPackageName(methodNameDemangled, curFuncLocation);
			modifiedFunctionsComponent.addRule(new MatchingRuleInclude( generateRegex(packageName),generateRegex(className), generateRegex( methodSignature ) ) );
		} else {

			modifiedFunctionsComponent.addRule(new MatchingRuleInclude(".*",".*", escapeString( methodName ) ) );
		}
	}

	private String generateRegex(String packageName) {
		// TODO Auto-generated method stub
		return packageName.replace("(", "\\(").replace(")", "\\)");
	}


	private String escapeString(String methodName) {
		return methodName.replace("*", "\\*")
				.replace("(", "\\(")
				.replace(")", "\\)")
				;
		
	}

	@Override
	public void returnInstruction(String address) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newFunctionName(String substring) {
//		curFuncName = substring;
	}

	@Override
	public void instruction(String address, String string) {
		
	}

	@Override
	public void objdumpEnd() {
		checkLastFUnction();
	}

	public void addModifiedFunctions(HashSet<String> modifiedFunctions2) {
		modifiedFunctions.addAll(modifiedFunctions2);
	}


	@Override
	public void leaveInstruction(String address) {
		// TODO Auto-generated method stub
		
	}


	public void setModifiedFunctions(HashSet<String> modifiedFunctions2) {
		modifiedFunctions = modifiedFunctions2;
	}





	/* (non-Javadoc)
	 * @see cpp.gdb.ModifiedFunctionsAnalysisResult#getAddedFunctions()
	 */
	@Override
	public HashSet<String> getAddedFunctions() {
		return addedFunctions;
	}


	public void setAddedFunctions(HashSet<String> addedFunctions) {
		this.addedFunctions = addedFunctions;
	}

	@Override
	public void jmpInstruction(String address, String jmpToAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newLine(String line) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void popEbp(String address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fldlInstruction(String address, String jmpToAddress) {
		// TODO Auto-generated method stub
		
	}

}
