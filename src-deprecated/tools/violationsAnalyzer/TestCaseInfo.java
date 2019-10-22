package tools.violationsAnalyzer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class TestCaseInfo {
	HashMap<Violation,Integer> violations = new HashMap<Violation, Integer>();
	HashSet<ViolationPoint> violationPoints = new HashSet<ViolationPoint>();
	private String testCaseName;
	private boolean pass; 
	
	public TestCaseInfo(String testCase, boolean pass) {
		testCaseName = testCase;
		this.pass = pass;
	}

	public boolean containsViolation(Violation violation) {
		return violations.containsKey(violation);
	}

	/**
	 * Add a violation point to the test case
	 * 
	 * @param vp
	 */
	public void addViolation(ViolationPoint vp) {
		int n = 1;
		Violation viol = vp.getViolation();
		
		if ( containsViolation(viol) ){
			n = violations.get(viol);
			++n;
		}
		violations.put(viol,n);
		violationPoints.add(vp);
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public HashSet<ViolationPoint> getViolationPoints() {
		return violationPoints;
	}

	public Set<Violation> getViolations() {
		return violations.keySet();
	}

	public Integer getOccurrencies( Violation violation ){
		return violations.get(violation);
	}

	public boolean isPassed() {
		return pass;
	}
}
