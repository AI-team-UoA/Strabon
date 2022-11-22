/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb.util;

import info.aduna.concurrent.locks.Lock;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.openrdf.sail.SailLockedException;
import org.openrdf.sail.rdbms.util.DatabaseLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Leigh
 */
public class MonetDBLockManager extends DatabaseLockManager {

	private static final String CREATE_LOCKED = "CREATE TABLE \"locked\" ( process VARCHAR(128) )";

	private static final String INSERT = "INSERT INTO \"locked\" VALUES ('";

	private static final String SELECT = "SELECT process FROM \"locked\"";

	private static final String DROP = "DROP TABLE \"locked\"";

	Logger logger = LoggerFactory.getLogger(DatabaseLockManager.class);

	private DataSource ds;

	private String user;

	private String password;

	public MonetDBLockManager(DataSource ds) {
		super(ds);
		this.ds = ds;
	}

	public MonetDBLockManager(DataSource ds, String user, String password) {
		super(ds, user, password);
		this.ds = ds;
		this.user = user;
		this.password = password;
	}

	public String getLocation() {
		try {
			// Both BasicDataSource and MysqlDataSource have getUrl
			Method getUrl = ds.getClass().getMethod("getUrl");
			return getUrl.invoke(ds).toString();
		} catch (Exception e) {
			// getUrl cannot be accessed
		}
		return ds.toString();
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isLocked() {
		try {
			Connection con = getConnection();
			try {
				Statement st = con.createStatement();
				try {
					ResultSet rs = st.executeQuery(SELECT);
					try {
						return rs.next();
					}
					finally {
						rs.close();
					}
				}
				finally {
					st.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return false;
		}
	}

	public Lock tryLock() {
		Lock lock = null;
		try {
			Connection con = getConnection();
			try {
				Statement st = con.createStatement();
				try {
					st.execute(CREATE_LOCKED);
					lock = createLock();
					st.execute(INSERT + getProcessName() + "')");
					return lock;
				}
				finally {
					st.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			if (lock != null) {
				lock.release();
			}
			return null;
		}
	}

	public Lock lockOrFail()
		throws SailLockedException
	{
		Lock lock = tryLock();
		if (lock != null) {
			return lock;
		}

		String requestedBy = getProcessName();
		String lockedBy = getLockedBy();
		if (lockedBy != null) {
			throw new SailLockedException(lockedBy, requestedBy, this);
		}

		lock = tryLock();
		if (lock != null) {
			return lock;
		}

		throw new SailLockedException(requestedBy);
	}

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	public boolean revokeLock() {
		try {
			Connection con = getConnection();
			try {
				Statement st = con.createStatement();
				try {
					st.execute(DROP);
					return true;
				}
				finally {
					st.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return false;
		}
	}

	private Connection getConnection()
		throws SQLException
	{
		if (user == null) {
			return ds.getConnection();
		}

		return ds.getConnection(user, password);
	}

	private String getLockedBy() {
		try {
			Connection con = getConnection();
			try {
				Statement st = con.createStatement();
				try {
					ResultSet rs = st.executeQuery(SELECT);
					try {
						if (!rs.next()) {
							return null;
						}
						return rs.getString(1);
					}
					finally {
						rs.close();
					}
				}
				finally {
					st.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return null;
		}
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	private Lock createLock() {
		return new Lock() {

			private boolean active = true;

			public boolean isActive() {
				return active;
			}

			public void release() {
				active = false;
				try {
					Connection con = getConnection();
					try {
						Statement st = con.createStatement();
						try {
							st.execute(DROP);
						}
						finally {
							st.close();
						}
					}
					finally {
						con.close();
					}
				}
				catch (SQLException exc) {
					logger.error(exc.toString(), exc);
				}
			}

			@Override
			protected void finalize()
				throws Throwable
			{
				if (active) {
					release();
				}
				super.finalize();
			}
		};
	}
}