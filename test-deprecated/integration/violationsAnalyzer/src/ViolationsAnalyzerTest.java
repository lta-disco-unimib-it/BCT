
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import automata.Node;
import automata.Transition;

import tools.ViolationsAnalyzer;
import tools.violationsAnalyzer.ProgramGraph;
import tools.violationsAnalyzer.TestCaseInfo;
import tools.violationsAnalyzer.ViolationPoint;
import tools.violationsAnalyzer.ViolationsManager;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculator;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorParent;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorUtil;
import tools.violationsAnalyzer.distanceCalculators.DistanceTable;


public class ViolationsAnalyzerTest extends TestCase {
	static final File test01File = new File ( "test/integration/violationsAnalyzer/artifacts/01.txt" );
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void test01() throws IOException{
		ArrayList<String> failing = new ArrayList<String>();
		failing.add("1");
		
		ViolationsManager vm = ViolationsAnalyzer.createViolationsManager(test01File, failing );
		ProgramGraph pg = vm.getProgramGraph();
		DistanceCalculator dc = new DistanceCalculatorParent("testParent");
		
		HashMap<TestCaseInfo, DistanceTable> ts = ViolationsAnalyzer.getDistanceTables( vm );
		
		assertEquals( 1, ts.size() );
		
		Set<Entry<TestCaseInfo, DistanceTable>> elements = ts.entrySet();
		
		Object[] a = elements.toArray();
		Entry<TestCaseInfo, DistanceTable> entry = (Entry<TestCaseInfo, DistanceTable>) a[0];
		
		TestCaseInfo tcInfo = entry.getKey();
		DistanceTable dt = entry.getValue();
		
		//dt.getDistanceDown(getVP(tcInfo,"1"), to);
	}

	
}
