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
package recorders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;

import modelsViolations.BctIOModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.util.EventFormatter;

import util.RuntimeContextualDataUtil;
import util.cbe.AnomalousCallSequencesExporter;

import conf.ConfigurationSettings;
import conf.EnvironmentalSetter;
import conf.ViolationsRecorderSettings;

public class FileCBEViolationsRecorder extends CBEViolationsRecorder {

	private File logFile;

	public void init(ConfigurationSettings opts) {
		String dest = opts.getProperty("violationsFile");
		File violationsFile;
		if ( dest == null ){
			violationsFile = new File ( EnvironmentalSetter.getBctHome(), "bctCBELog");
		} else {
			violationsFile = new File ( dest );
		}
		init(violationsFile);
	}
	
	public FileCBEViolationsRecorder(){};
	
	public FileCBEViolationsRecorder(File logFile){
		init(logFile);
	}

	private void init(File file) {
		this.logFile = file;
		File parent = logFile.getParentFile();
		if ( parent != null ){
			parent.mkdirs();
		}	
	}

	public static void main(String[] args){
		FileCBEViolationsRecorder r = new FileCBEViolationsRecorder();
		
		Properties p = new Properties();
		p.setProperty("FileCBEViolationsRecorder.violationsFile", "viol.xml");
		ConfigurationSettings s = new ViolationsRecorderSettings(FileCBEViolationsRecorder.class,p);
		r.init(s);
		try {
			BctIOModelViolation modelViolation = new BctIOModelViolation(
					"A",
					"a.b()",
					BctIOModelViolation.Position.EXIT,
					"x != y",
					System.currentTimeMillis(),
					new String[]{"1","2"},
					new String[]{"1"},
					new String[]{"a:2","b:3","main:4"},
					RuntimeContextualDataUtil.retrievePID(),
					RuntimeContextualDataUtil.retrieveThreadId(),
					"x=9"
					);
			r.recordIoViolation(modelViolation);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void recordEvent(CommonBaseEvent event) throws ViolationsRecorderException {
		FileWriter fw;
		try {
			fw = new FileWriter(logFile,true);
			fw.write(EventFormatter.toCanonicalXMLString(event,true));
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ViolationsRecorderException("Cannot write to file "+logFile.getAbsolutePath(),e);
		}
		
	}

	

	
	
}
