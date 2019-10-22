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
package util.cbe;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.FormattingException;
import org.eclipse.tptp.logging.events.cbe.util.EventFormatter;


/**
 * This class permits to load BCT entities stored in CBE files
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class CBELogLoader {
	private ModelViolationsExporter mve = new ModelViolationsExporter();
	private FailuresExporter fe = new FailuresExporter();
	private AnomalousCallSequencesExporter acse = new AnomalousCallSequencesExporter();
	
	/**
	 * This class is used to load the CBE log file produced by BCT. 
	 * The CBE log has multiple roots so it cannot be read by the CBE EvntFormatter.
	 * We add a common root by wrapping the file.
	 * 
	 * CBE reading within Eclipse should be made through the EMF based CBE libraries.
	 *  
	 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
	 *
	 */
	public static class CBEInputStrem extends InputStream {

		private File file;
		private Reader stringReader;
		private boolean readHeader = true;
		private boolean readFile = true;
		private boolean readFooter = true;
		
//		int lines = 0;
//		int counter = 0;
		
		public CBEInputStrem( File file ) {
			this.file = file;
		}
		
		@Override
		public int read() throws IOException {
			
			
			while(true){
				
				if ( stringReader == null ){
					if ( readHeader ){
						stringReader = new StringReader(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><CommonBaseEvents xmlns=\"http://www.ibm.com/AC/commonbaseevent1_0_1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ibm.com/AC/commonbaseevent1_0_1 commonbaseevent1_0_1.xsd\">"
						); 
					} else if ( readFile ){
						stringReader = new FileReader(file);
					} else if ( readFooter ){
						stringReader = new StringReader(
								"</CommonBaseEvents>"
						); 
					} else {
						return -1;
					}
				}
				int readed = stringReader.read();
				
				//Workaround for bug #148, non UTF-8 chars are sometimes recorded in the CBE log, we simply ignore them
				if ( readed > 126 ){ 
					return ' ';
				}
//				counter++;
//				if ( readed == '\n' ){
//					
//					lines++;
//					System.out.println(lines+" "+counter);
//					counter=0;
//				}
				
				if ( readed != -1 ){
					return readed;
				}
				stringReader.close();
				stringReader = null;
				if ( readHeader ){
					readHeader = false;
				} else if ( readFile ){
					readFile = false;
				} else if ( readFooter ){
					readFooter = false;
				}
			}
		}

	}
	
	public CommonBaseEvent[] loadCBE(File logFile) throws FormattingException{
		if ( ! logFile.exists() ){
			return new CommonBaseEvent[0];
		}
		return EventFormatter.eventsFromCanonicalXMLDoc(new CBEInputStrem(logFile));
		
	}
	
	public Object[] loadEntitiesFromCBEFile(File logFile) throws FormattingException{
		CommonBaseEvent[] cbes = loadCBE(logFile);
		return loadEntitiesFromCBE(cbes);
	}
	
	public Object[] loadEntitiesFromCBE( CommonBaseEvent commonBaseEvents[] ){
		List entities = new ArrayList(); 
		
		for ( CommonBaseEvent cbe : commonBaseEvents ){
			Object el = loadEntityFromCBE(cbe);
			if ( el != null ){
				entities.add(el);
			}
		}
		
//		ArrayList<BctAnomalousCallSequence> css = new ArrayList<BctAnomalousCallSequence>();
//		ArrayList<BctModelViolation> mvs = new ArrayList<BctModelViolation>();
//		
//		for ( Object el : entities ){
//			if ( el instanceof BctAnomalousCallSequence ){
//				css.add((BctAnomalousCallSequence) el);
//			} else if ( el instanceof BctModelViolation ){
//				mvs.add((BctModelViolation) el);
//			}
//		}
//		
//		for ( BctModelViolation mv : mvs ){
//			for ( BctAnomalousCallSequence cs : css ){
//				
//			}	
//		}
		
		return entities.toArray(new Object[entities.size()]);
	}
	
	public Object loadEntityFromCBE( CommonBaseEvent commonBaseEvent ){
		
		if ( mve.isModelViolation( commonBaseEvent) ){
			return mve.loadViolation(commonBaseEvent);
		} else if ( fe.isFailure(commonBaseEvent) ){
			return fe.loadFailureEvent(commonBaseEvent);
		} else if ( acse.isAnomalousCallSequence(commonBaseEvent) ){
			return acse.loadAnomalousCallSequence(commonBaseEvent);
		}
		
		return null;
	}
	
	public static void main( String args[] ){
		File file = new File( args[0]);
		
		CBELogLoader loader = new CBELogLoader();
		try {
			CommonBaseEvent[] entities = loader.loadCBE(file);
			
		} catch (FormattingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
