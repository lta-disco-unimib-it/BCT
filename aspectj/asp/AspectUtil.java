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
package asp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class AspectUtil {
	protected static final long BUFFER_THREAD_WAIT = 1000;
	static int BUFFER_SIZE = 2000000;
	private int MAX_CURRENT_DATA = 5;

	private static class MutableInteger {
		private int value;

		public boolean isZero() {
			return value==0;
		}

		public void increase() {
			++value;
		}

		public void decrease() {
			--value;
		}
	}
	
	ThreadLocal<MutableInteger> entryPoints = new ThreadLocal<MutableInteger>(){
		@Override
		protected MutableInteger initialValue() {
			return new MutableInteger();
		}
	};

	ConcurrentHashMap<Integer,BufferedWriter> threadWriters = new ConcurrentHashMap<Integer,BufferedWriter>();
	
	ConcurrentLinkedQueue<LinkedList<Data>> allThreadsData = new ConcurrentLinkedQueue<LinkedList<Data>>();

	ThreadLocal<LinkedList<Data>> allDataT = new ThreadLocal<LinkedList<Data>>(){
		@Override
		protected LinkedList<Data> initialValue() {
			LinkedList<Data> list = new LinkedList<Data>();
			allThreadsData.add(list);
			return list;
		}
	};

	ThreadLocal<Data> currentDataT = new ThreadLocal<AspectUtil.Data>(){
		@Override
		protected Data initialValue() {
			Data d = new Data();
			d.setFirst(true);
			allDataT.get().add(d);
			return d;
		}
	};





	private BufferWriterHook hook;



	private long beginTime;

	public class BufferWriterHook implements Runnable {


		private boolean shutDown=true;

		public synchronized void startNoShutDown(){
			if ( shutDown==false ){
				//already running
				return;
			}

			Thread t = new Thread(){

				@Override
				public void run() {
					runNoShutDown();
				}

			};	
			t.start();
		}

		public synchronized void runNoShutDown(){
			shutDown= false;
			run();
			shutDown = true;
		}

		public synchronized void run(){

			

			//Iterate over all the threads
			Iterator<LinkedList<Data>> it = allThreadsData.iterator();
			
			int threadCounter = 0;
			while( it.hasNext()){
				threadCounter++;
				
				BufferedWriter bw = getBufferedWriter(threadCounter);
				
				LinkedList<Data> allData = it.next();
				while ( ! allData.isEmpty() ){
					
					if ( shutDown == false && allData.size() == 1 ){
						break;
					}

					Data cd = allData.remove();
					flush( bw, cd );
				}

				if ( shutDown ){
					endTraceFile(bw);
				}
			}
		}


		 
		private BufferedWriter getBufferedWriter(int threadCounter) {
			
			BufferedWriter bw = threadWriters.get(threadCounter);
			if ( bw != null ){
				return bw;
			}
			
			try {
				String traceName = System.getProperty("traceName");

				if ( traceName == null ){
					traceName = "trace."+beginTime;
				}
				traceName += "."+threadCounter+".csv";
				File file = new File(traceName);

				bw = new BufferedWriter(new FileWriter(file));

				threadWriters.put(threadCounter, bw);
				
				return bw;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		private void flush(BufferedWriter bw, Data currentData) {
			
			if ( currentData.first ){
				try{
					bw.write("START");
					bw.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			for ( int i = 0; i < currentData.callsCounter; i++ ){
				Signature sig = currentData.signatures[i];
				String pos = currentData.enter[i] ? "B" : "E";
				try {
					bw.write("S"+currentData.hashes[i]+";"+printableSig(currentData.runtimeClass[i],sig)+";"+pos+";"+currentData.time[i]);
					bw.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		private void endTraceFile(BufferedWriter bw) {
			try {
				bw.write("STOP");
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private String printableSig(String runtimeClass, Signature sig) {
			return runtimeClass+"."+sig.getName();
		}
	}

	public static class Data { 
		int callsCounter;
		boolean first = false;
		Signature[] signatures = new Signature[BUFFER_SIZE];
		String[] runtimeClass = new String[BUFFER_SIZE];
		long[] time = new long[BUFFER_SIZE];
		boolean[] enter = new boolean[BUFFER_SIZE];
		public int[] hashes  = new int[BUFFER_SIZE];


		public void setFirst(boolean b) {
			first=true;
		}


	}

	{
		beginTime = System.currentTimeMillis();





		hook = new BufferWriterHook();

		Runtime.getRuntime().addShutdownHook(new Thread(hook));




	}




	public void enter(JoinPoint jp){
		entryPoints.get().increase();
		traceEnter(jp);
	}
	
	public void enterInternal(JoinPoint jp){
		if ( entryPoints.get().isZero() ){
			return;
		}
		
		traceEnter(jp);
	}

	private void traceEnter(JoinPoint jp) {
		Data currentData = tracePerformanceData(jp);
		currentData.enter[currentData.callsCounter] = true;

		++currentData.callsCounter;
	}

	private Data tracePerformanceData(JoinPoint jp) {
		Data currentData = currentDataT.get();
		currentData = handleMax(currentData);

		int callsCounter = currentData.callsCounter;
		currentData.hashes[callsCounter] = System.identityHashCode(jp.getThis());
		currentData.signatures[callsCounter] = jp.getSignature();
		String rtClass;
		
		
		
		Object thisObj = jp.getThis();
		if ( thisObj == null ){
			rtClass=currentData.signatures[callsCounter].getDeclaringTypeName();
		} else {
			rtClass=thisObj.getClass().getCanonicalName();
		}
		
		if ( rtClass == null ){
			rtClass = jp.getStaticPart().getSignature().getDeclaringTypeName();
		}
		
		currentData.runtimeClass[callsCounter] = rtClass;
		currentData.time[callsCounter] = System.currentTimeMillis();
		return currentData;
	}

	public void exit(JoinPoint jp){
		entryPoints.get().decrease();
		traceExit(jp);
	}
	
	public void exitInternal(JoinPoint jp){
		if ( entryPoints.get().isZero() ){
			return;
		}
		
		traceExit(jp);
	}

	private void traceExit(JoinPoint jp) {
		Data currentData = tracePerformanceData(jp);
		
		currentData.enter[currentData.callsCounter] = false;

		++currentData.callsCounter;
	}

	private Data handleMax(Data currentData) {

		if ( currentData.callsCounter < BUFFER_SIZE ){
			return currentData;
		}

		

		currentData = new Data();
		LinkedList<Data> list = allDataT.get();
		
		list.add(currentData);

		if ( list.size() >= MAX_CURRENT_DATA ){
			hook.startNoShutDown();
		}

		currentDataT.set(currentData);

		return currentData;
	}

}
