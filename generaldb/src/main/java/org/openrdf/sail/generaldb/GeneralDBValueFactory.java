/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.WritePrefReadWriteLockManager;

import java.io.IOException;
import java.sql.SQLException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.generaldb.managers.BNodeManager;
import org.openrdf.sail.generaldb.managers.LiteralManager;
import org.openrdf.sail.generaldb.managers.PredicateManager;
import org.openrdf.sail.generaldb.managers.UriManager;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.LiteralTable;
import org.openrdf.sail.generaldb.schema.ValueTable;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import com.vividsolutions.jts.io.ParseException;

import eu.earthobservatory.constants.GeoConstants;

/**
 * Provides basic value creation both for traditional values as well as values
 * with an internal id. {@link RdbmsValue}s behaviour similar to the default
 * {@link Value} implementation with the addition that they also include an
 * internal id and a version associated with that id. The internal ids should
 * not be accessed directly, but rather either through this class or the
 * corresponding manager class.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBValueFactory extends ValueFactoryBase {

	@Deprecated
	public static final String NIL_LABEL = "nil";

	private ValueFactory vf;

	private BNodeManager bnodes;

	private UriManager uris;

	private LiteralManager literals;

	private PredicateManager predicates;

	private WritePrefReadWriteLockManager lock = new WritePrefReadWriteLockManager();

	private IdSequence ids;

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void setBNodeManager(BNodeManager bnodes) {
		this.bnodes = bnodes;
	}

	public void setURIManager(UriManager uris) {
		this.uris = uris;
	}

	public void setLiteralManager(LiteralManager literals) {
		this.literals = literals;
	}

	public void setPredicateManager(PredicateManager predicates) {
		this.predicates = predicates;
	}

	public void setDelegate(ValueFactory vf) {
		this.vf = vf;
	}

	public void flush()
	throws RdbmsException
	{
		try {
			bnodes.flush();
			uris.flush();
			literals.flush();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	public RdbmsBNode createBNode(String nodeID) {
		RdbmsBNode resource = bnodes.findInCache(nodeID);
		if (resource == null) {
			try {
				BNode impl = vf.createBNode(nodeID);
				resource = new RdbmsBNode(impl);
				bnodes.cache(resource);
			}
			catch (InterruptedException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return resource;
	}

	public RdbmsLiteral createLiteral(String label) {
		return asRdbmsLiteral(vf.createLiteral(label));
	}

	public RdbmsLiteral createLiteral(String label, String language) {
		if (LiteralTable.ONLY_INSERT_LABEL)
			return createLiteral(label);
		return asRdbmsLiteral(vf.createLiteral(label, language));
	}

	public RdbmsLiteral createLiteral(String label, URI datatype) {
		if (LiteralTable.ONLY_INSERT_LABEL)
			return createLiteral(label);
		return asRdbmsLiteral(vf.createLiteral(label, datatype));
	}

	public RdbmsStatement createStatement(Resource subject, URI predicate, Value object) {
		return createStatement(subject, predicate, object, null);
	}

	public RdbmsStatement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		RdbmsResource subj = asRdbmsResource(subject);
		RdbmsURI pred = asRdbmsURI(predicate);
		RdbmsValue obj = asRdbmsValue(object);
		RdbmsResource ctx = asRdbmsResource(context);
		return new RdbmsStatement(subj, pred, obj, ctx);
	}

	public RdbmsURI createURI(String uri) {
		RdbmsURI resource = uris.findInCache(uri);
		if (resource == null) {
			try {
				URI impl = vf.createURI(uri);
				resource = new RdbmsURI(impl);
				uris.cache(resource);
			}
			catch (InterruptedException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return resource;
	}

	public RdbmsURI createURI(String namespace, String localName) {
		return createURI(namespace + localName);
	}

	public RdbmsResource getRdbmsResource(Number num, String stringValue) {
		assert stringValue != null : "Null stringValue for ID: " + num;
		Number id = ids.idOf(num);
		if (ids.isURI(id))
			return new RdbmsURI(id, uris.getIdVersion(), vf.createURI(stringValue));
		return new RdbmsBNode(id, bnodes.getIdVersion(), vf.createBNode(stringValue));
	}

	public RdbmsLiteral getRdbmsLiteral(Number num, String label, String language, String datatype) {
		Number id = ids.idOf(num);
		if (datatype == null && language == null)
			return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label));
		if (datatype == null)
			return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label, language));
		return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label, vf.createURI(datatype)));
	}

	public RdbmsResource asRdbmsResource(Resource node) {
		if (node == null)
			return null;
		if (node instanceof URI)
			return asRdbmsURI((URI)node);
		if (node instanceof RdbmsBNode) {
			try {
				bnodes.cache((RdbmsBNode)node);
				return (RdbmsBNode)node;
			}
			catch (InterruptedException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return createBNode(((BNode)node).getID());
	}

	public RdbmsURI asRdbmsURI(URI uri) {
		if (uri == null)
			return null;
		if (uri instanceof RdbmsURI) {
			try {
				uris.cache((RdbmsURI)uri);
				return (RdbmsURI)uri;
			}
			catch (InterruptedException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return createURI(uri.stringValue());
	}

	public RdbmsValue asRdbmsValue(Value value) {
		if (value == null)
			return null;
		if (value instanceof Literal)
			return asRdbmsLiteral((Literal)value);
        /*****************************************/
        if (value instanceof GeneralDBPolyhedron)
                return asRdbmsLiteral((GeneralDBPolyhedron)value);
        if (value instanceof StrabonPolyhedron)
            return asRdbmsLiteral((StrabonPolyhedron)value);
        /****************************************/
		return asRdbmsResource((Resource)value);
	}

    /****************************************************/
    public RdbmsLiteral asRdbmsLiteral(GeneralDBPolyhedron polyhedron) {
        try {
                URI wkt = new URIImpl(GeoConstants.WKT);
                RdbmsLiteral literal = new RdbmsLiteral(polyhedron.getInternalId(), polyhedron.getVersion(),new LiteralImpl(polyhedron.stringValue(), wkt));

                if (polyhedron instanceof GeneralDBPolyhedron) {
                        literals.cache(literal);
                        return (RdbmsLiteral)literal;
                }

                RdbmsLiteral lit = literals.findInCache(literal);
                
                if (lit == null) {
                        lit = new RdbmsLiteral(literal);
                        literals.cache(lit);
                }
                return lit;
        }
        catch (InterruptedException e) {
                throw new RdbmsRuntimeException(e);
        }
    }
    
    public RdbmsLiteral asRdbmsLiteral(StrabonPolyhedron polyhedron) {
        try {
                URI wkt = new URIImpl(GeoConstants.WKT);
                RdbmsLiteral literal = new RdbmsLiteral(new LiteralImpl(polyhedron.stringValue(), wkt));

                if (polyhedron instanceof StrabonPolyhedron) {
                        literals.cache(literal);
                        return (RdbmsLiteral)literal;
                }
                RdbmsLiteral lit = literals.findInCache(literal);
                if (lit == null) {
                        lit = new RdbmsLiteral(literal);
                        literals.cache(lit);
                }
                return lit;
        }
        catch (InterruptedException e) {
                throw new RdbmsRuntimeException(e);
        }
}
    /****************************************************/

	
	public RdbmsLiteral asRdbmsLiteral(Literal literal) {
		try {
			if (literal instanceof RdbmsLiteral) {
				literals.cache((RdbmsLiteral)literal);
				return (RdbmsLiteral)literal;
			}
			RdbmsLiteral lit = literals.findInCache(literal);
			if (lit == null) {
				lit = new RdbmsLiteral(literal);
				literals.cache(lit);
			}
			return lit;
		}
		catch (InterruptedException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	public RdbmsResource[] asRdbmsResource(Resource... contexts) {
		RdbmsResource[] ctxs = new RdbmsResource[contexts.length];
		for (int i = 0; i < ctxs.length; i++) {
			ctxs[i] = asRdbmsResource(contexts[i]);
		}
		return ctxs;
	}

	public RdbmsStatement asRdbmsStatement(Statement stmt) {
		if (stmt instanceof RdbmsStatement)
			return (RdbmsStatement)stmt;
		Resource s = stmt.getSubject();
		URI p = stmt.getPredicate();
		Value o = stmt.getObject();
		Resource c = stmt.getContext();
		return createStatement(s, p, o, c);
	}

	public Number getInternalId(Value r)
	throws RdbmsException
	{
		try {
			if (r == null)
				return ValueTable.NIL_ID;
			RdbmsValue value = asRdbmsValue(r);
			if (value instanceof RdbmsURI)
				return uris.getInternalId((RdbmsURI)value);
			if (value instanceof RdbmsBNode)
				return bnodes.getInternalId((RdbmsBNode)value);
			return literals.getInternalId((RdbmsLiteral)value);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	public Number getPredicateId(RdbmsURI predicate)
	throws RdbmsException
	{
		try {
			return predicates.getIdOfPredicate(predicate);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	public Lock getIdReadLock() throws InterruptedException {
		return lock.getReadLock();
	}

	public Lock tryIdWriteLock() {
		return lock.tryWriteLock();
	}

	public Lock getIdWriteLock() throws InterruptedException {
		return lock.getWriteLock();
	}

	/**
	 * FIXME my addition
	 * @throws ParseException 
	 */
	public GeneralDBPolyhedron getRdbmsPolyhedron(Number num, String datatype, byte[] wkb, int srid)  {

		Number id = ids.idOf(num);
		try {
			if(wkb != null) {
				return new GeneralDBPolyhedron(id, literals.getIdVersion(), vf.createURI(datatype), wkb, srid);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This function is called only for SELECT constructs, thus we do not create an id
	 * for the resulting geometry because we don't push it in the database since
	 * there is small possibility to meet this geometry in the future.
	 * {@link PostGISBindingIteration.createWellKnownTextGeoValueForSelectConstructs},
	 * {@link PostGISBindingIteration.createWellKnownTextLiteralGeoValueForSelectConstructs},
	 * {@link and MonetDBBindingIteration.createWellKnownTextGeoValueForSelectConstructs},
	 * {@link and MonetDBBindingIteration.createWellKnownTextLiteralGeoValueForSelectConstructs}
	 * @param datatype
	 * @param wkb
	 * @param srid
	 * @return
	 */
	public GeneralDBPolyhedron getRdbmsPolyhedron(String datatype, byte[] wkb, int srid)  {

		try {
			if(wkb != null) {
				return new GeneralDBPolyhedron(vf.createURI(datatype), wkb, srid);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
