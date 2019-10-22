package support;

import java.util.ArrayList;
import java.util.List;

import probes.ClassFormatter;

public class BctInstrumentationCheckerComplex extends BctInstrumentationChecker {

	public BctInstrumentationCheckerComplex(){
		expectedInteractions.addAll(getExpectedInteractions("test.ComplexComponentsTest"));
	}

	public static List<BctMonitoredInteraction> getExpectedInteractions( String testPackage) {
		
		String testMm = testPackage+".testMm(()V)";
		
		ArrayList<BctMonitoredInteraction> myExpectedInteractions = new ArrayList<BctMonitoredInteraction>();
		//Oracle Mm
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM/M", "m", "()I"), testMm ) );
		
			myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one/MOne", "<init>", "()V"), testMm ) );
			myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one/MOne", "<init>", "()V"), testMm ) );
		
			myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one/MOne", "m", "()I"), testMm ) );	
		
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "<init>", "()V"), testMm ) );
				
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "s", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "s", "()V"), testMm ) );
		
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM/M", "s", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM/M", "s", "()V"), testMm ) );
		
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM/M", "<init>", "()V"), testMm ) );
		
		
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageN/N", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageN/N", "<init>", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageN/N", "s", "()V"), testMm ) );
				myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageN/N", "s", "()V"), testMm ) );
		
			myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one/MOne", "m", "()I"), testMm ) );
				
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM/M", "m", "()I"), testMm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "<init>", "()V"), testMm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "<init>", "()V"), testMm ) );
		
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.ENTER, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "s", "()V"), testMm ) );
		myExpectedInteractions.add( new BctMonitoredInteraction(ExecutionPoint.EXIT, ClassFormatter.getSignature("packageM.one.two/MOneTwo", "s", "()V"), testMm ) );
		
		return myExpectedInteractions;
	}
}
