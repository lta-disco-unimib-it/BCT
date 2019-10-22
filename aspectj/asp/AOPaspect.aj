package asp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.Signature;
 
aspect AOPaspect{
 
	private BctAspectUtil util = new BctAspectUtil();
	
    pointcut component() : ( execution(* com.google.common.collect.LinkedHashMultimap.*(..)) || execution(com.google.common.collect.LinkedHashMultimap.new(..)) ) ;
    

    
   
    

	public AOPaspect(){
		
	}
	
    Object around(): component(){
    	Object res = null;
    	Throwable e = null;
        util.enter(thisJoinPoint);
        
        try {
        	res = thisJoinPoint.proceed();
        	return res;
        } catch ( Throwable t ) {
        	e=t;
        	throw t;
        } finally {
        	util.exit(thisJoinPoint,res,e);
        }
    }
 
   
    


 
}
