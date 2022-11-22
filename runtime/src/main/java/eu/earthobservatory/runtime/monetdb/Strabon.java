/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.monetdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.openrdf.sail.monetdb.MonetDBSqlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class Strabon extends eu.earthobservatory.runtime.generaldb.Strabon {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.monetdb.Strabon.class);

	public Strabon(String databaseName, String user, String password, int port, 
			String serverName, boolean checkForLockTable) throws Exception {
		super(databaseName, user, password, port, serverName, checkForLockTable);
	}

	protected void initiate(String databaseName, String user, String password, int port, String serverName) {
		db_store = new MonetDBSqlStore();

		MonetDBSqlStore monetDB_store = (MonetDBSqlStore)db_store;
		
//		Map<String, String> properties = new HashedMap();
//		properties.put("debug", "true");
//		monetDB_store.setProperties(properties);
		
		monetDB_store.setDatabaseName(databaseName);
		monetDB_store.setUser(user);
		monetDB_store.setPassword(password);
		monetDB_store.setPortNumber(port);
		monetDB_store.setServerName(serverName);
		monetDB_store.setMaxNumberOfTripleTables(2048);
		init();
		logger.info("[Strabon] Initialization completed.");
	}


	protected void checkAndDeleteLock(String databaseName, String user, String password, int port, String serverName)
	throws SQLException, ClassNotFoundException {
		String url = "";
		try {
			logger.info("[Strabon] Cleaning...");
			Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
			url = "jdbc:monetdb://" + serverName + ":" + port + "/"
			+ databaseName + "?user=" + user + "&password=" + password;
			Connection conn = DriverManager.getConnection(url);
			java.sql.Statement st = conn.createStatement();
			// change
			ResultSet resultSet = st.executeQuery("SELECT true FROM sys._tables WHERE name='locked' AND system=false");
			if (resultSet.next()) {
				st.execute("DROP TABLE \"locked\"");
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error("SQL Exception occured. Connection URL: " + url, e);
			throw e;
		} catch (ClassNotFoundException e) {
			logger.error("Could not load monetdb jdbc driver...", e);
			throw e;
		}
	}
	
	@Override
	public void deregisterDriver() {
		try {
			logger.info("[Strabon.deregisterDriver] Deregistering JDBC driver...");
	        java.sql.Driver driver = DriverManager.getDriver("jdbc:monetdb://" + serverName + ":" + port + "/");
	        DriverManager.deregisterDriver(driver);
	        logger.info("[Strabon.deregisterDriver] JDBC driver deregistered successfully.");
	        
	    } catch (SQLException e) {
	        logger.warn("[Strabon.deregisterDriver] Could not deregister JDBC driver: {}", e.getMessage());
	    }
	}

	@Override
	protected boolean isLocked() throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Statement st = null;
		String url = "";
		
		try {
			logger.info("[Strabon] Checking for locks...");
			Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
			url = "jdbc:monetdb://" + serverName + ":" + port + "/" + databaseName + "?user=" + user + "&password=" + password;
			
			conn = DriverManager.getConnection(url);
			st = conn.createStatement();

			ResultSet resultSet = st.executeQuery("SELECT true FROM sys._tables WHERE name='locked' AND system=false");
			return resultSet.next() ? true:false;
			
		} catch (SQLException e) {
			logger.error("[Strabon.isLocked] SQL Exception occured. Connection URL is <{}>: {}", url, e.getMessage());
			throw e;
			
		} catch (ClassNotFoundException e) {
			logger.error("[Strabon.isLocked] Could not load MonetDB jdbc driver: {}", e.getMessage());
			throw e;
			
		} finally {
			// close statement and connection
			if (st != null) {
				st.close();
			}
			
			if (conn != null) {
				conn.close();
			}
		}

	}
}
