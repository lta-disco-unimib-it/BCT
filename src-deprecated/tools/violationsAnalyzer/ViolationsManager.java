package tools.violationsAnalyzer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;


public class ViolationsManager {
	
	private HashMap<String,Violation> violations = new HashMap<String, Violation>();
	private HashMap<String,String> violationIds = new HashMap<String, String>();
	private HashMap<String,TestCaseInfo> testCases = new HashMap<String,TestCaseInfo>();
	
	
	private ProgramGraph programGraph = new ProgramGraph();
	private int time;
	private ArrayList failingTcs;



	public ViolationsManager(ArrayList<String> failingTcs) {
		this.failingTcs = new ArrayList(failingTcs);
	}

	public void addViolation(String testCase, String violationName, CallTrace trace, int lineNumber ) {

		Violation violation = getViolation( violationName );
		ViolationPoint vp = new ViolationPoint(violation, lineNumber, trace);
		
		TestCaseInfo tc;
		if ( testCases.containsKey(testCase) ){
			tc = testCases.get(testCase);
		} else {
			boolean pass = !failingTcs.contains(testCase);
			tc = new TestCaseInfo(testCase,pass);
			testCases.put(testCase, tc);
		}
		tc.addViolation( vp );
		violation.add( tc );
		
		programGraph.addTraceExecution( vp, trace );
	}

	private Violation getViolation(String violationName) {
		String violationId;
		
		if ( ! violationIds.containsKey(violationName) ){
			violationId = String.valueOf(violationIds.size());
			violationIds.put(violationName, violationId );
			
		} else {
			violationId = violationIds.get(violationName);
		}
		
		if ( violations.containsKey(violationId) ){
			return violations.get(violationId);
		} else {
			Violation viol = new Violation(violationId,violationName);
			violations.put( violationId, viol );
			return viol;
		}
	}

	private int getTimeStamp() {
		return ++time;
	}

	public String getId(Entry violEntry) {
		return violationIds.get(violEntry.getKey());
	}

	public ProgramGraph getProgramGraph() {
		return programGraph;
	}
	
	public int getOccurrencies( String testCase, String violation ){
		TestCaseInfo tc = testCases.get(testCase);
		String id = getViolationId( violation );
		Violation viol = violations.get(id);
		return tc.getOccurrencies(viol);
	}

	private String getViolationId(String violation) {
		return violationIds.get(violation);
	}

	public String getId(String viol) {
		return violationIds.get(viol);
	}

	public Collection<TestCaseInfo> getTestCases() {
		return testCases.values();
	}

	public Collection<Violation> getViolations(){
		return violations.values();
	}
	
	
}

