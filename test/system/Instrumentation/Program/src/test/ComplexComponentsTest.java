package test;

import packageM.M;
import packageM.one.MOne;
import packageM.one.two.MOneTwo;
import support.BctInstrumentationChecker;
import support.BctInstrumentationCheckerComplex;
import junit.framework.TestCase;

public class ComplexComponentsTest extends TestCase {
	
	public ComplexComponentsTest(String name) {
		super(name);
		BctInstrumentationCheckerComplex checker = new BctInstrumentationCheckerComplex();
		BctInstrumentationChecker.setTestChekcer(checker);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	public void testMm(){
		M m = new M();
		assertEquals ( 2, m.m());
		
		MOne mOne = new MOne();
		assertEquals ( 5, mOne.t());
		
		MOneTwo mOneTwo = new MOneTwo();
		mOneTwo.s();
	}
	
	public void testFinish(){
		BctInstrumentationChecker.getInstance().finished();
	}
}
