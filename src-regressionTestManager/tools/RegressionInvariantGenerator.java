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
package tools;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import regressionTestManager.RegressionTestCollection;
import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.TcPrioritySorter;
import regressionTestManager.TestCasesMetaDataHandler;
import regressionTestManager.detectionMatrix.DetectionMatrixGenerator;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.InteractionTcDetectionMatrix;
import regressionTestManager.detectionMatrix.IoTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.detectionMatrix.UnknownTestCaseInfo;
import regressionTestManager.detectionMatrix.launchers.MatrixLauncher;
import regressionTestManager.ioInvariantParser.InvariantParseException;
import regressionTestManager.ioInvariantParser.IoInvariantParser;
import regressionTestManager.priorityComparators.InteractionPriorityComparator;
import regressionTestManager.priorityComparators.InteractionPriorityComparatorOccurrencies;
import regressionTestManager.priorityComparators.IoPriorityComparator;
import regressionTestManager.priorityComparators.IoPriorityComparatorOccurrencies;
import regressionTestManager.priorityComparators.PriorityComparatorOccurrencies;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerFactory;
import regressionTestManager.tcSpecifications.TcSpecification;
import conf.EnvironmentalSetter;

public class RegressionInvariantGenerator extends InvariantGenerator {
	private static final String skipLPOption = "-skipLP";
	private static final String componentFileOption = "-componentFile";
	private static final String ignoreDefaultOption = "-ignoreDefault";
	private static final String useMatrixOption = "-useMatrix";
	private static final String groupClassOption = "-groupByTestClass";
	private static final String printMethodsInfoOption = "-printMethodsInfo";
	private static boolean groupClass;
	private static boolean ignoreDefault;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InvariantGenerator.setMetaDataHandler( new TestCasesMetaDataHandler() );
		
		ArrayList<String> _args = new ArrayList<String>(args.length);
		
		boolean skipLP = false;
		boolean printMethodsInfo = false;
		String matrixToUse = null;
		String componentFile = null;
		
		//remove regression options
		for ( int i = 0; i < args.length; ++i ){
			if ( args[i].equals(skipLPOption)){
				skipLP=true;
			} else if ( args[i].equals(groupClassOption)){
				groupClass=true;
			} else if ( args[i].equals(ignoreDefaultOption)){
				ignoreDefault=true;
			} else if ( args[i].equals(componentFileOption)){//Unused 
				//TODO: add real implementation for the management of multiple componnet substitution
				//bug #90
				componentFile = args[++i];
			} else if ( args[i].equals(useMatrixOption)) {
				matrixToUse = args[++i];
			} else if ( args[i].equals(printMethodsInfoOption)) {
				printMethodsInfo = true;
			}	else {
				_args.add(args[i]);
			}
			
		}
		
		if ( printMethodsInfo == true ){
			TcInfoHandler infoHandler = TcInfoHandlerFactory.getTcInfoHandler();
			Set<String> tcIds = infoHandler.getTestCasesIds();
			for ( String id : tcIds ){
				TestCaseInfo tci = infoHandler.getTestCaseInfoFromId(id);
				System.out.println(tci);
			}
		}
		
		if ( matrixToUse != null ){
			try {
				String[] matrixesToUse = matrixToUse.split(",");
				processGenericMatrix(matrixesToUse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		
		String[] argsToPass = new String[_args.size()];
		
		InvariantGenerator.main(_args.toArray(argsToPass));
		
		
		
		TcInfoHandler infoHandler = TcInfoHandlerFactory.getTcInfoHandler();
		
		
		RegressionTestManagerHandlerSettings regressionTestManagerHandler = (RegressionTestManagerHandlerSettings) EnvironmentalSetter.getInvariantGeneratorSettings().getMetaDataHandlerSettings();
		MatrixLauncher launcher = regressionTestManagerHandler.getLinearLauncher();
		
		
		InteractionTcDetectionMatrix dmInteraction = DetectionMatrixGenerator.createInteractionMatrix(infoHandler);
		

		try {
			TcDetectionMatrix.printCSVFormat( dmInteraction, new FileOutputStream("interactionMatrix.csv") );
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if ( ! skipLP ){
			defineInteractionCoverageTS(dmInteraction,launcher);
		} else {
			System.out.println("Option "+skipLPOption+" set, skipping generation of matrix");
		}
		
		//
		//	IO
		//
		
		ArrayList<TcSpecification> ioSpecifications;
		try {
			ioSpecifications = getTestCasesIoSpecifications();
			IoTcDetectionMatrix dmIO = DetectionMatrixGenerator.createIOMatrix( infoHandler, ioSpecifications );
			
			TcDetectionMatrix.printCSVFormat( dmIO, new FileOutputStream("ioMatrixAll.csv") );
//			
//			
//			IoTcDetectionMatrix newDM = new IoTcDetectionMatrix();
//			copyCoveredElements( dmIO, newDM );
			
			dmIO = removeUncoveredElements(dmIO);
			
			TcDetectionMatrix.printCSVFormat( dmIO, new FileOutputStream("ioMatrix.csv") );
			
			if ( ! skipLP ){
				defineIOCoverageTS(dmIO,launcher);
			} else {
				System.out.println("Option "+skipLPOption+" set, skipping generation of matrix");
			}
			
			
			
			
		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		
		}
	
		
	}
	
	private static GenericTcDetectionMatrix removeUncoveredElements(GenericTcDetectionMatrix dmIO) {
		int coveredCount[] = new int[dmIO.getElementsSize()];
		
		
		
		Iterator<TestCaseInfo> tcIt = dmIO.getTestCasesIterator();
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = tcIt.next();
			Boolean[] covered = dmIO.getCoverageVectorForTest(tcInfo.getName());
			int c = 0;
			for ( Boolean coveredEl : covered ){
				if ( coveredEl ){
					++coveredCount[c];
				}
				++c;
			}
		}
		
		
		//search if there are elements with no coverage
		int notCovered=0;
		for ( int covered : coveredCount ){
			if ( covered==0 ){
				++notCovered;
			}
		}
		
		if ( notCovered == 0)
			return dmIO;
		
		//calculate the size of the final elements
		ArrayList<String> elements = dmIO.getElementsToCover();
		int coveredSize = elements.size() - notCovered;
		ArrayList<String> newEls = new ArrayList<String>(coveredSize);
		
		int count = 0;
		for ( String el : elements ){
			if ( coveredCount[count] != 0 ){
				newEls.add(elements.get(count));
			}
			++count;
		}
		
		GenericTcDetectionMatrix result = new GenericTcDetectionMatrix();
		result.setElementsToCover(newEls);
		
		tcIt = dmIO.getTestCasesIterator();
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = tcIt.next();
			Boolean[] covered = dmIO.getCoverageVectorForTest(tcInfo.getName());
			Boolean[] newCovered = new Boolean[coveredSize]; 
			int c = 0;
			int posNew=0;
			for ( Boolean coveredEl : covered ){
				if ( coveredCount[c] != 0 ){
					newCovered[posNew]=coveredEl;
					++posNew;
				}
				++c;
			}
			
			result.addTestVector(tcInfo, newCovered);
		}
		
		return result;
	}
	
	
	/**
	 * In certain cases elements are not covered by tests (for example in case of string)
	 * 
	 * @param dmIO
	 * @return
	 */
	private static IoTcDetectionMatrix removeUncoveredElements(IoTcDetectionMatrix dmIO) {
		int coveredCount[] = new int[dmIO.getElementsSize()];
		
		
		
		Iterator<TestCaseInfo> tcIt = dmIO.getTestCasesIterator();
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = tcIt.next();
			Boolean[] covered = dmIO.getCoverageVectorForTest(tcInfo.getName());
			int c = 0;
			for ( Boolean coveredEl : covered ){
				if ( coveredEl ){
					++coveredCount[c];
				}
				++c;
			}
		}
		
		
		//search if there are elements with no coverage
		int notCovered=0;
		for ( int covered : coveredCount ){
			if ( covered==0 ){
				++notCovered;
			}
		}
		
		if ( notCovered == 0)
			return dmIO;
		
		//calculate the size of the final elements
		ArrayList<TcSpecification> elements = dmIO.getElementsToCover();
		int coveredSize = elements.size() - notCovered;
		ArrayList<TcSpecification> newEls = new ArrayList<TcSpecification>(coveredSize);
		
		int count = 0;
		for ( TcSpecification el : elements ){
			if ( coveredCount[count] != 0 ){
				newEls.add(elements.get(count));
			}
			++count;
		}
		
		IoTcDetectionMatrix result = new IoTcDetectionMatrix();
		result.setElementsToCover(newEls);
		
		tcIt = dmIO.getTestCasesIterator();
		while ( tcIt.hasNext() ){
			TestCaseInfo tcInfo = tcIt.next();
			Boolean[] covered = dmIO.getCoverageVectorForTest(tcInfo.getName());
			Boolean[] newCovered = new Boolean[coveredSize]; 
			int c = 0;
			int posNew=0;
			for ( Boolean coveredEl : covered ){
				if ( coveredCount[c] != 0 ){
					newCovered[posNew]=coveredEl;
					++posNew;
				}
				++c;
			}
			
			result.addTestVector(tcInfo, newCovered);
		}
		
		return result;
	}
		

	private static void processGenericMatrix(String[] matrixesToUse) throws IOException {
		
		File matrixFile = new File(matrixesToUse[0]);
		
		TcDetectionMatrix dm = getMatrixFromFile(matrixFile);
		
		for ( int i = 1; i < matrixesToUse.length;++i){
			matrixFile = new File(matrixesToUse[i]);
			
			TcDetectionMatrix ldm = getMatrixFromFile(matrixFile);
			
			dm = mergeMatrix(dm,ldm);
		}
		
		//TcDetectionMatrix.printCSVFormat( dm, new FileOutputStream("merged.csv") );
		
		RegressionTestManagerHandlerSettings regressionTestManagerHandler = (RegressionTestManagerHandlerSettings) EnvironmentalSetter.getInvariantGeneratorSettings().getMetaDataHandlerSettings();
		MatrixLauncher launcher = regressionTestManagerHandler.getLinearLauncher();
		
		defineGenericCoverageTS(dm, launcher);
	}

	/**
	 * LOad  a matrix from a csv file, if specified in configuration options the matrix is processed removing default test case and group test by class name
	 * 
	 * @param matrixFile
	 * @return
	 * @throws IOException
	 */
	public static TcDetectionMatrix getMatrixFromFile(File matrixFile) throws IOException {
		
		FileInputStream is = new FileInputStream(matrixFile);
		
		TcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		is.close();
		
		if ( ignoreDefault ){
			dm = copyCoveredElements(dm);
		}
		
		
		if ( groupClass ){
			dm = groupTestCasesByClass(dm);
		}
		
		
		return dm;
	}

	private static TcDetectionMatrix mergeMatrix(TcDetectionMatrix ldm, TcDetectionMatrix rdm) {
		ArrayList lElements = ldm.getElementsToCover();
		ArrayList rElements = rdm.getElementsToCover();
		int lElementsSize = lElements.size();
		int rElementsSize = rElements.size();
		int totalSize = lElements.size()+rElements.size();
		
		//Merge elements to cover
		ArrayList elements = new ArrayList(totalSize);
		elements.addAll(lElements);
		elements.addAll(rElements);
		
		GenericTcDetectionMatrix dm = new GenericTcDetectionMatrix();
		
		dm.setElementsToCover(elements);
		
		Set<TestCaseInfo> testCases = new HashSet<TestCaseInfo>();
		Iterator<TestCaseInfo> it = ldm.getTestCasesIterator();
		while ( it.hasNext() ){
			TestCaseInfo tc = it.next();
			testCases.add(tc);
		}
		
		it = rdm.getTestCasesIterator();
		while ( it.hasNext() ){
			TestCaseInfo tc = it.next();
			testCases.add(tc);
		}
		
		Boolean[] falseArray = new Boolean[totalSize];
		for ( int i = 0 ; i < totalSize; ++i){
			falseArray[i]=false;
		}
//		System.out.println("L elements "+lElementsSize);
//		System.out.println("R elements "+rElementsSize);
//		System.out.println("Total elements "+totalSize);
		for ( TestCaseInfo tc : testCases ){
//			System.out.println(tc.getName());
			Boolean[] lCoverage = ldm.getCoverageVectorForTest(tc.getName());
			Boolean[] rCoverage = rdm.getCoverageVectorForTest(tc.getName());
			Boolean[] coverage = new Boolean[totalSize];
//			System.out.println("L cov l "+lCoverage.length);
//			System.out.println("R cov l "+rCoverage.length);
			if ( lCoverage != null ){
				System.arraycopy(lCoverage, 0, coverage, 0, lElementsSize);
			} else {
				System.arraycopy(falseArray, 0, coverage, 0, lElementsSize);
			}
			if ( rCoverage != null ){
				System.arraycopy(rCoverage, 0, coverage, lElementsSize, rElementsSize);
			} else {
				System.arraycopy(falseArray, 0, coverage, lElementsSize, rElementsSize);
			}
			dm.addTestVector(tc, coverage);
		}
		
		return dm;
	}

	private static GenericTcDetectionMatrix groupTestCasesByClass(TcDetectionMatrix dm) {
		Iterator<TestCaseInfo> tcIt = dm.getTestCasesIterator();
		HashMap<String,Boolean[]> tcMap=new HashMap<String,Boolean[]>();
		while ( tcIt.hasNext() ){
			TestCaseInfo tc = tcIt.next();
			Boolean[] els = dm.getCoverageVectorForTest(tc.getName());
			
			String cName = tc.getTestClassName();
			Boolean[] b = tcMap.get(cName);
			if ( b == null ){
				b = new Boolean[dm.getElementsSize()];
				for ( int i = 0; i < els.length; ++i){
					b[i]=els[i];
				}
			}else{
				for ( int i = 0; i < els.length; ++i){
					b[i]|=els[i];
				}
			}
			
			tcMap.put(cName, b);
		}
		
		GenericTcDetectionMatrix result = new GenericTcDetectionMatrix();
		result.setElementsToCover(dm.getElementsToCover());
		for ( String tc : tcMap.keySet() ){
			result.addTestVector(new UnknownTestCaseInfo(tc), tcMap.get(tc) );
		}
		return result;
	}

	private static void defineIOCoverageTS(IoTcDetectionMatrix dmIO, MatrixLauncher launcher) {
		// TODO Auto-generated method stub
		
		
		
		
		RegressionTestCollection resultIo = launcher.launch( dmIO );
		System.out.println("=====================                              ======================");
		System.out.println("=====================               IO             ======================");
		System.out.println("=====================                              ======================");
		System.out.println("\nCompatibility Test Suite: \n");
		printResults(dmIO,resultIo);
		
		
		
		IoPriorityComparator pcIo = new IoPriorityComparatorOccurrencies(dmIO);
		List elements = TcPrioritySorter.sortIO( resultIo, dmIO, pcIo );
		
		Iterator<TestCaseInfo> it = elements.iterator();
		
	
		System.out.println("\n\nPrioritized Test Suite:\n");
		while(it.hasNext())
			System.out.println(it.next().getName());
		
		System.out.println("=====================================================================");
	}

	/**
	 * Defines and print the interaction coverage test suite
	 * 
	 * @param dmInteraction
	 * @param launcher
	 */
	private static void defineInteractionCoverageTS(InteractionTcDetectionMatrix dmInteraction, MatrixLauncher launcher) {
		RegressionTestCollection resultInt = launcher.launch( dmInteraction );


		InteractionPriorityComparator intPc = new InteractionPriorityComparatorOccurrencies(dmInteraction);
		List elements = TcPrioritySorter.sortInteraction( resultInt, dmInteraction, intPc );


		System.out.println("========================                          ======================");
		System.out.println("========================       Interaction        ======================");
		System.out.println("========================                          ======================");
		System.out.println("\nCompatibility Test Suite: \n");
		printResults(dmInteraction, resultInt);



		System.out.println("\n\nPrioritized Test Suite :\n");
		Iterator<TestCaseInfo> it = elements.iterator();
		while(it.hasNext())
			System.out.println(it.next().getName());


	}

	private static void defineGenericCoverageTS(TcDetectionMatrix dmInteraction, MatrixLauncher launcher) {
		RegressionTestCollection resultInt = launcher.launch( dmInteraction );


		PriorityComparatorOccurrencies intPc = new PriorityComparatorOccurrencies(dmInteraction);
		List elements = TcPrioritySorter.sortGeneric( resultInt, dmInteraction, intPc );


		System.out.println("========================                          ======================");
		System.out.println("========================       result             ======================");
		System.out.println("========================                          ======================");
		System.out.println("\nCompatibility Test Suite: \n");
		printResults(dmInteraction, resultInt);



		System.out.println("\n\nPrioritized Test Suite :\n");
		Iterator<TestCaseInfo> it = elements.iterator();
		while(it.hasNext())
			System.out.println(it.next().getName());


	}
	
	
	/**
	 * This method populates a new InteractionMatrix only with the entries covered by test cases.
	 * 
	 * In real experiments it can happen that a component interaction is recorded while no tests are in execution, in this case the TestCaseMetadataHanlder associate 
	 * a default test name to the generated models.
	 * 
	 * @param tcDM
	 * @return 
	 * @return
	 */
	public static TcDetectionMatrix copyCoveredElements(TcDetectionMatrix tcDM) {
		GenericTcDetectionMatrix newDm = new GenericTcDetectionMatrix();
		Iterator<TestCaseInfo> tcIt = tcDM.getTestCasesIterator();
		Iterator vIt = tcDM.coverageIterator();
		
		ArrayList<TestCaseInfo> testCasesClean = new ArrayList<TestCaseInfo>(tcDM.getTestCasesNumber());
		
		newDm.setElementsToCover(tcDM.getElementsToCover());
		
		
		while ( tcIt.hasNext() ){
			TestCaseInfo tc = (TestCaseInfo) tcIt.next();
			
			if ( ! tc.getName().equals("default") ){
				newDm.addTestVector(tc, tcDM.getCoverageVectorForTest(tc.getName()));
			} 
		}
		
		newDm = removeUncoveredElements(newDm);
		
		return newDm;
	}


	public static void printResults( TcDetectionMatrix matrix, RegressionTestCollection resultInt ){
		
		System.out.println("Accepted:");
		
		Iterator<TestCaseInfo> it = resultInt.getOrderedIterator();
		while ( it.hasNext() )
			System.out.println( it.next().getName() );
		
	}
	
	
	public static ArrayList<TcSpecification> getTestCasesIoSpecifications() throws ModelsFetcherException {
		ModelsFetcher modelsFetcher = ModelsFetcherFactoy.modelsFetcherInstance;
		Set methods = modelsFetcher.getIoModelsNames();
		ArrayList<TcSpecification> specifications = new ArrayList<TcSpecification>();
		
		Iterator mit = methods.iterator();
		while(mit.hasNext()){
			String methodSignature = (String) mit.next();
			
			Iterator modelItEnter = modelsFetcher.getIoModelIteratorEnter(methodSignature);
			specifications.addAll( getIoSpecifications(modelItEnter, methodSignature+":::ENTER" ));
			
			Iterator modelItExit = modelsFetcher.getIoModelIteratorExit(methodSignature);
			specifications.addAll( getIoSpecifications(modelItExit, methodSignature+":::EXIT1" ));
		}
		
		return specifications;
	}
	
	private static ArrayList<TcSpecification> getIoSpecifications( Iterator modelIt, String programPoint ){
		ArrayList<TcSpecification> specifications = new ArrayList<TcSpecification>();
		while ( modelIt.hasNext() ){
			String expression = (String) modelIt.next();
			try {
				specifications.addAll( IoInvariantParser.getTestCaseSpecifications( expression, programPoint ) );
			} catch (InvariantParseException e) {
				System.err.println("Caught pareser exception on program point "+programPoint);
				e.printStackTrace();
				System.err.println("Ignoring...");
			}
		}
		return specifications;
	}

	
	
	
}
