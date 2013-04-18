/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 Jiri Mares
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

package net.sourceforge.cobertura.coveragedata;

import junit.framework.TestCase;

public class PackageDataTest extends TestCase
{

	private PackageData packageData;

	public void setUp()
	{
		packageData = new PackageData("com.example");
		assertEquals("com.example", packageData.getName());
	}

	public void testAddClass()
	{
		ClassData classData;

		assertEquals(0, packageData.getNumberOfChildren());

		classData = new ClassData("com.example.HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 10; i++)
			classData.addLine(i, "test", "(I)B");
		packageData.addClassData(classData);
		assertEquals(1, packageData.getNumberOfChildren());
		assertTrue(packageData.contains(classData.getBaseName()));

		classData = new ClassData("com.example.HelloWorldHelper");
		classData.setSourceFileName("com/example/HelloWorldHelper.java");
		for (int i = 0; i < 14; i++)
			classData.addLine(i, "test", "(I)B");
		packageData.addClassData(classData);
		assertEquals(2, packageData.getNumberOfChildren());
		assertTrue(packageData.contains(classData.getBaseName()));

		// See what happens when we try to add the same class twice
		classData = new ClassData("com.example.HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 19; i++)
			classData.addLine(i, "test", "(I)B");
		try
		{
			packageData.addClassData(classData);
			// removed by Jeremy Thomerson when changing PackageData
			// fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			// Good!
		}

		assertEquals(2, packageData.getNumberOfChildren());
	}

	public void testBranchCoverage()
	{
		assertEquals(0, packageData.getNumberOfCoveredBranches());
		assertEquals(0, packageData.getNumberOfValidBranches());
		assertEquals(1.00d, packageData.getBranchCoverageRate(), 0d);

		ClassData classData = new ClassData("com.example.HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 10; i++)
			classData.addLine(i, "test", "(I)B");
		packageData.addClassData(classData);

		assertEquals(0, packageData.getNumberOfCoveredBranches());
		assertEquals(0, packageData.getNumberOfValidBranches());
		assertEquals(1.00d, packageData.getBranchCoverageRate(), 0d);

		classData.addLineJump(1, 0);
		classData.addLineJump(2, 0);
		classData.addLineSwitch(3, 0, 1, 3, 10);

		assertEquals(0, packageData.getNumberOfCoveredBranches());
		assertEquals(8, packageData.getNumberOfValidBranches());
		assertEquals(0.00d, packageData.getBranchCoverageRate(), 0d);

		classData.touch(1,1);
		classData.touchJump(1, 0, true,1);
		classData.touch(1,1);
		classData.touchJump(1, 0, false,1);
		classData.touchSwitch(1, 1, 0,1);
		classData.touch(2,1);
		classData.touchJump(2, 0, false,1);
      
		assertEquals(4, packageData.getNumberOfCoveredBranches());
		assertEquals(11, packageData.getNumberOfValidBranches());
		assertEquals(4.0d/11.0d, packageData.getBranchCoverageRate(), 0.01d);
	}

	public void testConstructor()
	{
		try
		{
			new PackageData(null);
			fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			// Good!
		}
	}

	public void testEquals()
	{
		PackageData a = new PackageData("com.example");
		PackageData b = new PackageData("com.example");
		PackageData c = new PackageData("com.example");
		ClassData classData1 = new ClassData("com.example.HelloWorld1");
		ClassData classData2 = new ClassData("com.example.HelloWorld2");
		ClassData classData3 = new ClassData("com.example.HelloWorld3");
		ClassData classData4 = new ClassData("com.example.HelloWorld4");

		classData1.setSourceFileName("com/example/HelloWorld1.java");
		classData2.setSourceFileName("com/example/HelloWorld2.java");
		classData3.setSourceFileName("com/example/HelloWorld3.java");
		classData4.setSourceFileName("com/example/HelloWorld4.java");

		a.addClassData(classData1);
		a.addClassData(classData2);
		a.addClassData(classData3);
		b.addClassData(classData1);
		b.addClassData(classData2);
		c.addClassData(classData1);
		c.addClassData(classData2);
		c.addClassData(classData4);

		assertFalse(a.equals(null));
		assertFalse(a.equals(classData1));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(b.equals(a));
		assertTrue(b.equals(b));
		assertFalse(b.equals(c));
		assertFalse(c.equals(a));
		assertFalse(c.equals(b));
		assertTrue(c.equals(c));

		b.addClassData(classData3);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));

		assertFalse(a.equals(c));
		assertFalse(c.equals(a));
	}

	public void testHashCode()
	{
		PackageData a = new PackageData("com.example");
		PackageData b = new PackageData("com.example");
		ClassData classData1 = new ClassData("com.example.HelloWorld1");
		ClassData classData2 = new ClassData("com.example.HelloWorld2");
		ClassData classData3 = new ClassData("com.example.HelloWorld3");

		classData1.setSourceFileName("com/example/HelloWorld1.java");
		classData2.setSourceFileName("com/example/HelloWorld2.java");
		classData3.setSourceFileName("com/example/HelloWorld3.java");

		a.addClassData(classData1);
		a.addClassData(classData2);
		a.addClassData(classData3);
		b.addClassData(classData1);
		b.addClassData(classData2);

		assertEquals(a.hashCode(), a.hashCode());
		assertEquals(b.hashCode(), b.hashCode());

		b.addClassData(classData3);
		assertEquals(a.hashCode(), b.hashCode());
		assertEquals(b.hashCode(), b.hashCode());
	}
}
