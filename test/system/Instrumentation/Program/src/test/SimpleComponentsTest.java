package test;

import packageA.A;
import packageC.C;
import packageF.F;
import support.BctInstrumentationChecker;
import support.BctInstrumentationCheckerSimple;
import junit.framework.TestCase;

public class SimpleComponentsTest extends TestCase {

	public SimpleComponentsTest(String name) {
		super(name);
		BctInstrumentationCheckerSimple testChecker = new BctInstrumentationCheckerSimple();
		BctInstrumentationChecker.setTestChekcer(testChecker);
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
	
	public void testFinish(){
		BctInstrumentationChecker.getInstance().finished();
	}
}
