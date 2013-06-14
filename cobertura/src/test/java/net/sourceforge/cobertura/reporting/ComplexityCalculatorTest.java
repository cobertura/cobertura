/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
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
package net.sourceforge.cobertura.reporting;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.FileFixture;

public class ComplexityCalculatorTest extends TestCase {
	private FileFixture fileFixture;
	private FileFinder fileFinder;
	private ComplexityCalculator complexity;

	public void testGetCCNForSourceFile() {
		/*
		 * Sample1.java has a @Deprecated annotation to make sure the complexity works with annotations.
		 */
		double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
				"com/example/Sample1.java"));
		assertTrue(ccn1 != 0.0);
		double ccn2 = complexity.getCCNForSourceFile(new SourceFileData(
				"com/example/Sample2.java"));
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);

		ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
				"com/example/Sample5.java"));
		assertTrue(ccn1 != 0.0);
		ccn2 = complexity.getCCNForSourceFile(new SourceFileData(
				"com/example/Sample6.java"));
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);

		double ccn0 = complexity.getCCNForSourceFile(new SourceFileData(
				"com/example/Sample8.java"));
		assertTrue(ccn0 == 0.0);

		ccn0 = complexity.getCCNForSourceFile(new SourceFileData("Foo.java"));
		assertTrue(ccn0 == 0.0);
	}

	public void testGetCCNForClass() {
		double ccn1 = complexity.getCCNForClass(new ClassData(
				"com.example.Sample3"));
		assertTrue(ccn1 != 0.0);
		double ccn2 = complexity.getCCNForClass(new ClassData(
				"com.example.Sample4"));
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);

		ccn1 = complexity.getCCNForClass(new ClassData("com.example.Sample5"));
		assertTrue(ccn1 != 0.0);
		ccn2 = complexity.getCCNForClass(new ClassData("com.example.Sample6"));
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);

		double ccn0 = complexity.getCCNForClass(new ClassData(
				"com.example.Sample8"));
		assertEquals(0.0, ccn0, 0.0);

		ccn0 = complexity.getCCNForClass(new ClassData("Foo"));
		assertEquals(0.0, ccn0, 0.0);
	}

	public void testGetCCNForPackage() {
		PackageData pd = new PackageData("com.example");
		pd.addClassData(new ClassData("com.example.Sample3"));
		double ccn1 = complexity.getCCNForPackage(pd);
		assertTrue(ccn1 != 0.0);

		ComplexityCalculator complexity2 = new ComplexityCalculator(fileFinder);
		pd.addClassData(new ClassData("com.example.Sample4"));
		double ccn2 = complexity2.getCCNForPackage(pd);
		double ccn3 = complexity2.getCCNForPackage(pd);
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);
		assertEquals(ccn2, ccn3, 0e-9);

		PackageData empty = new PackageData("com.example2");
		ComplexityCalculator complexity3 = new ComplexityCalculator(fileFinder);
		assertEquals(0.0, complexity3.getCCNForPackage(empty), 0.0);
	}

	public void testGetCCNForProject() {
		ProjectData project = new ProjectData();
		project.addClassData(new ClassData("com.example.Sample5"));
		double ccn1 = complexity.getCCNForProject(project);
		assertTrue(ccn1 != 0.0);

		ComplexityCalculator complexity2 = new ComplexityCalculator(fileFinder);
		project.addClassData(new ClassData("com.example.Sample4"));
		double ccn2 = complexity2.getCCNForProject(project);
		assertTrue(ccn2 != 0.0);
		assertTrue(ccn1 != ccn2);

		ComplexityCalculator complexity3 = new ComplexityCalculator(fileFinder);
		project.addClassData(new ClassData("com.example.Sample8"));
		double ccn3 = complexity3.getCCNForProject(project);
		assertEquals(ccn2, ccn3, 0e-9);

		ComplexityCalculator complexity4 = new ComplexityCalculator(fileFinder);
		double ccn0 = complexity4.getCCNForProject(new ProjectData());
		assertEquals(0.0, ccn0, 0.0);
	}

	public void testGetCCNForSourceFile_null() {
		try {
			complexity.getCCNForSourceFile(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	public void testGetCCNForPackage_null() {
		try {
			complexity.getCCNForPackage(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	public void testGetCCNForProject_null() {
		try {
			complexity.getCCNForProject(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	public void testConstructor_null() {
		try {
			new ComplexityCalculator(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		fileFixture = new FileFixture();
		fileFixture.setUp();

		fileFinder = new FileFinder();
		fileFinder.addSourceDirectory(fileFixture.sourceDirectory(
				FileFixture.SOURCE_DIRECTORY_IDENTIFIER[0]).toString());
		fileFinder.addSourceDirectory(fileFixture.sourceDirectory(
				FileFixture.SOURCE_DIRECTORY_IDENTIFIER[1]).toString());
		fileFinder.addSourceFile(fileFixture.sourceDirectory(
				FileFixture.SOURCE_DIRECTORY_IDENTIFIER[2]).toString(),
				"com/example\\Sample5.java");
		fileFinder.addSourceFile(fileFixture.sourceDirectory(
				FileFixture.SOURCE_DIRECTORY_IDENTIFIER[2]).toString(),
				"com/example/Sample6.java");
		fileFinder.addSourceFile(fileFixture.sourceDirectory(
				FileFixture.SOURCE_DIRECTORY_IDENTIFIER[3]).toString(),
				"com/example/Sample7.java");

		// Do not add com/example/Sample8.java
		// fileFinder.addSourceFile( fileFixture.sourceDirectory(FileFixture.SOURCE_DIRECTORY_IDENTIFIER[3]).toString(), "com/example/Sample8.java");

		complexity = new ComplexityCalculator(fileFinder);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		fileFixture.tearDown();
	}
}
