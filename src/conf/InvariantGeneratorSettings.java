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
package conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import dfmaker.core.DaikonDeclarationMaker.DaikonComparisonCriterion;

import regressionTestManager.MetaDataHandlerSettings;
import traceReaders.normalized.traceCreation.MethodOutgoingTraceMaintainer;
import traceReaders.normalized.traceCreation.TraceMaintainer;

public class InvariantGeneratorSettings extends ConfigurationSettings {
	private File temporaryDir;
	
	private Class normalizedTraceHandlerType;
	private Class traceReaderType;
	private File distilledDir;

	private String fsaEngine;

	private String daikonConfig;

	private MetaDataHandlerSettings metaDataHandlerSettings = null;

	private Set<String> methodsToIgnore;

	private boolean addAdditionalInvariants = false;

	private String daikonAdditionalOptions;

	private int parallelInferenceThreads; 
	private boolean expandReferences = true;

	private boolean optimizationEnabled;

	private String daikonPath;

	private String daikonConfidenceLevel; 
	
	private int daikonExecutionTimeLimit = -1;

	private Class<? extends TraceMaintainer> traceMaintainerType = MethodOutgoingTraceMaintainer.class;

	private boolean inferComponentsIOModels = true;

	private boolean inferComponentsInteractionModels = true;

	private boolean inferComponentsIOInteractionModels = false;

	private boolean inferClassesUsageInteractionModels = false;

	private DaikonComparisonCriterion comparisonCriterion = DaikonComparisonCriterion.CompareAll;

	private boolean deleteTemporaryDir;

	private boolean excludeConstantLikeVariables = false;

	private boolean skipInference = false;

	private Set<String> sessionsToIgnore;

	private Set<String> testsToIgnore;

	private Set<String> actionsToIgnore;

	private boolean invertFiltering = false;

	private boolean inferClassesUsageInteractionModelsWithOutgoingCalls = false;

	private Set<String> methodsToIncludePatterns;

	private boolean daikonUndoOptimizations;

	private boolean identifyTestedMethods;


	public interface Options{
		public final String temporaryDir = "temporaryDir";
		public final String normalizedTraceHandlerType = "normalizedTraceHandler.type";
		public final String traceReaderType = "traceReader.type";
		public final String fsaEngine = "FSAEngine";
		public final String daikonPath = "daikonPath";
		public final String daikonUndoOptmizations = "daikonUndoOptimizations";
		public final String daikonConfidenceLevel = "daikonConfidenceLevel";
		public final String daikonConfig = "daikonConfig";
		public final String daikonAdditionalOptions = "daikonAdditionalOptions";
		public final String daikonComparisonCriterion = "daikonComparisonCriterion";
		public final String methodsToIgnoreFile = "methodsToIgnoreFile";
		public final String sessionsToIgnoreFile = "sessionsToIgnoreFile";
		public final String addAdditionalInvariants = "addAdditionalInvariants";
		public final String expandReferences = "expandReferences";
		public final String excludeConstantLikeVariables = "excludeConstantLikeVariables";
		public final String parallelInferenceThreads = "parallelInferenceThreads";
		public final String metaDataHandlerSettingsType = "metaDataHandlerSettingsType";
		public final String enableOptimization = "enableOptimization";
		public final String daikonExecutionTimeLimit = "daikonExecutionTimeLimit";
		public final String traceMaintainerType = "traceMaintainerType";
		public final String deleteTemporaryDir = "deleteTemporaryDir";
		
		public final String inferComponentsIOModels = "inferComponentsIOModels";
		public final String inferComponentsInteractionModels = "inferComponentsInteractionModels";
		public final String inferComponentsIOInteractionModels = "inferComponentsIOInteractionModels"; //EFSA, actually not managed
		public final String inferClassesUsageInteractionModels = "inferClassesUsageInteractionModels";
		public final String inferClassesUsageInteractionModelsWithOutgoingCalls = "inferClassesUsageInteractionModelsWithOutgoingCalls";
		public final String identifyTestedMethods = "identifyTestedMethods";
		
		public final String skipInference = "skipInference";
		
		public final String testsToIgnoreFile = "testsToIgnoreFile";
		public final String actionsToIgnoreFile = "actionsToIgnoreFile";
		public final String processesToIgnoreFile = "processesToIgnoreFile";
		public final String invertFiltering = "invertFiltering";
		public final String methodsToIncludePatternsFile = "methodsToIncludePatternsFile";
		
	}
	
	
	
	
	public InvariantGeneratorSettings(Class type, Properties p) throws SettingsException {
		super(type, p);
		
		initTemporaryDir(p);
		
		initNormalizedTraceHandlerType(p);
		
		initTraceReaderType(p);

		
		initModelsTypesToInfer(p);
		
		initFSAEngineType(p);
		
		initIOInferenceOptions(p);
		
		
		methodsToIgnore = getMethodsToIgnore( p.getProperty( Options.methodsToIgnoreFile ) );
		
		methodsToIncludePatterns = getMethodsToIgnore( p.getProperty( Options.methodsToIncludePatternsFile ) );
	
		sessionsToIgnore = getMethodsToIgnore( p.getProperty( Options.sessionsToIgnoreFile ) );

		testsToIgnore = getMethodsToIgnore( p.getProperty( Options.testsToIgnoreFile ) );
		
		actionsToIgnore = getMethodsToIgnore( p.getProperty( Options.actionsToIgnoreFile ) );
		
		String enableOptimizationString = p.getProperty(Options.enableOptimization);
		if ( enableOptimizationString != null ){
			try{
				optimizationEnabled = Boolean.valueOf(enableOptimizationString);
			} catch ( Exception e ){
				throw new SettingsException("Wrong value for "+Options.enableOptimization+", can be one of true or false. Found "+enableOptimizationString);
			}
		}
		
		
		String invertFilteringString = p.getProperty(Options.invertFiltering);
		if ( invertFilteringString != null ){
			try{
				invertFiltering = Boolean.valueOf(invertFilteringString);
			} catch ( Exception e ){
				throw new SettingsException("Wrong value for "+Options.invertFiltering+", can be one of true or false. Found "+invertFilteringString);
			}
		}
		
		String skipInferenceString = p.getProperty(Options.skipInference);
		if ( skipInferenceString != null ){
			try{
				skipInference = Boolean.valueOf(skipInferenceString);
			} catch ( Exception e ){
				throw new SettingsException("Wrong value for "+Options.skipInference+", can be one of true or false. Found "+enableOptimizationString);
			}
		}
		
		initMetaDataHandlerSettings(p);
		
		initMultiThreadingOptions(p);
		
		initTraceMaintainer(p);
	}

	public boolean getInvertFiltering() {
		return invertFiltering;
	}

	public void setInvertFiltering(Boolean invertFiltering) {
		this.invertFiltering = invertFiltering;
	}

	public Set<String> getActionsToIgnore() {
		return actionsToIgnore;
	}

	private void initMetaDataHandlerSettings(Properties p) throws SettingsException {
		String metaSettings = (String) p.getProperty(Options.metaDataHandlerSettingsType);
		try {
			if ( metaSettings != null ){
				Class metaDataHandlerSettingsClass = Class.forName( metaSettings );
				Object instance = metaDataHandlerSettingsClass.newInstance();
				metaDataHandlerSettings = (MetaDataHandlerSettings) instance;
				metaDataHandlerSettings.init( extractMataDataHandlerProperties(metaSettings, p) );
			}
		} catch (ClassNotFoundException e) {
			throw new SettingsException( "Wrong meta data handler class name",e);
		} catch (InstantiationException e) {
			throw new SettingsException("Wrong meta data handler class name",e);
		} catch (IllegalAccessException e) {
			throw new SettingsException("Wrong meta data handler class name",e);
		}
	}

	private void initMultiThreadingOptions(Properties p) {
		String parallelInferenceThreadsString = (String) p.getProperty(Options.parallelInferenceThreads);
		if ( parallelInferenceThreadsString != null ){
			parallelInferenceThreads = Integer.valueOf(parallelInferenceThreadsString);
		}
		
		if ( parallelInferenceThreads < 1 ){
			parallelInferenceThreads = 1;
		}
	}

	private void initTraceMaintainer(Properties p) throws SettingsException {
		String traceMaintainerClassName = (String) p.getProperty(Options.traceMaintainerType);
		try {
			if ( traceMaintainerClassName != null ){
				traceMaintainerType = (Class<TraceMaintainer>) Class.forName( traceMaintainerClassName );
			}
		} catch (ClassNotFoundException e) {
			throw new SettingsException( "Wrong trace maintainer class name",e);
		}
	}

	private void initFSAEngineType(Properties p) {
		fsaEngine = (String) p.getProperty(Options.fsaEngine);
	}

	private void initIOInferenceOptions(Properties p) throws SettingsException {
		
		excludeConstantLikeVariables = Boolean.parseBoolean( p.getProperty(Options.excludeConstantLikeVariables) );
		
		String daikonExecutionTimeLimitString = (String) p.getProperty(Options.daikonExecutionTimeLimit);
		if ( daikonExecutionTimeLimitString != null ){
			daikonExecutionTimeLimit = Integer.valueOf(daikonExecutionTimeLimitString);
		}
		
		
		String addAdditionalInvariantsString = (String) p.getProperty(Options.addAdditionalInvariants);
		if ( addAdditionalInvariantsString != null ){
			try {
				addAdditionalInvariants  = Boolean.valueOf(addAdditionalInvariantsString);
			} catch ( Exception e ){
				throw new SettingsException("addAdditionalInvariants must be true or false");
			}
		}
		
		String expandReferencesString = p.getProperty(Options.expandReferences);
		if ( expandReferencesString != null ){
			try{
				expandReferences = Boolean.valueOf(expandReferencesString);
			} catch ( Exception e ){
				throw new SettingsException("Wrong value for "+Options.expandReferences+", can be one of true or false. Found "+expandReferencesString);
			}
		}
		
		String daikonComparisonCriterionString = (String) p.getProperty(Options.daikonComparisonCriterion);
		if ( daikonComparisonCriterionString != null ){
			comparisonCriterion = DaikonComparisonCriterion.valueOf(daikonComparisonCriterionString);
		}
		if ( comparisonCriterion == null ){
			comparisonCriterion = DaikonComparisonCriterion.CompareAll;
		}
		
		daikonPath = (String) p.getProperty(Options.daikonPath);
		
		daikonConfidenceLevel = (String) p.getProperty(Options.daikonConfidenceLevel);
		
		daikonUndoOptimizations = Boolean.parseBoolean(p.getProperty(Options.daikonUndoOptmizations));
		
		daikonConfig = (String) p.getProperty(Options.daikonConfig);
		
		daikonAdditionalOptions = (String) p.getProperty(Options.daikonAdditionalOptions);
		if ( daikonAdditionalOptions == null ){
			daikonAdditionalOptions = "";
		}
		
		distilledDir = new File ( temporaryDir, "distilled");
		distilledDir.mkdirs();
	}

	private void initModelsTypesToInfer(Properties p) {
		String inferComponentIOModelsString = p.getProperty(Options.inferComponentsIOModels);
		if ( inferComponentIOModelsString != null ){
			inferComponentsIOModels = Boolean.valueOf(inferComponentIOModelsString);
		}
		
		String value = p.getProperty(Options.inferComponentsInteractionModels);
		if ( value != null ){
			inferComponentsInteractionModels = Boolean.valueOf(value);
		}
		value = p.getProperty(Options.inferComponentsIOInteractionModels);
		if ( value != null ){
			inferComponentsIOInteractionModels = Boolean.valueOf(value);
		}
		value = p.getProperty(Options.inferClassesUsageInteractionModels);
		if ( value != null ){
			inferClassesUsageInteractionModels = Boolean.valueOf(value);
		}
		value = p.getProperty(Options.inferClassesUsageInteractionModelsWithOutgoingCalls);
		if ( value != null ){
			inferClassesUsageInteractionModelsWithOutgoingCalls = Boolean.valueOf(value);
		}
		
		value = p.getProperty(Options.identifyTestedMethods);
		if ( value != null ){
			identifyTestedMethods = Boolean.valueOf(value);
		}
	}

	private void initTraceReaderType(Properties p) throws SettingsException {
		String typeR = (String) p.getProperty(Options.traceReaderType);
		try {
			traceReaderType = Class.forName( typeR );
		} catch (ClassNotFoundException e) {
			throw new SettingsException("Wrong traceReaderType");
		}
	}
	
	private void initNormalizedTraceHandlerType(Properties p) throws SettingsException {
		String typeN = (String) p.getProperty(Options.normalizedTraceHandlerType);
		try {
			normalizedTraceHandlerType = Class.forName( typeN );
		} catch (ClassNotFoundException e) {
			throw new SettingsException("Wrong normalizedTraceHandlerType : "+typeN);
		}
	}

	private void initTemporaryDir(Properties p) throws SettingsException {
		temporaryDir = new File(p.getProperty(Options.temporaryDir));
		temporaryDir.mkdirs();
		if ( ! temporaryDir.exists() )
			throw new SettingsException("Cannot set temporary dir "+temporaryDir);
		
		
		deleteTemporaryDir = Boolean.parseBoolean(p.getProperty(Options.deleteTemporaryDir));
	}

	private Properties extractMataDataHandlerProperties ( String metaDataHandlerClassName, Properties p ){
		Iterator it = p.keySet().iterator();
		String prefix = metaDataHandlerClassName+".";
		
		Properties properties = new Properties();
		
		while ( it.hasNext() ){
			String property = (String) it.next();
			if ( property.startsWith(prefix) ){
				properties.put(property.substring(prefix.length()), p.getProperty(property));
			}
		}
		return properties;
	}

	private Set<String> getMethodsToIgnore(String fileName) throws SettingsException {
		Set<String> toIgnore = new HashSet<String>();
		if ( fileName == null )
			return toIgnore;
		File file = new File(fileName);
		if ( ! file.exists() )
			throw new SettingsException("InvariantGeneratorSettings, "+Options.methodsToIgnoreFile+" : file "+fileName+" does not exist");
		Properties p = new Properties();
		InputStream is;
		try {
			is = new FileInputStream(file);
			p.load(is);
			
			for ( Object o : p.keySet() ){
				toIgnore.add(o.toString());
			}
			is.close();
			return toIgnore;
		} catch (FileNotFoundException e) {
			throw new SettingsException("InvariantGeneratorSettings, "+Options.methodsToIgnoreFile+" : file "+fileName+" cannot be read");
		} catch (IOException e) {
			throw new SettingsException("InvariantGeneratorSettings, "+Options.methodsToIgnoreFile+" : file "+fileName+" cannot be read");
		}
		
	}

	public File getTemporaryDir() {
		return temporaryDir;
	}

	public Class getNormalizedTraceHandlerType() {
		return normalizedTraceHandlerType;
	}

	public File getDistilledDir() {
		return distilledDir;
	}

	public Class getTraceReaderType() {
		return traceReaderType;
	}

	public String getFSAEngine() {
		return fsaEngine;
	}

	public String getDaikonConfig() {
		return daikonConfig;
	}

	public Set<String> getMethodsToIgnore() {
		return methodsToIgnore;
	}

	/**
	 * Returns true if InvariantGenerator can add additional derivide invariants like NOT NULL statements
	 * 
	 * @return
	 */
	public boolean addAdditionInvariants() {
		return addAdditionalInvariants;
	}

	public String getDaikonAdditionalOptions() {
		return daikonAdditionalOptions;
	}

	/**
	 * Returns the number of parallel threads to execute in the invariant generation process.
	 * The value i sset by the user in the iNvariantGeneratorSettings.conf file
	 *  
	 * @return
	 */
	public int getParallelInferenceThreads() {
		return parallelInferenceThreads;
	}
	
	public boolean isExapandReferences() {
		return expandReferences;
	}

	public MetaDataHandlerSettings getMetaDataHandlerSettings() {
		return metaDataHandlerSettings;
	}

	public boolean isOptimizationEnabled() {
		return optimizationEnabled;
	}

	public String getFsaEngine() {
		return fsaEngine;
	}

	public boolean isAddAdditionalInvariants() {
		return addAdditionalInvariants;
	}

	public boolean isExpandReferences() {
		return expandReferences;
	}

	public String getDaikonPath() {
		return daikonPath;
	}

	public String getDaikonConfidenceLevel() {
		return daikonConfidenceLevel;
	}

	public int getDaikonExecutionTimeLimit() {
		return daikonExecutionTimeLimit;
	}

	public void setDaikonExecutionTimeLimit(int daikonExecutionTimeLimit) {
		this.daikonExecutionTimeLimit = daikonExecutionTimeLimit;
	}

	public Class<? extends TraceMaintainer> getTraceMaintainerType() {
		return traceMaintainerType;
	}

	public Boolean getInferComponentsIOModels() {
		return inferComponentsIOModels;
	}

	public void setInferComponentsIOModels(Boolean inferComponentsIOModels) {
		this.inferComponentsIOModels = inferComponentsIOModels;
	}

	public boolean getInferComponentsInteractionModels() {
		return inferComponentsInteractionModels;
	}

	public void setInferComponentsInteractionModels(
			Boolean inferComponentsInteractionModels) {
		this.inferComponentsInteractionModels = inferComponentsInteractionModels;
	}

	public boolean getInferComponentsIOInteractionModels() {
		return inferComponentsIOInteractionModels;
	}

	public void setInferComponentsIOInteractionModels(
			Boolean inferComponentsIOInteractionModels) {
		this.inferComponentsIOInteractionModels = inferComponentsIOInteractionModels;
	}

	public Boolean getInferClassesUsageInteractionModels() {
		return inferClassesUsageInteractionModels;
	}

	public void setInferClassesUsageInteractionModels(
			Boolean inferClassesUsageInteractionModels) {
		this.inferClassesUsageInteractionModels = inferClassesUsageInteractionModels;
	}

	public void setTraceMaintainerType(
			Class<? extends TraceMaintainer> traceMaintainerType) {
		this.traceMaintainerType = traceMaintainerType;
	}

	public DaikonComparisonCriterion getDaikonComparisonCriterion() {
		return comparisonCriterion ;
	}

	public boolean getDeleteTemporaryDir() {
		return deleteTemporaryDir;
	}

	public boolean getExcludeConstantLikeNames() {
		return excludeConstantLikeVariables ;
	}

	public boolean getSkipInference() {
		return skipInference ;
	}

	


	
	public Set<String> getSessionsToIgnore() {
		return sessionsToIgnore;
	}

	public void setSessionsToIgnore(Set<String> sessionsToIgnore) {
		this.sessionsToIgnore = sessionsToIgnore;
	}

	public Set<String> getTestsToIgnore() {
		return testsToIgnore;
	}

	public void setTestsToIgnore(Set<String> testsToIgnore) {
		this.testsToIgnore = testsToIgnore;
	}

	public void setActionsToIgnore(Set<String> actionsToIgnore) {
		this.actionsToIgnore = actionsToIgnore;
	}

	public boolean getInferClassesUsageInteractionModelsWithOutgoingCalls() {
		return inferClassesUsageInteractionModelsWithOutgoingCalls ;
	}

	public Set<String> getMethodsToIncludePatterns() {
		// TODO Auto-generated method stub
		return methodsToIncludePatterns;
	}

	public void setDeleteTemporaryDir(boolean b) {
		deleteTemporaryDir=false;
	}

	public boolean getDaikonUndoOptimizations() {
		return daikonUndoOptimizations;
	}

	public boolean getIdentifyTestedMethods() {
		return identifyTestedMethods;
	}

	

}
