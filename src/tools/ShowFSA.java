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
/*
 * This class implements functionalities for visualizing a serialized FSA.
 * 
 */
package tools;

import gui.environment.EnvironmentFrame;
import gui.environment.FrameFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import tools.fsa2xml.LazyFSALoader;
import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;

import automata.fsa.FiniteStateAutomaton;

public class ShowFSA {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java tools.ShowFSA <fileName>");
			System.out.println("Example: java tools.ShowFSA method().ser");
			System.exit(0);
		}
		
/*		File toBeOpened = new File(args[0]);
		if (!toBeOpened.exists()) {
			System.out.println("Error: File " + toBeOpened.getAbsolutePath() + " does not exist!");
			System.exit(1);
		}
		
	  	ObjectInputStream in;
	  	FiniteStateAutomaton fsa=null;
		try {
			in = new ObjectInputStream(new FileInputStream(toBeOpened.getAbsolutePath()));
			try {
				fsa = (FiniteStateAutomaton)in.readObject();
			} catch (ClassNotFoundException e1) {
				System.out.println("The class corresponding to the deserialized object has not been found");
				e1.printStackTrace();
				System.exit(1);
			}
		} catch (IOException e) {
			System.out.println("Input/Output error while opening " + toBeOpened.getAbsolutePath() + " resource");
			e.printStackTrace();
			System.exit(1);
		}*/
		
		try{
			FiniteStateAutomaton fsa = LazyFSALoader.loadFSA(args[0]);
			
			//FiniteStateAutomaton fsa = FiniteStateAutomaton.readSerializedFSA(args[0]);
			showFSA(fsa);
		} catch (FileNotFoundException e) {
			System.out.println("Error: File " + new File(args[0]).getAbsolutePath() + " does not exist!");
			//e.printStackTrace();
			System.exit(1);
//		} catch (ClassNotFoundException e) {
//			System.out.println("The class corresponding to the deserialized object has not been found");
//			//e.printStackTrace();
//			System.exit(1);
		} catch (IOException e) {
			System.out.println("Input/Output error while opening " + new File(args[0]).getAbsolutePath() + " resource");
			//e.printStackTrace();
			System.exit(1);
		} catch (LazyFSALoaderException e) {
			System.out.println("Input/Output error while opening " + new File(args[0]).getAbsolutePath() + " resource");
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}
	
	public static void showFSA( FiniteStateAutomaton fsa ){
	    	
			EnvironmentFrame f = FrameFactory.createFrame(fsa);
			
			
		
		
	}
}