/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
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

package test.first;

import junit.framework.TestCase;

/**
 * Simple class used in functional testing.
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
		A a = new A();
		a.call();

		B b = new B();
		b.call();

		test.second.A a2 = new test.second.A();
		a2.call();

		test.second.B b2 = new test.second.B();
		b2.call();
	}

}
