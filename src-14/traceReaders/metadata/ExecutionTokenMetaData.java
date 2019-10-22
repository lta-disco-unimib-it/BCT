package traceReaders.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import tools.TokenMetaData;

public class ExecutionTokenMetaData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String KEY_calledObjectId = "calledObjectId";
	private static final String KEY_actions = "actions";
	private static final String KEY_tests = "tests";
	private static final String KEY_timestamp = "timestamp";
	private static final String KEY_context = "context";
	
	
	private String calledObjectId;
	private long timestamp;
	private List currentActions;
	private List currentTests;
	private List contextData;
	
	public List getContextData() {
		return contextData;
	}

	public void setContextData(List contextData) {
		this.contextData = contextData;
	}

	public String getCalledObjectId() {
		return calledObjectId;
	}

	public List getCurrentActions() {
		return currentActions;
	}

	public List getCurrentTests() {
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

	public void setCurrentActions(List currentActions) {
		this.currentActions = currentActions;
	}

	public void setCurrentTests(List currentTests) {
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
			Iterator it = currentTests.iterator();
			while ( it.hasNext() ){
				String test = (String) it.next();
				sb.append(test);
				sb.append(",");
			}
		}
		
		if ( currentActions != null ){
			sb.append("\n");
			sb.append(KEY_actions);
			sb.append("=");
			Iterator it = currentActions.iterator();
			while ( it.hasNext() ){
				String test = (String) it.next();
				sb.append(test);
				sb.append(",");
			}
		}
		
		
		if ( contextData != null ){
			sb.append("\n");
			sb.append(KEY_context);
			sb.append("=");
			Iterator it = contextData.iterator();
			while ( it.hasNext() ){
				String test = (String) it.next();
				sb.append(test);
				sb.append(",");
			}
		}
		
		return sb.toString();
		
	}

//	public static ExecutionTokenMetaData loadFromString(String metaDataLine) {
//
//		BufferedReader reader = new BufferedReader ( new StringReader(metaDataLine) );
//		String line;
//		ExecutionTokenMetaData metaData = new ExecutionTokenMetaData();
//		try {
//			while( ( line = reader.readLine() ) != null ){
//				int pos = line.indexOf('=');
//				
//				String key = line.substring(0, pos);
//				
//				String values = line.substring(pos+1);
//				
////				String[] lineContents = line.split("=");
////				 lineContents[0];
////				 lineContents[1];
//				if ( key.equals(KEY_timestamp) ){
//					metaData.setTimestamp(Long.valueOf(values));
//				} else if ( key.equals(KEY_tests) ){
//					List<String> tests = new ArrayList<String>();
//					for ( String test : values.split(",") ){
//						tests.add(test);
//					}
//					metaData.setCurrentTests(tests);
//				} else if ( key.equals(KEY_actions) ){
//					List<String> actions = new ArrayList<String>();
//					for ( String action : values.split(",") ){
//						actions.add(action);
//					}
//					metaData.setCurrentActions(actions);
//				} else if ( key.equals(KEY_context) ){
//					List<String> actions = new ArrayList<String>();
//					for ( String action : values.split(",") ){
//						actions.add(action);
//					}
//					metaData.setContextData(actions);	
//				} else if ( key.equals(KEY_calledObjectId) ){
//					metaData.setCalledObjectId(values);
//				}  
//			}
//			return metaData;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		
//	}

}
