package support;
import java.util.ArrayList;
import java.util.List;


public abstract class BctInstrumentationChecker {
	
	static class BctMonitoredInteraction{
		private ExecutionPoint fragmentType;
		private String monitoredMethod;
		private String testCase;

		public BctMonitoredInteraction(ExecutionPoint fragmentType, String monitoredMethod, String testCase){
			this.fragmentType = fragmentType;
			this.monitoredMethod = monitoredMethod;
			this.testCase = testCase;
		}
		
		public boolean match(ExecutionPoint fragmentType, String monitoredMethod, String testCase){
			return ( this.fragmentType.equals(fragmentType)
					&& this.monitoredMethod.equals(monitoredMethod)
					&& this.testCase.equals(testCase));
		}
		
		public boolean matchCall(ExecutionPoint fragmentType, String monitoredMethod){
			return ( this.fragmentType.equals(fragmentType)
					&& this.monitoredMethod.equals(monitoredMethod)
					);
		}
		
		public String toString(){
			return fragmentType+" "+monitoredMethod+" "+testCase;
		}
	}
	
	
	
	public static enum ExecutionPoint { ENTER, EXIT };
	private static enum TestResult { PASSED, FAILED};
	private static BctInstrumentationChecker instance;
	protected ArrayList<BctMonitoredInteraction> expectedInteractions = new ArrayList<BctMonitoredInteraction>();
	private int callsCounter;
	
	public BctInstrumentationChecker (){
		
	}
	
	
	


	public static BctInstrumentationChecker getInstance() {
		return instance;
	}


//	public void check(ExecutionPoint fragmentType, String signature) {
//		
//	}

	public void check(ExecutionPoint fragmentType, String monitoredMethod, String testCase) {
		if ( ! ( expectedInteractions.size() > callsCounter ) ){
			failedNoMoreExecutionExpected(callsCounter,fragmentType,monitoredMethod,testCase);
			return;
		}
		BctMonitoredInteraction expected = expectedInteractions.get(callsCounter);
		if ( expected.match(fragmentType, monitoredMethod, testCase) ){
			passed(callsCounter,fragmentType,monitoredMethod,testCase);
		} else {
			failed(callsCounter,expected,fragmentType,monitoredMethod,testCase);
		}
			
			
			
		callsCounter++;
	}

	
	public void finished(){
		if(  callsCounter  >= expectedInteractions.size() ){
			finishedOk();
		} else {
			finishedFail(expectedInteractions.subList(callsCounter, expectedInteractions.size()-1));
		}
	}
	

	private void finishedOk() {
		System.out.println("TEST FINISHED");
	}

	private void finishedFail( List<BctMonitoredInteraction> missing ) {
		System.out.println("FAILED : finish");
		System.out.println("\tRaeson : missing calls");
		for ( BctMonitoredInteraction missingCall : missing ){
			System.out.println("\t\t"+missingCall);
		}
	}

	private void failedNoMoreExecutionExpected(int curCounter, ExecutionPoint fragmentType, String monitoredMethod, String testCase) {
		logTestResult(TestResult.FAILED, curCounter, fragmentType, monitoredMethod, testCase);
		System.out.println("\tReason : No method call expected");
	}


	private void passed(int curCounter, ExecutionPoint fragmentType, String monitoredMethod, String testCase) {
		logTestResult(TestResult.PASSED, curCounter, fragmentType, monitoredMethod, testCase);
	}

	private void failed(int curCounter, BctMonitoredInteraction expected, ExecutionPoint fragmentType, String monitoredMethod, String testCase) {
		logTestResult(TestResult.FAILED, curCounter, fragmentType, monitoredMethod, testCase);
		System.out.println("\tReason : Expected "+expected);
	}

	private void logTestResult(TestResult result, int curCounter, ExecutionPoint fragmentType, String monitoredMethod, String testCase) {
		System.out.println(result+"\t"+curCounter+"\t"+fragmentType+"\t"+monitoredMethod+"\t"+testCase);
	}

	public static void setTestChekcer(BctInstrumentationChecker checker) {
		instance = checker;
	}


}
