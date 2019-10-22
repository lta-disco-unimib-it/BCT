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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import modelsFetchers.IoModelIterator;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import modelsViolations.BctModelViolation;
import tools.fshellExporter.parser.InvariantParseException;
import tools.fshellExporter.parser.IoInvariantParser;
import tools.violationsAnalyzer.ViolationsUtil;
import util.StringUtils;
import util.componentsDeclaration.CppMangledSignatureParser;
import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.LocalVariableDeclaration;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.VariablesDetector;

public class ModelsExporter {
	///PROPERTIES-DESCRIPTION: Options for the exporting of data properties as assertions
	
	///if true skip exporting struct fields
	public static final String BCT_EXPORT_SKIP_STRUCT_FIELDS = "bct.export.skipStructFields";
	///if true skip monitoring global variables
	public static final String BCT_EXPORT_SKIP_GLOBALS = "bct.export.skipGlobals";
	
	///list of tokens to replace in the C assertions generated from data properties. Use ; to separate each replacing rule, Use : to separate original and replacement. E.g. <originalString1>:<newString1>;<originalString2>:<newString2>; 
	public static final String BCT_EXPORT_REPLACE = "bct.export.replace";
	
	///list of tokens to replace in the data properties
	public static final String BCT_EXPORT_REPLACE_PRE = "bct.export.replacePre";
	
	///list of models to export (separated by ;)
	public static final String BCT_EXPORT_MODELS_TO_EXPORT = "bct.export.modelsToExport";
	
	private static Logger LOGGER = Logger.getLogger(ModelsExporter.class.getCanonicalName());
	private ModelInfoBuilder modelInfoBuilder;
	private List<ModelFilter> additionalVarFilters = new ArrayList<ModelFilter>();
	private boolean unitVerification;
	private boolean skipStructFields;
	private boolean skipGlobals;
	
	
	private HashMap<String,String> assertionReplacements = new HashMap<String, String>(); 
	private HashMap<String,String> assertionReplacementsPre = new HashMap<String, String>();
	private HashSet<String> modelsToExport;
	private ModelsFetcher mf; 
	
	{
		String skipStructFieldsString = System.getProperty(BCT_EXPORT_SKIP_STRUCT_FIELDS);
		skipStructFields = Boolean.parseBoolean(skipStructFieldsString);
		
		String skipGlobalsString = System.getProperty(BCT_EXPORT_SKIP_GLOBALS);
		skipGlobals = Boolean.parseBoolean(skipGlobalsString);
		
		String replacements = System.getProperty(BCT_EXPORT_REPLACE);
		if ( replacements != null ){
			fillReplacements(assertionReplacements, replacements, "POST");
		}
		
		String replacementsPre = System.getProperty(BCT_EXPORT_REPLACE_PRE);
		if ( replacementsPre != null ){
			fillReplacements(assertionReplacementsPre, replacementsPre, "PRE");
		}
		
		String functionsToExportString = System.getProperty(BCT_EXPORT_MODELS_TO_EXPORT);
		if ( functionsToExportString != null ){
			modelsToExport = new HashSet<String>();
			String[] functions = functionsToExportString.split(";");
			for ( String function : functions ){
				modelsToExport.add(function);
			}
			System.out.println("Models to export: "+modelsToExport);
		}
	}



	public void fillReplacements(Map<String,String> assertionReplacements, String replacements, String when) {
		StringTokenizer t = new StringTokenizer(replacements,";");
		while ( t.hasMoreTokens() ){
			String replacement = t.nextToken();
			int separatorPos = replacement.indexOf(':');
			if ( separatorPos < 0 ){
				continue;
			}
			String input = replacement.substring(0, separatorPos);
			String output;
			if ( separatorPos == ( replacement.length() - 1) ){
				output = "";
			} else {
			 output = replacement.substring(separatorPos+1 );
			}
			LOGGER.info("Adding Replacement "+when+" : "+input+" TO "+output);
			assertionReplacements.put(input, output);
		}
		LOGGER.info("Replacements to perform "+when+" : "+assertionReplacements);
	}
	
	
	public ModelsExporter( Map<String, FunctionMonitoringData> monitoredFunctionsData, Map<String, FunctionMonitoringData> monitoredFunctionsDataV1, SourceLinesMapper sourceLinesMapper, ModelsFetcher mf ){
		this.mf = mf;
		this.modelInfoBuilder = new ModelInfoBuilder( monitoredFunctionsData, monitoredFunctionsDataV1, sourceLinesMapper );
	}
	
	private void process(ModelsExporterListener listener, boolean isEnter, ModelInfo modelInfo, IoModelIterator ioIt)  {

		if ( ! modelInfo.isLineModel() ){			
			processFunctionModel(listener,isEnter,modelInfo,ioIt);
		} else {
			processLineModel(listener,modelInfo,ioIt);
		}

	}

	public void processModels(ModelsExporterListener listener) throws ModelsFetcherException {
		
		
		LinkedList<ModelInfo> modelsToProcess = new LinkedList<ModelInfo>();
		
		for ( String ioModels : mf.getIoModelsNames() ) {
			
			
			LOGGER.fine("Processing model: "+ioModels);
			
			ModelInfo modelInfo;
			try {
				modelInfo = modelInfoBuilder.createModelInfo(ioModels,true);
			} catch (ModelInfoBuilderException e) {
				e.printStackTrace();
				LOGGER.fine("Skipping model: "+ioModels);
				LOGGER.fine(e.getMessage());
				continue;
			}
			
			if ( modelInfo == null ){
				LOGGER.fine("Function data not available for "+ioModels);
				continue;
			}
			
			if ( modelInfo.getFileModified() == null ){
				LOGGER.fine("Skipping model: "+ioModels);
				continue;
			}
			
			if ( modelInfo.getFileOriginal() == null ){
				LOGGER.fine("Skipping model: "+ioModels);
				continue;
			}
			
			LOGGER.fine("Adding model: "+ioModels);
			modelsToProcess.add(modelInfo);
			
			
		}
		
		Collections.sort(modelsToProcess, new Comparator<ModelInfo>() {

			@Override
			public int compare(ModelInfo arg0, ModelInfo arg1) {
				int fileComp = arg0.getFileOriginal().compareTo(arg1.getFileOriginal());
				if ( fileComp != 0 ){
					return fileComp;
				}
				if ( arg0.isLineModel() && ( ! arg1.isLineModel() ) ){
					return -1;
				}
				if ( arg1.isLineModel() && ( ! arg0.isLineModel() )){
					return 1;
				}
				return arg1.getLineOriginal()-arg0.getLineOriginal();
			}
		});
		
		if ( unitVerification ){
			System.out.println("Functions to export: ");
			for ( ModelInfo modelInfo : modelsToProcess ){
				FunctionMonitoringData func = modelInfo.getFunctionOriginal();
				if (  func != null ){
					if ( func.isTargetFunction() || func.isCallerOfTargetFunction()){
						System.out.println("\t"+func.getMangledName());
					}
				}
			}
		}
		
		for ( ModelInfo modelInfo : modelsToProcess ){
			
			try {
				processModel(listener, mf, modelInfo);
			} catch (ModelsFetcherException e ){
				e.printStackTrace();
			}
		}
		
		listener.processingEnd();
	}

	public void processModel(ModelsExporterListener listener,
			ModelsFetcher mf, ModelInfo modelInfo)
			throws ModelsFetcherException {
		
		if ( isModelExcludedFromMonitoring( modelInfo ) ){
			LOGGER.info("Excluding model "+modelInfo.functionName+ ". Does not match given expressions.");
			return;
		}
		
		String ioModels = modelInfo.getBctModelName();
		
		IoModelIterator ioIt;

		ioIt = mf.getIoModelIteratorExit(ioModels);
		process( listener, false, modelInfo, ioIt );
		
		ioIt = mf.getIoModelIteratorEnter(ioModels);
		process( listener, true, modelInfo, ioIt );
		
	}
	
	private boolean isModelExcludedFromMonitoring(ModelInfo modelInfo) {
		
		String model = modelInfo.getBctModelName().trim();
		
		LOGGER.info("Checking for inclusion of "+model);
		
		if ( modelsToExport == null ){
			return false;
		}
		
		for ( String matchingExpr : modelsToExport ){
			if ( model.matches(matchingExpr) ){
				return false;
			}
		}
		
		
		return true;
	}


	private void processFunctionModel( ModelsExporterListener listener, boolean isEnter, ModelInfo modelInfo, IoModelIterator ioIt) {
		while ( ioIt.hasNext() ){
			String model = ioIt.next();
			
			LOGGER.fine("Processing model "+model+" "+modelInfo.functionName);
			
			model = model.replace("BCT_return", "returnValue"); ///PIN uses the term BCT_return
			String processedModel = model;
			if ( ( model = processModel( model, modelInfo ) ) == null ){
				LOGGER.fine("Skipping model "+processedModel+" "+modelInfo.functionName);
				continue;
			}
			
			model = processExpression(model,modelInfo);
			
			if ( model == null ){
				LOGGER.fine("Skipping model (not exportable) :"+processedModel+" "+modelInfo.functionName);
				continue;
			}
			
			if ( isEnter ){
				listener.addAssertionAtFunctionEnter(modelInfo, model);
			} else {
				listener.addAssertionAtFunctionExit(modelInfo, model);
			}
		}
	}

	
	
	
	private String processExpression(String model, ModelInfo modelInfo) {
		model = userDefinedReplacementsPre( model, modelInfo );
		
		model = processExpression(model);
		
		if ( model == null ){
			return null;
		}
		
		model = replacePointersWithArrows( model, modelInfo );
		
		model = userDefinedReplacements( model, modelInfo );
		
		return model;
	}

	private String userDefinedReplacementsPre(String model, ModelInfo modelInfo) {
		for( Entry<String, String> e : assertionReplacementsPre.entrySet() ){
			String toReplace = e.getKey();
			String replacement = e.getValue();
			
			model = model.replace(toReplace, replacement);
		}
		return model;
	}
	
	private String userDefinedReplacements(String model, ModelInfo modelInfo) {
		for( Entry<String, String> e : assertionReplacements.entrySet() ){
			String toReplace = e.getKey();
			String replacement = e.getValue();
			
			model = model.replace(toReplace, replacement);
		}
		return model;
	}

	private String replacePointersWithArrows(String model, ModelInfo modelInfo) {
		Set<String> flattenedVars = ViolationsUtil.extractVariables(model);
		
		Set<String> pointers = modelInfo.getFunctionOriginal().getPointerArgs();
		
		for ( String flattenedVar : flattenedVars ){
			int occurrencies = StringUtils.occurrenciesOf(flattenedVar, '.');
			if ( occurrencies > 1 ){
				LOGGER.warning("More than one deref "+model);
				//return null;
			}
			if ( occurrencies == 0 ){
				continue;
			}
			
			if ( skipStructFields ){
				LOGGER.fine("SKIPPING because all structures are skipped ");
				return null;
			}
			
			int dotPos = flattenedVar.indexOf('.');
			
			String parentVar = flattenedVar.substring(0, dotPos);
			
			//FIXME: does not work in case of type change from pointer to ref
			if ( pointers.contains(parentVar) ) {
				model = model.replace(flattenedVar+".", flattenedVar+"->");
			} //FIXME: other changes are needed (for example for locl variables that are pointers (but maybe this are notr fully flattened
		}
		
		LOGGER.fine("REPLACED POINTERS: "+model);
		return model;
	}

	private void processLineModel(ModelsExporterListener listener, ModelInfo modelInfo,
			IoModelIterator ioIt) {
		while ( ioIt.hasNext() ){
			String model = ioIt.next();
			
			
			LOGGER.info("Processing model "+model+" "+modelInfo.functionName);
			
			if ( modelInfo.getLineOriginal() <= 0 ){
				System.err.println("Skipping model: "+modelInfo.functionName+" line is: "+modelInfo.getLineOriginal() );
				continue;
			}
			
			
			String processedModel = model;
			if ( ( model = processModel( model, modelInfo ) ) == null ){
				LOGGER.info("Skipping model (not exportable) :"+processedModel+" "+modelInfo.getLineOriginal());
				continue;
			}
			
			model = processExpression(model,modelInfo);
			
			if ( model == null ){
				LOGGER.info("Skipping model "+processedModel+" "+modelInfo.functionName);
				continue;
			}
			
			
			
			listener.addAssertionAtLineBegin(modelInfo, model);
			
		}
	}

	private HashMap<String,HashSet<String>> originalInjected = new HashMap<String, HashSet<String>>();
	private boolean filterRedundants = false;

	public boolean isFilterRedundants() {
		return filterRedundants;
	}


	public void setFilterRedundants(boolean filterRedundants) {
		this.filterRedundants = filterRedundants;
	}


	public String processModel(String model, ModelInfo modelInfo) {
		
		model = filterRedundant( model, modelInfo );
		
		model = processModel(model, modelInfo, modelInfo.getFunctionOriginal());
		
		model = processModel(model, modelInfo, modelInfo.getFunctionModified());
		
		return model;
	}

	private String filterRedundant(String model, ModelInfo modelInfo) {
		if ( ! filterRedundants ){
			return model;
		}
		String function = modelInfo.getFunctionName();
		
		int idx = function.indexOf(':');
		if ( idx > 0 ){
			function = function.substring(0, idx);
		}
		
		HashSet<String> models = originalInjected.get(function);
		if ( models == null ){
			models = new HashSet<String>();
			originalInjected.put(function, models );
		}
		
		if ( models.contains(model) ){
			LOGGER.info("Redundant: "+model);
			return null;
		}
		
		models.add(model);
		
		return model;
	}

//	Map<String,Set<String>> varsNotUsedInFileCache = new HashMap<String,Set<String>>();

	public String processModel(String model, ModelInfo modelInfo,
			FunctionMonitoringData violatedFunctonData) {
		if ( violatedFunctonData != null && model != null){
			
			if ( model.contains("orig(") || model.contains("store(") || model.contains("subset of") ){
				return null;
			}
			
			if ( ! matchGranularity( violatedFunctonData ) ){
				LOGGER.info("Granularity does not match");
				return null;
			}
			
			
			
			Set<String> varsToIdentify = ViolationsUtil.extractParentVariablesNoStar(model);
			
			for ( String var : varsToIdentify) {
				if ( var.contains(".[") ){
					LOGGER.info("Buggy var name: "+var);
					return null;
				}
			}
			
			
			
			Set<String> varsInFile = identifyVariablesUsedInWholeFile(
					violatedFunctonData, varsToIdentify);
			if ( varsToIdentify.size() != varsInFile.size() ) {
				LOGGER.info("Variables are not used in file. Vars in file: "+varsInFile+". Looking for: "+varsToIdentify);
//				Set<String> notUsed = varsNotUsedInFileCache.get( violatedFunctonData.getMangledName() );
				return null;
			}
			
			if ( ! allVariablesDeclaredInBothVersions ( violatedFunctonData, modelInfo, varsToIdentify ) ){
				LOGGER.info("Not all variables were declared ");
				return null;
			}
			
			Set<String> varsInMethod = identifyVariablesUsedInMethodDefinition(
					violatedFunctonData, varsToIdentify);
			
			
			
			
			if ( varsToIdentify.size() > 0 && varsInMethod.size() == 0 ) {
				LOGGER.info("Variables are not used in metho. Vars in method: "+varsInMethod+". Looking for: "+varsToIdentify);
				return null;
			}
			
			for ( ModelFilter additionalVarFilter : additionalVarFilters ){
				model = additionalVarFilter.processDataProperty( model, modelInfo );
				if ( model == null ){
					LOGGER.info("Filtered out by "+additionalVarFilter.getClass().getCanonicalName());
					return null;
				}
			}
		}
		
		return model;
	}

	private boolean allVariablesDeclaredInBothVersions(
			FunctionMonitoringData violatedFunctonData, ModelInfo modelInfo,
			Set<String> varsToIdentify) {
		return allVariablesDeclared(violatedFunctonData, modelInfo, varsToIdentify, modelInfo.getLineOriginal()) &&
				allVariablesDeclared(violatedFunctonData, modelInfo, varsToIdentify, modelInfo.getLineModified());
	}

	private boolean allVariablesDeclared(
			FunctionMonitoringData violatedFunctonData, ModelInfo modelInfo,
			Set<String> varsToIdentify, int currentLine) {
		
		if ( skipGlobals ){
			HashSet<String> globals = new HashSet<String>();
			globals.addAll(varsToIdentify);
			globals.removeAll(violatedFunctonData.getLocalVariableDeclarations());
			globals.removeAll(violatedFunctonData.getScalarArgs());
			globals.removeAll(violatedFunctonData.getPointerArgs());
			globals.removeAll(violatedFunctonData.getReferenceArgs());
			
			if ( globals.size() > 0 ){
				//there are global vars
				return false;
			}
		}
		
		if ( modelInfo.isEntryExitPoint() && ( ! modelInfo.isEnter()) ){
			return true; //TODO: check before instrumentation
		}
		
//		int currentLine;
//		
//		if ( modelInfo.isEntryExitPoint() && modelInfo.isEnter() ){
//			currentLine = violatedFunctonData.getFirstSourceLine();
//		} else {
//			currentLine = modelInfo.getLine_(violatedFunctonData);
//		}
		
		List<LocalVariableDeclaration> locals = violatedFunctonData.getLocalVariableDeclarations();
		
		
		for ( LocalVariableDeclaration local : locals ){
			if ( local.getLineNo() >= currentLine ){
				//variable declared in the future => remove model
				if ( varsToIdentify.contains(local.getName()) ){
					return false;
				}
			}
		}
		
		return true;
	}

	private boolean matchGranularity(FunctionMonitoringData violatedFunctonData) {
		if ( ! unitVerification ){
			return true;
		}
		
		if ( violatedFunctonData.isTargetFunction() ){
			return true;
		}
		if ( violatedFunctonData.isCallerOfTargetFunction() ){
			return true;
		}
		if ( violatedFunctonData.isCalledByTargetFunction() ){
			return true;
		}

		return false;
	}

	
	public static Set<String> identifyVariablesUsedInWholeFile(
			FunctionMonitoringData violatedFunctonData,
			Set<String> varsToIdentify) {
		Set<String> toRemove = new HashSet<String>();
		for ( String var : varsToIdentify ){
			if ( var.equals("returnValue") ){
				toRemove.add(var);
			}
		}
		
		varsToIdentify.removeAll(toRemove);
		
		Set<String> varsInFile = VariablesDetector.identifyVariableNamesInFile(varsToIdentify, 
				violatedFunctonData.getAbsoluteFile(), 
				1, 
				Integer.MAX_VALUE);
		return varsInFile;
	}
	
	public static Set<String> identifyVariablesUsedInMethodDefinition(
			FunctionMonitoringData violatedFunctonData,
			Set<String> varsToIdentify) {
		
		Set<String> toRemove = new HashSet<String>();
		for ( String var : varsToIdentify ){
			if ( var.equals("returnValue") ){
				toRemove.add(var);
			}
		}
		
		varsToIdentify.removeAll(toRemove);
		
		Set<String> varsInFile = VariablesDetector.identifyVariableNamesInFile(varsToIdentify, 
				violatedFunctonData.getAbsoluteFile(), 
				violatedFunctonData.getFirstSourceLine(), 
				violatedFunctonData.getLastSourceLine() //violation is observed at the beginning of line, so we stop looking at the line where violation is found (the passed line is excluded)
				);
		return varsInFile;
	}

//	public ModelFilter getAdditionalVarFilter() {
//		return additionalVarFilter;
//	}

	public void setAdditionalVarFilter(ModelFilter additionalVarFilter) {
		if ( additionalVarFilter == null ){
			return;
		}
		this.additionalVarFilters.add( additionalVarFilter );
	}
	
	private static final String javaIdentifierChars = "[A-Za-z_\\.]";
	private static final Pattern pointerPattern = Pattern.compile("\\s*\\*["+javaIdentifierChars+"]+\\s*!=\\s*null\\s*");
	

	public static String processExpression( String expression ) {
		LOGGER.info("Processing expression "+expression);
		
		expression = expression.trim();
		
		if ( expression.length() == 0 ){
			LOGGER.info("Skipping model "+expression+" : empty ");
			return null;
		}
		
		if ( expression.contains("%") ){
			LOGGER.info("Skipping model "+expression+" : MODULUS operator not defined for floats, cannot determine if variables are float or not. ");
			return null;
		}
	
		Matcher notNullCheckMatcher = pointerPattern.matcher(expression);
		if ( notNullCheckMatcher.matches() ){
			LOGGER.info("Skipping model "+expression+" : null pointer ");
			return null; //we discard expressions like *p != null
		}
		
		expression = expression.replace("BCT_return", "returnValue"); //PIN uses BCT_return, this replacement is used when invoked by CBMCMOdelsFilter
	
		try {
			String result = IoInvariantParser.evaluateExpression(expression);
			System.out.println("RESULT "+result);
			if ( result != null && result.trim().isEmpty() ){
				LOGGER.info("Skipping model "+expression+" : error evaluating expression ");
				return null;
			}
			
			if ( result != null ){
				result = result.replaceAll("returnValue\\.eax", "return");
				
				result = result.replaceAll("returnValue", "return"); //handles cases like "returnValue != null" "(returnValue != null) ==> ..."
				
//				result = result.replaceAll("null$", "0");
				
//				result = result.replaceAll(" null([\\) ])", " 0$1");
				
//				result = result.replaceAll("\\*(\\S+)\\.", "\\1->" );
			}
		
			LOGGER.fine("Processed expression: "+result);
			return result;
		} catch (InvariantParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Skipping model "+expression+" : exception "+e.getMessage());
			System.err.println("IGNORING MODEL "+expression);
		}
		
		
		return null;
	
	}

	public void setUnitVerification(boolean unitVerification) {
		this.unitVerification = unitVerification;
	}

	public void setAdditionalVarFilters(
			List<ModelFilter> additionalVariablesFilters) {
		additionalVarFilters.addAll(additionalVariablesFilters);
	}

	

	

}
