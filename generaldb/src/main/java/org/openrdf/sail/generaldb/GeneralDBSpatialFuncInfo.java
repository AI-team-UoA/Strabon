package org.openrdf.sail.generaldb;

import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluation.ResultType;

/**
 * Class used to store all info needed about a spatial construct / metric / property 
 * located in select clause. Currently storing info about the name of the field that 
 * has to be retrieved from the Result Set, as well as the type of the spatial function.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class GeneralDBSpatialFuncInfo {

	private String fieldName;
	private ResultType type;
	private boolean sridFunc;

	public GeneralDBSpatialFuncInfo(String fieldName, ResultType type, boolean sridFunc) {
		this.fieldName = fieldName;
		this.type = type;
		this.sridFunc = sridFunc;
	}
	
	public boolean isSRIDFunc() {
		return sridFunc;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public ResultType getType() {
		return type;
	}
	
	public void setType(ResultType type) {
		this.type = type;
	}
}


