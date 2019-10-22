package test.system;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;

public class RegressionInvariantGeneratorUtil {
	public static final String test0Name = "test.Test0.test0(()V)";
	public static final String test1Name = "test.Test1.test1(()V)";
	public static final String test2Name = "test.Test2.test2(()V)";
	public static final String test3Name = "test.Test3.test3(()V)";
	
	public static final String methodAAm = "packageA.A.m(()I)";
	public static final String methodBBm = "packageB.B.m(()I)";
	public static final String methodCCm = "packageC.C.m(()I)";
	public static final String methodDDm = "packageD.D.m(()I)";
	
	public static final String ioElementA0 = "packageA.A.m(()I):::EXIT1.returnValue == 1.0";
	public static final String ioElementB0 = "packageB.B.m(()I):::EXIT1.returnValue == 1.0";
	public static final String ioElementC0 = "packageC.C.m(()I):::EXIT1.returnValue == 1.0";
	public static final String ioElementD0 = "packageD.D.m(()I):::EXIT1.returnValue == 1.0";
	public static final String ioElementD1 = "packageD.D.m(()I):::EXIT1.returnValue == 2.0";
	public static final File artifactsDir = new File("test/system/RegressionInvariantGenerator/artifacts/");
	public static final File interactionMatrixFile = new File("interactionMatrix.csv");
	public static final File ioMatrixFile = new File("ioMatrix.csv");
	public static final File ioMatrixAllFile = new File("ioMatrixAll.csv");
	/**
	 * Removes all the files and directories created during a regression test execution
	 * @param tc
	 */
	static void clean(String tc) {
		File tmpDir = new File (artifactsDir,tc+"/tmp");
		TestCase.assertTrue("Test "+tc+" : unable to delete folder "+tmpDir,FileUtil.deleteDirectory(tmpDir));
		
		File modelsDir = new File(artifactsDir,tc+"/Models");
		TestCase.assertTrue("Test "+tc+" : unable to delete folder "+modelsDir,FileUtil.deleteDirectory(modelsDir));
		
		File testCasesDir = new File(artifactsDir,tc+"/testCasesDir");
		TestCase.assertTrue("Test "+tc+" : unable to delete folder "+testCasesDir,FileUtil.deleteDirectory(testCasesDir));
		
		TestCase.assertTrue("Test "+tc+" : unable to delete file "+ioMatrixFile,ioMatrixFile.delete());
		
		TestCase.assertTrue("Test "+tc+" : unable to delete file "+ioMatrixAllFile,ioMatrixAllFile.delete());
		
		TestCase.assertTrue("Test "+tc+" : unable to delete file "+ioMatrixFile,interactionMatrixFile.delete());
	}
}
