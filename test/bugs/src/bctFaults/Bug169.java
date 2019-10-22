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
package bctFaults;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import recorders.NullDataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import testSupport.TestArtifactsManager;
import tools.TokenMetaData;
import traceReaders.metaData.ExecutionTokenMetaData;
import conf.DataRecorderSettings;
import conf.EnvironmentalSetter;
import flattener.core.Handler;

public class Bug169 {
	
	public static class MemoryDataRecorder extends NullDataRecorder{

		List<String> metaInfos = new ArrayList<String>();
		
		@Override
		public void recordIoInteractionEnterMeta(Object calledObject,
				String methodSignature, Handler[] parameters, long threadId,
				String metaInfo) throws RecorderException {
			// TODO Auto-generated method stub
			super.recordIoInteractionEnterMeta(calledObject, methodSignature, parameters,
					threadId, metaInfo);
			metaInfos.add(metaInfo);
		}

		@Override
		public void recordIoInteractionExitMeta(Object calledObject,
				String methodSignature, Handler[] parameters, long threadId,
				String metaInfo) throws RecorderException {
			// TODO Auto-generated method stub
			super.recordIoInteractionExitMeta(calledObject, methodSignature, parameters,
					threadId, metaInfo);
			metaInfos.add(metaInfo);
		}

		@Override
		public void recordIoInteractionExitMeta(Object calledObject,
				String methodSignature, Handler[] parameters,
				Handler returnValue, long threadId, String metaInfo)
				throws RecorderException {
			// TODO Auto-generated method stub
			super.recordIoInteractionExitMeta(calledObject, methodSignature, parameters,
					returnValue, threadId, metaInfo);
			metaInfos.add(metaInfo);
		}
		
	}
	
	
	
	public static class TestClass_CallerSideInstrumented{
		
		public void callerMethod() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
			
			//Args for after
			Object /*returnedObject*/ returnedObject = null;
			String /*className*/ className = TestClass_CallerSideInstrumented.class.getCanonicalName();
			String /*methodName*/ methodName = "calledMethod";
			String /*methodSig*/ methodSig = "()";
			Object /*thisObject*/ thisObject = this;
			Object[] /*args*/ args = new Object[0];
			
			
			bctTCLPAllStubBug169.Probe_0._beforeCall(
					className,
					methodName,
					methodSig,
					thisObject,
					args
				);
			
			
			calledMethod();
			
			
			bctTCLPAllStubBug169.Probe_0._afterCall(
				returnedObject,
				className,
				methodName,
				methodSig,
				thisObject,
				args
			);
			
			
		}
		
		public void calledMethod() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SecurityException, NoSuchMethodException{
			
			
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	public static class TestClass_CalledSideInstrumented{
		
		public void callerMethod() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
			calledMethod();
		}
		
		public void calledMethod() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SecurityException, NoSuchMethodException{

			
			//Args for after
			Object /*returnedObject*/ returnedObject = null;
			String /*className*/ className = TestClass_CalledSideInstrumented.class.getCanonicalName();
			String /*methodName*/ methodName = "calledMethod";
			String /*methodSig*/ methodSig = "()";
			Object /*thisObject*/ thisObject = this;
			Object[] /*args*/ args = new Object[0];
			
			
			
			
			bctTCLPAllStubBug169.Probe_1._entry(
					className,
					methodName,
					methodSig,
					thisObject,
					args
				);
			

			
			
			bctTCLPAllStubBug169.Probe_1._exit(
				returnedObject,
				className,
				methodName,
				methodSig,
				thisObject,
				args
			);
			
			
						
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void testCalledSideMonitoring() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		File bctHome = TestArtifactsManager.getBugFile("169/bctData");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		
		
		MemoryDataRecorder recorder = new MemoryDataRecorder();
		
		RecorderFactory.setLoggingRecorder(recorder);
		
		
		
		EnvironmentalSetter.setDataRecorderSettings(new DataRecorderSettings(MemoryDataRecorder.class, new Properties()));
		
		
		
		
		TestClass_CalledSideInstrumented calledInstrumented = new TestClass_CalledSideInstrumented();
		calledInstrumented.callerMethod();
		
		
		
		assertEquals(2,recorder.metaInfos.size());
		
		
		String enterMetaInfoString = recorder.metaInfos.get(0);
		
		TokenMetaData enterMetaInfo = ExecutionTokenMetaData.loadFromString(enterMetaInfoString);
		
		
		assertEquals( "bctFaults.Bug169$TestClass_CalledSideInstrumented:137" , enterMetaInfo.getContextData().get(0));
		
		assertEquals(""+System.identityHashCode(calledInstrumented), enterMetaInfo.getCalledObjectId());
		
		
		
		
		String exitMetaInfoString = recorder.metaInfos.get(1);
		
		TokenMetaData exitMetaInfo = ExecutionTokenMetaData.loadFromString(exitMetaInfoString);
		
		assertEquals( "bctFaults.Bug169$TestClass_CalledSideInstrumented:148" , exitMetaInfo.getContextData().get(0));
		
		assertEquals(""+System.identityHashCode(calledInstrumented), exitMetaInfo.getCalledObjectId());
	}	
	
	
//	timestamp=1297187236602
//	calledObjectId=608889682
//	tests=*bctFaults.Bug169$TestClass_CalledSideInstrumented.calledMethod,
//	context=
//	timestamp=1297187236606
//	calledObjectId=608889682
//	tests=*bctFaults.Bug169$TestClass_CalledSideInstrumented.calledMethod,
//	context=bctFaults.Bug169$TestClass_CalledSideInstrumented:119,
//	timestamp=1297187236624
//	calledObjectId=1829923591
//	tests=*bctFaults.Bug169$TestClass_CallerSideInstrumented.callerMethod,
//	context=bctFaults.Bug169$TestClass_CallerSideInstrumented:76,
//	timestamp=1297187236624
//	calledObjectId=1829923591
//	tests=*bctFaults.Bug169$TestClass_CallerSideInstrumented.callerMethod,
//	context=bctFaults.Bug169$TestClass_CallerSideInstrumented:88,
	
	

	@Test
	public void testCallerSideMonitoring() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		File bctHome = TestArtifactsManager.getBugFile("169/bctData");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		MemoryDataRecorder recorder = new MemoryDataRecorder();
		
		RecorderFactory.setLoggingRecorder(recorder);
		
		
		
		
		
		EnvironmentalSetter.setDataRecorderSettings(new DataRecorderSettings(MemoryDataRecorder.class, new Properties()));
		
		
		
		
		
		TestClass_CallerSideInstrumented callerInstrumented = new TestClass_CallerSideInstrumented();
		callerInstrumented.callerMethod();
		
		
		
		
		assertEquals(2,recorder.metaInfos.size());
		
		
		String enterMetaInfoString = recorder.metaInfos.get(0);
		
		TokenMetaData enterMetaInfo = ExecutionTokenMetaData.loadFromString(enterMetaInfoString);
		
		
		assertEquals( "bctFaults.Bug169$TestClass_CallerSideInstrumented:77" , enterMetaInfo.getContextData().get(0));
		
		assertEquals(""+System.identityHashCode(callerInstrumented), enterMetaInfo.getCalledObjectId());
		
		
		
		
		String exitMetaInfoString = recorder.metaInfos.get(1);
		
		TokenMetaData exitMetaInfo = ExecutionTokenMetaData.loadFromString(exitMetaInfoString);
		
		assertEquals( "bctFaults.Bug169$TestClass_CallerSideInstrumented:89" , exitMetaInfo.getContextData().get(0));
		
		assertEquals(""+System.identityHashCode(callerInstrumented), exitMetaInfo.getCalledObjectId());
		
		
	}
}
