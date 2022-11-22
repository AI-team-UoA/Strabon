/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * A template test. It: 
 * 1) creates a database
 * 2) stores a dataset
 * 3) poses a query
 * 4) checks if the results of the query are the expected 
 * 	  If you use ORDER BY do NOT use this class.  
 * 5) drops the database
 * 
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 */
public abstract class TemplateTest
{	
	protected String datasetFile;
	protected ArrayList<String> queryFile;
	protected ArrayList<String> resultsFile;
	protected Boolean inference;
	protected Boolean orderResults;
	
	public TemplateTest()
	{
		queryFile=new ArrayList<String>();
		resultsFile=new ArrayList<String>();
		
		String testname=this.getClass().getSimpleName();
		
		String testpackage=this.getClass().getPackage().getName().substring(this.getClass().getPackage().getName().lastIndexOf('.')+1);
		File testfolder = null;
		
		try 
		{
			testfolder = new File(this.getClass().getResource(File.separator+testpackage+File.separator+testname+File.separator).toURI());
		} 
		catch (URISyntaxException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		String[] files = testfolder.list();
		
		for(String file : files)
		{
			if(file.endsWith(".nt") || file.endsWith(".nq"))
			{
				datasetFile=File.separator+testpackage+File.separator+testname+File.separator+file;
			}
			else if(file.endsWith(".rq"))
			{
				queryFile.add(File.separator+testpackage+File.separator+testname+File.separator+file);
				resultsFile.add(File.separator+testpackage+File.separator+testname+File.separator+file.substring(0, file.length()-3)+".srx");
			}
		}
		
		inference=false;
		orderResults=false;
	}

	@Before
	public void before() throws Exception
	{
		Utils.createdb();
		Utils.storeDataset(datasetFile, inference);
	}
	
	@Test
	public void test() throws Exception
	{
		Iterator<String> queryFileIterator = queryFile.iterator();
		Iterator<String> resultsFileIterator = resultsFile.iterator();
		
		while(queryFileIterator.hasNext() && resultsFileIterator.hasNext())
		{
			Utils.testQuery(queryFileIterator.next(), resultsFileIterator.next(),orderResults);
		}
	}
	
	@After
	public void after() throws Exception
	{
		Utils.dropdb();
	}
}
