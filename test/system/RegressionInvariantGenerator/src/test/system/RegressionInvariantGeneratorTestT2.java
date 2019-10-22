package test.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;
import tools.RegressionInvariantGenerator;
import conf.EnvironmentalSetter;

public class RegressionInvariantGeneratorTestT2 extends TestCase {
	
	private static final File artifactsDir = new File("test/system/RegressionInvariantGenerator/artifacts/");
	static final File interactionMatrixFile = new File("interactionMatrix.csv");
	static final File ioMatrixFile = new File("ioMatrix.csv");
	
	public void testT2() throws IOException{
		
		setTestHome("T2");
		
		String args[]={"-default","-skipLP"};
		RegressionInvariantGenerator.main(args);
		
		
		//Check interaction matrix
		
		FileInputStream is = new FileInputStream(interactionMatrixFile);
		
		GenericTcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 2, dm.getTestCasesNumber() );
		
		assertEquals ( 2,dm.getElementsToCover().size() );
		
		Iterator<Boolean[]> it = dm.coverageIterator();
		Boolean[] vector = it.next();
		
		assertTrue(vectorsEqual(new boolean[]{true,false},vector));
		
		
		vector = it.next();
		
		assertTrue(vectorsEqual(new boolean[]{false,true},vector));
		
		
		//Check IO matrix
		
		is = new FileInputStream(ioMatrixFile);
		
		dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 2, dm.getTestCasesNumber() );
		
		Iterator<TestCaseInfo> testIt = dm.getTestCasesIterator();
		
		TestCaseInfo testInfo;
		
		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test0Name, testInfo.getName());

		testInfo = testIt.next();
		assertEquals(RegressionInvariantGeneratorUtil.test1Name, testInfo.getName());
		
		
		Iterator<String> elementsIt = dm.getElementsToCoverIterator();
		
		String el;
		
		el = elementsIt.next();
		System.out.println(el);
		System.out.println(RegressionInvariantGeneratorUtil.ioElementA0);
		
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementA0, el );

		el = elementsIt.next();
		System.out.println(el);
		assertEquals ( RegressionInvariantGeneratorUtil.ioElementB0, el );


		assertEquals ( 2,dm.getElementsToCover().size() );

		it = dm.coverageIterator();
		vector = it.next();
		
		assertTrue(vectorsEqual(new boolean[]{true,false},vector));
		
		
		vector = it.next();
		assertTrue(vectorsEqual(new boolean[]{false,true},vector));
		
		
		
		assertTrue(ioMatrixFile.delete());
		assertTrue(interactionMatrixFile.delete());
		
		cleanDir("T2");
	}



	
	
	static void cleanDir(String tc) {
		File tmpDir = new File (artifactsDir,tc+"/tmp");
		assertTrue(deleteDirectory(tmpDir));
		
		File modelsDir = new File(artifactsDir,tc+"/Models");
		assertTrue(deleteDirectory(modelsDir));
		
		File testCasesDir = new File(artifactsDir,tc+"/testCasesDir");
		assertTrue(deleteDirectory(testCasesDir));
	}

	static public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					if ( ! deleteDirectory(files[i]) )
						System.out.println("cannotDelete "+files[i]);
					else
						System.out.println("deletet "+files[i]);
				}
				else {
					if ( ! files[i].delete() )
						System.out.println("cannotDelete "+files[i]);
					else
						System.out.println("deletet "+files[i]);
				}
			}
		}
		return( path.delete() );
	}



	public static boolean vectorsEqual(boolean[] bs, Boolean[] vector) {
		if ( bs.length != vector.length )
			return false;
		
		for ( int  i= 0; i < vector.length; ++i ){
			System.out.println(vector[i]);
			if ( ! vector[i].equals(bs[i]) )
				return false;
		}
		
		return true;
	}

	public static void setTestHome(String test) {
		
		EnvironmentalSetter.setBctHome(new File(artifactsDir,test).getAbsolutePath());
	}
}
