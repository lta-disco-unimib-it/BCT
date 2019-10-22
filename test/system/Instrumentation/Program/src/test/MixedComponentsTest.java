package test;

import packageA.A;
import packageC.C;
import packageF.F;
import packageM.M;
import packageM.one.MOne;
import packageM.one.two.MOneTwo;
import support.BctInstrumentationChecker;
import support.BctInstrumentationCheckerMixed;
import junit.framework.TestCase;

public class MixedComponentsTest extends TestCase {
	
	public MixedComponentsTest(String name) {
		super(name);
		BctInstrumentationCheckerMixed checker = new BctInstrumentationCheckerMixed();
		BctInstrumentationChecker.setTestChekcer(checker);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testAm(){
		A a = new A();
		assertEquals(0,a.m());
	}
	
	public void testCm(){
		C c = new C();
		assertEquals(1,c.m());
	}
	
	public void testFm(){
		F f = new F();
		assertEquals(4,f.m());
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
