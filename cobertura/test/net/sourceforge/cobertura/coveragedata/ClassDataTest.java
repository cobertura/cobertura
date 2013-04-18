/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
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

import java.util.Collection;

import junit.framework.TestCase;

public class ClassDataTest extends TestCase
{

	private final ClassData a = new ClassData("com.example.HelloWorld");
	private final ClassData b = new ClassData("com.example.HelloWorld");
	private final ClassData c = new ClassData("com.example.HelloWorld");
	private final ClassData defPckg = new ClassData("DefaultPackageClass");

	public void setUp()
	{
		a.setSourceFileName("com/example/HelloWorld.java");
		b.setSourceFileName("com/example/HelloWorld.java");
		c.setSourceFileName("com/example/HelloWorld.java");
		defPckg.setSourceFileName("DefaultPackageClass.java");

		for (int i = 1; i <= 5; i++)
			b.addLine(i, "test", "(I)B");
		for (int i = 1; i <= 5; i++)
			c.addLine(i, "test", "(I)B");
		for (int i = 1; i <= 5; i++)
			defPckg.addLine(i, "test", "(I)B");

		b.touch(1,1);
		b.touch(2,1);
	}

	public void testBranch()
	{
		// Setting an invalid line as a branch should not make the line valid
		assertFalse(a.hasBranch(2));
		a.addLineJump(2, 0);
		assertFalse(a.hasBranch(2));

		assertFalse(b.hasBranch(2));
		b.addLineJump(2, 0);
		assertTrue(b.hasBranch(2));

		assertTrue(b.hasBranch(2));
		b.addLineJump(2, 1);
		assertTrue(b.hasBranch(2));

		assertFalse(b.hasBranch(4));
		b.addLineSwitch(4, 0, 1, 9, Integer.MAX_VALUE);
		assertTrue(b.hasBranch(4));

		Collection branches = b.getBranches();
		assertEquals(2, branches.size());
		assertEquals(14, b.getNumberOfValidBranches());
		assertTrue(branches.contains(new Integer(2)));
		assertTrue(branches.contains(new Integer(4)));
		//assertTrue(branches.contains(new LineData(2, "test", "(I)B")));
		//assertTrue(branches.contains(new LineData(4, "test", "(I)B")));
	}

	public void testBranchCoverage()
	{
		assertEquals(0, a.getNumberOfValidBranches());
		assertEquals(0, b.getNumberOfValidBranches());
		assertEquals(1.00d, a.getBranchCoverageRate(), 0d);
		assertEquals(1.00d, b.getBranchCoverageRate(), 0d);

		assertEquals(1.00d, a.getBranchCoverageRate("test(I)B"), 0d);
		assertEquals(1.00d, b.getBranchCoverageRate("test(I)B"), 0d);

		c.addLineJump(1, 0);
		c.addLineJump(2, 0);
		c.addLineSwitch(3, 0, 1, 3, Integer.MAX_VALUE);
		c.addLineSwitch(4, 0, 1, 3, Integer.MAX_VALUE);

		assertEquals(12, c.getNumberOfValidBranches());
		assertEquals(0, c.getNumberOfCoveredBranches());
		assertEquals(0.00d, c.getBranchCoverageRate(), 0d);
		assertEquals(0.00d, c.getBranchCoverageRate("test(I)B"), 0d);

		c.touchJump(1, 0, true,1);
		c.touchJump(1, 0, false,1);
		c.touchJump(2, 0, true,1);
		c.touchJump(2, 0, false,1);

		assertEquals(12, c.getNumberOfValidBranches());
		assertEquals(4, c.getNumberOfCoveredBranches());
		assertEquals(0.33d, c.getBranchCoverageRate(), 0.01d);
		assertEquals(0.33d, c.getBranchCoverageRate("test(I)B"), 0.01d);

		c.touchSwitch(3, 0, 0,1);
		c.touchSwitch(3, 0, 1,1);
		c.touchSwitch(4, 0, 2,1);
		c.touchSwitch(4, 0, -1,1);
		
		assertEquals(12, c.getNumberOfValidBranches());
		assertEquals(8, c.getNumberOfCoveredBranches());
		assertEquals(0.66d, c.getBranchCoverageRate(), 0.01d);
		assertEquals(0.66d, c.getBranchCoverageRate("test(I)B"), 0.01d);
	}

	public void testConstructor()
	{
		try
		{
			new ClassData(null);
			fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			// Good!
		}

		assertEquals("HelloWorld", a.getBaseName());
		assertEquals("com.example", a.getPackageName());
		assertEquals("com.example.HelloWorld", a.getName());
		
		assertEquals("DefaultPackageClass", defPckg.getBaseName());
		assertEquals("", defPckg.getPackageName());
		assertEquals("DefaultPackageClass", defPckg.getName());
	}

	public void testEquals()
	{
		assertFalse(a.equals(null));
		assertFalse(a.equals(new Integer(4)));
		assertFalse(a.equals(new PackageData("com.example")));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(a.equals(defPckg));
		assertFalse(b.equals(a));
		assertTrue(b.equals(b));
		assertFalse(b.equals(c));
		assertFalse(b.equals(defPckg));
		assertFalse(c.equals(a));
		assertFalse(c.equals(b));
		assertTrue(c.equals(c));
		assertFalse(c.equals(defPckg));
		assertFalse(defPckg.equals(a));
		assertFalse(defPckg.equals(b));
		assertFalse(defPckg.equals(c));
		assertTrue(defPckg.equals(defPckg));
		

		c.touch(1,1);
		c.touch(2,1);
		assertTrue(b.equals(c));
	}

	public void testLineCoverage()
	{
		assertEquals(0, a.getNumberOfCoveredLines());
		assertEquals(0, a.getNumberOfValidLines());
		assertEquals(2, b.getNumberOfCoveredLines());
		assertEquals(5, b.getNumberOfValidLines());
		assertEquals(0, c.getNumberOfCoveredLines());
		assertEquals(5, c.getNumberOfValidLines());
		assertEquals(1d, a.getLineCoverageRate(), 0d);
		assertEquals(0.4d, b.getLineCoverageRate(), 0d);
		assertEquals(0d, c.getLineCoverageRate(), 0d);

		assertEquals(1d, a.getLineCoverageRate("test(I)B"), 0d);
		assertEquals(0.4d, b.getLineCoverageRate("test(I)B"), 0d);
		assertEquals(0d, c.getLineCoverageRate("test(I)B"), 0d);

		assertEquals(1d, a.getLineCoverageRate("notReal(I)B"), 0d);
		assertEquals(1d, b.getLineCoverageRate("notReal(I)B"), 0d);
		assertEquals(1d, c.getLineCoverageRate("notReal(I)B"), 0d);
	}

	public void testRemoveLine()
	{
		assertEquals(0, a.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(0, a.getNumberOfValidLines());
		a.removeLine(3);
		assertEquals(0, a.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(0, a.getNumberOfValidLines());

		assertEquals(0, b.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(5, b.getNumberOfValidLines());
		b.removeLine(3);
		assertEquals(0, b.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(4, b.getNumberOfValidLines());

		c.addLineJump(2, 0);
		c.addLineSwitch(3, 0, 1, 2, Integer.MAX_VALUE);
		c.addLineJump(3, 0);
		c.addLineJump(4, 0);
		assertEquals(9, c.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(5, c.getNumberOfValidLines());
		c.removeLine(3);
		assertEquals(4, c.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(4, c.getNumberOfValidLines());
	}

	public void testSourceFileName()
	{
		a.setSourceFileName(null);
		assertEquals("com/example/HelloWorld.java", a.getSourceFileName());
		a.setSourceFileName("HelloWorld.java");
		assertEquals("com/example/HelloWorld.java", a.getSourceFileName());

		ClassData d = new ClassData("org.jaxen.expr.IdentitySet$IdentityWrapp");
		assertEquals("org/jaxen/expr/IdentitySet.java", d.getSourceFileName());

		ClassData e = new ClassData("org.jaxen.expr.NodeComparator$1");
		assertEquals("org/jaxen/expr/NodeComparator.java", e.getSourceFileName());
		
		assertEquals( "DefaultPackageClass.java", defPckg.getSourceFileName());
		
		ClassData f = new ClassData("$strangeClass");
		assertEquals( "$strangeClass.java", f.getSourceFileName());
	}

	public void testTouch()
	{
		int line = 3;

		assertFalse(a.isValidSourceLineNumber(line));
		a.touch(line,1);
		assertTrue(a.isValidSourceLineNumber(line));

		assertTrue(b.isValidSourceLineNumber(line));
		assertEquals(0, b.getLineCoverage(line).getHits());
		b.touch(line,1);
		assertTrue(b.isValidSourceLineNumber(line));
		assertEquals(1, b.getLineCoverage(line).getHits());
		b.touch(line,1);
		assertEquals(2, b.getLineCoverage(line).getHits());
		assertTrue(b.isValidSourceLineNumber(line));
	}

}