/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.generaldb.managers.base.ManagerBase;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.iteration.RdbmsBindingIteration;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.generaldb.schema.Batch;
import org.openrdf.sail.generaldb.schema.HashBatch;
import org.openrdf.sail.generaldb.schema.HashTable;
import org.openrdf.sail.generaldb.schema.IdSequence;

/**
 * 
 * @author James Leigh
 */
public class HashManager extends ManagerBase {

	public static HashManager instance;
	
	private static final boolean USE_THREAD = true;
	
	private Logger logger = LoggerFactory.getLogger(HashManager.class);

	private HashTable table;

	private Map<Long, Number> ids;

	//TODO my addition
	private Map<Long, Number> geoIds;

	private AtomicInteger version = new AtomicInteger();

	private BNodeManager bnodes;

	private UriManager uris;

	private LiteralManager literals;

	private Thread lookupThread;

	//FIXME
	public Object assignIds = new Object();

	public Object working = new Object();

	private BlockingQueue<RdbmsValue> queue;

	private IdSequence idseq;

	Exception exc;

	RdbmsValue closeSignal = new RdbmsValue() {

		private static final long serialVersionUID = -2211413309013905712L;

		public String stringValue() {
			return null;
		}
	};

	private RdbmsValue taken;

	public HashManager() {
		instance = this;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
		ids = new HashMap<Long, Number>(table.getBatchSize());
	}

	public void setBNodeManager(BNodeManager bnodeTable) {
		this.bnodes = bnodeTable;
	}

	public void setLiteralManager(LiteralManager literalTable) {
		this.literals = literalTable;
	}

	public void setUriManager(UriManager uriTable) {
		this.uris = uriTable;
	}

	public void setIdSequence(IdSequence idseq) {
		this.idseq = idseq;
	}

	
	public void init() {
		queue = new ArrayBlockingQueue<RdbmsValue>(table.getBatchSize());
		if (USE_THREAD) {
			lookupThread = new Thread(new Runnable() {

				public void run()  {
					try {
						lookupThread(working, assignIds);
					}
					catch (Exception e) {
						exc = e;
						logger.error(e.toString(), e);
						//konstantina-do sth to propagate the exception!
						//throw new RuntimeException(e);
						

					}
				}
			}, "id-lookup");
			lookupThread.start();


	
		}
	}

	@Override
	public void close()
	throws SQLException
	{
		if (queue == null)
			return;
		try {
			flush();
			if (lookupThread != null) {
				queue.put(closeSignal);
				lookupThread.join();
			}
		}
		catch (InterruptedException e) {
			logger.warn(e.toString(), e);
		}
		super.close();
		table.close();
	}

	public int getIdVersion() {
		return version.intValue();
	}

	public void optimize()
	throws SQLException
	{
		table.optimize();
	}

	public boolean removedStatements(int count, String condition)
	throws SQLException
	{
		if (table.expungeRemovedStatements(count, condition)) {
			version.addAndGet(1);
			return true;
		}
		return false;
	}

	public void lookupId(RdbmsValue value)
	throws InterruptedException
	{
		queue.put(value);
	}
	/*
	public void assignId(RdbmsValue value, int version)
	throws InterruptedException, SQLException
	{
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		synchronized (assignIds) {
			throwException();
			if (value.isExpired(version)) {
				Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
				//FIXME careful here!! my additions!!!

				//System.out.println("taken not null");
				if((value instanceof RdbmsLiteral))
				{
					System.out.println("In assignId, found a literal "+value.toString());
					URI datatype = ((RdbmsLiteral)value).getDatatype();
					if(!XMLDatatypeUtil.isGeoSpatialDatatype(datatype))
					{
						System.out.println("In assignId, no geospatial");
						//the only original
						//if (taken != null) {
						values.add(value);
						assignIds(values, map);

					}
					else
					{
						System.out.println("In assignId, geospatial found, therefore no additions");
					}
				}
			}

		}
		for (RdbmsValue v : values) {
			insert(v);
		}
	}
	 */
	//backing up

	//original version after all. new one wasn't needed
	public void assignId(RdbmsValue value, int version)
	throws InterruptedException, SQLException
	{
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		synchronized (assignIds) {
			throwException();
			if (value.isExpired(version)) {
				Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
				values.add(value);
				assignIds(values, map);
			}
		}
		for (RdbmsValue v : values) {
			insert(v);
		}
	}

	/*
	@Override
	public void flush()
	throws SQLException, InterruptedException
	{
		throwException();
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
		RdbmsValue taken = queue.poll();
		while (taken != null) {
			values.clear();
			values.add(taken);
			synchronized (assignIds) {
				//System.out.println("invoking assignIds");
				assignIds(values, map);
			}
			for (RdbmsValue v : values) {
				insert(v);
			}
			taken = queue.poll();
			if (taken == closeSignal) {
				queue.add(taken);
				taken = null;
			}
		}
		synchronized (working) {
			values.clear();
			synchronized (assignIds) {
				throwException();



				//System.out.println("taken not null");
				if((this.taken instanceof RdbmsLiteral))
				{
					System.out.println("In flush, found a literal "+this.taken.toString());
					URI datatype = ((RdbmsLiteral)this.taken).getDatatype();
					if(!XMLDatatypeUtil.isGeoSpatialDatatype(datatype))
					{
						System.out.println("In flush, no geospatial");
						//the only original
						//if (taken != null) {
						values.add(this.taken);
						assignIds(values, map);

					}
					else
					{
						System.out.println("In flush, geospatial found, therefore no additions");
					}
					this.taken = null;
				}
			}
			for (RdbmsValue v : values) {
				insert(v);
			}
		}
		super.flush();
	}
	 */
	
	//original version after all. new one wasn't needed
	@Override
	public void flush()
	throws SQLException, InterruptedException
	{
		throwException();
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
		RdbmsValue taken = queue.poll();
		while (taken != null) {
			values.clear();
			values.add(taken);
			synchronized (assignIds) {
				//System.out.println("invoking assignIds");
				assignIds(values, map);
			}
			for (RdbmsValue v : values) {
				insert(v);
			}
			taken = queue.poll();
			if (taken == closeSignal) {
				queue.add(taken);
				taken = null;
			}
		}
		synchronized (working) {
			values.clear();
			synchronized (assignIds) {
				if (this.taken != null && this.taken != closeSignal) {
					//System.out.println("invoking assignIds");
					values.add(this.taken);
					assignIds(values, map);
					this.taken = null;
				}
			}
			for (RdbmsValue v : values) {
				insert(v);
			}
		}
		super.flush();
	}


	public String getExpungeCondition() {
		StringBuilder sb = new StringBuilder();
		sb.append("AND id NOT IN (SELECT id FROM ");
		sb.append(table.getName()).append(")");
		return sb.toString();
	}

	//FIXME i am not supposed to be public!!!
	//modified to use it at RdbmsTripleRepository -> insert
	public int getChunkSize() {
		return table.getSelectChunkSize();
	}

	@Override
	protected void flush(Batch batch) throws SQLException
	{
		super.flush(batch);
		synchronized (assignIds) {
			synchronized (ids) {
				HashBatch hb = (HashBatch)batch;
				for (Long hash : hb.getHashes()) {
					ids.remove(hash);
				}
			}
		}
	}

	//the original method. the one commented below was used by me previously
	void lookupThread(Object working, Object assignIds)
	throws InterruptedException, NullPointerException,SQLException
	{
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
		taken = queue.take();
		for (; taken != closeSignal; taken = queue.take()) {
			synchronized (working) {
				values.clear();
				synchronized (assignIds) {
					if (taken != null) {
						values.add(taken);
						assignIds(values, map);
						taken = null;
					}
				}
				for (RdbmsValue v : values) {
					insert(v);
				}
			}
		}
	}
	/*
	void lookupThread(Object working, Object assignIds)
	throws InterruptedException, SQLException
	{
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
		taken = queue.take();
		for (; taken != closeSignal; taken = queue.take()) {
			synchronized (working) {
				values.clear();
				synchronized (assignIds) {
					//FIXME VERY CAREFUL HERE!!MY ADDITIONS!!
					if(taken!=null)
					{
						//System.out.println("taken not null");
						if((taken instanceof RdbmsLiteral))
						{
							System.out.println("taken a literal "+taken.toString());
							URI datatype = ((RdbmsLiteral)taken).getDatatype();
							if(!XMLDatatypeUtil.isGeoSpatialDatatype(datatype))
							{
								System.out.println("no geospatial here");
								//the only original
								//if (taken != null) {
								values.add(taken);
								assignIds(values, map);
								taken = null;
							}
							else
							{
								System.out.println("Geospatial, therefore no additions");
							}
						}
					}
				}
				for (RdbmsValue v : values) {
					insert(v);
				}
			}
		}
	}
	 */

	//FIXME i am not supposed to be public!!!
	//modified to use it at RdbmsTripleRepository -> insert
	public void assignIds(List<RdbmsValue> values, Map<Long, Number> map)
	throws SQLException, InterruptedException
	{
		while (values.size() < getChunkSize()) {
			RdbmsValue taken = queue.poll();
			if (taken == closeSignal) {
				queue.add(taken);
				break;
			}
			if (taken == null)
				break;
			values.add(taken);
		}
		Map<Long, Number> existing = lookup(values, map);
		Iterator<RdbmsValue> iter = values.iterator();
		while (iter.hasNext()) {
			RdbmsValue value = iter.next();
			Long hash = idseq.hashOf(value);
			if (existing.get(hash) != null) {
				//TODO remove comments
				//System.out.println("whoah, diploeggrafi!!");
				// already in database
				Number id = idseq.idOf(existing.get(hash));
				value.setInternalId(id);
				value.setVersion(getIdVersion(value));
				iter.remove();
			}
			else {
				synchronized (ids) {
					if (ids.containsKey(hash)) {
						//TODO remove comments
						//System.out.println("already inserting this value");
						// already inserting this value
						Number id = ids.get(hash);
						value.setInternalId(id);
						value.setVersion(getIdVersion(value));
						iter.remove();
					}
					else {
						//TODO remove comments
						//System.out.println("new id to be inserted --> "+hash);
						// new id to be inserted
						Number id = idseq.nextId(value);
						value.setInternalId(id);
						value.setVersion(getIdVersion(value));
						ids.put(hash, id);
						// keep on list for later insert
					}
				}
			}
		}
	}

	/**
	 * FIXME obviously my addition
	 * doesn't affect the other functions in any way - just added variable GeoIds for my convenience
	 * after all it seems it is not needed
	 */
	/*
	public void assignGeoSpatialIds(RdbmsValue objectLiteral)
	throws SQLException, InterruptedException
	{
		String query = "SELECT id FROM manosgeofull2_values";
		try {
			Connection conn = triples.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);

			//Collection<ColumnVar> proj = qb.getProjections();
			RdbmsBindingIteration result = new RdbmsBindingIteration(stmt);
			//result.setProjections(proj);
			result.setBindings(bindings);
			result.setValueFactory(vf);
			result.setIdSequence(ids);
			return result;
		}
		catch (SQLException e) {
			throw new RdbmsQueryEvaluationException(e.toString() + "\n" + query, e);
		}
	}
	 */

	private Map<Long, Number> lookup(Collection<RdbmsValue> values, Map<Long, Number> map)
	throws SQLException
	{
		assert !values.isEmpty();
		assert values.size() <= getChunkSize();
		map.clear();
		for (RdbmsValue value : values) {
			map.put(idseq.hashOf(value), null);
		}
		return table.load(map);
	}

	private Integer getIdVersion(RdbmsValue value) {
		if (value instanceof RdbmsLiteral)
			return literals.getIdVersion();
		if (value instanceof RdbmsURI)
			return uris.getIdVersion();
		assert value instanceof RdbmsBNode;
		return bnodes.getIdVersion();
	}

	private void insert(RdbmsValue value)
	throws SQLException, InterruptedException,NullPointerException,IllegalArgumentException
	{
		/***********************************************/
		//FIXME den xerw kata poso boleyei edw i eisagwgi tis diaxeirisis tou neou pinaka
		//den exei kopei akoma se kommatia
		//System.out.println("I am in hash manager, "+value.toString());
		/***********************************************/
		Number id = value.getInternalId();
		table.insert(id, idseq.hashOf(value));
		if (value instanceof RdbmsLiteral) {
			//System.out.println("Ayto stelnw ston LiteralManager, "+value.toString());
			literals.insert(id, (RdbmsLiteral)value);
		}
		else if (value instanceof RdbmsURI) {
			uris.insert(id, (RdbmsURI)value);
		}
		else {
			assert value instanceof RdbmsBNode;
			bnodes.insert(id, (RdbmsBNode)value);
		}
	}

	private void throwException()
	throws SQLException
	{
		if (exc instanceof SQLException) {
			SQLException e = (SQLException)exc;
			exc = null;
			throw e;
		}
		else if (exc instanceof RuntimeException) {
			RuntimeException e = (RuntimeException)exc;
			exc = null;
			throw e;
		}
	}

}
