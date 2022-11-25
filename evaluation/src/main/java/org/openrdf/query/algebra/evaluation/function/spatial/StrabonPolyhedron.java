/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import java.util.ArrayList;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.evaluation.util.JTSWrapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;

/**
 * A {@link StrabonPolyhedron} is a {@link Value} that is used to represent geometries.
 * Therefore, a {@link StrabonPolyhedron} wraps around the construct of an RDF {@link Value}
 * the notion of geometry. This geometry can be expressed in different kinds of
 * representations, such as linear constraints over the reals with addition
 * (Semi-linear point sets), Well-Known Text (WKT), or Geography Markup Language (GML).
 * 
 * The former kind of representation, i.e., Semi-linear point sets, was the first
 * representation to be supported by StrabonPolyhedron and now has been deprecated and
 * not supported any more. It can be enabled by setting the value for variable
 * {@link #EnableConstraintRepresentation} to <tt>true</tt>. However, this is hardly 
 * suggested and it is discouraged. 
 * 
 * The other two kinds of representation is WKT and GML which are representations
 * standardized by the Open Geospatial Consortium (OGC). Both representations can be
 * used to represent a geometry and they are enabled by default.
 * 
 * {@link StrabonPolyhedron} does not store a specific representation for a geometry. In
 * contrast, it stores the plain geometry as a byte array using a {@link Geometry} object.
 * However, {@link StrabonPolyhedron} offers constructors and methods for getting a
 * {@link StrabonPolyhedron} instance through any kind of representation and of course
 * getting a {@link StrabonPolyhedron} instance in a specific representation.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * @author Kostis Kyzirakos <kk@di.uoa.gr>
 *
 */
public class StrabonPolyhedron implements Value {

	private static final long serialVersionUID = 894529468109904724L;
	
	public static String CACHEPATH = "";
	public static String TABLE_COUNTS = "counts.bin";
	public static String TABLE_SUBJ_OBJ_TYPES = "tableProperties.bin";
	public static String TABLE_SHIFTING = "groupbys.bin";

	public static final boolean EnableConstraintRepresentation = false;

	//private static int MAX_POINTS = Integer.MAX_VALUE;//40000;//Integer.MAX_VALUE;//10000;
	
	/**
	 * Get the Java Topology Suite wrapper instance.
	 */
	private static JTSWrapper jts = JTSWrapper.getInstance();
	
	/**
	 * The underlying geometry
	 */
	private Geometry geometry;

	private GeometryDatatype datatype;
	
	/**
	 * Creates a {@link StrabonPolyhedron} instance with an empty geometry.
	 */
	public StrabonPolyhedron(GeometryDatatype datatype) {
		this.geometry = null;
		this.datatype = datatype;
	}
	
	public StrabonPolyhedron(Geometry geo, int srid, GeometryDatatype datatype) {
		this(datatype);
		this.geometry = geo;
		this.geometry.setSRID(srid);
	}
	
	/**
	 * Creates a {@link StrabonPolyhedron} instance with the given geometry.
	 * 
	 * @param geo
	 * @throws Exception
	 */
	public StrabonPolyhedron(Geometry geo, GeometryDatatype datatype) {
		this(geo, geo.getSRID(), datatype);
	}
	
	/**
	 * Creates a {@link StrabonPolyhedron} instance with a geometry given
	 * in the representation of the argument. The representation could be
	 * either in WKT or in GML. Since, we construct the {@link Geometry}
	 * object ourselves there is no way of knowing the SRID, so the
	 * constructor requires it as well. 
	 * 
	 * NOTICE: whoever creates StrabonPolyhedron objects is responsible
	 * for cleaning the representation of the geometry by removing any
	 * stRDF/GeoSPARQL specific information, such as the SRID.
	 * 
	 * @param representation
	 * @throws Exception
	 */
	public StrabonPolyhedron(String representation, int srid, GeometryDatatype datatype) throws ParseException {
		this(datatype);
		
		try {
			// try first as WKT
			geometry = jts.WKTread(representation);
			
		} catch (ParseException e) {
			try {
				// try as GML
				geometry = jts.GMLread(representation);
				
				// set datatype (to be on the safe side, when the specified datatype
				// was unknown; after all, there is no other case for GML
				if (datatype == GeometryDatatype.UNKNOWN) {
					datatype = GeometryDatatype.GML;
				}
				
			} catch (Exception e1) {
				throw new ParseException("The given WKT/GML representation is not valid.");
			}
		}
		
		// set its SRID
		geometry.setSRID(srid);
	}

	/**
	 * Creates a {@link StrabonPolyhedron} instance with a geometry represented 
	 * by the given byte array.
	 * 
	 * @param byteArray
	 * @throws ParseException
	 */
	public StrabonPolyhedron(byte[] byteArray, GeometryDatatype datatype) throws ParseException {
		this(datatype);
		this.geometry = jts.WKBread(byteArray);
	}

	/**
	 * Creates a {@link StrabonPolyhedron} instance with a geometry represented
	 * by the given byte array and sets the SRID of the geometry to the given one.
	 * 
	 * @param byteArray
	 * @param srid
	 * @throws ParseException
	 */
	public StrabonPolyhedron(byte[] byteArray, int srid, GeometryDatatype datatype) throws ParseException {
		this(byteArray, datatype);
		this.geometry.setSRID(srid);
	}
	
	public GeometryDatatype getGeometryDatatype() {
		return datatype;
	}
	
	/**
	 * Returns the string representation of the geometry of this 
	 * {@link StrabonPolyhedron} instance. The result of this method
	 * is the same to the one of method {@link #toWKT()}.
	 */
	public String stringValue() {
		switch (datatype) {
			case GML:
				return toGML();
				
			case stRDFWKT:
			case wktLiteral:
				return toWKT();
				
			default: // UNKNOWN
				return toWKT();	
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if(other instanceof StrabonPolyhedron) {
			if (((StrabonPolyhedron) other).geometry.equals(this.getGeometry())) {
				return true;
			}
		}
		return false;
	}
	
	// unused
	@Deprecated
	private StrabonPolyhedron(Geometry geo, int algorithm, int maxPoints) throws Exception {		
		if (geo.isEmpty()) {
			this.geometry = geo;
			return;
		}

		if (!EnableConstraintRepresentation) {
			this.geometry = geo;
			return;
		}

		//always returns true...
		//if (!geo.isSimple())
		//	throw new Exception("The polygon is not simple. Only simple polygons are supported.");

		if (Point.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (LineString.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (Polygon.class.isInstance(geo)) {
			//if (!geo.isValid()) {
			//	System.out.println("Non valid " + FindGeoType(geo) + " found. ("+ geo.toString() +")");
			//	geo = geo.buffer(0.0);
			//	System.out.println("Converted to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid. ("+geo.toString()+")");
			//	this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//} else {
			this.geometry = new StrabonPolyhedron((Polygon) geo, algorithm, maxPoints).geometry;
			//}
		} else if (MultiPoint.class.isInstance(geo)) {
			this.geometry = geo;
		} else if (MultiLineString.class.isInstance(geo)) {
			//throw new Exception("MultiLineString not implemented yet.");
			MultiLineString mline = (MultiLineString)geo;
			ArrayList<LineString> collection = new ArrayList<LineString>(mline.getNumGeometries());

			for (int i = 0; i < mline.getNumGeometries(); i++) {
				System.out.println("[1] " + mline.getNumGeometries());
				StrabonPolyhedron line = new StrabonPolyhedron(mline.getGeometryN(i), algorithm, maxPoints);
				System.out.println("[2] " + line.geometry.getNumGeometries());
				for (int j = 0; j < line.geometry.getNumGeometries(); j++) {
					collection.add((LineString)line.geometry.getGeometryN(j));
				}
			}

			LineString[] linecollection = new LineString[collection.size()];
			int k = 0;
			for (LineString line : collection) {
				linecollection[k] = line;
				k++;
				assert (!line.isEmpty());
			}
			this.geometry = new MultiLineString(linecollection, new GeometryFactory());
		} else if (MultiPolygon.class.isInstance(geo)) {
			//			if (!geo.isValid()) {
			////				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			////				geo = geo.buffer(0.0);
			////				
			////				Geometry[] geometries = new Geometry[geo.getNumGeometries()];
			////				for (int i = 0; i < geo.getNumGeometries(); i++) {
			////					boolean before = geo.getGeometryN(i).isValid();
			////					geometries[i] = geo.getGeometryN(i).buffer(0.0);
			////					boolean after = geometries[i].isValid();
			////					//System.out.println("Geometry " + i + " was " + (before ? "" : "not ") + "valid and now it is " + (after ? "still " : "not ") + "valid.");
			////				}			
			////				
			////				Geometry col = new GeometryCollection(geometries, new GeometryFactory()).buffer(0.0);
			////				System.out.println("Converted to a "+FindGeoType(col)+" that is "+(col.isValid() ? "" : "not ")+"valid.");
			////				this.geometry = new StrabonPolyhedron(col, algorithm, maxPoints).geometry;
			//				
			////				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			////				
			////				System.out.println("Number of geometries: " + geo.getNumGeometries());
			////				MultiPolygon multipoly = (MultiPolygon)geo;
			////				Geometry newPoly = multipoly.getGeometryN(0);
			////				
			////				for (int i = 1; i < geo.getNumGeometries(); i++) {
			////					newPoly = newPoly.union(geo.getGeometryN(i));
			////				}			
			////				
			////				newPoly.buffer(0.0);
			////				
			////				//Geometry col = new GeometryCollection(geometries, new GeometryFactory()).buffer(0.0);
			////				System.out.println("Converted to a "+FindGeoType(newPoly)+" that is "+(newPoly.isValid() ? "" : "not ")+"valid.");
			////				this.geometry = new StrabonPolyhedron(newPoly, algorithm, maxPoints).geometry;
			//				
			//				//System.out.println("Non valid " + FindGeoType(geo) + " found. (coordinates:"+geo.getCoordinates().length+")");
			//				//geo = TopologyPreservingSimplifier.simplify(geo, 0.2);
			//				while (true) {
			//					if (geo.getCoordinates().length > 300000) {
			//						geo = TopologyPreservingSimplifier.simplify(geo, 0.1);
			//						System.out.println("Simplified to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid (coordinates:"+geo.getCoordinates().length+").");
			//					}
			//					geo = geo.buffer(0.0);
			//					System.out.println("Buffered to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid (coordinates:"+geo.getCoordinates().length+").");
			//					
			//					if (geo.isValid() && (geo.getCoordinates().length < 300000))
			//						break;
			//				}								
			//				
			//				this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//				
			//				//System.out.println("Are the geometries the same? Answer: " + (geo.equals(this.geometry) ? "true" : "false"));
			//				
			//			} else {
			MultiPolygon mpoly = (MultiPolygon)geo;
			ArrayList<Polygon> collection = new ArrayList<Polygon>(mpoly.getNumGeometries());

			for (int i = 0; i < mpoly.getNumGeometries(); i++) {
				System.out.println("[1] " + mpoly.getNumGeometries());
				StrabonPolyhedron poly = new StrabonPolyhedron(mpoly.getGeometryN(i), algorithm, maxPoints);
				System.out.println("[2] " + poly.geometry.getNumGeometries());
				for (int j = 0; j < poly.geometry.getNumGeometries(); j++) {
					collection.add((Polygon)poly.geometry.getGeometryN(j));
				}
			}

			Polygon[] polycollection = new Polygon[collection.size()];
			int k = 0;
			for (Polygon polygon : collection) {
				polycollection[k] = polygon;
				k++;
				assert (!polygon.isEmpty());
			}
			this.geometry = new MultiPolygon(polycollection, new GeometryFactory());
			//			}
		} else {
			//			if (!geo.isValid()) {
			//				System.out.println("Non valid " + FindGeoType(geo) + " found.");
			//				geo = geo.buffer(0.0);
			//				System.out.println("Converted to a "+FindGeoType(geo)+" that is "+(geo.isValid() ? "" : "not ")+"valid+.");
			//				this.geometry = new StrabonPolyhedron(geo, algorithm, maxPoints).geometry;
			//			} else {
			for (int i = 0; i < geo.getNumGeometries(); i++) {
				StrabonPolyhedron smallGeo = new StrabonPolyhedron(geo.getGeometryN(i), algorithm, maxPoints);

				if (this.geometry == null) {
					this.geometry = smallGeo.geometry;
				} else {
					this.geometry.union(smallGeo.geometry);
				}
			}
			//			}
		}
	}

	/**
	 * Sets the geometry of this {@link StrabonPolyhedron} instance to
	 * the given one.
	 * 
	 * @param geometry
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * Returns the string representation of the geometry.
	 */
	public String toString() {
		return geometry.toString();
	}

	/**
	 * Returns the representation of the geometry in WKT (assumed 
	 * as the default representation in {@link StrabonPolyhedron}).
	 * 
	 * @return
	 */
	public String toText() {
		return geometry.toText();
	}

	/**
	 * Return the geometry of {@link StrabonPolyhedron} in Well-Known
	 * Binary (WKB).
	 * 
	 * This method is equivalent to {@link #toByteArray()}.
	 * 
	 * @return
	 */
	public byte[] toWKB() {
		return jts.WKBwrite(this.geometry);		
	}

	/**
	 * Return the geometry of {@link StrabonPolyhedron} as WKT.
	 * 
	 * @return
	 */
	protected String toWKT() {
		return jts.WKTwrite(this.geometry);		
	}
	
	protected String toGML() {
		return jts.GMLWrite(this.geometry);
	}

	/**
	 * Return the geometry of {@link StrabonPolyhedron} as a byte array.
	 * 
	 * This method is equivalent to {@link #toWKB()}.
	 * 
	 * @return
	 */
	public byte[] toByteArray() {
		return jts.WKBwrite(this.geometry);
	}

	/**
	 * Returns the geometry of this {@link StrabonPolyhedron} instance.
	 * 
	 * @return
	 */
	public Geometry getGeometry() {
		return this.geometry;
	}
	
	public static StrabonPolyhedron ParseBigPolyhedron(Geometry polygon, int algorithm, boolean horizontal, int maxPoints) throws Exception {
		assert (Polygon.class.isInstance(polygon) || (MultiPolygon.class.isInstance(polygon)));

		if (polygon.getCoordinates().length > maxPoints) {
			//			if (polygon.isValid()){
			//				System.out.println("Found big polyhedron. Coordinates: " + polygon.getCoordinates().length + " (valid="+polygon.isValid()+").");
			//			} else {
			//				System.out.println("Found invalid big polyhedron. Coordinates: " + polygon.getCoordinates().length + ".");
			//				//IsValidOp err = new IsValidOp(polygon);
			//				//System.out.println("Validation error: " + err.getValidationError());
			//				//new Point(new CoordinateArraySequence(new Coordinate[] {polygon.getCoordinates()[0]}), new GeometryFactory());
			//				//polygon = polygon.union(onePoint);
			//				polygon = polygon.buffer(0.0);
			//				System.out.println("After conversion, coordinates: " + polygon.getCoordinates().length + " (valid="+polygon.isValid()+").");
			//			}
			double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE, 
					maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;

			Geometry bbox = polygon.getEnvelope();
			for (int i = 0; i < bbox.getCoordinates().length; i++) {
				Coordinate c = bbox.getCoordinates()[i];
				if (c.x > maxx) maxx = c.x;
				if (c.x < minx)	minx = c.x;
				if (c.y > maxy)	maxy = c.y;
				if (c.y < miny)	miny = c.y;
			}

			Polygon firsthalf = new Polygon(new LinearRing(new CoordinateArraySequence( 
					new Coordinate[] {
							new Coordinate(minx, 										miny),
							new Coordinate(horizontal ? (minx + (maxx-minx)/2) : maxx, 	miny),
							new Coordinate(horizontal ? (minx + (maxx-minx)/2) : maxx, 	horizontal ? maxy : (miny + (maxy-miny)/2)),
							new Coordinate(minx, 										horizontal ? maxy : (miny + (maxy-miny)/2)),
							new Coordinate(minx, 										miny)}
					), new GeometryFactory()), null, new GeometryFactory());

			firsthalf.normalize();

			Polygon secondhalf = (Polygon) bbox.difference(firsthalf);
			secondhalf.normalize();

			//			double a = polygon.getArea();
			//			double b = polygon.getEnvelope().getArea();
			//			double c = firsthalf.getArea();
			//			double d = bbox.difference(firsthalf).getArea();
			//			
			//			double e = b-c-d;
			//			double f = c-d;
			//			
			//			double kk = firsthalf.difference(bbox).difference(firsthalf).getArea();
			//			
			//			boolean g = firsthalf.equals(bbox.difference(firsthalf));
			//			boolean h = firsthalf.disjoint(bbox.difference(firsthalf));
			//			boolean i = bbox.equals(firsthalf.union(bbox.difference(firsthalf)));
			//			
			//			boolean j = firsthalf.intersects(polygon);
			//			boolean k = bbox.difference(firsthalf).intersects(polygon);

			Geometry A = polygon.intersection(firsthalf);
			System.out.println("First half  : " + A.getCoordinates().length + " coordinates.");
			//Geometry B = polygon.intersection(bbox.difference(firsthalf));
			Geometry B = polygon.intersection(secondhalf);
			System.out.println("Second half : " + B.getCoordinates().length + " coordinates.");

			StrabonPolyhedron polyA = ParseBigPolyhedron(A, algorithm, !horizontal, maxPoints);			
			StrabonPolyhedron polyB = ParseBigPolyhedron(B, algorithm, !horizontal, maxPoints);

			return StrabonPolyhedron.quickUnion(polyA, polyB);
		} else {
			System.out.println("Found small polyhedron. Coordinates: " + polygon.getCoordinates().length);
			return new StrabonPolyhedron(polygon, algorithm, maxPoints);
		}
	}

	@Deprecated
	public StrabonPolyhedron(Polygon polygon, int algorithm, int maxPoints) throws Exception {
		//		if (!polygon.isSimple())
		//			throw new Exception(
		//			"The polygon is not simple. Only simple polygons are supported");

		Coordinate[] coordinates = polygon.getCoordinates();

		if (coordinates.length > maxPoints) {
			this.geometry = ParseBigPolyhedron(polygon, algorithm, true, maxPoints).geometry;
			return;
		}		

		int distinctCoordinates = 0;
		boolean fix = false;
		for (int i = 0; i <= coordinates.length - 1; i++) {
			Coordinate c1 = coordinates[i];

			if (i == (coordinates.length - 1)) {
				// eimaste sto teleutaio simeio
				if ((c1.x != coordinates[0].x) || (c1.y != coordinates[0].y)) {
					// and den einai to idio me to 1o error
					//throw new Exception("Problem in  geometry. First and last point (i="+i+") do not match (coordinates: "+coordinates.length+", isValid:"+polygon.isValid()+").");
					distinctCoordinates++;
					fix = true;
				} else 
					if ((c1.x == coordinates[i-1].x) && (c1.y == coordinates[i-1].y)) {
						//einai to idio me to proigoumeno opote den kanoume tipota giati
						//exoun hdh auksithei ta dinstinct
					} else {				
						// den einai to idio me to proigoumeno opote auksise ta distinct
						distinctCoordinates++;
					}
				continue;
			} 

			Coordinate c2 = coordinates[i+1];

			if ((c1.x != c2.x) || (c1.y != c2.y)) {
				distinctCoordinates++;
			}
		}

		//System.out.println("---\n---\n---\n---\n---\n");
		//System.out.println("--- Coordinates.length   = " + coordinates.length);
		//System.out.println("--- Distinct coordinates = " + distinctCoordinates);
		//System.out.println("---\n---\n---\n---\n---\n");

		// cgal wants counter clockwise order
		//double[][] c = new double[coordinates.length - 1][2];
		int counter = 0;
		double[][] c = new double[(fix ? distinctCoordinates : (distinctCoordinates - 1))][2];
		for (int i = 0; i <= coordinates.length - 2; i++) {
			Coordinate c1 = coordinates[i];
			Coordinate c2 = coordinates[i+1];

			if ((c1.x != c2.x) || (c1.y != c2.y)) {
				c[counter][0] = c1.x;
				c[counter][1] = c1.y;
				counter++;
			}			
		}

		if (fix) {
			c[distinctCoordinates-1][0] = coordinates[coordinates.length-1].x;
			c[distinctCoordinates-1][1] = coordinates[coordinates.length-1].y;
		}

		double start = System.nanoTime();
		//		double[][][] convexified = Polyhedron.ConvexifyPolygon(c, algorithm);
		double[][][] convexified = new double[1][2][3];		

		//		if (convexified == null) {
		//			throw new ParseGeometryException("Invalid geometry. Only simple geometries are supported.");
		//		}

		System.out.println("ConvexifyTime " + (System.nanoTime()-start));

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < convexified.length; i++) {
			double[][] convexCoordinates = convexified[i];
			for (int j = 0; j < convexCoordinates.length; j++) {
				if (convexCoordinates[j][0] > max)
					max = convexCoordinates[j][0];
				if (convexCoordinates[j][0] < min)
					min = convexCoordinates[j][0];
			}

		}

		//		String gnuPlotScript = "";
		//		
		//		for (int i = 0; i < convexified.length; i++) {
		//			double[][] convexCoordinates = convexified[i];
		//			sizes[convexCoordinates.length]++;
		//			
		//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/data-" + i + ".dat")));
		//			bw2 = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/script-" + i + ".gnuplot")));
		//			for (int j = 0; j < convexCoordinates.length; j++) {
		//				bw.write(new Double(convexCoordinates[j][0]).toString());
		//				bw.write(" ");
		//				bw.write(new Double(convexCoordinates[j][1]).toString());
		//				bw.write("\n");
		//
		//			}
		//			bw.flush();
		//			bw.close();
		//			
		//			gnuPlotScript += "'data-" + i + ".dat' with lines,";
		//			
		//			bw2.write("set terminal postscript eps color\n");
		//			bw2.write("set out '/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/geo-"+i+".eps'\n");
		//			bw2.write("set key bmargin left horizontal Right noreverse enhanced autotitles box linetype -1 linewidth 1.000\n");
		//			bw2.write("plot ["+0.95*min+":"+1.05*max+"] 'data-" + i +".dat' with lines, 'original.dat' with lines\n");
		//			bw2.flush();
		//			bw2.close();
		//		}
		//			
		//		gnuPlotScript = "plot ["+0.95*min+":"+1.05*max+"] " + gnuPlotScript.substring(0, gnuPlotScript.length()-1);
		//		gnuPlotScript = "set terminal postscript eps color\n" +
		//						"set out '/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/all.eps'\n" +
		//						"set key bmargin left horizontal Right noreverse enhanced autotitles box linetype -1 linewidth 1.000\n" + 
		//						gnuPlotScript;
		//		
		//		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/kkyzir/Desktop/Spatial data/ssg4env/geometries/gnuplot/script-all.gnuplot")));
		//		bw.write(gnuPlotScript);
		//		bw.flush();
		//		bw.close();
		//		
		//		for (int i = 0; i < convexified.length; i++) {
		//			Runtime.getRuntime().exec("gnuplot /home/kkyzir/Desktop/Spatial\\ data/ssg4env/geometries/gnuplot/script-"+i+".gnuplot");
		//		}
		//		
		//		Runtime.getRuntime().exec("gnuplot /home/kkyzir/Desktop/Spatial\\ data/ssg4env/geometries/gnuplot/script-all.gnuplot");
		//		

		//Geometry[] collection = new Geometry[convexified.length];
		Polygon[] collection = new Polygon[convexified.length];
		System.out.println("Convex parts: " + convexified.length);		
		for (int i = 0; i < convexified.length; i++) {
			GeometryFactory factory = new GeometryFactory();
			double[][] convexCoordinates = convexified[i];
			Coordinate[] jtsCoordinates = new Coordinate[convexCoordinates.length];
			for (int j = 0; j < convexCoordinates.length; j++) {
				Coordinate co = new Coordinate(convexCoordinates[j][0],
						convexCoordinates[j][1]);
				jtsCoordinates[j] = co;
			}

			CoordinateSequence points = new CoordinateArraySequence(
					jtsCoordinates);
			//System.out.println("Points: " + points.size());			
			LinearRing ring = new LinearRing(points, factory);
			Polygon poly = new Polygon(ring, null, factory);

			collection[i] = poly;
			//			if (this.geometry == null) {
			//				this.geometry = poly;
			//			} else {
			//				this.geometry = this.geometry.union(poly);
			//			}
		}

		//this.geometry = new GeometryCollection(collection, new GeometryFactory());
		//this.geometry.normalize();
		this.geometry = new MultiPolygon(collection, new GeometryFactory());
		this.geometry.normalize();
	}
	
	public String toConstraints() //throws ConversionException 
	{
		if (this.geometry.isEmpty())
			return "";

		if (!EnableConstraintRepresentation) {
			return "Constraint representation is disabled.";
		}

		//Polyhedron poly = new Polyhedron(this.geometry);
		//return poly.toConstraints();
		return "";
	}

	public static StrabonPolyhedron union(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron(A.getGeometryDatatype());

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);

		poly.geometry = A.geometry.union(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	/**
	 * Think that this computation is done in meters and there is no way of doing it
	 * in degrees, except if one calculate the corresponding transformation, which
	 * depends on the spatial reference system used!
	 * 
	 * A not so good approximation of meters for degrees:
	 * 		double meters = (degrees * 6378137 * Math.PI) / 180;
	 */
	public static StrabonPolyhedron buffer(StrabonPolyhedron A, double B) throws Exception {
		return new StrabonPolyhedron(A.geometry.buffer(B), A.getGeometry().getSRID(), A.getGeometryDatatype());
	}

	public static StrabonPolyhedron envelope(StrabonPolyhedron A) throws Exception {
		return new StrabonPolyhedron(A.geometry.getEnvelope(), A.getGeometry().getSRID(), A.getGeometryDatatype());
	}

	public static StrabonPolyhedron convexHull(StrabonPolyhedron A) throws Exception {
		return new StrabonPolyhedron(A.getGeometry().convexHull(), A.getGeometry().getSRID(), A.getGeometryDatatype());
	}

	public static StrabonPolyhedron boundary(StrabonPolyhedron A) throws Exception {
		return new StrabonPolyhedron(A.geometry.getBoundary(), A.getGeometry().getSRID(), A.getGeometryDatatype());
	}

	public static StrabonPolyhedron intersection(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		Geometry geo = A.geometry.intersection(x);
		return new StrabonPolyhedron(geo, targetSRID, A.getGeometryDatatype());
	}

	public static StrabonPolyhedron difference(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron(A.getGeometryDatatype());

		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);

		poly.geometry = A.geometry.difference(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	public static StrabonPolyhedron symDifference(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		StrabonPolyhedron poly = new StrabonPolyhedron(A.getGeometryDatatype());
		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		poly.geometry = A.geometry.symDifference(x);
		poly.geometry.setSRID(targetSRID);
		return poly;
	}

	public static double area(StrabonPolyhedron A) throws Exception {
		return A.geometry.getArea();
	}

	public static double distance(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		int targetSRID = A.getGeometry().getSRID();
		int sourceSRID = B.getGeometry().getSRID();
		Geometry x = JTSWrapper.getInstance().transform(B.getGeometry(), sourceSRID, targetSRID);
		return A.geometry.distance(x);
	}

	public static StrabonPolyhedron project(StrabonPolyhedron A, int[] dims) throws Exception {
		ProjectionsFilter filter = new ProjectionsFilter(dims);
		A.geometry.apply(filter);
		A.geometry.geometryChanged();
		return new StrabonPolyhedron(A.getGeometry(), A.getGeometry().getSRID(), A.getGeometryDatatype());
	}
	
	public static StrabonPolyhedron transform(StrabonPolyhedron A, URI srid) throws Exception {
		int parsedSRID = WKTHelper.getSRID_forURI(srid.toString());
		Geometry converted = JTSWrapper.getInstance().transform(A.getGeometry(), A.getGeometry().getSRID(), parsedSRID);
		return new StrabonPolyhedron(converted, A.getGeometryDatatype());
	}

	/**
	 * Performs quick union between polygons or multipolygons.
	 * 
	 * @param A
	 * @param B
	 * @return
	 * @throws Exception
	 */
	public static StrabonPolyhedron quickUnion(StrabonPolyhedron A, StrabonPolyhedron B) throws Exception {
		System.out.println("Merging polyhedrons: A.coordinates=" + A.getGeometry().getCoordinates().length + 
				", B.coordinates=" + B.getGeometry().getCoordinates().length);

		int polygons = 0;
		if (Polygon.class.isInstance(A.geometry)) {			
			polygons++;
		} else if (MultiPolygon.class.isInstance(A.geometry)) {
			polygons += ((MultiPolygon)(A.geometry)).getNumGeometries();
		}
		if (Polygon.class.isInstance(B.geometry)) {
			polygons++;
		} else if (MultiPolygon.class.isInstance(B.geometry)) {
			polygons += ((MultiPolygon)(B.geometry)).getNumGeometries();
		}

		assert (polygons >= 2);

		int index = 0;
		Polygon[] polys = new Polygon[polygons];

		if (Polygon.class.isInstance(A.geometry)) {
			polys[index] = (Polygon)(A.geometry);
			index++;
		}
		if (Polygon.class.isInstance(B.geometry)) {
			polys[index] = (Polygon)(B.geometry);
			index++;
		}
		if (MultiPolygon.class.isInstance(A.geometry)) {
			MultiPolygon multi = (MultiPolygon)(A.geometry);
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				polys[index] = (Polygon)multi.getGeometryN(i);
				index++;
			}
		}
		if (MultiPolygon.class.isInstance(B.geometry)) {
			MultiPolygon multi = (MultiPolygon)(B.geometry);
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				polys[index] = (Polygon)multi.getGeometryN(i);
				index++;
			}
		}

		return new StrabonPolyhedron(new MultiPolygon(polys, new GeometryFactory()), 
									 A.getGeometry().getSRID(), 
									 A.getGeometryDatatype());
	}

	public StrabonPolyhedron getBuffer(double distance) throws Exception {
		Geometry geo = this.geometry.buffer(distance);
		return new StrabonPolyhedron(geo, this.geometry.getSRID(), datatype);
	}

	public StrabonPolyhedron getBoundary() throws Exception {
		Geometry geo = this.geometry.getBoundary();
		return new StrabonPolyhedron(geo, this.geometry.getSRID(), datatype);
	}

	public StrabonPolyhedron getEnvelope() throws Exception {
		Geometry geo = this.geometry.getEnvelope();
		return new StrabonPolyhedron(geo, this.geometry.getSRID(), datatype);
	}

	public double getArea() throws Exception {
		return this.getArea();
	}
	
	public int getNumPoints() {
		return this.geometry.getNumPoints();
	}

	@SuppressWarnings("unused")
	private static String FindGeoType(Geometry geo) {
		return 
				Point.class.isInstance(geo) ? "Point" :
					MultiPoint.class.isInstance(geo) ? "MultiPoint" :
						LineString.class.isInstance(geo) ? "LineString" :
							MultiLineString.class.isInstance(geo) ? "MultiLineString" :
								Polygon.class.isInstance(geo) ? "Polygon" :
									MultiPolygon.class.isInstance(geo) ? "MultiPolygon" :
										GeometryCollection.class.isInstance(geo) ? "GeometryCollection" : 
											"Unknown";
	}
	
	
	/***
	 * Additions by charnik.
	 * Why all the above operations (symdifference, boundary, etc.) are static methods
	 * and not member methods?
	 */
	
	/**
	 * Returns the centroid of this StrabonPolyhedron as 
	 * a new StrabonPolyhedron.
	 * 
	 * @return
	 */
	public StrabonPolyhedron getCentroid() {
		Point point = geometry.getCentroid();
		return new StrabonPolyhedron(point, geometry.getSRID(), datatype);
	}
}
