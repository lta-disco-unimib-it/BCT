package bctaj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import check.Checker;

@Aspect
public abstract class AbstractBctCA {

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
    	
    	String signature = extractSignature(joinPoint);
		Checker.checkIoEnter( signature, joinPoint.getArgs());
		Checker.checkInteractionEnter( signature, Thread.currentThread().getId() );
       	
    }
    
    /**
     * Invoked before the invocation of a monitored envronment method
     * 
     * @param joinPoint
     */
    @Before("callScope() || constructorsScope()")
    public void beforeCall(JoinPoint joinPoint) {

  	  	
  	  	String signature = extractSignature(joinPoint);
		Checker.checkIoEnter( signature, joinPoint.getArgs());
		Checker.checkInteractionEnter( signature, Thread.currentThread().getId() );
       	
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

		
		String signature = extractSignature(joinPoint);
		Checker.checkIoExit( signature, joinPoint.getArgs());
		Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
    }
    
	/**
     * Invoked when a monitored environment constructor returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
	@AfterReturning(value = "constructorsScope()")
    public void afterConstructors(JoinPoint joinPoint) {

		String signature = extractSignature(joinPoint);
		Checker.checkIoExit( signature, joinPoint.getArgs());
		Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
    }
    
	/**
     * Invoked when a component method returns correctly
     * 
     * @param joinPoint
     * @param ret
     */
    @AfterReturning(pointcut="componentEnterScope()",returning="ret")
    public void afterComponentReturning(JoinPoint joinPoint,Object ret) {

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
		String signature = extractSignature(joinPoint);
		
		Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
		
		if ( isVoid( joinPoint ) ){
			Checker.checkIoExit( signature, joinPoint.getArgs() );	
		} else {
			Checker.checkIoExit( signature, joinPoint.getArgs(), ret);
		}
	}

}

