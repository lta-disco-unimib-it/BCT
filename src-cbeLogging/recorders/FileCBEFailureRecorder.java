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

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.util.EventFormatter;

import conf.ConfigurationSettings;

public class FileCBEFailureRecorder extends CBEFailureRecorder {

	private File logFile;

	public FileCBEFailureRecorder( File file ){
		init(file);
	}
	
	public void init(ConfigurationSettings opts) {
		File file = new File ( opts.getProperty("logFile") );
		init( file);
	}
	
	private void init (File file){
		logFile = file;
		File parent = logFile.getParentFile();
		if ( parent != null ){
			parent.mkdirs();
		}
	}
	
	@Override
	protected void recordEvent(CommonBaseEvent cbe) throws RecorderException {
		FileWriter fw;
		try {
			fw = new FileWriter(logFile,true);
			fw.write(EventFormatter.toCanonicalXMLString(cbe,true));
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RecorderException("Cannot write to file "+logFile.getAbsolutePath(),e);
		}
	}

}
