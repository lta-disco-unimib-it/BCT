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
package conf;


import java.util.Properties;

import check.FineInteractionChecker;

import conf.InteractionCheckerSettings;

public class FineInteractionCheckerSettings extends InteractionCheckerSettings {

	public interface Options {
		public static final String dfa = "dfa";
		public static final String avaPathLen = "avaPathLen";
		public static final String fineAnalysisEnabled = "fineAnalysisEnabled";
		public static final String anomalousSequencesRecordingEnabled = "anomalousSequencesRecordingEnabled";
	}

	private boolean dfa;
	private int avaPathLen;
	private boolean fineAnalysisEnabled;
	private boolean anomalousSequencesRecordingEnabled;
	
	
	
	public void setDfa(boolean dfa) {
		this.dfa = dfa;
	}

	public void setAvaPathLen(int avaPathLen) {
		this.avaPathLen = avaPathLen;
	}

	public void setFineAnalysisEnabled(boolean fineAnalysisEnabled) {
		this.fineAnalysisEnabled = fineAnalysisEnabled;
	}

	public void setAnomalousSequencesRecordingEnabled(
			boolean anomalousSequencesRecordingEnabled) {
		this.anomalousSequencesRecordingEnabled = anomalousSequencesRecordingEnabled;
	}

	public boolean isAnomalousSequencesRecordingEnabled() {
		return anomalousSequencesRecordingEnabled;
	}

	public FineInteractionCheckerSettings(Class type, Properties p) {
		super(type, p);	

		
		
		String dfaValue = properties.getProperty(Options.dfa);
		dfa = Boolean.valueOf(dfaValue);
		
		String avaPathLenValue = properties.getProperty(Options.avaPathLen);
		avaPathLen = Integer.valueOf(avaPathLenValue);
		
		String fineAnalysisEnabledString = properties.getProperty(Options.fineAnalysisEnabled); 
		fineAnalysisEnabled = Boolean.valueOf(fineAnalysisEnabledString);
		
		String anomalousSequencesRecordingString = properties.getProperty(Options.anomalousSequencesRecordingEnabled);
		anomalousSequencesRecordingEnabled = Boolean.valueOf(anomalousSequencesRecordingString);
	}



	public FineInteractionCheckerSettings(InteractionCheckerSettings genericSettings) {
		this(genericSettings.getType(),genericSettings.properties);
	}

	public FineInteractionCheckerSettings() {
		super(FineInteractionChecker.class,new Properties());
	}

	public boolean isDFA() {
		return dfa;
	}

	public int getAVAPathLen() {
		return avaPathLen;
	}


	public boolean isFineAnalysisEnabled() {
		return fineAnalysisEnabled;
	}
	
	
	
	@Override
	public String getProperty(String key) {
		return toProperties().getProperty(key);
	}

	public Properties toProperties(){
		Properties p = new Properties();
		p.put("type", FineInteractionChecker.class.getCanonicalName().toString());
		p.put(Options.avaPathLen, String.valueOf(avaPathLen));
		p.put(Options.anomalousSequencesRecordingEnabled, String.valueOf(anomalousSequencesRecordingEnabled));
		p.put(Options.fineAnalysisEnabled, String.valueOf(fineAnalysisEnabled));
		p.put(Options.dfa, String.valueOf(dfa));
		
		return p;
	}
}
