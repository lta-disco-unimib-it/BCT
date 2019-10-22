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
package conf;


import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ConfigurationSettings {
	
	private Class type;
	protected Properties properties;
	private ReplacementProvider envReplacer = new ReplacementProviderEnv();
	private ReplacementProvider javaOptsReplacer = new ReplacementProviderJavaOpts();
	

	
	public static interface ReplacementProvider{
		
		public Pattern getPattern();
		
		public String getReplacement(String variable);
		
	}
	
	public static class ReplacementProviderEnv implements ReplacementProvider {
		private final Pattern envVarPattern = Pattern.compile("\\$\\{.*\\}");
		
		public Pattern getPattern(){
			return envVarPattern;
		}
		
		public String getReplacement(String variable){
			return System.getenv(variable).replace('\\', '/');
		}
		
	}
	
	public static class ReplacementProviderJavaOpts implements ReplacementProvider {
		private final Pattern declVarPattern = Pattern.compile("%\\{.*\\}");
		
		public Pattern getPattern(){
			return declVarPattern;
		}
		
		public String getReplacement(String variable){
			return System.getProperty(variable).replace('\\', '/');
		}
		
	}
	
	public ConfigurationSettings( Class type, Properties p ) {
		this.type = type;
		this.properties = p; 
		
		addBctHomeProperty();
		replaceEnvironmentVariables();
		replaceJavaDeclarations();
		replaceVariables();
		
	}

	private void addBctHomeProperty() {
		String bctHome = EnvironmentalSetter.getBctHome();
		properties.put("%BCT_HOME%",bctHome);
	}

	/**
	 * This method replace environmental variables names addressed in the value field of property files with their values.
	 * Environmental variables can be addressed in this way ${ENV_VAR_NAME}
	 * 
	 * 
	 *
	 */
	private void replaceEnvironmentVariables() {
		replaceDeclarations(envReplacer);
	}

	/**
	 * This method replace all references to java properties declared on the command line with-D<property>.
	 * References can be addressed as %{property}
	 * 
	 */
	private void replaceJavaDeclarations() {
		replaceDeclarations( javaOptsReplacer  );
	}

	private void replaceDeclarations(ReplacementProvider replacementProvider) {
		Iterator keyIt = this.properties.keySet().iterator();
		
		while ( keyIt.hasNext() ){
			String key = (String)keyIt.next();
			
			String value = (String) properties.get(key);
			
			Matcher matcher = replacementProvider.getPattern().matcher(value);
			
			//Replace environment variables
			if ( matcher.find() ){
				
				StringBuffer sb = new StringBuffer();
				do{
					 
					String match = matcher.group();
					String envVar = match.substring(2, match.length()-1);
					String envVarValue = replacementProvider.getReplacement(envVar);
					matcher.appendReplacement(sb, envVarValue);
					
				}while( matcher.find() );
				matcher.appendTail(sb);
				
				properties.setProperty(key, sb.toString() );
			}
			
			
		}
	}
	
	/**
	 * This method replace property names used as variables in other properties 
	 * For example we can have:
	 * temporaryDir = /tmp
	 * normalizedDir = %temporaryDir%/normalized
	 * 
	 * after this method call we will have:
	 * 	
	 * 	temporaryDir = /tmp
	 *  normalizedDir = /tmp/normalized
	 *  
	 */
	private void replaceVariables() {
		
		

		Iterator keyIt = this.properties.keySet().iterator();
		
		while ( keyIt.hasNext() ){
			replaceVariableName( this.properties, (String)keyIt.next() );
		}
	}

	/**
	 * This method replaces:
	 * a property name used inside a property value with its value
	 * 
	 * 
	 * Properties should be addressed in this way %propertyName%
	 * 
	 *  
	 * For example we can have:
	 * temporaryDir = /tmp
	 * normalizedDir = %temporaryDir%/normalized
	 * 
	 *
	 * 
	 * after this method call with temporaryDir as the second parameter we will have normalizedDir = /tmp/normalized
	 * 
	 * 
	 * We can also have:
	 * 
	 * temporaryDir = ${BCT_HOME}/tmp
	 * normalizedDir = %temporaryDir%/normalized
	 * 
	 * suppose that BCT_HOME is /home/user/BCT
	 * 
	 * after this method call with temporaryDir as the second parameter we will have normalizedDir = /home/user/BCT/tmp/normalized
	 *  
	 * @param properties	properties that can be changed
	 * @param variable		the property name to replace
	 * 
	 */
	private void replaceVariableName(Properties properties, String variable) {
		Iterator keyIt = properties.keySet().iterator();
		String variableValue = properties.getProperty(variable);
		
		while ( keyIt.hasNext() ){  
			String key = (String) keyIt.next(); //we replace every property
			if ( ! key.equals(variable)){		//if it is not the one that is currently defined
				String value = properties.getProperty(key);
				String varKey = "%"+variable+"%";
				//System.out.println(value+" "+varKey);
				if ( value.contains(varKey)){	//if the value contains %propertyName% replace it with its value
					//System.out.println("REPLACING KEY "+varKey+" IN "+key+":"+value+" WITH "+variableValue);
					String newValue = value.replace(varKey, variableValue);
					properties.put(key, newValue);
				}
			}
		}
	}


	public Class getType (){
		return type;
	}


	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public void setProperty(String key,String value) {
		properties.setProperty(key,value);
	}
	
	public Properties getProperties(){
		Properties p = new Properties();
		p.putAll(properties);
		return p;
	}
	
}
