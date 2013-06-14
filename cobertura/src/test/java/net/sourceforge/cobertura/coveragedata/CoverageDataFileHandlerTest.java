/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
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

import java.io.File;

public class CoverageDataFileHandlerTest extends TestCase {

	private final static String basedir = ".";
	private final static String pathToTestOutput = basedir
			+ "/target/test/CoverageDataFileHandlerTest";

	private final ProjectData a = new ProjectData();
	private File tmpDir = new File(pathToTestOutput);

	public void setUp() {
		// Create some coverage data
		ClassData classData;
		assertEquals(0, a.getNumberOfClasses());
		assertEquals(0, a.getNumberOfChildren());

		classData = new ClassData("HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 10; i++)
			classData.addLine(i, "test", "(I)B");
		a.addClassData(classData);
		assertEquals(1, a.getNumberOfClasses());
		assertEquals(1, a.getNumberOfChildren());

		classData = new ClassData("HelloWorldHelper");
		classData.setSourceFileName("com/example/HelloWorldHelper.java");
		for (int i = 0; i < 14; i++)
			classData.addLine(i, "test", "(I)B");
		a.addClassData(classData);
		assertEquals(2, a.getNumberOfClasses());
		assertEquals(1, a.getNumberOfChildren());

		// Create the directory for our serialized coverage data
		tmpDir.mkdirs();
	}

	public void tearDown() {
		tmpDir = new File(pathToTestOutput);
		File files[] = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++)
			files[i].delete();
		tmpDir.delete();
	}

	public void testSaveAndRestore() {
		File dataFile = new File(tmpDir, "cobertura.ser");
		CoverageDataFileHandler.saveCoverageData(a, dataFile);

		ProjectData b;
		b = CoverageDataFileHandler.loadCoverageData(dataFile);
		assertEquals(a, b);
	}

}
