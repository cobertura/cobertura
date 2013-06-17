/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2007 John Lewis
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package test.condition;

import junit.framework.TestCase;

/**
 * Simple class used in functional testing of branch coverage.
 * 
 * @author John Lewis
 */
public class Test extends TestCase
{

	public Test(String name)
	{
		super(name);
	}

	/**
	 * Call the methods called "call"
	 */
	public void testMethod()
	{
		ConditionCalls branch = new ConditionCalls();
		branch.call(7);
		branch.callLookupSwitch(1);
		branch.callTableSwitch(100);
		branch.callMultiCondition(3, 7, 1);
		branch.callMultiCondition2(7, 7, 100);
	}

}
