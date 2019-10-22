package bctaj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import probes.ComponentCallStack;
import recorders.LoggingActionRecorder;

@Aspect
public abstract class AbstractBctComponentLA {

	public Class myClass = this.getClass();
	
    // abstract pointcut: no expression is defined
    @Pointcut
    abstract void componentEnterScope();

    @Pointcut
    abstract void callScope();
    
    @Pointcut
    abstract void componentConstructorsScope();
    
    @Pointcut
    abstract void constructorsScope();
    
    /**
     * Invoked before the invocation of a component method
     * 
     * @param joinPoint
     */
    @Before("componentEnterScope() || componentConstructorsScope()")
    public void beforeComponentEnter(JoinPoint joinPoint) {
//    	System.out.println("ENTER "+joinPoint.getSignature().toLongString());
    	ComponentCallStack s = ComponentCallStack.INSTANCE.get();
    	
    	if ( myClass == s.lastElement() ){
    		
    		s.push( myClass );
    		return;
    	}
//    	System.out.println("HERE");
    	s.push( myClass );
//    	System.out.println(s.size());
       	LoggingActionRecorder.logIoInteractionEnter(extractSignature(joinPoint), joinPoint.getArgs(), Thread.currentThread().getId() );
    }
    
    /**
     * Invoked before the invocation of a monitored envronment method
     * 
     * @param joinPoint
     */
    @Before("callScope() || constructorsScope()")
    public void beforeCall(JoinPoint joinPoint) {
//    	System.out.println("ENTER "+joinPoint.getSignature().toLongString());
    	ComponentCallStack s = ComponentCallStack.getInstance();
  	  	s.push( myClass );
  	  	
       	LoggingActionRecorder.logIoInteractionEnter(extractSignature(joinPoint), joinPoint.getArgs(), Thread.currentThread().getId() );
    }
    
    /**
     * Returns the signature of a monitored method
     * 
     * @param joinPoint
     * @return
     */
    private String extractSignature(JoinPoint joinPoint) {
		String signature = joinPoint.getSignature().toLongString();
		int indexOfSpace = signature.lastIndexOf(' ');
		return signature.substring(indexOfSpace+1);
	}

    /**
     * Invoked when a component constructor returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
	@AfterReturning(value = "componentConstructorsScope()")
    public void afterComponentConstructors(JoinPoint joinPoint) {
//		System.out.println("EXIT CC "+joinPoint.getSignature().toLongString());
		
		ComponentCallStack s = ComponentCallStack.INSTANCE.get();
//		System.out.println(s.size());
		s.pop();
		if ( myClass == s.lastElement() ){
			return;
		}
		
    	LoggingActionRecorder.logIoInteractionExit(extractSignature(joinPoint), joinPoint.getArgs(), Thread.currentThread().getId() );
    }
    
	/**
     * Invoked when a monitored environment constructor returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
	@AfterReturning(value = "constructorsScope()")
    public void afterConstructors(JoinPoint joinPoint) {
//		System.out.println("EXIT EC "+joinPoint.getSignature().toLongString());
		ComponentCallStack.getInstance().pop();
    	LoggingActionRecorder.logIoInteractionExit(extractSignature(joinPoint), joinPoint.getArgs(), Thread.currentThread().getId() );
    }
    
	/**
     * Invoked when a component method returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
    @AfterReturning(pointcut="componentEnterScope()",returning="ret")
    public void afterComponentReturning(JoinPoint joinPoint,Object ret) {
//    	System.out.println("EXIT CE "+joinPoint.getSignature().toLongString());
    	ComponentCallStack s = ComponentCallStack.INSTANCE.get();
		s.pop();
		if ( myClass == s.lastElement() ){
			return;
		}
		logExit( joinPoint, ret );
    }
    
    /**
     * Invoked when a monitored environment method returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
    @AfterReturning(pointcut="callScope()",returning="ret")
    public void afterReturning(JoinPoint joinPoint,Object ret) {
//    	System.out.println("EXIT "+joinPoint.getSignature().toLongString());
    	ComponentCallStack.INSTANCE.get().pop();
    	logExit( joinPoint, ret );
    }
    
    /**
     * Returns true if the passed method does not return any value
     * 
     * @param joinPoint
     * @return
     */
    private boolean isVoid(JoinPoint joinPoint) {
		return joinPoint.getSignature().toShortString().startsWith("void");
	}

    /**
     * Invoked when a component method throws an execption
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(pointcut="componentEnterScope() || componentConstructorsScope()",throwing="exception")
    public void afterComponentThrowing(JoinPoint joinPoint,Throwable exception) {
//    	System.out.println("EXIT CTH "+joinPoint.getSignature().toLongString());
    	ComponentCallStack s = ComponentCallStack.INSTANCE.get();
    	s.pop();
    	if ( myClass == s.lastElement() ){
    		return;
    	}
    	logExit( joinPoint, null );
    }
    
    /**
     * Invoked when a monitored environment method throws an exception
     * 
     * @param joinPoint
     * @param exception
     */
	@AfterThrowing(pointcut="callScope() || constructorsScope()",throwing="exception")
    public void afterThrowing(JoinPoint joinPoint,Throwable exception) {
//		System.out.println("EXIT TH "+joinPoint.getSignature().toLongString());
       logExit( joinPoint, null );
    }
    
	/**
	 * Used to log exit data
	 * 
	 * @param joinPoint
	 * @param ret
	 */
	private void logExit(JoinPoint joinPoint, Object ret ) {
		//    	ComponentCallStack.getInstance().pop();
		if ( isVoid( joinPoint ) ){
			LoggingActionRecorder.logIoInteractionExit(extractSignature(joinPoint), joinPoint.getArgs(), Thread.currentThread().getId() );
		} else {
			LoggingActionRecorder.logIoInteractionExit(extractSignature(joinPoint), joinPoint.getArgs(), ret, Thread.currentThread().getId() );
		}
	}

}

