package support;

import java.util.ArrayList;
import java.util.List;

import probes.ClassFormatter;


public class BctInstrumentationCheckerSimple extends BctInstrumentationChecker {

	public BctInstrumentationCheckerSimple(){
		expectedInteractions.addAll(getExpectedInteractions("test.SimpleComponentsTest"));
	}
	
	public static List<BctMonitoredInteraction> getExpectedInteractions(String testPackage) {
		String testAm = testPackage+".testAm(()V)";
		String testCm = testPackage+".testCm(()V)";
		String testFm = testPackage+".testFm(()V)";
		
		ArrayList<BctMonitoredInteraction> myExpectedInteractions = new ArrayList<BctMonitoredInteraction>();
		//
		//Oracle for test A
		//
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageA/A", "<init>", "()V"), testAm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageA/A", "<init>", "()V"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageA/A", "m", "()I"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageB/B", "<init>", "()V"), testAm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageB/B", "<init>", "()V"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageB/B", "mb", "()V"), testAm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageB/B", "mb", "()V"), testAm ) );
		
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageI/Interface", "interfaceMethod", "()V"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageB/B", "interfaceMethod", "()V"), testAm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageB/B", "interfaceMethod", "()V"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageI/Interface", "interfaceMethod", "()V"), testAm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageA/A", "m", "()I"), testAm ) );
		
		//
		//Oracle for test Cm
		//
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageC/C", "<init>", "()V"), testCm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageC/C", "<init>", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageC/C", "m", "()I"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageD/D", "<init>", "()V"), testCm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageD/D", "<init>", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageD/D", "mb", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageI/Interface", "interfaceMethod", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageD/D", "interfaceMethod", "()V"), testCm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageD/D", "interfaceMethod", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageI/Interface", "interfaceMethod", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageD/D", "mb", "()V"), testCm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageC/C", "m", "()I"), testCm ) );
		
		//
		//Oracle for test Fm
		//
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageF/F", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageF/F", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageF/F", "m", "()I"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "mb", "()V"), testFm ) );
		
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageH/H", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageH/H", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageH/H", "doSome", "(I)V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageL/L", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageL/L", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageL/L", "doSome", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "doNothing", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "doNothing", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageL/L", "doSome", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "<init>", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageG/G", "interfaceMethod", "()V"), testFm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "interfaceMethod", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageH/H", "doSome", "(I)V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageG/G", "mb", "()V"), testFm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageF/F", "m", "()I"), testFm ) );
		
		
		
		return myExpectedInteractions;
	}
}
