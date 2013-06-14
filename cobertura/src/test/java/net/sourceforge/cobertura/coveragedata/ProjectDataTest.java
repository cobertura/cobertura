/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2010 Charlie Squires
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

import java.util.Iterator;
import java.util.SortedSet;

public class ProjectDataTest extends TestCase {

	private ProjectData coverageData;

	public void setUp() {
		coverageData = new ProjectData();
	}

	public void testAddClass() {
		ClassData classData;

		assertEquals(0, coverageData.getNumberOfChildren());
		assertEquals(0, coverageData.getClasses().size());
		assertEquals(null, coverageData.getClassData("gobbleDeeGoop"));

		classData = new ClassData("com.example.HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 10; i++)
			classData.addLine(i, "test", "(I)B");
		coverageData.addClassData(classData);
		assertEquals(1, coverageData.getNumberOfChildren());
		assertEquals(1, coverageData.getClasses().size());
		assertEquals(classData, coverageData.getClassData(classData.getName()));

		classData = new ClassData("com.example.HelloWorldHelper");
		classData.setSourceFileName("com/example/HelloWorldHelper.java");
		for (int i = 0; i < 14; i++)
			classData.addLine(i, "test", "(I)B");
		coverageData.addClassData(classData);
		assertEquals(1, coverageData.getNumberOfChildren());
		assertEquals(2, coverageData.getClasses().size());
		assertEquals(classData, coverageData.getClassData(classData.getName()));

		// See what happens when we try to add the same class twice
		classData = new ClassData("com.example.HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 19; i++)
			classData.addLine(i, "test", "(I)B");
		try {
			coverageData.addClassData(classData);
			// removed by Jeremy Thomerson when changing PackageData
			// fail("Expected an IllegalArgumentException but did not receive one!");
		} catch (IllegalArgumentException e) {
			// Good!
		}
		assertEquals(1, coverageData.getNumberOfChildren());
		assertEquals(2, coverageData.getClasses().size());
	}

	public void testEquals() {
		ProjectData a = new ProjectData();
		ProjectData b = new ProjectData();
		ProjectData c = new ProjectData();
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

	public void testHashCode() {
		ProjectData a = new ProjectData();
		ProjectData b = new ProjectData();
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
	}

	public void testGetSubPackages() {
		coverageData.addClassData(new ClassData("com.example.HelloWorld"));
		coverageData.addClassData(new ClassData(
				"com.example.test.HelloWorldTest"));
		coverageData.addClassData(new ClassData(
				"com.examplesomething.HelloWorld"));

		SortedSet subPackagesSet = coverageData.getSubPackages("com.example");
		assertEquals(2, subPackagesSet.size());

		Iterator subPackages = subPackagesSet.iterator();
		assertEquals("com.example", ((PackageData) subPackages.next())
				.getName());
		assertEquals("com.example.test", ((PackageData) subPackages.next())
				.getName());
	}

}
