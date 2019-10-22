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
package conf.management;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import modelsFetchers.FileModelsFetcher;
import recorders.BufferedRecorder;
import recorders.FileDataRecorder;
import recorders.FileOptimizedDataRecorder;
import traceReaders.metaData.ExecutionMetaDataHandlerSettings;
import traceReaders.normalized.NormalizedTraceHandlerFile;
import traceReaders.raw.FileTracesReader;
import conf.EnvironmentalSetter;
import conf.FineInteractionCheckerSettings;
import conf.InvariantGeneratorSettings;
import executionContext.ActionsRegistry;
import executionContext.ExecutionContextStateMaintainerMemory;
import executionContext.TestCasesRegistry;
import flattener.flatteners.BreadthObjectFlattener;

public class ConfigurationFilesManager {

	static String DEFAULT_ONE_OF_SIZE = "3";
	


	public static String getDEFAULT_ONE_OF_SIZE() {
		return DEFAULT_ONE_OF_SIZE;
	}

	public static void setDEFAULT_ONE_OF_SIZE(String dEFAULT_ONE_OF_SIZE) {
		DEFAULT_ONE_OF_SIZE = dEFAULT_ONE_OF_SIZE;
	}



	private static String daikon_config = "default";
	
	public static String getDaikon_config() {
		return daikon_config;
	}

	public static void setDaikon_config(String daikon_config) {
		ConfigurationFilesManager.daikon_config = daikon_config;
	}


	public static class Files {
		public static final String flattenerPropertiesFileName = "objectFlattener.conf";
		public static final String fieldsFilterFileName = "FieldsFilters.properties";
		public static final String classesToIgnoreFileName = "classesToIgnore.list";
		public static final String interactionCheckerPropertiesFileName = "InteractionCheckerSettings.properties";
		public static final String invariantGeneratorPropertiesFileName = "InvariantGenerator.properties";
		public static final String inferenceEnginePropertiesFileName = "InferenceEngine.properties";
		public static final String bctPropertiesFileName = "BCT.properties";
		public static final String dataRecorderSettingsFileName = "DataRecorderSettings.properties";
		public static final String modelsFetcherSettingsFileName = "ModelsFetcherSettings.properties";
		public static final String violationsRecorderSettingsFileName = "ViolationsRecorderSettings.properties";
		public static final String actionsRegistrySettingsFileName = "ActionsRegistrySettings.properties";
		public static final String testCasesRegistrySettingsFileName = "TestCasesRegistrySettings.properties";
		public static final String dbConnectionSettingsFileName = "DBConnectionSettings.properties";
		
		public static final String dataRecordingDirName = "DataRecording";
		public static final String modelsDirName = "Models";
		public static final String scriptDirName="scripts";
		public static final String preprocessingDirName = "Preprocessing";
		public static final String tmpDirName = "tmp";
		
		public static final String runtimeCheckingProbeName = "bctCP.probescript";
		public static final String dataRecordingProbeName = "bctLP.probescript";
		
		public static final String bctCBELogFileName = "bctCBELog";
		public static final String bctViolationsLogAnalysisFolder = "ViolationsLogAnalysis";
		public static final String bctInferenceLogAnalysisFolder = "InferenceLogAnalysis";
		
		public static final String invariantGeneratorTestsToIgnoreFileName = "testsToIgnore.properties";
		public static final String invariantGeneratorActionsToIgnoreFileName = "actionsToIgnore.properties";
		public static final String invariantGeneratorProcessesToIgnoreFileName = "processesToIgnore.properties";
		
		
	}
	
	private static class BctProperties {
		
		private static final String defaultFlattenerType = BreadthObjectFlattener.class.getCanonicalName();
	}

	private static class Constants {
		
		public static final String BctHomeReplacementKey = "%%BCT_HOME%%";
	}
	

	
	
	/**
	 * Return the BCT_HOME folder for the given MonitoringCOnfiguration
	 * 
	 * @param mc
	 * @return
	 * @throws ConfigurationFilesManagerException
	 */
	public static File getBctHomeDir() throws ConfigurationFilesManagerException {
		
			return new File( EnvironmentalSetter.getBctHome() );
			
		
	}
	
	/**
	 * Return the directory containing the BCT configuration files for a MonitoringCOnfiguration 
	 * @param mc
	 * @return
	 * @throws ConfigurationFilesManagerException
	 */
	public static File getConfigurationFilesDir() throws ConfigurationFilesManagerException{
		File bctHome;
		try {
			bctHome = getBctHomeDir();
			return new File(bctHome+File.separator+"conf"+File.separator+"files");
		} catch (ConfigurationFilesManagerException e) {
			throw new ConfigurationFilesManagerException(e);
		}
		
	}
	
	public static void updateConfigurationFiles() throws ConfigurationFilesManagerException{
		updateConfigurationFiles(getConfigurationFilesDir());
	}
	
	
	/**
	 * Updates the bct properties files used by BCT during its runtime phases (Data Recording, Model Inference, Runtime Checking).
	 * The files are usually created under BCT_DATA/<monitoring_configuration_name>/conf/file.
	 * 
	 * @param mc
	 * @throws ConfigurationFilesManagerException
	 */
	public static void updateConfigurationFiles( File confDir ) throws ConfigurationFilesManagerException{
		
		//TODO: check if mc was changed, otherwise do not create files
		
//		File confDir = getConfigurationFilesDir();
		
		if ( ! confDir.exists() ){
			boolean result = confDir.mkdirs();
			if ( ! result ){
				throw new ConfigurationFilesManagerException("Cannot create configuration files directory "+confDir.getAbsolutePath());
			}
		}
		
		saveBCTProperties(confDir);
		
		saveDataRecorderSettings(confDir);
		
		saveDBConnectionSettings(confDir);

		
		
		saveModelsFetcherSettings(confDir );
		
		saveViolationsRecorderSettings(confDir);
		
		saveInvariantGeneratorSettings(confDir);
		
		saveInferenceEngineSettings(confDir);
		
		saveObjectFlattenerOptions(confDir);
		
		saveDaikonConfigOptions(confDir);
		
		createDaikonConfigFiles(confDir);
		
		saveInteractionCheckerSettings(confDir);
		
		saveActionsManagerSettings(confDir);
		
		saveTestCasesRegistrySettings(confDir);
	}

	public static void createDaikonConfigFiles(File confDir) throws ConfigurationFilesManagerException {
		DaikonFilesCreator.createDefaultFileIfAbsent(confDir);
		DaikonFilesCreator.createEssentialsFileIfAbsent(confDir);
	}

	static void saveProperty(Properties defaultP, File file) throws IOException {
		FileWriter fos = new FileWriter(file);
		defaultP.store(fos, "Created by "+ConfigurationFilesManager.class.getCanonicalName());
		fos.close();
	}

	private static void saveInteractionCheckerSettings( File confDir) throws ConfigurationFilesManagerException {
		Properties p = createInteractionCheckerSettings();
		saveProperty(p, confDir, Files.interactionCheckerPropertiesFileName );
	}

	private static Properties createInteractionCheckerSettings() {
		
			FineInteractionCheckerSettings s = new FineInteractionCheckerSettings();
			s.setAnomalousSequencesRecordingEnabled(true);
			s.setAvaPathLen(2);
			s.setDfa(false);
			s.setFineAnalysisEnabled(false);
			Properties p = s.toProperties();
		
		return p;
	}

	private static void saveActionsManagerSettings( File confDir) throws ConfigurationFilesManagerException {
		Properties p = createActionsManagerOptionsObject();
		
		saveProperty(p, confDir, Files.actionsRegistrySettingsFileName );
	}

	private static void saveTestCasesRegistrySettings(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createTestCasesRegistryOptionsObject();
		
		saveProperty(p, confDir, Files.testCasesRegistrySettingsFileName );
	}
	
	private static Properties createActionsManagerOptionsObject() {
		Properties p = new Properties();
		
		p.setProperty("type", ActionsRegistry.class.getCanonicalName());
		p.setProperty("stateRecorderType", ExecutionContextStateMaintainerMemory.class.getCanonicalName());
		return p;
	}

	private static Properties createTestCasesRegistryOptionsObject(
			) {
//		return new Properties();
		Properties p = new Properties();
		
		p.setProperty("type", TestCasesRegistry.class.getCanonicalName());
		p.setProperty("stateRecorderType", ExecutionContextStateMaintainerMemory.class.getCanonicalName());
		return p;
	}
	
	private static void saveDaikonConfigOptions(File confDir) throws ConfigurationFilesManagerException {
		
	}

	private static void saveObjectFlattenerOptions( File confDir) throws ConfigurationFilesManagerException {
		saveFlattenerProperties(confDir);
		
		saveFieldsFilter(confDir);
		
		saveClassesToIgnore(confDir);
	}

	private static void saveClassesToIgnore(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createClassesToIgnorePropertiesObject(); 
		saveProperty(p, confDir, Files.classesToIgnoreFileName );
	}

	private static Properties createClassesToIgnorePropertiesObject() {
		Properties p = new Properties();
		
		return p;
	}

	private static void saveFieldsFilter(File confDir) throws ConfigurationFilesManagerException {
		
		
		
	}

	private static void saveFlattenerProperties(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createFlattenerPropertiesObject(confDir);
		saveProperty(p, confDir, Files.flattenerPropertiesFileName );
	}

	private static Properties createFlattenerPropertiesObject(File confDir) {
		Properties p = new Properties();
		
		p.setProperty("objectFlattener.smashAggregations", String.valueOf(false) );
		p.setProperty("objectFlattener.maxDepth", String.valueOf(3) );
		p.setProperty("fieldsRetrieverConfig", "all" );
		p.setProperty("classesToIgnore.load", "false" );
		
		return p;
	}

	private static void saveInferenceEngineSettings( File confDir) throws ConfigurationFilesManagerException {
		Properties p = createInferenceEngineSettingsObject();
		saveProperty(p, confDir, Files.inferenceEnginePropertiesFileName );
	}

	private static Properties createInferenceEngineSettingsObject() {
		
		Properties p = new Properties();
		
		
		p.put("level", "2");
		p.put("logger", "console");
		p.put("logfile" , "event.log");

		p.put("minTrustLen", "2");
		p.put("maxTrustLen", "4");

		p.put("enableMinimization","end");

		p.put("cutOffSearch","true");
		
		return p;
		
	}

	private static void saveInvariantGeneratorSettings(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createInvariantGeneratorSettingsObject();
		
		
		
		
		
		saveProperty(p, confDir, Files.invariantGeneratorPropertiesFileName );
		
		
	}

	private static Properties createInvariantGeneratorSettingsObject() throws ConfigurationFilesManagerException {
		
		
		
		Properties p = new Properties();
		p.setProperty("type", tools.InvariantGenerator.class.getCanonicalName());
		
		String bctHomeValue = Constants.BctHomeReplacementKey;
		
		String preprocessingDir = bctHomeValue+"/Preprocessing";
		
		p.setProperty(InvariantGeneratorSettings.Options.temporaryDir, preprocessingDir );
		p.setProperty(InvariantGeneratorSettings.Options.deleteTemporaryDir, "true");
		
		
		p.setProperty(InvariantGeneratorSettings.Options.fsaEngine, "KBehavior");
//		p.setProperty(InvariantGeneratorSettings.Options.daikonPath, "/opt/daikon/daikon.jar");
		p.setProperty(InvariantGeneratorSettings.Options.daikonPath, getCurrentClassPath() );
		
		p.setProperty(InvariantGeneratorSettings.Options.daikonConfidenceLevel, "0.99");
		p.setProperty(InvariantGeneratorSettings.Options.daikonConfig, daikon_config);
		p.setProperty(InvariantGeneratorSettings.Options.addAdditionalInvariants, "true");
		p.setProperty(InvariantGeneratorSettings.Options.expandReferences, "true");
		
		p.setProperty(InvariantGeneratorSettings.Options.traceReaderType, FileTracesReader.class.getCanonicalName());
		p.setProperty(FileTracesReader.Options.tracesPath,bctHomeValue+"/DataRecording/");
		p.setProperty(FileTracesReader.Options.ioTracesDirName, "ioInvariantLogs");
		p.setProperty(FileTracesReader.Options.interactionTracesDirName, "interactionInvariantLogs");
		
		p.setProperty(InvariantGeneratorSettings.Options.normalizedTraceHandlerType, NormalizedTraceHandlerFile.class.getCanonicalName());
		p.setProperty(NormalizedTraceHandlerFile.Options.declsDir, preprocessingDir+"/decls");
		p.setProperty(NormalizedTraceHandlerFile.Options.dtraceDir, preprocessingDir+"/dtrace");
		p.setProperty(NormalizedTraceHandlerFile.Options.interactionDir, preprocessingDir+"/interaction");
		
		
		p.put(InvariantGeneratorSettings.Options.excludeConstantLikeVariables, "true");
		
//		if ( Boolean.parseBoolean(p.getProperty(InvariantGeneratorSettings.Options.inferClassesUsageInteractionModels)) ){
			p.put(InvariantGeneratorSettings.Options.metaDataHandlerSettingsType, ExecutionMetaDataHandlerSettings.class.getCanonicalName());
//		}
		
		return p;


	}

	public static String getCurrentClassPath(){
		return System.getProperty("java.class.path");
	}

	private static void saveViolationsRecorderSettings(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createViolationsRecorderSettingsObject();
		saveProperty(p, confDir, Files.violationsRecorderSettingsFileName );
	}

	private static Properties createViolationsRecorderSettingsObject() {

		
		Properties p = new Properties();

		p.put("type", "recorders.FileCBEViolationsRecorder");
		
		
		return p;
		
	}

	private static void saveModelsFetcherSettings(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createModelsFetcherSettingsObject();
		saveProperty(p, confDir, Files.modelsFetcherSettingsFileName );
	}

	private static Properties createModelsFetcherSettingsObject() throws ConfigurationFilesManagerException {
		Properties p = new Properties();
		
			p.setProperty("type", FileModelsFetcher.class.getCanonicalName() );
			
			
			
			p.setProperty(FileModelsFetcher.Options.modelsDir,  Constants.BctHomeReplacementKey +"/"+ Files.modelsDirName);
			//p.setProperty(FileModelsFetcher.Options.exportFormat,  "ser");
			
			p.setProperty(FileModelsFetcher.Options.exportFormat,  "fsa");
			
		
		return p;
	}

	private static void saveDBConnectionSettings( File confDir) throws ConfigurationFilesManagerException {
		Properties p = createDBConnectionSettingsObject();
		saveProperty(p, confDir, Files.dbConnectionSettingsFileName );
	}

	private static Properties createDBConnectionSettingsObject() {
		Properties p = new Properties();
		
//		DBStorageConfiguration dbsc = (DBStorageConfiguration) mc.getStorageConfiguration();
		p.setProperty("databaseURI", "");
		p.setProperty("databaseUSER", "bct");
		p.setProperty("databasePASSWORD", "bct");
		
		return p;
	}

	private static void saveDataRecorderSettings(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createDataRecorderSettingsObject();
		saveProperty(p, confDir, Files.dataRecorderSettingsFileName);
	}

	private static Properties createDataRecorderSettingsObject() {
		Properties p = new Properties();



		//p.setProperty("type", FileDataRecorder.class.getCanonicalName());

		p.setProperty("type", BufferedRecorder.class.getCanonicalName());
		p.setProperty("optimizedDataRecorder", FileOptimizedDataRecorder.class.getCanonicalName());



		p.setProperty(FileDataRecorder.Options.loggingDataDir, Constants.BctHomeReplacementKey +"/"+ Files.dataRecordingDirName );



		return p;
	}

	/**
	 * Create the BCT.properties file
	 * @param mc
	 * @param confDir
	 * @throws ConfigurationFilesManagerException
	 */
	private static void saveBCTProperties(File confDir) throws ConfigurationFilesManagerException {
		Properties p = createBCTPropertiesObject();
		saveProperty ( p, confDir, Files.bctPropertiesFileName);
	}

	private static void saveProperty(Properties p, File destDir, String fileName) throws ConfigurationFilesManagerException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream( new File(destDir, fileName) );
			p.store(fos, "Auto generated by class "+ConfigurationFilesManager.class.getCanonicalName());
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException(e);
		} finally {
			if ( fos != null ){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates the default properties object
	 * 
	 * @return
	 */
	private static Properties createBCTPropertiesObject() {
		Properties p = new Properties();
		
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.flattenerType, BctProperties.defaultFlattenerType);
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.dataRecorderSettings, Files.dataRecorderSettingsFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.interactionInferenceEngineSettings, Files.inferenceEnginePropertiesFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.invariantGeneratorSettings, Files.invariantGeneratorPropertiesFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.interactionCheckerSettings, Files.interactionCheckerPropertiesFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.modelsFetcherSettings, Files.modelsFetcherSettingsFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.violationsRecorderSettings, Files.violationsRecorderSettingsFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.actionsRegistrySettings, Files.actionsRegistrySettingsFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.testCasesRegistrySettings, Files.testCasesRegistrySettingsFileName );
		p.setProperty(EnvironmentalSetter.BctPropertiesOptions.dbConnectionSettings, Files.dbConnectionSettingsFileName );
		
		return p;
	}

	public static Properties loadInvariantGeneratorOptions(File defaultOptionsDir) throws ConfigurationFilesManagerException {
		Properties p = new Properties();
		FileInputStream inStream;
		try {
			System.out.println("LOADING");
			inStream = new FileInputStream( new File(defaultOptionsDir,Files.invariantGeneratorPropertiesFileName) );
			p.load(inStream);
			System.out.println("LOADED");
		} catch (FileNotFoundException e) {
			throw new ConfigurationFilesManagerException("Cannot load invariant generator properties ",e);
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException("Cannot load invariant generator properties ",e);
		}
		return p;
	}

	public static Properties loadInferenceEngineOptionsFromFile(File inputFile) throws ConfigurationFilesManagerException {
		Properties p = new Properties();
		FileInputStream inStream;
		try {
			inStream = new FileInputStream( inputFile );
			p.load(inStream);
		} catch (FileNotFoundException e) {
			throw new ConfigurationFilesManagerException("Cannot load inference negine properties ",e);
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException("Cannot load inference engine properties ",e);
		}
		return p;
	}

	public static void saveInvariantGeneratorOptions( File defaultOptionsDir,
			Properties invariantGeneratorOptions) throws ConfigurationFilesManagerException {
		FileOutputStream os;
		try {
			os = new FileOutputStream( new File(defaultOptionsDir,Files.invariantGeneratorPropertiesFileName) );
			invariantGeneratorOptions.store(os, "");
		} catch (FileNotFoundException e) {
			throw new ConfigurationFilesManagerException("Cannot save invarinat generator properties ",e);
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException("Cannot save invarinat generator properties ",e);
		}
		
	}
	
	public static void saveInferenceEngineOptionsToFile( File dest,
			Properties inferenceEngineOptions) throws ConfigurationFilesManagerException {
		FileOutputStream os;
		try {
			os = new FileOutputStream( dest );
			inferenceEngineOptions.store(os, "");
		} catch (FileNotFoundException e) {
			throw new ConfigurationFilesManagerException("Cannot save inference engine properties ",e);
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException("Cannot save inference engine properties ",e);
		}
		
	}

	/**
	 * Return the data recording script dir where to store scripts
	 * @param mc
	 * @return 
	 * @throws ConfigurationFilesManagerException 
	 */
	public static File getScriptsDir() throws ConfigurationFilesManagerException {
		File bctHome = getConfigurationFilesDir();
		File scriptsDir = new File(bctHome,Files.scriptDirName);
		if ( ! scriptsDir.exists() ){
			scriptsDir.mkdirs();
		}
		return scriptsDir;
	}

	
	public static File getDataRecordingDir( ) throws ConfigurationFilesManagerException{
		return new File(getBctHomeDir(),Files.dataRecordingDirName);
	}
	
	public static File getModelsDir( ) throws ConfigurationFilesManagerException{
		return new File(getBctHomeDir(),Files.modelsDirName);
	}
	
	public static File getPreprocessingDir( ) throws ConfigurationFilesManagerException{
		return new File(getBctHomeDir(),Files.preprocessingDirName );
	}
	
	public static File getTemporaryDir( ) throws ConfigurationFilesManagerException{
		return new File(getBctHomeDir(),Files.tmpDirName );
	}
	

	
	/**
	 * Returns the CBE log file associated to a monitoring configuration.
	 * The file is created when an application is monitored for checking its behavior with the given monitorin cnfiguration.
	 * 
	 * @param mc
	 * @return
	 * @throws ConfigurationFilesManagerException 
	 */
	public static File getBCTCbeLogFile() throws ConfigurationFilesManagerException{
		File home = getBctHomeDir();
		return new File(home, Files.bctCBELogFileName);
	}

	/**
	 * Returns the folder in which CBE log analysis data is stored. If the folder does not exists the method creates it. 
	 * @param mc
	 * @return
	 * @throws ConfigurationFilesManagerException
	 */
	public static File getViolationsLogAnalysisFolder() throws ConfigurationFilesManagerException {
		File home = getBctHomeDir();
		File folder = new File(home, Files.bctViolationsLogAnalysisFolder );
		
		createFolder( folder );
		
		
		return folder;
	}
	
	
	/**
	 * Returns the folder in which CBE log analysis data is stored when generated for models inference. 
	 * If the folder does not exists the method creates it. 
	 * @param mc
	 * @return
	 * @throws ConfigurationFilesManagerException
	 */
	public static File getInferenceLogAnalysisFolder() throws ConfigurationFilesManagerException {
		File home = getBctHomeDir();
		File folder = new File(home, Files.bctInferenceLogAnalysisFolder );
		
		createFolder( folder );
		
		
		return folder;
	}
	
	
	
	
	private static void createFolder(File folder) throws ConfigurationFilesManagerException {
		
		folder.mkdirs();
	}

}
