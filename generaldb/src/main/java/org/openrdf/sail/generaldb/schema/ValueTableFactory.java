
package org.openrdf.sail.generaldb.schema;



import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.generaldb.schema.TripleTable;
import org.openrdf.sail.generaldb.schema.IdSequence;

import static java.sql.Types.BIGINT;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.VARCHAR;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * Factory class used to create or load the database tables.
 * 
 * @author Initial rdbms version: James Leigh
 * @author generaldb version: Manos Karpathiotakis
 */
public class ValueTableFactory {

	private static final int VCS = 127;

	private static final int VCL = 255;

	public static final boolean INDEX_VALUES = false;

	protected static final String LANGUAGES = "LANGUAGES";

	protected static final String NAMESPACES = "NAMESPACE_PREFIXES";

	protected static final String RESOURCES = "RESOURCES";

	protected static final String BNODE_VALUES = "BNODE_VALUES";

	protected static final String URI_VALUES = "URI_VALUES";

	protected static final String LURI_VALUES = "LONG_URI_VALUES";

	protected static final String LBS = "LABEL_VALUES";

	protected static final String LLBS = "LONG_LABEL_VALUES";

	protected static final String LANGS = "LANGUAGE_VALUES";

	protected static final String DTS = "DATATYPE_VALUES";

	protected static final String NUM_VALUES = "NUMERIC_VALUES";

	protected static final String TIMES = "DATETIME_VALUES";

	protected static final String HASH_TABLE = "HASH_VALUES";

	private TableFactory factory;

	private IdSequence ids;

	private boolean sequenced;

	public ValueTableFactory(TableFactory factory) {
		super();
		this.factory = factory;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void setSequenced(boolean sequenced) {
		this.sequenced = sequenced;
	}

	public HashTable createHashTable(Connection conn, BlockingQueue<Batch> queue)
		throws SQLException
	{
		ValueTable table = newValueTable();
		table.setRdbmsTable(createTable(conn, HASH_TABLE));
		// table.setTemporaryTable(factory.createTemporaryTable(conn, "INSERT_" +
		// HASH_TABLE));
		initValueTable(table, queue, BIGINT, -1, true);
		HashTable hashTable = newHashtable(table);
		hashTable.init();
		return hashTable;
	}

	public NamespacesTable createNamespacesTable(Connection conn) {
		return new NamespacesTable(createTable(conn, NAMESPACES));
	}

	public BNodeTable createBNodeTable(Connection conn, BlockingQueue<Batch> queue)
		throws SQLException
	{
		ValueTable table = createValueTable(conn, queue, BNODE_VALUES, VARCHAR, VCS);
		return new BNodeTable(table);
	}

	public URITable createURITable(Connection conn, BlockingQueue<Batch> queue)
		throws SQLException
	{
		ValueTable shorter = createValueTable(conn, queue, URI_VALUES, VARCHAR, VCL);
		ValueTable longer = createValueTable(conn, queue, LURI_VALUES, LONGVARCHAR);
		return new URITable(shorter, longer);
	}

	public LiteralTable createLiteralTable(Connection conn, BlockingQueue<Batch> queue)
		throws SQLException
	{
		ValueTable lbs = createValueTable(conn, queue, LBS, VARCHAR, VCL);
		ValueTable llbs = createValueTable(conn, queue, LLBS, LONGVARCHAR);
		ValueTable lgs = createValueTable(conn, queue, LANGS, VARCHAR, VCS);
		ValueTable dt = createValueTable(conn, queue, DTS, VARCHAR, VCL);
		ValueTable num = createValueTable(conn, queue, NUM_VALUES, DOUBLE);
		ValueTable dateTime = createValueTable(conn, queue, TIMES, BIGINT);
		LiteralTable literals = new LiteralTable();
		literals.setLabelTable(lbs);
		literals.setLongLabelTable(llbs);
		literals.setLanguageTable(lgs);
		literals.setDatatypeTable(dt);
		literals.setNumericTable(num);
		literals.setDateTimeTable(dateTime);
		
		/****************************************************************/
		//TODO
		GeoValueTable myAddition = createGeoValueTable(conn,queue,"geo_values",VARCHAR,VCL);
		literals.setGeoSpatialTable(myAddition);
		return literals;
	}

	public TripleTable createTripleTable(Connection conn, String tableName) {
		RdbmsTable table = createTable(conn, tableName);
		return new TripleTable(table);
	}

	protected RdbmsTable createTable(Connection conn, String name) {
		return factory.createTable(conn, name);
	}

	protected ValueTable createValueTable(Connection conn, BlockingQueue<Batch> queue, String name, int sqlType)
		throws SQLException
	{
		return createValueTable(conn, queue, name, sqlType, -1);
	}

	protected ValueTable createValueTable(Connection conn, BlockingQueue<Batch> queue, String name,
			int sqlType, int length)
		throws SQLException
	{
		ValueTable table = newValueTable();
		table.setRdbmsTable(createTable(conn, name));
		if (!sequenced) {
			table.setTemporaryTable(factory.createTemporaryTable(conn, "INSERT_" + name));
		}
		if ( name.equals(TIMES) )
			initValueTable(table, queue, sqlType, length, true);
		else
		initValueTable(table, queue, sqlType, length, INDEX_VALUES);
		return table;
	}

	
	/***********************************************************************************************************/
	//my additions
	//FIXME
	//highly optimistic
	protected GeoValueTable createGeoValueTable(Connection conn, BlockingQueue<Batch> queue, String name,
			int sqlType, int length)
		throws SQLException
	{
		//System.out.println("Create GeoValueTable!!");
		GeoValueTable table = newGeoValueTable();
		
		table.setRdbmsTable(createTable(conn, name));
	
		if (!sequenced) {
			table.setTemporaryTable(factory.createTemporaryTable(conn, "INSERT_" + name));
		}
		
		initGeoValueTable(table, queue, sqlType, length, INDEX_VALUES);
		
		return table;
	}
	
	protected GeoValueTable newGeoValueTable() {
		return new GeoValueTable();
	}
	
	private void initGeoValueTable(GeoValueTable table, BlockingQueue<Batch> queue, int sqlType, int length,
			boolean indexValues)
		throws SQLException
	{
		table.setQueue(queue);
		table.setSqlType(sqlType);
		table.setIdType(ids.getJdbcIdType());
		table.setLength(length);
		table.setIndexingValues(indexValues);
		table.initialize();
	}
	/***********************************************************************************************************/
	protected HashTable newHashtable(ValueTable table) {
		return new HashTable(table);
	}

	protected ValueTable newValueTable() {
		return new ValueTable();
	}

	private void initValueTable(ValueTable table, BlockingQueue<Batch> queue, int sqlType, int length,
			boolean indexValues)
		throws SQLException
	{
		table.setQueue(queue);
		table.setSqlType(sqlType);
		table.setIdType(ids.getJdbcIdType());
		table.setLength(length);
		table.setIndexingValues(indexValues);
		table.initialize();
	}
}
