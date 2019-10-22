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
package codesurfer.slice;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import cpp.gdb.FileChangeInfo;
import cpp.gdb.FileChangeInfo.Delta;
import cpp.gdb.coverage.FileNameAndCoverageKey;
import cpp.gdb.coverage.GCovParser;

public class SuspiciousLinesFilter {

	public static Set<FileNameAndCoverageKey<Integer>> getSuspiciousLines(String coverageDataFile, String sliceFile, List<FileChangeInfo> diffs) throws IOException {
		Set<FileNameAndCoverageKey<Integer>> suspiciousLines = new HashSet<FileNameAndCoverageKey<Integer>>();
		
		Hashtable<FileNameAndCoverageKey<Integer>, Integer> coveredLines = GCovParser.<Integer>load(coverageDataFile);
		Set<FileNameAndCoverageKey<Integer>> sliceLines = CodesurferSliceParser.load(sliceFile);
		
		/* For each covered line, check if it was modified and if it is in the slice */
		for (FileNameAndCoverageKey<Integer> line : coveredLines.keySet()) {
			if (isInDiff(diffs, line.getFilePath(), line.getLineNumber())) {
				if (sliceLines.contains(line)) {
					suspiciousLines.add(line);
				}
			}
		}
		
		return suspiciousLines;
	}
	
	private static boolean isInDiff(List<FileChangeInfo> diffs, String path, int line) {
		FileChangeInfo diff = findFileChanges(diffs, path);
		
		if (diff == null) {
			return false;
		}
		
		List<Delta> deltas = diff.getDeltas();
		for (Delta delta : deltas) {
			if (line >= delta.getStartModified() && line <= delta.getEndModified()) {
				return true;
			}
		}
		
		return false;
	}

	private static FileChangeInfo findFileChanges(List<FileChangeInfo> diffs, String path) {
		for (FileChangeInfo diff : diffs) {
			if (diff.getModifiedFile().getPath().equals(path)) {
				return diff;
			}
		}
		return null;
	}
}
