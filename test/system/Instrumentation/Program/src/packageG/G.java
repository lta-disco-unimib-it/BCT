package packageG;

import packageF.FInterface;
import packageH.H;
import packageI.Interface;

public class G implements FInterface {

	public void interfaceMethod() {
		
	}

	public void mb() {
		FInterface i = this;
		i.interfaceMethod();
		InternalUse u = new InternalUse();
		H c = new H();
		c.doSome(8);
	}

	public void doNothing(){
		
	}
}
