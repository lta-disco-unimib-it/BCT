package tools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;


import tools.violationsAnalyzer.CEVisitor;
import tools.violationsAnalyzer.CallElement;
import tools.violationsAnalyzer.CallTrace;
import tools.violationsAnalyzer.DynamicCallTreeExtractor;
import tools.violationsAnalyzer.GraphClusteringThreshold;
import tools.violationsAnalyzer.ViolationEdge;
import tools.violationsAnalyzer.ViolationsManager;
import tools.violationsAnalyzer.DCVP;
import tools.violationsAnalyzer.DistCount;
import tools.violationsAnalyzer.GlobalDistances;
import tools.violationsAnalyzer.ProgramGraph;
import tools.violationsAnalyzer.ReachabilityInfo;
import tools.violationsAnalyzer.TCInfo;
import tools.violationsAnalyzer.TestCaseInfo;
import tools.violationsAnalyzer.ThreadElement;
import tools.violationsAnalyzer.Violation;
import tools.violationsAnalyzer.ViolationPoint;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculator;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorDown;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorParent;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorTotal;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorTotalNoLine;
import tools.violationsAnalyzer.distanceCalculators.DistanceCalculatorWeighted;
import tools.violationsAnalyzer.distanceCalculators.DistanceTable;

/**
 * This class given a file containing violations reported by BCT produces report files with information on correlation
 * between different violations.
 * 
 * @author Fabrizio Pastore
 *
 */
public class ViolationsAnalyzer {
	private static final String failingOpt = "-failing";
	private static final String destFolderOpt = "-destFolder";
	private static final String graphLimitOpt = "-graphLimit";
	private static final String saveAllOpt = "-saveAll";
	private static final String graphRulesOpt = "-rules";
	private static final String saveDynamicCallTreeOpt = "-saveDynamicCallTree";
	
	
	/**
	 * This method creates the violation manager from the violations recorded in a plain text file
	 * 
	 * @param violationsFile
	 * @param failingTcs
	 * @return
	 * @throws IOException
	 */
	public static ViolationsManager createViolationsManager(File violationsFile, ArrayList<String> failingTcs) throws IOException{
		BufferedReader fr = new BufferedReader ( new FileReader( violationsFile ) );
		String inpline;
		String lastLine="";
		String currentTest=null;
		int lines = 0;
		ViolationsManager cv = new ViolationsManager(failingTcs);
		int outside = 0;
		
		String violation = null;
		CallTrace callTrace = null;
		boolean stack = false;
		int testLine = 1;
		boolean stackPreprocessed = false;
		while ( ( inpline  = fr.readLine() ) != null ){
			String line = inpline.trim();
			lines++;
			if ( isTestInfo(line) ){
				//record the last violation found before processing the new test information
				if ( violation != null ){
					stack=false;
					cv.addViolation( currentTest, violation, callTrace, testLine );
				}
				stackPreprocessed = false;
				testLine = lines;
				currentTest = getTestName(line);
			} else if ( isInteractionViolation(line) ) {
				if ( currentTest != null ){
					fr.readLine();
					String state = fr.readLine();
					String transition = fr.readLine();
					lines+=3;
					if ( state == null || transition == null ){
						System.err.println("Line : "+lines+"Malformed log: found "+line+" "+state+" "+transition );
					}
					
					violation = getIntViolation(line,state,transition);
				} else {
					outside++;
				}
			} else if ( isIoViolation(line) ) {
				if ( currentTest != null ){
					
					lines++;
					String info = fr.readLine();
					 
					violation = getIOViolation(line,info);
				} else {
					outside++;
				}
			} else if ( isThread( line )){
				callTrace = new CallTrace( line.split(":")[1] );
			} else if ( isStackTrace( line )){
				stack = true;
			} else {
				if ( line.contains(" : ")  && stack ){
					if ( stackPreprocessed || ( !line.startsWith("java.lang.Thread") &&
							!line.startsWith("check.IoChecker") &&
							!line.startsWith("aspects.Bct") &&
							!line.startsWith("check.InteractionInvariantHandler") &&
							!line.startsWith("check.Checker") &&
							!line.startsWith("probes.") &&
							!line.startsWith("bct")
							)
					){
						stackPreprocessed = true;
						String[] lineEls = line.split(" : ");
						callTrace.addBottom(lineEls[0], Integer.valueOf(lineEls[1]));
					}
					
				}
			}
		}
		
		if ( violation != null ){
				cv.addViolation( currentTest, violation, callTrace, lines );
		}
		
		System.out.println(lastLine);
		fr.close();
		
		return cv;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if ( args.length < 1 ){
			printUsage();
			System.exit(-1);
		}
		
		String failingFileName = null;
		String destFolderName = "out";
		boolean saveDynamicCallTree = false;
		
		boolean saveAll = false;
		File violationsFile = new File(args[args.length-1]);
		String graphRules[] = null;
		
		for ( int i = 0; i < args.length - 1; ++i ){
			
			//Single parameter args
			if ( args[i].equals(saveAllOpt ) ){
				saveAll = true;
			} else if ( args[i].equals(saveDynamicCallTreeOpt ) ){
				saveDynamicCallTree = true;
			}else { 
				//two parameters needed
				if ( i >= args.length -2 ) {
					printUsage();
					System.exit(-1);
				} else {
					if ( args[i].equals(failingOpt) ){
						failingFileName = args[++i];
					} else if ( args[i].equals(destFolderOpt) ){
						destFolderName = args[++i];
					} else if ( args[i].equals(graphRulesOpt) ){
						graphRules = args[++i].split(",");
					}
				}
			}
		}
		
		File destFolder = new File(destFolderName);
		
		if ( destFolder.exists() && ! destFolder.isDirectory() ){
			System.err.println("Folder "+destFolder.getAbsolutePath()+" is not a directory");
			System.exit(-1);
		}
		
		if ( destFolder.exists() ){
			System.out.println("Folder "+destFolder.getAbsolutePath()+" exist");
			System.exit(-1);
		}
		
		destFolder.mkdirs();
		
		
		
		File violationsCsvFile = new File( destFolder, "violations.csv");
		File tcCsvFile = new File( destFolder, "tc.csv");
		
		
		HashMap<DistanceCalculator,Integer> limits = new HashMap<DistanceCalculator, Integer>();
		ArrayList<DistanceCalculator> dcs = new ArrayList<DistanceCalculator>();
		
		setDistanceCalculators(limits,dcs,graphRules);
		
		
		
		try {
			ArrayList<String> failingTcs = getTCFailing(failingFileName);
		
			
			violationsFile = filterAW( destFolder, violationsFile );
			
			
			
			ViolationsManager cv = createViolationsManager(violationsFile, failingTcs);
			
			if ( saveDynamicCallTree ){
				Iterator<Entry<String, ThreadElement>> tit = cv.getProgramGraph().getThreads().iterator();
				while( tit.hasNext() ){
					Entry<String, ThreadElement> entry = tit.next();
					ThreadElement te = entry.getValue();
					SimpleDirectedGraph<String, String> g = DynamicCallTreeExtractor.getTree(te);
					
					saveGraph(new File(destFolder,"Thread-"+te.getThreadID()+".calTree.tgf"), g);
				}
			}
			
			writeViolations( violationsCsvFile, cv );
			
			writeTestCases( tcCsvFile, cv);
			
			
			
			ProgramGraph pg = cv.getProgramGraph();
			
			HashMap<DistanceCalculator,GlobalDistances> globalDistances = new HashMap<DistanceCalculator, GlobalDistances>();
			DCVP dcvp = new DCVP();
			
			for ( DistanceCalculator dc : dcs ){
				
				int graphLimit = limits.get(dc);
				HashMap<TestCaseInfo, DistanceTable> dts = getDistanceTables( cv);
				
				GlobalDistances distances = new GlobalDistances();
				
				//HashMap<Violation,HashMap<Violation,DistCount>> distances = new HashMap<Violation, HashMap<Violation,DistCount>>();
				
				
				HashMap<TestCaseInfo, TCInfo> usefulViolations = new HashMap<TestCaseInfo, TCInfo>();	
				for ( TestCaseInfo tc : dts.keySet() ){
					addDistances( distances, dts.get(tc), dc, graphLimit );
					DistanceTable distanceTable = dts.get(tc);
					HashMap<ViolationPoint, ReachabilityInfo> tcUsefulViolations = getUsefulViolations(dts.get(tc),dc,graphLimit,saveAll);
					usefulViolations.put(tc, new TCInfo( tcUsefulViolations, distanceTable ) );
				}
			
				globalDistances.put(dc,distances);
				dcvp.put(dc,usefulViolations);
			}
			
			for ( TestCaseInfo tc : cv.getTestCases() ){
				if ( ! failingTcs.contains(tc.getTestCaseName()) )
					continue;
				
				Set<Entry<DistanceCalculator, TCInfo>> data = dcvp.getData(tc);
				
				for ( Entry<DistanceCalculator, TCInfo> entry : data ){
					
					File file = new File( destFolder, tc.getTestCaseName().replace("/", ".")+entry.getKey().getName()+".tgf");
					
					DistanceTable distanceTable = entry.getValue().getDistanceTable();
					DistanceCalculator dc = entry.getKey();
					saveGraph(file, distanceTable, dc, limits.get(dc),saveAll);
					
					GraphClusteringThreshold gct = new GraphClusteringThreshold();
					HashMap<SimpleDirectedWeightedGraph<String, ViolationEdge>, Double> graphs = gct.getGraphs( distanceTable, dc);
					for ( SimpleDirectedWeightedGraph<String, ViolationEdge> g : graphs.keySet() ){
						Double threshold = graphs.get(g);
						File fileClustered = new File( destFolder, (tc.getTestCaseName()+"-cluster").replace("/", ".")+dc.getName()+"-"+threshold+".tgf");
						saveGraph(fileClustered,g);	
					}
					
				}
			}
			
			
			
			saveUsefulViolations( new File ( destFolder, "useful.csv"), dcvp );
			
			
			for ( DistanceCalculator dc : globalDistances.keySet() ){
				saveGraphDists( new File(destFolder,"distance."+dc.getName()+".tgf"), globalDistances.get(dc), failingTcs.size() );
			}
//			
//			
//			
//			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	


	private static void saveGraph(File file, SimpleDirectedGraph<String, String> g) throws IOException {
		System.out.println("SAVING GRAPH");
		BufferedWriter w = new BufferedWriter( new FileWriter(file));
		for ( String v : g.vertexSet() ){
			w.write(v.trim()+" "+v.trim()+"\n");
		}
		w.write("#\n");
		for ( String from : g.vertexSet() ){
			for ( String to : g.vertexSet() ){
				String edge = g.getEdge(from, to);
				if ( edge != null){
					w.write(from.trim()+" "+to.trim()+"\n");
				}
			}
		}
		
		w.close();
	
	}

	private static void saveGraph(File fileClustered, SimpleDirectedWeightedGraph<String, ViolationEdge> g) throws IOException {
		System.out.println("SAVING GRAPH");
		BufferedWriter w = new BufferedWriter( new FileWriter(fileClustered));
		for ( String v : g.vertexSet() ){
			w.write(v.trim()+" "+v.trim()+"\n");
		}
		w.write("#\n");
		for ( String from : g.vertexSet() ){
			for ( String to : g.vertexSet() ){
				ViolationEdge edge = g.getEdge(from, to);
				if ( edge != null){
					w.write(from.trim()+" "+to.trim()+" "+g.getEdgeWeight(edge)+"\n");
				}
			}
		}
		
		w.close();
	}

	private static void setDistanceCalculators(HashMap<DistanceCalculator,Integer> limits, ArrayList<DistanceCalculator> dcs, String[] graphRules) {
		// TODO Auto-generated method stub
		if ( graphRules == null ){
			dcs.add(
					new DistanceCalculatorParent("Parent 20")) ;
			dcs.add(	new DistanceCalculatorWeighted("Weighted 20",1.0,0.5));

			dcs.add(new DistanceCalculatorTotal("Total 50"));
			dcs.add(new DistanceCalculatorTotal("Total 40"));
			dcs.add(new DistanceCalculatorTotal("Total 30"));
			dcs.add(new DistanceCalculatorTotal("Total 75"));


			limits.put(dcs.get(0), 20);
			limits.put(dcs.get(1), 20);
			limits.put(dcs.get(2), 50);
			limits.put(dcs.get(3), 40);
			limits.put(dcs.get(4), 30);
			limits.put(dcs.get(5), 75);
		} else {
			for ( String inputRule : graphRules ){
				String[] input = inputRule.split(":");
				String type = input[0];
				int threshold = Integer.valueOf(input[1]);
				DistanceCalculator rule = null;
				String ruleName = type+"_"+threshold;
				if ( type.equals("total") ){
					rule = new DistanceCalculatorTotal(ruleName);
				} else if ( type.equals("totalNoLine") ){
					rule = new DistanceCalculatorTotalNoLine(ruleName);
				} else if ( type.equals("weighted") ) {
					rule = new DistanceCalculatorWeighted(ruleName,Double.valueOf(input[2]),Double.valueOf(input[3]));
				} else if ( type.equals("parent") ) {
					rule = new DistanceCalculatorDown(ruleName);
				}	else if ( type.equals("down") ) {
					rule = new DistanceCalculatorParent(ruleName);
				}	else {
					printUsage();
					System.exit(-1);
				}

				limits.put(rule, threshold);
				dcs.add(rule);
			}
		}

	}

	private static File filterAW(File outDir,  File violationsFile) throws IOException {
		File f = new File(outDir, "violations.tmp");
		
		BufferedReader r = new BufferedReader ( new FileReader(violationsFile) );
		
		BufferedWriter w = new BufferedWriter ( new FileWriter(f) );
		
		String line;
		String adviceLine = null;
		while ( ( line = r.readLine() ) != null ){
			if ( line.contains("_AW_") ){
				
				if ( adviceLine == null ) {
					adviceLine = line.split(":")[1];
				}
			} else {
				if ( adviceLine!=null){
					String method = line.split(":")[0];
					line = method+":"+adviceLine;
					adviceLine = null;
				}
				w.write(line+"\n");
			}
		}
		w.close();
		r.close();
		return f;
	}







	public static HashMap<TestCaseInfo, DistanceTable> getDistanceTables( ViolationsManager cv  ) {
		Collection<TestCaseInfo> testCases = cv.getTestCases(); 
		ProgramGraph pg = cv.getProgramGraph();
		
		HashMap<TestCaseInfo,DistanceTable> results = new HashMap<TestCaseInfo, DistanceTable>();
		
		for ( TestCaseInfo tc : testCases ){
			
			//if ( ! tc.getTestCaseName().contains("GetProjectRelativePath") && ! tc.getTestCaseName().contains("GetWorkspaceRelativePath") )
			//	continue;
			System.out.println("Creating DistanceTable for :"+tc.getTestCaseName());
			
			HashSet<ViolationPoint> vps = tc.getViolationPoints();
			CEVisitor v = new CEVisitor( vps );
			
			for ( Entry<String, ThreadElement> entry : pg.getThreads() ){
				ThreadElement te = entry.getValue();
				for ( CallElement child : te.getChildren() )
					child.accept(v);
			}
			
			System.out.println("DistanceTable "+v.getDistanceTable());
			results.put(tc, v.getDistanceTable());
		}
		
		System.out.println("DistanceTables "+results);
		return results;
	}







	private static void saveUsefulViolations(File file, DCVP dcvp) throws IOException {
		BufferedWriter bw = new BufferedWriter( new FileWriter(file) );
		bw.write("Test case\tviolation point\t#children\t#reachable parents\t#reachable useful\t#reachable\n");
		Set<DistanceCalculator> calculators = dcvp.getCalculators();
		DistanceCalculator[] dcs = new DistanceCalculator[calculators.size()];
		int i = 0;
		for ( DistanceCalculator dc : calculators ){
			dcs[i]=dc;
			++i;
		}
		
		
		
		//Construct a table with for every test case the useful violation points for the different distance calculators used
		//
		//e.g.
		//	TestCase  ViolationPointId  ReachabilityInfoTotal ReachabilityInfoParent
		//
		//
		HashMap<TestCaseInfo, HashMap<DistanceCalculator, TCInfo>> data = dcvp.getData();
		HashMap<TestCaseInfo, HashMap<ViolationPoint,ReachabilityInfo[]> > result = new HashMap<TestCaseInfo, HashMap<ViolationPoint,ReachabilityInfo[]>>();
		for ( TestCaseInfo tc : data.keySet() ){
			String test = tc.getTestCaseName();
			HashMap<DistanceCalculator, TCInfo> info = data.get(tc);
			HashMap<ViolationPoint,ReachabilityInfo[]> testCaseRecords = new HashMap<ViolationPoint, ReachabilityInfo[]>();
			
			for ( int column = 0; column < dcs.length; ++column ){
				DistanceCalculator dc = dcs[column];
				
				TCInfo tcInfo = info.get(dc);
				HashMap<ViolationPoint, ReachabilityInfo> uv = tcInfo.getUsefulViolations();
				for(Entry<ViolationPoint, ReachabilityInfo> entry : uv.entrySet() ){
					ViolationPoint evp = entry.getKey();
					ReachabilityInfo[] record = testCaseRecords.get(evp);
					if ( record == null ){
						record = new ReachabilityInfo[dcs.length];
						testCaseRecords.put(evp, record);
					}
					record[column] = entry.getValue();
					
				}
				
			}
			
			result.put(tc, testCaseRecords);
		}

		
		//
		//Write header
		//
		bw.write("-\t-");
		for( DistanceCalculator dc : dcs ){
			bw.write("\t"+dc.getName()+"\t-\t-\t-");
		}
		bw.write("\n");
		
		bw.write("TestCase\tFailing\tViolationPoint");
		for( DistanceCalculator dc : dcs ){
			bw.write("\t# children\t# children+parents\t#useful reachable\t#reachable nodes");
		}
		bw.write("\n");
		
		//
		//Write records
		//
		for ( Entry<TestCaseInfo, HashMap<ViolationPoint,ReachabilityInfo[]>> entry : result.entrySet() ){
			String testCaseName = entry.getKey().getTestCaseName();
			
			//write down the record
			for( Entry<ViolationPoint,ReachabilityInfo[]> evp : entry.getValue().entrySet() ){
				//write: testCase name and violationPoint name
				
				bw.write(testCaseName+"\t"+!entry.getKey().isPassed()+"\t"+getVpName(evp.getKey()));
				
				//write down for every violationPoint:
				for ( ReachabilityInfo ri : evp.getValue() ){
					if ( ri == null ){
						bw.write("\t-"
								+"\t-"
								+"\t-"
								+"\t-");
					} else {
						bw.write("\t"+ri.getChildren().size()
								+"\t"+ri.getReachableParent().size()
								+"\t"+ri.getReachableUseful().size()
								+"\t"+ri.getReachable().size());
					}
				}
				
				bw.write("\n");
			}
		}
		bw.close();
	}







	private static HashMap<ViolationPoint, ReachabilityInfo> getUsefulViolations( DistanceTable v, DistanceCalculator dc, int distanceLimit, boolean saveAll) {
		HashSet<ViolationPoint> nodes = new HashSet<ViolationPoint>();
		HashSet<ViolationPoint> useful = new HashSet<ViolationPoint>();
		HashSet<ViolationPoint> reachable = new HashSet<ViolationPoint>();
		
		
		Set<ViolationPoint> vps = v.getVps();
		for ( ViolationPoint vp : vps ){
			if ( saveAll || vp.getViolation().getTcPassOccurrencies() == 0 ){
				String name=getVpName(vp);
				nodes.add(vp);
				useful.add(vp);
			}
		}

		HashMap<ViolationPoint,Set<ViolationPoint> > children = new HashMap<ViolationPoint, Set<ViolationPoint>>();
		for ( ViolationPoint parent : nodes ){
			HashSet<ViolationPoint> pchildren = new HashSet<ViolationPoint>();
			for ( ViolationPoint child : nodes ){
				Double distUp = v.getDistanceUp(child,parent);
				Double distDown = v.getDistanceDown(child,parent);
				
				double dist = dc.calculate(v, child, parent);
				if ( child != parent && distUp>=0 && ( dist <= distanceLimit  || distDown == 0 ) ){
					pchildren.add(child);
				}
					
			}
			children.put(parent,pchildren);
		}

		HashMap<ViolationPoint,Set<ViolationPoint> > tmpchildren = new HashMap<ViolationPoint, Set<ViolationPoint>>();
		for ( ViolationPoint parent : children.keySet() ){
			HashSet<ViolationPoint> newChildren = new HashSet<ViolationPoint>();
			for ( ViolationPoint child : children.get(parent) ){
				newChildren.add(child);
			}
			tmpchildren.put(parent,newChildren);
		}
		
		
		
		
		
		for ( ViolationPoint child : nodes ){
			for ( ViolationPoint parent : nodes ){
				Double distUp = v.getDistanceUp(child,parent);
				Double distDown = v.getDistanceDown(child,parent);
				double dist = dc.calculate(v, child, parent);
				if ( child != parent && distUp>=0 && ( dist <= distanceLimit  || distDown == 0 ) ){
					if (useful.contains(child) ){
						Set<ViolationPoint> pchildren = tmpchildren.get(parent);
						pchildren.addAll(tmpchildren.get(child));
						
						useful.remove(child);
					}
					
					reachable.add(child);
					
					if ( ! reachable.contains(parent) ){
						useful.add(parent);
					}
				}
					
			}
		}

		
		HashMap<ViolationPoint, ReachabilityInfo> result = new HashMap<ViolationPoint,ReachabilityInfo>();
		for ( ViolationPoint parent : useful ){
			Set<ViolationPoint> childrenNodes = tmpchildren.get(parent);
			int childrenN = childrenNodes.size();
			Set<ViolationPoint> reachableNodes = getReachable(parent,tmpchildren);
			Set<ViolationPoint> reachableUseful = new HashSet<ViolationPoint>();
			for ( ViolationPoint vp : useful ){
				if ( reachableNodes.contains(vp) ){
					reachableUseful.add(vp);
				}
			}
			ReachabilityInfo ri = new ReachabilityInfo(childrenNodes,getReachableParent(parent,tmpchildren),reachableNodes,reachableUseful);
			result.put(parent, ri);
		}
		return result;
	}

	










	private static Set<ViolationPoint> getReachableParent(ViolationPoint parent, HashMap<ViolationPoint, Set<ViolationPoint>> children) {
		HashSet<ViolationPoint> result = new HashSet<ViolationPoint>();
		
		
		Set<ViolationPoint> pchildren = children.get(parent);
		result.addAll(pchildren);
		
		for ( ViolationPoint vp : children.keySet() ){
			if ( ! vp.equals(parent) && ! result.contains(vp)){
				HashSet<ViolationPoint> els = intersect( pchildren, children.get(vp) );
				if ( els.size() > 0 ){
					result.add(vp);
				}
			}
		}
		
		return result;
	}


	private static Set<ViolationPoint> getReachable(ViolationPoint parent, HashMap<ViolationPoint, Set<ViolationPoint>> children) {
		HashSet<ViolationPoint> result = new HashSet<ViolationPoint>();
		
		
		Set<ViolationPoint> pchildren = children.get(parent);
		result.addAll(pchildren);
		
		for ( ViolationPoint vp : children.keySet() ){
			if ( ! vp.equals(parent) && ! result.contains(vp)){
				HashSet<ViolationPoint> els = intersect( pchildren, children.get(vp) );
				if ( els.size() > 0 ){
					result.add(vp);
					result.addAll(children.get(vp));
				}
				
			}
		}
		
		return result;
	}





	private static HashSet<ViolationPoint> intersect(Set<ViolationPoint> l, Set<ViolationPoint> r) {
		HashSet<ViolationPoint> result = new HashSet<ViolationPoint>();
		
		for( ViolationPoint vp : l ){
			if ( r.contains(vp) ){
				result.add(vp);
			}
		}
		
		for( ViolationPoint vp : r ){
			if ( l.contains(vp) ){
				result.add(vp);
			}
		}
		
		return result;
	}







	private static void saveGraphDists(File dest, GlobalDistances distances, int totalFailingTC) {
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
			
			
			for( Violation v : distances.getViolations() ){
				bw.write( v.getId()+" "+v.getId()+"\n");
			}
			
			bw.write("#\n");
			for ( Entry<Violation, HashMap<Violation, DistCount>> entry : distances.getEntries() ){
				Violation v = entry.getKey();
				HashMap<Violation, DistCount> distancesOfV = entry.getValue();
				for (  Entry<Violation,DistCount> dist : distancesOfV.entrySet() ){
					String from = v.getId();
					String to = dist.getKey().getId();
					DistCount dc = dist.getValue();
					Double distance = dc.getAvg();
					bw.write(from+" "+to+" "+distance+"\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}







	private static void addDistances(GlobalDistances distances, DistanceTable distanceTable, DistanceCalculator dc, int graphLimit) {
		
		Set<ViolationPoint> vps = distanceTable.getVps();
		
		for ( ViolationPoint vp : vps ){
			for ( ViolationPoint vpi : vps ){
				if ( vpi.getTimeStamp() < vp.getTimeStamp() 
						&& vp.getViolation().getTcPassOccurrencies() == 0 
						&& vpi.getViolation().getTcPassOccurrencies() == 0){
					Double distUp = distanceTable.getDistanceUp(vp,vpi);
					Double distDown = distanceTable.getDistanceDown(vp,vpi);
					
					double dist = dc.calculate( distanceTable, vp, vpi );
					if ( vp != vpi && distUp>=0 && ( dist <= graphLimit  || distDown == 0 ) ){
						addDistance(distances,vpi.getViolation(),vp.getViolation(),(int)dist);
					}
				}
			}
		}
	}







	private static void addDistance(GlobalDistances distances, Violation parent, Violation child, double totalDist) {
		distances.put(parent,child,totalDist);
	}







	private static void writeTestCases(File tcCsvFile, ViolationsManager cv) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(tcCsvFile));
		bw.write("TestCase Name\tViolation Name\tViolation ID\t#Test for this violation\t#Passing test for violation\t#Occurrencies in test\n");
		
		for ( TestCaseInfo tc : cv.getTestCases() ){
			
			Set<Violation> tviols = tc.getViolations();
			for ( Violation viol : tviols ){
				bw.write( tc.getTestCaseName()+
						"\t"+viol.getName()+
						"\t"+viol.getId()+
						"\t"+viol.getTcOccurencies()+
						"\t"+viol.getTcPassOccurrencies()+
						"\t"+tc.getOccurrencies(viol)+
						"\n");
				
			}
			
		}
		
		
		
		
		bw.close();
	}







	private static void writeViolations(File violationsCsvFile, ViolationsManager cv) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(violationsCsvFile));
		bw.write("Violation Id\tViolation Name\tTotal Number of TC\tPassed TC in wich occurr\n");
		for ( Violation viol : cv.getViolations() ){
			bw.write(  viol.getId()+
					"\t"+viol.getName() +
					"\t"+viol.getTcOccurencies() +
					"\t"+viol.getTcPassOccurrencies()+
					"\n");
		}
		bw.close();
	}







	private static ArrayList<String> getTCFailing(String failingFileName) throws IOException {
		ArrayList<String> res = new ArrayList<String>();
		if ( failingFileName == null )
			return res;
		
		BufferedReader reader = new BufferedReader( new FileReader( failingFileName ));
		
		String tcName;
		while ( ( tcName = reader.readLine() ) != null ){
			res.add(tcName);
		}
		
		reader.close();
		
		return res;
	}







	private static void printUsage() {
		System.err.println("Usage: ");
		System.err.println(ViolationsAnalyzer.class.getName()+" [options] <violationsFile>");
		System.err.println("Options should be:");
		System.err.println("\t-failing <failingFile>: path to file containing names of failing test cases, one on each line");
		System.err.println("\t-destFolder <destination>: path to the folder where to store report files");
		System.err.println("\t-graphLimit <number>: max distance between nodes to consider when creating a the graph");
		System.err.println("\t-saveAll : on graphs report also violations that occurr in correct test cases");
	}


	private static void saveGraph(File file, DistanceTable v, DistanceCalculator dc, Integer graphLimit, boolean saveAll ) {
		try {
			System.out.println("Save graph");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			ArrayList<ViolationPoint> nodes = new ArrayList<ViolationPoint>();
			
			Set<ViolationPoint> vps = v.getVps();
			for ( ViolationPoint vp : vps ){
				if ( saveAll || vp.getViolation().getTcPassOccurrencies() == 0 ){
					String name=getVpName(vp);
					nodes.add(vp);
					bw.write(name+" "+name+"\n");
				}
			}
			bw.write("#\n");
			for ( ViolationPoint from : nodes ){
				for ( ViolationPoint to : nodes ){
					
					
					Double distUp = v.getDistanceUp(from,to);
					Double distDown = v.getDistanceDown(from,to);
					
					double dist = dc.calculate(v, from,to);
					System.out.println(from.getViolation().getId()+" "+to.getViolation().getId()+" "+dist);
					if ( from != to
							&& to.getTimeStamp() >= from.getTimeStamp()
							&& dist>=0 
							&& ( saveAll || dist <= graphLimit  || distDown == 0 ) ){
						bw.write(getVpName(from)+" "+getVpName(to)+" "+dist+"("+distUp+"+"+distDown+")\n");
						//bw.write(getVpName(from)+" "+getVpName(to)+" "+distUp);
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






	private static String getVpName(ViolationPoint vp) {
		return vp.getViolation().getId()+":"+vp.getTimeStamp();
	}







	public static boolean isStackTrace(String line) {
		return line.startsWith("Stack trace");
	}

	public static boolean isThread(String line) {
		return line.startsWith("Thread :");
	}

	public static String getIOViolation(String line, String info) {
		return line.trim()+"_"+info.trim();
	}

	public static String getIntViolation(String line, String state, String transition) {
		return line.trim()+"_"+state.trim()+"_"+transition.trim();
	}

	public static boolean isInteractionViolation(String line) {
		return line.trim().startsWith("Interaction Invariant");
	}

	public static boolean isIoViolation(String line) {
		return line.trim().startsWith("Violation of");
	}
	
	public static boolean isTestEnd(String line) {
		if ( isTestInfo(line) ){
			return line.endsWith("END");
		}
		return false;
	}

	private static String getTestName(String line) {
		
		return line.split("\t")[1];
	}

	private static boolean isTestStart(String line) {
		if ( isTestInfo(line) ){
			return line.endsWith("START");
		}
		return false;
	}

	private static boolean isTestInfo(String line) {
		return line.startsWith("TEST_INFO");
	}
	
	

}
