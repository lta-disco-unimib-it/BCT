// generated source from Probekit compiler
/* probekit /BCT_new/probes/bctComponentLogger.probe
*/
// "imports" specifications for probes (if any):

import probes.ClassFormatter;
import probes.ComponentCallStack; // from unnamed_probe
import probes.LoggerProbe;
import support.BctInstrumentationChecker;
import util.TcMetaInfoHandler;

class bctTCLP7 {
	// Class for probe unnamed_probe
	public static class Probe_0 {
		// Fragment at class scope
		
		public static void _afterCall (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"CALLEXIT"+bctLP0.class);
				ComponentCallStack.getInstance().pop();
				BctInstrumentationChecker.getInstance().check(BctInstrumentationChecker.ExecutionPoint.EXIT, ClassFormatter.getSignature(className,methodName,methodSig), TcMetaInfoHandler.getCurrentTestCase() );
				
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
		public static void _beforeCall (
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"CALLENTER"+bctLP0.class);
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.push( bctTCLP7.Probe_0.class );
				BctInstrumentationChecker.getInstance().check(BctInstrumentationChecker.ExecutionPoint.ENTER, ClassFormatter.getSignature(className,methodName,methodSig), TcMetaInfoHandler.getCurrentTestCase() );
				
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

		}
	}
	// Class for probe unnamed_probe
	public static class Probe_1 {
		// Fragment at class scope
		
		public static void _exit (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.pop();
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"EXIT "+className+"."+methodName+" "+methodSig);
				if ( bctTCLP7.Probe_1.class == s.lastElement() ){
					//System.out.println("#"+Thread.currentThread().getId()+"#"+"DISCARD");
					return;
				}
				BctInstrumentationChecker.getInstance().check(BctInstrumentationChecker.ExecutionPoint.EXIT, ClassFormatter.getSignature(className,methodName,methodSig), TcMetaInfoHandler.getCurrentTestCase() );
				

			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

		}
		
		
		public static void _entry (
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"ENTER "+className+"."+methodName+" "+methodSig);
				if ( bctTCLP7.Probe_1.class == s.lastElement() ){
					s.push( bctTCLP7.Probe_1.class );
					//System.out.println("#"+Thread.currentThread().getId()+"#"+"DISCARD");
					return;
				}
				s.push( bctTCLP7.Probe_1.class );
				BctInstrumentationChecker.getInstance().check(BctInstrumentationChecker.ExecutionPoint.ENTER, ClassFormatter.getSignature(className,methodName,methodSig), TcMetaInfoHandler.getCurrentTestCase() );
				
				
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

		}
	}
}
