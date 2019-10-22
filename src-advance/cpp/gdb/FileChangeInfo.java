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
package cpp.gdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileChangeInfo {

	public static class Delta {
		int start;
		int end;
		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public int getStartModified() {
			return startModified;
		}

		public int getEndModified() {
			return endModified;
		}

		private int startModified;
		private int endModified;
		private boolean pureAddition;
		private boolean pureDeletion;
		
		public boolean isPureAddition() {
			return pureAddition;
		}

		public boolean isPureDeletion() {
			return pureDeletion;
		}

		public Delta(int start, int end, int startModified, int endModified, boolean pureAddition, boolean pureDeletion ) {
			if ( start > end ){
				throw new IllegalArgumentException();
			}
			this.start = start;
			this.end = end;
			this.startModified = startModified;
			this.endModified = endModified;
			this.pureAddition = pureAddition;
			this.pureDeletion = pureDeletion;
		}
		
		public String toString(){
			return "<"+start+":"+end+">";
		}
		
	}
	
	private File file, modifiedFile;
	
	public File getFile() {
		return file;
	}
	
	public File getModifiedFile() {
		return modifiedFile;
	}

	private List<Delta> deltas = new ArrayList<Delta>();

	private boolean removed;
	private String relativeName;
	
	public String getRelativeName() {
		return relativeName;
	}

	public FileChangeInfo(String relativeName, File file) {
		this.file = file;
		this.relativeName = relativeName;
	}
	
	public FileChangeInfo(String relativeName, File file, File modifiedFile) {
		this.file = file;
		this.modifiedFile = modifiedFile;
		this.relativeName = relativeName;
	}
	
	public void addChange( int start, int end, int startModified, int endModified, boolean pureAddition, boolean pureDeletion ){
		addChange( new Delta(start,end,startModified,endModified, pureAddition, pureDeletion) );
	}
	
	private void addChange(Delta delta) {
		deltas.add(delta);
	}

	public boolean isLineChanged( int line ){
		if ( isRemoved() ){
			return true;
		}
		
		for ( Delta delta : deltas ){
			
			if ( line >= delta.start && line <= delta.end ){
				return true;
			}
			
		}
		return false;
	}

	public void setRemoved(boolean b) {
		removed=b;
	}

	public boolean isRemoved() {
		return removed;
	}
	
	public String toString(){
		String s = "Changes for file "+file.getAbsolutePath()+" : "+deltas.toString();
		return s;
	}
	
	public List<Delta> getDeltas() {
		return deltas;
	}
}
