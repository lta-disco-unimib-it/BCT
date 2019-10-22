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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;

import conf.DBConnectionSettings;

public class MySqlDBManager implements DBManager {

	private DBConnectionSettings setting;

	/**
	 * Create a db manager using the passed settings
	 * 
	 * @param settings
	 */
	public MySqlDBManager( DBConnectionSettings settings ){
		this.setting = settings;
	}
	
	
	public void createTables() throws DataLayerException{
		
		String sql = DBScriptsUtils.getDBScriptContent("createBCTDataBase.sql");
		
		try {
			Statement statement = ConnectionDispenser.getConnection().createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			throw new DataLayerException(e);
		} catch (DBException e) {
			throw new DataLayerException(e);
		}
	}


	public void clearTables() throws DataLayerException {
		
	}
}
