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
package genericProgramPoint;

import static org.junit.Assert.*;

import java.io.File;

import modelsFetchers.IoModelIterator;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;

import org.junit.Test;

import recorders.DataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import tools.InvariantGenerator;
import util.FileUtil;
import util.GenericProgramPointNaming;

import conf.EnvironmentalSetter;
import conf.InvariantGeneratorSettings;
import conf.management.ConfigurationFilesManager;
import conf.management.ConfigurationFilesManagerException;

import cpp.gdb.GdbRegressionConfigCreator.Configuration;
import flattener.core.Handler;
import flattener.handlers.RawHandler;

public class TestGenericProgramPointHandling {

	File baseFolder = new File("test/system/genericProgramPointHandling");
	
	@Test
	public void testSimple() throws ConfigurationFilesManagerException, RecorderException, ModelsFetcherException{
		
		File bctHome = new File( baseFolder, "BCT_HOME" );
		
		FileUtil.deleteDirectoryContents(bctHome);
		
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		
		//Create config folder
		ConfigurationFilesManager.updateConfigurationFiles();
		
		InvariantGeneratorSettings iGS = EnvironmentalSetter.getInvariantGeneratorSettings();
		iGS.setDeleteTemporaryDir(false);
		
		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
		
		String function = "myFunc";
		
		
		String myBlock = GenericProgramPointNaming.nameForBlockProgramPoint(function, 12, 15);
		String myLine = GenericProgramPointNaming.nameForLineProgramPoint(function, 12);
		
		RawHandler handler = new RawHandler("");
		handler.addNodeValue("myInt", "3");
		handler.addNodeValue("myStruct.value", "5");
		
		{
			Handler[] variables = new Handler[2];

			variables[0] = new RawHandler("");
			((RawHandler)variables[0]).addNodeValue("myInt", "3");

			variables[1] = new RawHandler("");
			((RawHandler)variables[1]).addNodeValue("myStruct.value", "5");

			recorder.recordGenericProgramPoint(myBlock, variables, 0);
		}
		
		
		{
			Handler[] variables = new Handler[2];

			variables[0] = new RawHandler("");
			((RawHandler)variables[0]).addNodeValue("myInt", "3");

			variables[1] = new RawHandler("");
			((RawHandler)variables[1]).addNodeValue("myStruct.value", "7");

			recorder.recordGenericProgramPoint(myBlock, variables, 0);
		}
		
		
		
		
		
		
		{
			Handler[] variables = new Handler[2];

			variables[0] = new RawHandler("");
			((RawHandler)variables[0]).addNodeValue("myInt", "3");

			variables[1] = new RawHandler("");
			((RawHandler)variables[1]).addNodeValue("myStruct.value", "7");

			recorder.recordGenericProgramPoint(myLine, variables, 0);
		}
		
		//This call will force the saving of traces
		recorder.newExecution("");
		
		InvariantGenerator.main(new String[]{"-default"});
		
		IoModelIterator ioModelsIterator = ModelsFetcherFactoy.modelsFetcherInstance.getIoModelIteratorEnter(myBlock);
		
		assertNotNull( ioModelsIterator.next() );
		assertNotNull( ioModelsIterator.next() );
		assertNotNull( ioModelsIterator.next() );
		
		
		ioModelsIterator = ModelsFetcherFactoy.modelsFetcherInstance.getIoModelIteratorEnter(myLine);
		
		assertNotNull( ioModelsIterator.next() );
		assertNotNull( ioModelsIterator.next() );
		assertNotNull( ioModelsIterator.next() );
		
		
		//infer models
	}
}
