/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class StrabonBeanWrapperConfiguration {
	private String label;
	private String bean;
	private String statement;
	private String format;
	private String title;
	private String handle;
	private boolean isHeader;
	private boolean isBean;

	public StrabonBeanWrapperConfiguration(String label, String bean, String statement, String format, String title, String handle) {
		this.label = label;
		this.bean = bean;
		this.statement = statement;
		this.format = format;
		this.title = title;
		this.handle = handle;
		this.isHeader = false;
		this.isBean = false;
	}
	
	public StrabonBeanWrapperConfiguration(String label) {
		this.label = label;
		this.bean = null;
		this.isHeader = true;
		this.isBean = false;
	}
	
	public StrabonBeanWrapperConfiguration(String label, String bean) {
		this.label = label;
		this.bean = bean;
		this.isHeader = false;
		this.isBean = true;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getBean() {
		return bean;
	}
	
	public void setBean(String bean) {
		this.bean = bean;
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatement() {
		return statement;
	}
	
	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getHandle() {
		return this.handle;
	}
	
	public void setHandle(String handle) {
		this.handle = handle;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}
	public boolean isBean() {
		return isBean;
	}
	public void setBean(boolean isBean) {
		this.isBean = isBean;
	}
	
}

