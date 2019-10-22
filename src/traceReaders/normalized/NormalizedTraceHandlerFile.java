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
package traceReaders.normalized;

import java.io.File;

import conf.EnvironmentalSetter;

public class NormalizedTraceHandlerFile implements NormalizedTraceHandler {
	private File decls;
	private File dtrace;
	private File interaction;
	private NormalizedInteractionTraceHandlerFile interactionHandler;
	private NormalizedIoTraceHandlerFile ioHandler;
	
	public static class Options {
		public final static String declsDir = "normalizedTraceHandlerFile.declsDir";
		public static final String dtraceDir = "normalizedTraceHandlerFile.dtraceDir";
		public static final String interactionDir = "normalizedTraceHandlerFile.interactionDir";
		public static final String usageDir = "normalizedTraceHandlerFile.usageDir";
	}
	
	public NormalizedTraceHandlerFile() throws NormalizedTraceHandlerException{
		
		String declsDirName = EnvironmentalSetter.getInvariantGeneratorSettings().getProperty(Options.declsDir);
		if ( declsDirName == null )
			throw new NormalizedTraceHandlerException("Property normalizedTraceHandlerFile.declsDir not set in InvariantGeneratorSettings file");
		
		decls = new File( declsDirName );
		
		String dtraceDirName = EnvironmentalSetter.getInvariantGeneratorSettings().getProperty(Options.dtraceDir); 
		if ( dtraceDirName == null )
			throw new NormalizedTraceHandlerException("Property normalizedTraceHandlerFile.dtraceDir not set in InvariantGeneratorSettings file");
		
		dtrace = new File ( dtraceDirName );
		
		
		String interactionDirName = EnvironmentalSetter.getInvariantGeneratorSettings().getProperty(Options.interactionDir);
		if ( interactionDirName == null )
			throw new NormalizedTraceHandlerException("Property normalizedTraceHandlerFile.interactionDir not set in InvariantGeneratorSettings file");
		
		interaction = new File ( interactionDirName );
		
//		String usageDirName = EnvironmentalSetter.getInvariantGeneratorSettings().getProperty(Options.usageDir);
		//TODO: add this check in future releases
//		if ( interactionDirName == null )
//			throw new NormalizedTraceHandlerException("Property normalizedTraceHandlerFile.usageDir not set in InvariantGeneratorSettings file");
		
//		usage = new File ( usageDirName );
		
		
		
		decls.mkdirs();
		
		if ( ! decls.exists() )
			throw new NormalizedTraceHandlerException("cannot create "+decls);
		
		dtrace.mkdirs();
		if ( ! decls.exists() )
			throw new NormalizedTraceHandlerException("cannot create "+dtrace);
		
		interaction.mkdirs();
		if ( ! interaction.exists() )
			throw new NormalizedTraceHandlerException("cannot create "+interaction);

		
	}
	
	
	public NormalizedInteractionTraceHandler getInteractionTraceHandler() {
		if ( interactionHandler == null ){
			interactionHandler = new NormalizedInteractionTraceHandlerFile( interaction );
		}
		return interactionHandler;
	}

	public NormalizedIoTraceHandler getIoTraceHandler( ) {
		if ( ioHandler == null ){
			ioHandler = new NormalizedIoTraceHandlerFile( decls, dtrace);
		}
		return ioHandler;
	}



}
