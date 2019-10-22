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
package database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import automata.fsa.FiniteStateAutomaton;

public class Efsa {
	
	static int idEFSA;
	static Blob efsa;
	
	//FK
	static int idMethod = 0;
	
	public Efsa(){
	}
	
	public static void insert(int idMethod, FiniteStateAutomaton efsa) throws DataLayerException {
		
		try {
			// Serialize to a byte array
	        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
	        ObjectOutputStream oos = new ObjectOutputStream(bos) ;
	        oos.writeObject(efsa);
	        oos.close();
	    
	        // Get the bytes of the serialized object
	        SerialBlob efsmBlob = new SerialBlob(bos.toByteArray());
	        
			
			
			PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement(
			"INSERT INTO efsa (efsa, method_idMethod) VALUES(?,?)");
			stmt.setBlob(1, efsmBlob);
			stmt.setInt(2, idMethod);
			stmt.execute();
			
			ResultSet rs = stmt.getGeneratedKeys();
			if (! rs.first()) throw new DataLayerException("Unable to insert FSA model for " + idMethod); 
			idEFSA = rs.getInt(1); 

			//Log.getInstance().debug("Project " + name + " inserted");
			stmt.close();
			
		} catch (SQLException e) {
			throw new DataLayerException(e.getMessage());
		} catch (DBException e) {
			throw new DataLayerException(e.getMessage());
		} catch (IOException e) {
			throw new DataLayerException(e.getMessage());
		}	
	}
}