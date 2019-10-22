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
package tools;

public class StopperThread extends Thread {
	
	private Process ps;
	private int daikonLimit;
	private boolean kill = true;



	public StopperThread ( Process ps, int daikonLimit ){
		this.ps = ps;
		this.daikonLimit = daikonLimit;
	}
	
	
	public void run() {
		synchronized (this) {
			try {
				long time = System.currentTimeMillis();
				int waitDelta = daikonLimit*1000;
				long timeElapsed = 0;

				do {
					this.wait(waitDelta);
					timeElapsed += ( System.currentTimeMillis() - time );
				} while ( kill && timeElapsed < waitDelta);

				if ( kill ){
					System.err.println("Process took too much time ("+timeElapsed+" sec), killing it ("+ps.toString()+")");
					
					ps.destroy();
				}

			} catch (InterruptedException e) {

			}
		}
	}


	/**
	 * Terminate the execution of the stopper thread by notifying it
	 * 
	 */
	public void terminate() {
		synchronized (this) {
			this.kill = false;
			this.notify();
		}
	}
	

}
