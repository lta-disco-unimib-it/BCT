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
package modelsFetchers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import tools.InvariantGenerator;
import daikon.FileIO;
import daikon.PptMap;
import daikon.PptSlice;
import daikon.PptTopLevel;
import daikon.inv.Invariant;
import daikon.inv.filter.InvariantFilter;
import daikon.inv.filter.InvariantFilters;

public class DaikonFileModelsFetcher extends FileModelsFetcher {
	
	///skip checking of invariants filtered out by daikon
	private static final String BCT_CHECK_DO_FILTER = "bct.checking.daikonFilter";

	///skip checking of invariants that were not printed out by daikon
	private static final String BCT_CHECK_DO_TEXT_FILTER = "bct.checking.doTextualFilter";

	private boolean skipShift = true;

	private static Logger LOGGER = Logger.getLogger(DaikonFileModelsFetcher.class.getCanonicalName());
	
	private boolean doConf = false;
	private double confidence_limit;
	private boolean doFilter = false;

	private boolean skipArrays = false;

	private boolean undoOptimizations = false;

	private boolean doTextualFilter = true;
	
	
	
	{
		
		{
			String doTextualFilterString = System.getProperty(BCT_CHECK_DO_TEXT_FILTER);
			if ( doTextualFilterString != null ){
				doTextualFilter = Boolean.valueOf(doTextualFilterString);
			}
		}
		
		{
			String undoDaikonOptimizations = System.getProperty(InvariantGenerator.BCT_INFERENCE_UNDO_DAIKON_OPTIMIZATIONS);
			if ( undoDaikonOptimizations != null ){
				undoOptimizations = ! Boolean.valueOf(undoDaikonOptimizations);
			}
		}
		
		String doFilterString = System.getProperty(BCT_CHECK_DO_FILTER);
		if ( doFilterString != null ){
			doFilter = Boolean.valueOf(doFilterString);
		}
		
		LOGGER.info("doFilter = "+doFilter);
		
		String skipArraysString = System.getProperty(InvariantGenerator.BCT_SKIP_ARRAYS);
		if ( skipArraysString != null ){
			skipArrays = Boolean.valueOf(skipArraysString);
		}
		
		LOGGER.info("skipArrays = "+skipArrays);
		
		
		
		LOGGER.info("skipShift = "+skipShift);
	}
	
	public DaikonFileModelsFetcher() {
		
	}

	public DaikonFileModelsFetcher(File ioModelsDir, File interactionModelsDir) {
		super(ioModelsDir, interactionModelsDir);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List getSerializedIoModelsEnter(String methodSignature) throws ModelsFetcherException {
		File file = getIoModelEnterFile(methodSignature);
		
		String daikonInvName = file.getName()+".inv.gz";
		
		File daikonInvFile = new File( file.getParent(), daikonInvName );
		
		try {
			return loadInvariantsFromFile(daikonInvFile, true, super.getIoCollection(file) );
		} catch (IOException e) {
			throw new ModelsFetcherException("", e);
		}
	}

	@Override
	public List<Invariant> getSerializedIoModelsExit(String methodSignature) throws ModelsFetcherException {
		File file = getIoModelEnterFile(methodSignature); //both enter and exit models are saved in the same file
		
		String daikonInvName = file.getName()+".inv.gz";
		
		File daikonInvFile = new File( file.getParent(), daikonInvName );
		
		try {
			return loadInvariantsFromFile(daikonInvFile, false, super.getIoCollection(file) );
		} catch (IOException e) {
			throw new ModelsFetcherException("", e);
		}
	}

	private LinkedList<Invariant> loadInvariantsFromFile(File inv_file, boolean loadEnter, Collection<String> textualInvariants) throws IOException {
		
		HashSet<String> textualForm = new HashSet<String>();
		if ( textualInvariants != null ){
			textualForm.addAll(textualInvariants);
		}
		
		// Read the invariant file
		PptMap ppts = FileIO.read_serialized_pptmap (inv_file, true );

		//Yoav: make sure we have unique invariants
		InvariantFilters fi = InvariantFilters.defaultFilters();
		
		//Set<String> allInvariantsStr = new HashSet<String>();
//		LinkedList<Invariant> allInvariants = new LinkedList<Invariant>();
		LinkedList<Invariant> activeInvariants = new LinkedList<Invariant>();
		for (PptTopLevel ppt : ppts.all_ppts())
			for (Iterator<PptSlice> i = ppt.views_iterator(); i.hasNext(); ) {
				PptSlice slice = i.next();
				for (Invariant inv : slice.invs) {
					if (doConf &&
							inv.getConfidence() < confidence_limit){
						LOGGER.fine("Invariant ignored for confidence limit "+inv.ppt.name()+" "+inv.format()+" "+inv.getConfidence());
						continue;
					}
//inv.isAllPrestate();
					InvariantFilter keep = fi.shouldKeep(inv);
					
					if ( doTextualFilter ){
						if ( ! textualForm.contains(inv.format()) ){
							LOGGER.fine("Invariant ignored for text filter "+inv.ppt.name()+" "+inv.format());
							continue;
						}
					}
					
					
//					System.out.println(doFilter+" "+keep);
					if (doFilter && keep != null) {
						LOGGER.fine("Invariant ignored for daikon filter "+inv.ppt.name()+" "+inv.format()+" "+keep.getDescription());
//						System.out.println("Ignored for filter: "+keep.getDescription());
//						 System.out.printf ("inv ignored (filter): %s:%s %s\n",
//						                     inv.ppt.name(), inv.format(), keep.getDescription());
						continue;
					}
					
					if ( skipArrays && inv.toString().contains("[") ){
						continue;
					}
					
					if ( skipShift && inv.toString().contains(">>") ){ //to fix a daikon bug, seems the option is not valid : daikon.inv.binary.twoScalar.NumericInt.ShiftZero.enabled
						continue;
					}
					
//					if ( doFilter && keep.shouldDiscard(inv) ){
//						continue;
//					}
					
					boolean isExit = inv.ppt.parent.name.contains(" :::EXIT");
					
					if ( loadEnter ){
						if ( isExit ){
							continue;
						}
					} else { //loadExit
						if ( ! isExit ){
							continue;
						}
					}
					
					activeInvariants.add(inv);

					//String n = invariant2str(ppt, inv);
					//if (!allInvariants.contains(inv) && allInvariantsStr.contains(n)) throw new Daikon.TerminationMessage("Two invariants have the same ppt.name+inv.rep:"+n);
//					allInvariants.add(inv);
					//allInvariantsStr.add(n);
				}
			}
		
		return activeInvariants;
	}
	
	public static void main(String args[]){
		DaikonFileModelsFetcher mf = new DaikonFileModelsFetcher();
		mf.doFilter=false;
		for ( String arg : args ){
			try {
				LinkedList<Invariant> invariants = mf.loadInvariantsFromFile(new File(arg), true, null);
				for ( Invariant inv : invariants ){
					System.out.println(inv.format());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
