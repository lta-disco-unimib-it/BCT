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
package ccrt;

//import it.unimib.disco.lta.alfa.utils.Cleaner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import tools.gdbTraceParser.GdbTraceParser;
import util.Cleaner;

public class CcrtBctLauncher {






	private enum ExecutionPhase { training, checking };

	private static boolean validation=false;

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SecurityException 

	 */

	public static void main(String[] args)   {	

		
		String ccrtOptions;
		String ccrtLogs;
	
		String ccrtResultsFolderName = null;
		
		ccrtOptions = args[0];
		ccrtLogs = args[1];
		ccrtResultsFolderName  = args[2];
		

		System.out.println("Options loaded from file "+ccrtOptions);

		File optionsFile = new File ( ccrtOptions ); 
		File logsFile = new File ( ccrtLogs );
		
		File ccrtResultsFolder = new File( ccrtResultsFolderName );
		ccrtResultsFolder.mkdirs();
		
		File resultsFile = new File ( ccrtResultsFolder, "results.xml" );
		
		//Temporary for eventual errors during setup
		CcrtOutputFiles ccrtOutputs = new CcrtOutputFiles(logsFile, resultsFile );
		
		
		CcrtBctLauncherOptions options = null;

		try {
			//cleanWorkingDir(options, ccrtOutputs);


			options = extractCcrtBctLauncherOptions(optionsFile);

			writeLog(ccrtOutputs, "information", "Analysis Status", "Analysis process is running");

			runCcrtAnalysis(options, ccrtOutputs);
			
			writeLog(ccrtOutputs, "success", "Analysis Status", "Analysis process finished with success");
			
		} catch ( Exception e ) {
			e.printStackTrace();
			//TODO: put the exception message in log.xml
			String value = "";
			e.printStackTrace();
			StackTraceElement[] g = e.getStackTrace();
			for(int i = 0; i<g.length; i++){
				value = value + g[i].toString() + " ";
			}
			writeLog(ccrtOutputs, "error", e.getMessage(), value);

		}
	}


	
	public static void runCcrtAnalysis(CcrtBctLauncherOptions options,
			CcrtOutputFiles ccrtOutputs) throws Exception {
		//Update output dir
		//options.ccrtOutputsDir.mkdirs();
		
		//outputs .xml generaded where indicated as parameter
		//ccrtOutputs = new CcrtOutputFiles(options.ccrtOutputsDir);

		if ( options.clean ) {
			cleanWorkingDir(options, ccrtOutputs);
		}
		
		
		if ( options.cleanOnly ){
			return;
		}
		
		//
		//Training phase
		//
		
		if ( options.validTraces != null  && options.enableInference ){

			runGdbTraceParser(options, options.validTraces, false);

		}
		
		
		
		//
		//Checking phase
		//
		
		if ( options.faultyTraces != null   && options.enableChecking ){

			runGdbTraceParser(options, options.faultyTraces, true);

		}
		
		
	

		generateResults(options,ccrtOutputs);

			
		
	}



	static void writeLog(CcrtOutputFiles ccrtOutputs, String severity, String ttl, String txt) {
		
		try{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc;
			Element rootElement;
			if (new File(ccrtOutputs.logsXml.getAbsolutePath()).exists()){

				doc= docBuilder.parse(ccrtOutputs.logsXml);
				rootElement = (Element)doc.getFirstChild();
			}else {
				doc = docBuilder.newDocument();
				doc.setXmlStandalone(true);
				rootElement = doc.createElement("errors");
				doc.appendChild(rootElement);
			}



			Element error = doc.createElement("error");
			Attr attr = doc.createAttribute("severity");
			attr.setValue(severity);
			error.setAttributeNode(attr);
			rootElement.appendChild(error);




			Element message = doc.createElement("message");

			Attr title = doc.createAttribute("title");
			title.setValue(ttl);
			message.setAttributeNode(title);

			Attr text = doc.createAttribute("text");
			//String value = "";
			//e.printStackTrace();
			//StackTraceElement[] g = e.getStackTrace();
			//for(int i = 0; i<g.length; i++){
			//	value = value + g[i].toString() + " ";
			//}

			text.setValue(txt);
			message.setAttributeNode(text);

			error.appendChild(message);


			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			StreamResult result =  new StreamResult(ccrtOutputs.logsXml);
			transformer.transform(source, result);

			System.out.println("Done");

		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}catch(TransformerException tfe){
			tfe.printStackTrace();
		} catch (SAXException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}




	private static void generateResults(CcrtBctLauncherOptions options, CcrtOutputFiles ccrtOutputs) throws ParserConfigurationException, XMLStreamException, IOException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilderFactory domFactory = 
			DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder = domFactory.newDocumentBuilder();


		Document document = builder.newDocument();

		Element element = document.createElement("results");

		document.appendChild(element);


		addStringOutput(options,document,element);




		FileOutputStream fileWriter = new FileOutputStream( ccrtOutputs.resultsXml );

		try{
			Transformer xformer = TransformerFactory.newInstance().newTransformer(); 
			xformer.setOutputProperty(OutputKeys.INDENT,"yes");
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");

			DOMSource source = new DOMSource(document);

			StreamResult result = new StreamResult(fileWriter);
			xformer.transform(source, result); 


		} finally {
			if ( fileWriter != null ){
				fileWriter.close();
			}


		}


	}

	private static void addStringOutput(CcrtBctLauncherOptions options,
			Document document, Element parent) {
		Element element = document.createElement("simpleText");
		element.setAttribute("name", "standard output");
		parent.appendChild(element);


		Element body = document.createElement("body");



		element.appendChild(body);

		Text textNode = document.createTextNode("BCT succcesfully analyzed the logs.");

		body.appendChild(textNode);
	}











	private static String getTxtFileContent(File file) throws IOException {

		BufferedReader r = new BufferedReader(new FileReader(file));

		StringBuffer sb = new StringBuffer();
		String line;

		try {
			while ( ( line = r.readLine() ) != null ){
				sb.append(line);
				sb.append("\n");
			}
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return sb.toString();
	}


	private static void runGdbTraceParser(CcrtBctLauncherOptions options, String traceFilesPath, boolean checking) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<String> rCAKAR = new ArrayList<String>();


		rCAKAR.add("-resultsDir");
		rCAKAR.add(options.projectDir.getAbsolutePath());
		
		
		if ( checking ){
			rCAKAR.add("-check");	
		}
		
		if ( options.enableAva ){
			rCAKAR.add("-enableAva");
		}
		
//		if ( options.filterNonTerminatingFunctions ){
//			rCAKAR.add("-filterNonTerminatingFunctions");
//		}
		
		
		File traceFiles = new File ( traceFilesPath );
		
		if ( traceFiles.isDirectory() ){
			
			rCAKAR.addAll(getFilesInFolder(traceFiles));
			
		} else {
			rCAKAR.add(traceFiles.getAbsolutePath());
		}
		String[] arguments = new String [rCAKAR.size()];
		arguments = rCAKAR.toArray(arguments);
		
		GdbTraceParser.main(arguments);






	}




	static void cleanWorkingDir(CcrtBctLauncherOptions options, CcrtOutputFiles outputs) {
		if (options!=null){
		Cleaner.main(new String[]{
				options.projectDir.getAbsolutePath(),
				outputs.logsXml.getAbsolutePath(),
				outputs.resultsXml.getAbsolutePath()

		});
		} else{
			Cleaner.main(new String[]{
					outputs.logsXml.getAbsolutePath(),
					outputs.resultsXml.getAbsolutePath()
			});
			
		}

	}





	/**
	 * Returns a list with the absolute paths of the files contained in the given folder
	 * 
	 * @param f
	 * @return
	 */
	private static Collection<? extends String> getFilesInFolder(File f) {
		List<String> logs = new ArrayList<String>();
		String[] files = f.list();
		for (int i = 0; i < files.length; i++){
			File logFile = new File ( f.getPath() + File.separator + files[i] );
			if ( logFile.isDirectory() ){
				continue;
			}
			logs.add( logFile.getAbsolutePath() );
		}
		return logs;
	}

	/**
	 * Create a CccrtAvaLaucherOptions object by parsing the provided options file
	 * 
	 * @param file
	 * @return
	 * @throws Exception 
	 * @throws XPathException
	 */
	public static CcrtBctLauncherOptions extractCcrtBctLauncherOptions(File file) throws Exception {
		CcrtBctLauncherOptions options = null;


		DocumentBuilderFactory domFactory = 
			DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder;

		builder = domFactory.newDocumentBuilder();
		
		Document docc = builder.parse(file);
		String projectDirPath = findValue(docc, "-projectDir");

//		File installDir = new File(findValue(docc, "-installDir"));
//		if (!installDir.isDirectory()){
//			throw new Exception("Error in 'options.xml': The option "+ "-installDir" + " points to a folder which is not a directory: "+installDir.getAbsolutePath());
//
//		}

		

		//String ccrtOutputs = findValue(docc, "-ccrtOutputs");

		options = new CcrtBctLauncherOptions(projectDirPath);

		//Create output dir if necessary
		options.projectDir.mkdirs();

		//options.ccrtOutputsDir = new File ( ccrtOutputs ); 

//		options.installDir = installDir;


		options.validTraces = findValue(docc, "-validTraces");
		
		options.faultyTraces = findValue(docc, "-faultyTraces");

		options.enableAva = findBooleanValue(docc, "-enableAva");
		
//		try {
//			options.filterNonTerminatingFunctions = findBooleanValue(docc, "-filterNonTerminatingFunctions");
//		} catch ( Exception e ){
//			
//		}
		
		
		
		
		options.clean = findBooleanValue(docc, "-clean");
		
		options.cleanOnly = findBooleanValue(docc, "-cleanOnly");
		
		
		try {
			boolean checkOnly = findBooleanValue(docc, "-checkOnly");
			
			if ( checkOnly ){
				options.enableInference = false;
				options.enableChecking = true;
				options.clean = false;
			}
			
		} catch ( Exception e ) {
			
		}
		
		
		return options;
	}



	private static boolean findBooleanValue(Document docc, String name) throws Exception {
		String strngValue = findValue(docc, name);
		return Boolean.parseBoolean(strngValue);
	}

	public static String findValue(Document docc, String name) throws Exception {

		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;



		expr = xpath.compile("//valueOption[@name='"+name+"']");

		Object result = expr.evaluate(docc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		if (nodes.getLength()==0){
			expr = xpath.compile("//flagOption[@name='"+name+"']");
			result = expr.evaluate(docc, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		if (nodes.getLength()==0){
			throw new Exception("Error in 'options.xml': The option "+ name + " is missing or improperly configured");
		}
		if (((Element)nodes.item(0)).getAttribute("actual").isEmpty()){
			return ((Element)nodes.item(0)).getAttribute("default");
		}
		return ((Element)nodes.item(0)).getAttribute("actual");

	}


	public static String[] findList(Document docc, String name) throws Exception {
		ArrayList<String> itemList=new ArrayList<String>();
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;




		expr = xpath.compile("//listOption[@name='"+name+"']/actualList/item");
		Object result = expr.evaluate(docc, XPathConstants.NODESET );
		NodeList nodes = (NodeList) result;

		if (nodes.getLength()==0) {
			expr = xpath.compile("//listOption[@name='"+name+"']/defaultList/item");
			result = expr.evaluate(docc, XPathConstants.NODESET );
			nodes = (NodeList) result;
		} 

		//FAbrizio: why do we need this?
//		if (nodes.getLength()==0){
//			throw new Exception("Error in 'options.xml': The option "+ name + " is missing or improperly configured");
//		}

		for(int i = 0; i<nodes.getLength(); i++){
			if (nodes.item(i).getNodeName().equals("item")){
				itemList.add(((Element)nodes.item(i)).getAttribute("value"));
			}

		}

		String[] list= new String [itemList.size()];
		list = itemList.toArray(list);

		return list;

	}

}