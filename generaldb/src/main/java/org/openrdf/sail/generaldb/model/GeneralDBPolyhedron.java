package org.openrdf.sail.generaldb.model;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.evaluation.function.spatial.GeometryDatatype;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 *
 */
public class GeneralDBPolyhedron extends RdbmsValue {

	private static final long serialVersionUID = -7751266742783048766L;
	
	/**
	 * The string representation of this value. The representation
	 * may be one of the Constraint-based, WKT, or GML encodings.
	 * 
	 * @see #setPolyhedronStringRep(StrabonPolyhedron)
	 */
	private String polyhedronStringRep;
	
	/**
	 * The underlying strabon polyhedron
	 */
	private StrabonPolyhedron polyhedron;
	
	/**
	 * The datatype of the polyhedron
	 */
	private URI datatype;
	
	/**
	 * CONSTRUCTOR
	 */
	public GeneralDBPolyhedron(Number id, Integer version, URI datatype, byte[] polyhedron, int srid) throws IOException, ClassNotFoundException {
		super(id, version);

		try {
			this.polyhedron = new StrabonPolyhedron(polyhedron, srid, GeometryDatatype.fromString(datatype.stringValue()));
			
		} catch (ParseException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		
		setPolyhedronStringRep(this.polyhedron);
		this.datatype = datatype;
	}

	/**
	 * this method is called from the method:
	 * {@link GeneralDBValueFactory.getRdbmsPolyhedron}
	 * for SELECT constructs 
	 * 
	 * @param datatype
	 * @param polyhedron
	 * @param srid
	 */
	public GeneralDBPolyhedron(URI datatype, byte[] polyhedron, int srid) throws IOException, ClassNotFoundException {
		//set null id and version in the RdbmsSValue
		super(null, null);
		
		try {
			this.polyhedron = new StrabonPolyhedron(polyhedron, srid, GeometryDatatype.fromString(datatype.stringValue()));
			
		} catch (ParseException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		
		setPolyhedronStringRep(this.polyhedron);
		this.datatype = datatype;
	}

	public String getPolyhedronStringRep() {
		return polyhedronStringRep;
	}

	public void setPolyhedronStringRep(StrabonPolyhedron polyhedron) throws IOException, ClassNotFoundException {
		if (StrabonPolyhedron.EnableConstraintRepresentation) {
			this.polyhedronStringRep = polyhedron.toConstraints();	
			
		} else {
			this.polyhedronStringRep = polyhedron.stringValue();
		}		
	}

	public URI getDatatype() {
		return datatype;
	}

	public void setDatatype(URI datatype) {
		this.datatype = datatype;
	}

	public StrabonPolyhedron getPolyhedron() {
		return polyhedron;
	}


	public void setPolyhedron(StrabonPolyhedron polyhedron) {
		this.polyhedron = polyhedron;
	}


	public String stringValue() {
		// TODO FIXME we miss GML here
		return WKTHelper.createWKT(this.polyhedronStringRep, 
								   this.getPolyhedron().getGeometry().getSRID(), 
								   String.valueOf(datatype));
	}

	@Override
	public String toString() {
		// TODO FIXME we miss GML here
		return "\"" + WKTHelper.createWKT(this.polyhedronStringRep, 
				   						  this.getPolyhedron().getGeometry().getSRID(), 
				   						  String.valueOf(datatype)) +"\"" + "^^<" + String.valueOf(datatype) + ">";
	}

	@Override
	public int hashCode() {
		return polyhedronStringRep.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		if(other instanceof GeneralDBPolyhedron)
		{
			if (((GeneralDBPolyhedron) other).getPolyhedron().equals(this.getPolyhedron()))
			{
				return true;
			}

		}
		return false;
	}

}