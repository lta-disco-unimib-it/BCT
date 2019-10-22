package packageA;

import packageB.B;
import packageI.Interface;

public class A {
	
	public int m(){
		B b = new B();
		b.mb();
		
		Interface i = b;
		i.interfaceMethod();
		return 0;
	}
	
}
