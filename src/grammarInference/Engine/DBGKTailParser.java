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
package grammarInference.Engine;

import grammarInference.Record.Symbol;
import grammarInference.Record.Trace;
import grammarInference.Record.TraceParser;
import grammarInference.Record.VectorTrace;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import database.ConnectionDispenser;
import database.DBException;
import database.DataLayerException;

//FIXME: it's in the correct package?
public class DBGKTailParser implements TraceParser {
	
	int traceMethod;

	public DBGKTailParser(int method) {
		traceMethod = method;
	}

	public Iterator getTraceIterator() {
		
		Iterator it = null;
		try {
			it = getTraces();
		} catch (DataLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return it;
	}
	
	private Iterator getTraces() throws DataLayerException {
		ArrayList al = new ArrayList();
		
		try {
			PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement(
				"SELECT idGKTailInteractionTrace FROM gktailinteractiontrace WHERE method_idMethod = ? ");
			stmt.setInt(1, traceMethod);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				al.add(readTrace(rs.getInt("idGKTailInteractionTrace")));
			}
			
			stmt.close();
		} catch (SQLException e) {
			throw new DataLayerException(e.getMessage());
		} catch (DBException e) {
			throw new DataLayerException(e.getMessage());
		}
		System.out.println("TRACES " + al);
		return al.iterator();
	}

	public Trace readTrace(int idTrace) throws DataLayerException {
		
		Trace trace = new VectorTrace();
		
		try {
			PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement(
				"SELECT method_IdMethod, marker FROM gktailmethodcall " +
				"WHERE gktailinteractiontrace_idGKTailInteractionTrace = ? ");
			stmt.setInt(1, idTrace);
			ResultSet rs = stmt.executeQuery();
			
			if (!rs.first()) {		
				trace.addSymbol("");
				return trace;
			}
			rs.beforeFirst();
			while (rs.next()) {
				System.out.println("TRACE READ " + rs.getString("marker") + getMethodName(rs.getInt("method_IdMethod")));
				trace.addSymbol(rs.getString("marker") + getMethodName(rs.getInt("method_IdMethod")));
			}

			stmt.close();
		} catch (SQLException e) {
			throw new DataLayerException(e.getMessage());
		} catch (DBException e) {
			throw new DataLayerException(e.getMessage());
		}	
		return trace;
	}
	
	private String getMethodName(int idMethod) throws DataLayerException {
		
		String methodName = "";
		
		try {
			PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement(
				"SELECT methodDeclaration FROM method, gktailmethodcall " +
				"WHERE method_idMethod = idMethod AND method_idMethod = ? ");
			stmt.setInt(1, idMethod);
			ResultSet rs = stmt.executeQuery();
			if(rs.first()) {
				methodName = rs.getString("methodDeclaration");
			}else {
				throw new DataLayerException("Invalid method: " + idMethod );
			}
			stmt.close();
		} catch (SQLException e) {
			throw new DataLayerException(e.getMessage());
		} catch (DBException e) {
			throw new DataLayerException(e.getMessage());
		}
		return methodName;
	}

}