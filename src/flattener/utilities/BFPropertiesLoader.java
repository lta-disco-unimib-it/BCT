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
package flattener.utilities;

import java.util.Hashtable;

public class BFPropertiesLoader {
	private static BFPropertiesLoader instance;
	private int maxDepth;
	private boolean smashAggregations;
	private boolean loadClassesToIgnore;
	private Hashtable classesToIgnoreList;
	private FieldsRetriever fieldsRetriever;
	private boolean smashArraysAsAWhole;
	private boolean skipObjectsAlreadyVisited;
	
	

	public BFPropertiesLoader(){
		PropertiesLoader pl = PropertiesLoader.getInstance();
		Hashtable properties = pl.getObjectFlattenerProperties();
		    
	    // Get the object flattener properties
	    try {
	        this.maxDepth = Integer.parseInt((String) properties.get("objectFlattener.maxDepth"));		        
	    } catch (Exception e) {
//	        e.printStackTrace();
	        this.maxDepth = Integer.parseInt((String) pl.getObjectFlattenerDefaultProperties().get("objectFlattener.maxDepth"));				
		    System.out.println("Default max depth value will be used");		        
	    }
	    
	    
	    String skipObjectsAlreadyVisitedString = (String) properties.get("objectFlattener.skipObjectsAlreadyVisited");
	    if ( skipObjectsAlreadyVisitedString != null ){
	    	skipObjectsAlreadyVisited = Boolean.valueOf( skipObjectsAlreadyVisitedString );
	    }
	    
	    
	    //this.ignoreInnerClasses = Boolean.valueOf((String) properties.get("objectFlattener.ignoreInnerClasses")).booleanValue(); 
        				        
        this.smashAggregations = Boolean.valueOf((String) properties.get("objectFlattener.smashAggregations")).booleanValue();
		
		this.loadClassesToIgnore = Boolean.valueOf((String) properties.get("classesToIgnore.load")).booleanValue();
			
		if (this.loadClassesToIgnore) {
		    this.classesToIgnoreList = pl.getClassesToIgnoreList();
		} else {
			classesToIgnoreList = new Hashtable(0);
		}
		
		//This option allow to record an array in this way:
		//arrayName[]
		//[ 2.3 4.5 5.6 ]
		String wholeString = (String) properties.get("objectFlattener.smashArraysAsAWhole");
		if ( wholeString != null ){
			smashArraysAsAWhole = Boolean.parseBoolean(wholeString);
		}
		
		FieldFilter fieldsFilter = (FieldFilter) properties.get("fieldsFilter");
		
		String retriever = (String) properties.get("fieldsRetrieverConfig");
		
		if ( retriever == null )
			fieldsRetriever = new FieldsRetriever.FieldsRetrieverInspectable( fieldsFilter );
		
		else if ( retriever.toLowerCase().equals("all") )
			fieldsRetriever = new FieldsRetriever.FieldsRetrieverAll( fieldsFilter );
		else if ( retriever.toLowerCase().equals("getters") )	
			fieldsRetriever = new FieldsRetriever.FieldsRetireverGetters( fieldsFilter );
		else if ( retriever.toLowerCase().equals("instance") )	
			fieldsRetriever = new FieldsRetriever.FieldsRetrieverInstanceFields( fieldsFilter );
		else
			fieldsRetriever = new FieldsRetriever.FieldsRetrieverInspectable( fieldsFilter );
		
		
	}
	
	public static synchronized BFPropertiesLoader getInstance(){
		if ( instance == null ){
			instance = new BFPropertiesLoader();
		}
		return instance;
	}

	public Hashtable getClassesToIgnoreList() {
		return classesToIgnoreList;
	}


	public boolean isLoadClassesToIgnore() {
		return loadClassesToIgnore;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public boolean isSmashAggregations() {
		return smashAggregations;
	}

	/**
	 * Returns a FieldRetriever.
	 * Pay attention the IdManager is synchronized. A common retriever for all thread is returned, this can slow down systems.
	 * TODO: return a different retriever for every thread
	 * 
	 * @return
	 */
	public FieldsRetriever getFieldsRetriever() {
		return fieldsRetriever;
	}

	public boolean isSmashArraysAsAWhole() {
		return smashArraysAsAWhole;
	}

	public boolean isSkipObjectsAlreadyVisited() {
		return skipObjectsAlreadyVisited;
	}

	public void setSkipObjectsAlreadyVisited(boolean skipObjectsAlreadyVisited) {
		this.skipObjectsAlreadyVisited = skipObjectsAlreadyVisited;
	}
}
