/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package testSupport;

import java.lang.management.ManagementFactory;

import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;
import recorders.FileViolationsRecorder;
import tools.violationsAnalyzer.BctViolationsManager;
import tools.violationsAnalyzer.FailuresManager;

public class ViolationsAnalyzerSupport {
	
	public static String getIdPrefix(){
		return "12345628@112@localhost@";
	}
	
	public static BctIOModelViolation createV0(){
		
		BctIOModelViolation v1 = new BctIOModelViolation(
				getIdPrefix()+"1",
				"pack1.ClassB.b()",
				BctIOModelViolation.Position.ENTER,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"1"},
				new String[]{},
				new String[]{"1","2","3","4","5","6","pack1.ClassB.b:20","pack1.ClassA.a:20","pack1.Program.main:40"},
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"parameter[0]\n9\n1\nparameter[1].toString()\n\"A string\"\n1\n"
		);
		return v1;
	}
	
	
	public static BctIOModelViolation createV1(){
		
		BctIOModelViolation v1 = new BctIOModelViolation(
				getIdPrefix()+"1",
				"pack1.ClassB.b()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"1"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassB.b:20","pack1.ClassA.a:20","pack1.Program.main:40"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"parameter[0]\n9\n1\nparameter[1].toString()\n\"A string\"\n1\n"
		);
		return v1;
	}
	
	private static String[] reverse(String[] strings) {
		
		for( int i = 0; i < strings.length/2; i++ ){
			String aux = strings[i];
			strings[i] = strings[strings.length-i-1];
			strings[strings.length-i-1] = aux;
		}
		
		return strings;
	}

	static BctIOModelViolation createV2(){
		return new BctIOModelViolation(

				getIdPrefix()+"2",
				"pack1.ClassD.e()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassD.e:20","pack1.ClassC.c:39","pack1.Program.main:44"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	static BctModelViolation createV3(){
		return new BctIOModelViolation(
				getIdPrefix()+"3",
				"pack1.ClassE.e()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassE.e:20","pack1.ClassC.c:40","pack1.Program.main:44"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	static BctModelViolation createV4(){
		return new BctIOModelViolation(
				getIdPrefix()+"4",
				"pack1.ClassM.e()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassM.e:20","pack1.ClassL.e:20","pack1.ClassI.e:20","pack1.ClassH.e:20","pack1.ClassG.e:20","pack1.ClassF.e:20","pack1.ClassC.c:41","pack1.Program.main:44"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	static BctModelViolation createV5(){
		return new BctIOModelViolation(
				getIdPrefix()+"5",
				"pack1.ClassM.n()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassM.n:20","pack1.ClassL.n:20","pack1.ClassI.n:20","pack1.ClassH.n:20","pack1.ClassG.n:20","pack1.ClassF.n:20","pack1.ClassC.c:42","pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	//same type of 1
	static BctModelViolation createV6(){
		return new BctIOModelViolation(
				getIdPrefix()+"6",
				"pack1.ClassB.b()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassB.b:20","pack1.ClassL.n:21","pack1.ClassI.n:20","pack1.ClassH.n:20","pack1.ClassG.n:20","pack1.ClassF.n:20","pack1.ClassC.c:42","pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	static BctModelViolation createV7(){
		return new BctIOModelViolation(
				getIdPrefix()+"7",
				"pack1.ClassS.n()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassS.n:20","pack1.ClassR.n:20","pack1.ClassQ.n:20","pack1.ClassP.n:20","pack1.ClassO.n:20","pack1.ClassL.n:22","pack1.ClassI.n:20","pack1.ClassH.n:20","pack1.ClassG.n:20","pack1.ClassF.n:20","pack1.ClassC.c:42","pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	static BctModelViolation createV8(){
		return new BctIOModelViolation(
				getIdPrefix()+"8",
				"pack1.ClassQ.t()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassQ.t:20","pack1.ClassP.n:20","pack1.ClassO.n:20","pack1.ClassL.n:22","pack1.ClassI.n:20","pack1.ClassH.n:20","pack1.ClassG.n:20","pack1.ClassF.n:20","pack1.ClassC.c:42","pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}
	
	static BctModelViolation createV9(){
		return new BctIOModelViolation(
				getIdPrefix()+"9",
				"pack1.ClassU.u()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassU.u:20",
					"pack1.ClassT.t:20",
					"pack1.ClassS.t:20",
					"pack1.ClassR.t:20",
					"pack1.ClassQ.t:20",
					"pack1.ClassP.t:20",
					"pack1.ClassO.t:20",
					"pack1.ClassL.t:22",
					"pack1.ClassI.t:20",
					"pack1.ClassH.t:20",
					"pack1.ClassG.t:20",
					"pack1.ClassF.t:20",
					"pack1.ClassC.t:42",
					"pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}
	
	static BctModelViolation createV10(){
		return new BctIOModelViolation(
				getIdPrefix()+"10",
				"pack1.ClassS.s()",
				BctIOModelViolation.Position.EXIT,
				"x != y",
				System.currentTimeMillis(),
				new String[]{"2"},
				new String[]{},
				reverse ( new String[]{"1","2","3","4","5","6","pack1.ClassS.s:20",
					"pack1.ClassT.t:20",
					"pack1.ClassS.t:20",
					"pack1.ClassR.t:20",
					"pack1.ClassQ.t:20",
					"pack1.ClassP.t:20",
					"pack1.ClassO.r:20",
					"pack1.ClassL.t:23",
					"pack1.ClassI.t:20",
					"pack1.ClassH.t:20",
					"pack1.ClassG.t:20",
					"pack1.ClassF.t:20",
					"pack1.ClassC.t:42",
					"pack1.Program.main:46"} ),
				ManagementFactory.getRuntimeMXBean().getName(),
				String.valueOf(Thread.currentThread().getId()),
				"x=9"
		);
	}

	public static void makeSimpleConfiguration( BctViolationsManager vm, FailuresManager fm ){


		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());


		fm.addCorrectAction("1");

		fm.addFailingAction("2");


	}

	public static void makeSimpleConfigurationAllCorrect( BctViolationsManager vm, FailuresManager fm ){


		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());


		fm.addCorrectAction("1");

		fm.addCorrectAction("2");


	}

	public static void makeSimpleConfigurationAllFaulty( BctViolationsManager vm, FailuresManager fm ){


		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());


		fm.addFailingAction("1");

		fm.addFailingAction("2");


	}
	public static void makeSimpleConfiguration2(BctViolationsManager vm,
			FailuresManager fm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());

		vm.addDatum(createV6());

		fm.addCorrectAction("1");

		fm.addFailingAction("2");

	}

	public static void makeSimpleConfiguration2AllFaulty(BctViolationsManager vm,
			FailuresManager fm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());

		vm.addDatum(createV6());


		fm.addFailingAction("2");
		fm.addFailingAction("2");

	}

	public static void makeSimpleConfiguration2AllCorrect(BctViolationsManager vm,
			FailuresManager fm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV3());

		vm.addDatum(createV6());


		fm.addCorrectAction("1");
		fm.addCorrectAction("2");


	}
	public static void makeSimpleConfiguration2AllCorrect2(BctViolationsManager vm,
			FailuresManager fm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV3());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV6());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		fm.addFailingAction("1");
		fm.addCorrectAction("2");


	}

	public static void makeMediumConfiguration(BctViolationsManager vm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV3());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV4());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV5());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV6());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV7());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV8());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public static void makeComplexConfiguration(BctViolationsManager vm) {
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV9());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV10());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV3());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV4());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV5());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV6());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV7());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV8());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	public static void makeMediumConfigurationAllFailing(BctViolationsManager vm,
			FailuresManager fm) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub

		vm.addDatum(createV1());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		vm.addDatum(createV2());

		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV3());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV4());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV5());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV6());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV7());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vm.addDatum(createV8());
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fm.addFailingAction("2");
		fm.addFailingAction("1");


	}
	


}
