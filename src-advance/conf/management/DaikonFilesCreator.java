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
package conf.management;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class DaikonFilesCreator {

	public static void createDefaultFileIfAbsent(File confDir)
			throws ConfigurationFilesManagerException {
		try {
			File deafultPropertiesFile = new File(confDir,"default.txt");
			
			if ( ! deafultPropertiesFile.exists() ){
			
			Properties defaultP = new Properties();
			defaultP.put("daikon.Daikon.guardNulls","always");
			defaultP.put("daikon.PptTopLevel.pairwise_implications","true");
	
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqIntLessThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseIntGreaterEqual.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqFloatLessEqual.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqStringGreaterEqual.enabled","true"); 
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqFloatLessThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoString.StringLessThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqStringLessEqual.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseIntGreaterThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseFloatGreaterThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.SeqSeqIntGreaterThan.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseIntLessEqual.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseFloatLessEqual.enabled","true");
			defaultP.put("daikon.inv.binary.twoSequence.PairwiseFloatGreaterEqual.enabled","true");
			
			defaultP.put("daikon.inv.unary.sequence.OneOfFloatSequence.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.scalar.OneOfScalar.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.sequence.OneOfSequence.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.string.OneOfString.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.scalar.OneOfFloat.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.sequence.EltOneOfFloat.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			defaultP.put("daikon.inv.unary.sequence.EltOneOf.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
	
			ConfigurationFilesManager.saveProperty( defaultP, deafultPropertiesFile);
			}
		} catch (IOException e) {
			throw new ConfigurationFilesManagerException(e);
		}
	}

	public static void createEssentialsFileIfAbsent(File confDir)
			throws ConfigurationFilesManagerException {
		try {
			
			Properties essentialsP = new Properties();
	
			File essentialsFile = new File(confDir,"essentials.txt");
			if ( ! essentialsFile.exists() ){
			
			essentialsP.put("daikon.inv.filter.UnmodifiedVariableEqualityFilter.enabled","false");
	
	
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqFloatEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqFloatGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqFloatLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqIntEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqIntGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceScalar.SeqIntLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.sequenceString.MemberString.enabled","false");
			essentialsP.put("daikon.inv.binary.twoScalar.LinearBinary.enabled","false");
			essentialsP.put("daikon.inv.binary.twoScalar.LinearBinaryFloat.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseFloatEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseFloatGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseFloatGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseFloatLessEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseFloatLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseIntEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseIntGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseIntGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseIntLessEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseIntLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseLinearBinary.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.PairwiseLinearBinaryFloat.enabled","false");
			//essentialsP.put("daikon.inv.binary.twoSequence.PairwiseNumericFloat.enabled","false");
			//essentialsP.put("daikon.inv.binary.twoSequence.PairwiseNumericInt.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.Reverse.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.ReverseFloat.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqFloatEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqFloatGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqFloatGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqFloatLessEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqFloatLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqIntEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqIntGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqIntGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqIntLessEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqIntLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqStringEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqStringGreaterEqual.enabled","false"); 
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqStringGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqStringLessEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoSequence.SeqSeqStringLessThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoString.StringEqual.enabled","false");
			essentialsP.put("daikon.inv.binary.twoString.StringGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.binary.twoString.StringLessThan.enabled","false");
			essentialsP.put("daikon.inv.ternary.threeScalar.FunctionBinary.enabled","false");
			essentialsP.put("daikon.inv.ternary.threeScalar.FunctionBinaryFloat.enabled","false");
			essentialsP.put("daikon.inv.ternary.threeScalar.LinearTernary.enabled","false");
			essentialsP.put("daikon.inv.ternary.threeScalar.LinearTernaryFloat.enabled","false");
			essentialsP.put("daikon.inv.unary.scalar.Modulus.enabled","false");
			essentialsP.put("daikon.inv.unary.scalar.NonModulus.enabled","false");
			essentialsP.put("daikon.inv.unary.scalar.OneOfFloat.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.CommonFloatSequence.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.CommonSequence.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltOneOf.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltOneOfFloat.enabled","false");
			//essentialsP.put("daikon.inv.unary.sequence.EltRangeFloat.enabled","false");
			//essentialsP.put("daikon.inv.unary.sequence.EltRangeInt.enabled","false");
			
			essentialsP.put("daikon.inv.unary.sequence.EltwiseFloatEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseFloatGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseFloatGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseFloatLessEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseFloatLessThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseIntEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseIntGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseIntGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseIntLessEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.EltwiseIntLessThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.NoDuplicates.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.NoDuplicatesFloat.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.OneOfFloatSequence.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatLessEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatLessThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexFloatNonEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntGreaterEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntGreaterThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntLessEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntLessThan.enabled","false");
			essentialsP.put("daikon.inv.unary.sequence.SeqIndexIntNonEqual.enabled","false");
			essentialsP.put("daikon.inv.unary.string.OneOfString.enabled","false");
			essentialsP.put("daikon.inv.unary.stringsequence.CommonStringSequence.enabled","false");
			essentialsP.put("daikon.inv.unary.stringsequence.EltOneOfString.enabled","false");
			essentialsP.put("daikon.inv.unary.stringsequence.OneOfStringSequence.enabled","false");
	
	
			essentialsP.put("daikon.inv.unary.scalar.OneOfFloat.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.scalar.OneOfScalar.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.sequence.EltOneOf.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.sequence.EltOneOfFloat.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.sequence.OneOfFloatSequence.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.sequence.OneOfSequence.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.string.OneOfString.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.stringsequence.EltOneOfString.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
			essentialsP.put("daikon.inv.unary.stringsequence.OneOfStringSequence.size",ConfigurationFilesManager.DEFAULT_ONE_OF_SIZE);
	
			essentialsP.put("daikon.derive.binary.SequenceFloatSubscript.enabled","false");
			essentialsP.put("daikon.derive.binary.SequenceFloatSubsequence.enabled","false");
			essentialsP.put("daikon.derive.binary.SequenceScalarSubscript.enabled","false");
			essentialsP.put("daikon.derive.binary.SequenceScalarSubsequence.enabled","false");
			essentialsP.put("daikon.derive.binary.SequenceStringSubscript.enabled","false");
			essentialsP.put("daikon.derive.binary.SequenceStringSubsequence.enabled","false");
	
			essentialsP.put("daikon.Daikon.guardNulls","always");
			essentialsP.put("daikon.PptTopLevel.pairwise_implications","true");
			
			ConfigurationFilesManager.saveProperty( essentialsP, essentialsFile );
			
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationFilesManagerException(e);
		}
	}

}
