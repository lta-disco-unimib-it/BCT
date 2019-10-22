
import probes.InteractionCheckerProbe;
import probes.IoCheckerProbe;
import probes.IoLoggerProbe; // from unnamed_probe
import probes.InteractionLoggerProbe; // from unnamed_probe
import probes.ComponentCallStack; // from unnamed_probe

/**
 * BCT Checking Probe 
 */
class bctCP0 {
	// Class for probe unnamed_probe
	public static class Probe_0 {
		// Fragment at class scope
		int id = 1;
		public static void _afterCall (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				ComponentCallStack.getInstance().pop();
				IoCheckerProbe.checkExit(className,methodName,methodSig,args,returnedObject);
				InteractionCheckerProbe.checkExit(className,methodName,methodSig,args,returnedObject);
			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
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
//			------------------ begin user-written fragment code ----------------
			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				s.push( bctCP0.Probe_0.class );

				IoCheckerProbe.checkEnter(className,methodName,methodSig,args);
				InteractionCheckerProbe.checkEnter(className,methodName,methodSig,args);
			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
	}
	// Class for probe unnamed_probe
	public static class Probe_1 {
		// Fragment at class scope
		int id = 0;
		public static void _exit (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.pop();
				if ( bctCP0.Probe_1.class == s.lastElement() ){
					return;
				}
				IoCheckerProbe.checkExit(className,methodName,methodSig,args,returnedObject);
				InteractionCheckerProbe.checkExit(className,methodName,methodSig,args,returnedObject);

			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
		public static void _entry (
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				if ( bctCP0.Probe_1.class == s.lastElement() ){
					s.push( bctCP0.Probe_1.class );
					return;
				}
				s.push( bctCP0.Probe_1.class );
				IoCheckerProbe.checkEnter(className,methodName,methodSig,args);
				InteractionCheckerProbe.checkEnter(className,methodName,methodSig,args);
			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
	}
}
