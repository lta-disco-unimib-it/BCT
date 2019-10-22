package support;

import java.util.ArrayList;
import java.util.List;

import probes.ClassFormatter;

public class BctInstrumentationCheckerMixed extends BctInstrumentationChecker {

	public BctInstrumentationCheckerMixed(){
		expectedInteractions.addAll(BctInstrumentationCheckerSimple.getExpectedInteractions("test.MixedComponentsTest"));
		expectedInteractions.addAll(BctInstrumentationCheckerComplex.getExpectedInteractions("test.MixedComponentsTest"));
	}

	
}
