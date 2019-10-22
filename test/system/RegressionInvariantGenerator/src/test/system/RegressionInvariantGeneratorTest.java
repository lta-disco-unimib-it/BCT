package test.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import tools.RegressionInvariantGenerator;
import conf.EnvironmentalSetter;

public class RegressionInvariantGeneratorTest extends TestCase {
	
	public void testT0() throws IOException{
		
		setTestHome("T0");
		
		String args[]={"-default","-skipLP"};
		RegressionInvariantGenerator.main(args);
		
		
		//Check interaction matrix
		
		FileInputStream is = new FileInputStream(RegressionInvariantGeneratorUtil.interactionMatrixFile);
		
		GenericTcDetectionMatrix dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 1, dm.getTestCasesNumber() );
		
		assertEquals ( 1,dm.getElementsToCover().size() );
		
		Iterator<Boolean[]> it = dm.coverageIterator();
		Boolean[] vector = it.next();
		
		assertTrue(vectorsEqual(new boolean[]{true},vector));
		
		//Check IO matrix
		
		is = new FileInputStream(RegressionInvariantGeneratorUtil.ioMatrixFile);
		
		dm = TcDetectionMatrix.readCSVFormat(is);
		
		assertEquals ( 1, dm.getTestCasesNumber() );
		
		assertEquals ( 1,dm.getElementsToCover().size() );
		
		it = dm.coverageIterator();
		vector = it.next();
		
		assertTrue(vectorsEqual(new boolean[]{true},vector));
		
		assertTrue(RegressionInvariantGeneratorUtil.ioMatrixFile.delete());
		assertTrue(RegressionInvariantGeneratorUtil.interactionMatrixFile.delete());
		
		RegressionInvariantGeneratorUtil.clean("T0");
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
		
		EnvironmentalSetter.setBctHome(new File(RegressionInvariantGeneratorUtil.artifactsDir,test).getAbsolutePath());
	}
}
