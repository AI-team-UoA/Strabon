/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.SQLException;

import org.openrdf.generaldb.managers.base.ManagerBase;
/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 */
public class TripleManager extends ManagerBase {

	public static TripleManager instance;

	private TransTableManager statements;

	public TripleManager() {
		instance = this;
	}

	public void setTransTableManager(TransTableManager statements) {
		this.statements = statements;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		statements.close();
	}

	//FIXME 2 last arguments used to accommodate need for temporal
	public void insert(Number ctx, Number subj, Number pred, Number obj)//,Timestamp intervalStart, Timestamp intervalEnd)
		throws SQLException, InterruptedException
	{
		statements.insert(ctx, subj, pred, obj);//,intervalStart,intervalEnd);
//		System.err.println(subj+", "+pred+", "+obj);
	}

}
