/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package traceReaders.metaData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import tools.TokenMetaData;

public class ExecutionTokenMetaData implements TokenMetaData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String KEY_calledObjectId = "calledObjectId";
	private static final String KEY_actions = "actions";
	private static final String KEY_tests = "tests";
	private static final String KEY_timestamp = "timestamp";
	private static final String KEY_context = "context";
	private static final String KEY_class = "calledObjectClass";
	
	
	private String calledObjectId;
	private long timestamp;
	private List<String> currentActions;
	private List<String> currentTests;
	private List<String> contextData = new ArrayList<String>();

	private String calledObjectClass;

	public List<String> getContextData() {
		return contextData;
	}

	public void setContextData(List<String> contextData) {
		this.contextData = contextData;
	}

	public String getCalledObjectId() {
		return calledObjectId;
	}

	public List<String> getCurrentActions() {
		return currentActions;
	}

	public List<String> getCurrentTests() {
		return currentTests;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setCalledObjectId(String calledObjectId) {
		this.calledObjectId = calledObjectId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setCurrentActions(List<String> currentActions) {
		this.currentActions = currentActions;
	}

	public void setCurrentTests(List<String> currentTests) {
		this.currentTests = currentTests;
	}
	
	public String toString(){
		return storeToString();
	}
	
	/**
	 * Return a string version of the Token.
	 * 
	 * This string can be used to recreate the token using method loadFromString
	 */
	
	public String storeToString(){
	
		StringBuffer sb = new StringBuffer();
		
		sb.append(KEY_timestamp);
		sb.append("=");
		sb.append(timestamp);
		
		if ( calledObjectId != null ){
			sb.append("\n");
			sb.append(KEY_calledObjectId);
			sb.append("=");
			sb.append(calledObjectId);
		}
		
		if ( currentTests != null ){
			sb.append("\n");
			sb.append(KEY_tests);
			sb.append("=");
			for ( String test : currentTests ){
				sb.append(test);
				sb.append(",");
			}
		}
		
		if ( currentActions != null ){
			sb.append("\n");
			sb.append(KEY_actions);
			sb.append("=");
			for ( String test : currentActions ){
				sb.append(test);
				sb.append(",");
			}
		}
		
		
		if ( contextData != null ){
			sb.append("\n");
			sb.append(KEY_context);
			sb.append("=");
			for ( String test : contextData ){
				sb.append(test);
				sb.append(",");
			}
		}
		
		if ( contextData != null ){
			sb.append("\n");
			sb.append(KEY_class);
			sb.append("=");
			sb.append(calledObjectClass);
		}
		
		return sb.toString();
		
	}

	public static TokenMetaData loadFromString(String metaDataLine) {

		BufferedReader reader = new BufferedReader ( new StringReader(metaDataLine) );
		String line;
		ExecutionTokenMetaData metaData = new ExecutionTokenMetaData();
		try {
			while( ( line = reader.readLine() ) != null ){
				int pos = line.indexOf('=');
				
				String key = line.substring(0, pos);
				
				String values = line.substring(pos+1);
				
//				String[] lineContents = line.split("=");
//				 lineContents[0];
//				 lineContents[1];
				if ( key.equals(KEY_timestamp) ){
					metaData.setTimestamp(Long.valueOf(values));
				} else if ( key.equals(KEY_tests) ){
					List<String> tests = new ArrayList<String>();
					for ( String test : values.split(",") ){
						tests.add(test);
					}
					metaData.setCurrentTests(tests);
				} else if ( key.equals(KEY_actions) ){
					List<String> actions = new ArrayList<String>();
					for ( String action : values.split(",") ){
						actions.add(action);
					}
					metaData.setCurrentActions(actions);
				} else if ( key.equals(KEY_context) ){
					List<String> actions = new ArrayList<String>();
					for ( String action : values.split(",") ){
						actions.add(action);
					}
					metaData.setContextData(actions);	
				} else if ( key.equals(KEY_calledObjectId) ){
					metaData.setCalledObjectId(values);
				} else if ( key.equals(KEY_class) ){
					if ( ! "null".equals(values) ){
						metaData.setCalledObjectClass(values);
					}
				}  
			}
			return metaData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	public void setCalledObjectClass(String canonicalName) {
		this.calledObjectClass = canonicalName;
	}
	
	public String getCalledObjectClass() {
		return calledObjectClass;
	}

}
