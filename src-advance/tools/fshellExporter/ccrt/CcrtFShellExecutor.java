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
package tools.fshellExporter.ccrt;

import it.unimib.disco.lta.ava.ccrt.CcrtAvaLauncher;
import it.unimib.disco.lta.ava.ccrt.CcrtOutputFiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import tools.fshellExporter.FShellExecutor;
import tools.fshellExporter.FShellExecutor.FShellResult;
import tools.fshellExporter.FShellModelsExporter;
import util.FileUtil;
import util.JavaRunner;

public class CcrtFShellExecutor {

	
	private static final String ORIGINAL_FILES_TAG = "originalFiles";
	private static final String MODIFIED_FILES_TAG = "modifiedFiles";
	private static final String BCT_HOME = "bctHome";
	private static final String ORIGINAL_SOFTWARE_SRC_TAG = "originalSoftwareSrc";
	private static final String MODIFIED_SOFTWARE_SRC_TAG = "modifiedSoftwareSrc";
	private static final String FSHELL_OPTS = "fshellOpts";
	private static final String FSHELL_PATH = "fshellPath";

	public static class CcrtFShellExecutorOptions {
		String bctHome;
		
		File originalSoftwraeSources;
		
		File modifiedSoftwareSources;
		
		String fshellOptions;
		
		

		public String[] filesToProcess;
		
		public String[] modifiedFilesToProcess;

		public String fshellPath;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ccrtOptions;
		String ccrtLogs;
	
		String ccrtResultsFolderName = null;
		
		ccrtOptions = args[0];
		ccrtLogs = args[1];
		ccrtResultsFolderName  = args[2];
		

		

		File optionsFile = new File ( ccrtOptions ); 
		
		System.out.println("Options loaded from file "+optionsFile.getAbsolutePath());
		
		File logsFile = new File ( ccrtLogs );
		
		System.out.println("Logs file is "+logsFile.getAbsolutePath());
		
		File ccrtResultsFolder = new File( ccrtResultsFolderName );
		ccrtResultsFolder.mkdirs();
		
		File resultsFile = new File ( ccrtResultsFolder, "results.xml" );
		System.out.println("Result file is "+resultsFile.getAbsolutePath());
		
		try {
			CcrtFShellExecutorOptions opts = extractOptions(optionsFile);
			

			ArrayList<File> files = new ArrayList<File>();
			for ( String file : opts.filesToProcess ){
				files.add( new File(file) );
			}
			

			List<String> lists = new ArrayList<String>();
			lists.add(opts.bctHome);
			
			JavaRunner.runMainInClass(FShellModelsExporter.class, lists, 0 );
			
			File bctHome = new File(opts.bctHome);
			File validatedModelsDirectory = FShellModelsExporter.getValidatedModelsDir(bctHome);
			File fshellScript = FShellModelsExporter.getAllModelsFile(validatedModelsDirectory);
			
			FShellExecutor executor = new FShellExecutor();
			executor.setFShellPath( opts.fshellPath );
			executor.setFshellOptions( opts.fshellOptions );
			
			System.out.println("CcrtFShellExecutor: Calculating valid models:");
			
			
			FShellResult fresult = executor.execute(fshellScript , opts.originalSoftwraeSources, files );
			
			FShellModelsExporter fsm = new FShellModelsExporter(bctHome);
			fsm.exportValidModels( fresult, validatedModelsDirectory );
			
			File validModes = FShellModelsExporter.getValidModelsFile(validatedModelsDirectory);
			
			FShellResult fresultV1 = null; 
			
			List<String> faultyModelsAsString = null;
			
			System.out.println("Modified software sources: "+opts.modifiedFilesToProcess);

			if ( opts.modifiedFilesToProcess.length > 0 ){
				
				executor = new FShellExecutor();
				executor.setFshellOptions( opts.fshellOptions );
				System.out.flush();
				
				System.out.println("CcrtFShellExecutor: Calculating invalid models:");
				fresultV1 = executor.execute(validModes , opts.modifiedSoftwareSources, files );
				
				
				fsm.exportInvalidModelsV1( fresultV1, validatedModelsDirectory );
				
				File faultyModels = FShellModelsExporter.getInvalidatedModelsFile(validatedModelsDirectory);
				faultyModelsAsString = FileUtil.getLines(faultyModels);
			} 
			
			List<String> validModelsAsString = FileUtil.getLines(validModes);
			
			System.out.println("Valid models: \n\t"+validModelsAsString);
			System.out.println("Faulty models: \n\t"+faultyModelsAsString);
			
			generateResults(opts, resultsFile, validModelsAsString, faultyModelsAsString);
			
//			writeLog(logsFile, "INFO", "Result", "FShell executed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			writeLog(logsFile, "ERROR", "Processing error", e.getMessage());
		}
		
	}
	
	public static void writeLog(File logFile, String severity, String logEntryTitle, String txt) {

		try{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc;
			Element rootElement;
			if (logFile.exists()){

				doc= docBuilder.parse(logFile);
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
			title.setValue(logEntryTitle);
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
			StreamResult result =  new StreamResult(logFile);
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
	
	public static void generateResults(CcrtFShellExecutorOptions options, File ccrtOutputs, List<String> validModelsAsString, List<String> faultyModelsAsString ) throws ParserConfigurationException, XMLStreamException, IOException, TransformerFactoryConfigurationError, TransformerException {
			DocumentBuilderFactory domFactory = 
				DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder();


			Document document = builder.newDocument();

			Element element = document.createElement("results");

			document.appendChild(element);

			StringBuffer r1 = new StringBuffer();
			for( String s : validModelsAsString ){
				r1.append(s);
				r1.append("\n");
			}
			generateOutputModels( document, r1.toString(), element, "True Properties for Original Software" );	

			if ( faultyModelsAsString != null ){
				StringBuffer r2 = new StringBuffer();
				for( String s : faultyModelsAsString ){
					r2.append(s);
					r2.append("\n");
				}
				generateOutputModels( document, r2.toString(), element, "Properties invalidated by Upgraded Software" );
			}
			
			FileOutputStream fileWriter = new FileOutputStream( ccrtOutputs );

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


	private static void generateOutputModels(Document document,
			String holdingModels,  Element parent, String title) {
		Element element = document.createElement("simpleText");
		element.setAttribute("name", title);
		parent.appendChild(element);


		Element body = document.createElement("body");



		element.appendChild(body);
		


		Text textNode = document.createTextNode(holdingModels);

		body.appendChild(textNode);
	}

	public static CcrtFShellExecutorOptions extractOptions(File file) throws Exception{
		DocumentBuilderFactory domFactory = 
				DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder;

		builder = domFactory.newDocumentBuilder();

		Document docc = builder.parse(file);

		CcrtFShellExecutorOptions opt = new CcrtFShellExecutorOptions();

		opt.fshellPath= CcrtAvaLauncher.findValue(docc, FSHELL_PATH);
		
		opt.bctHome= CcrtAvaLauncher.findValue(docc, BCT_HOME);

		opt.originalSoftwraeSources = new File ( CcrtAvaLauncher.findValue(docc, ORIGINAL_SOFTWARE_SRC_TAG) );

		opt.modifiedSoftwareSources = new File( CcrtAvaLauncher.findValue(docc, MODIFIED_SOFTWARE_SRC_TAG) );

		opt.fshellOptions = CcrtAvaLauncher.findValue(docc, FSHELL_OPTS);

		opt.filesToProcess = CcrtAvaLauncher.findList(docc, ORIGINAL_FILES_TAG);

		opt.modifiedFilesToProcess = CcrtAvaLauncher.findList(docc, MODIFIED_FILES_TAG);

		return opt;



	}

}
