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
package net.sourceforge.cobertura.merge;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests merging feature by launching Main class.
 */
public class MergeMainTest extends TestCase {
	private ClassData firstClass = new ClassData("test.First");
	private ClassData secondClass = new ClassData("test.Second");
	private ClassData seventhClass = new ClassData("Seventh");

	private ProjectData greenProject = new ProjectData();
	private ProjectData redProject = new ProjectData();
	private ProjectData blueProject = new ProjectData();

	private List filesToRemove = new ArrayList();

	private File createTempSerFile() throws IOException {
		File result = File.createTempFile("cobertura", ".ser");
		result.delete();
		filesToRemove.add(result);
		return result;
	}

	protected void tearDown() throws Exception {
		for (int i = 0; i < filesToRemove.size(); i++) {
			((File) filesToRemove.get(i)).delete();
		}
	}

	public void testNewDestinationFile() throws IOException {
		// Create some coverage data
		greenProject.addClassData(firstClass);
		redProject.addClassData(secondClass);
		redProject.addClassData(seventhClass);

		// Generate filenames for serialized data
		File greenFile = createTempSerFile();
		File redFile = createTempSerFile();
		File dataFile = createTempSerFile();

		// Save coverage data for created data
		CoverageDataFileHandler.saveCoverageData(greenProject, greenFile);
		CoverageDataFileHandler.saveCoverageData(redProject, redFile);

		// Run merge task
		String[] args = {"--datafile", dataFile.getAbsolutePath(),
				greenFile.getAbsolutePath(), redFile.getAbsolutePath()};

		Main.main(args);

		// Read merged data
		ProjectData merged = CoverageDataFileHandler.loadCoverageData(dataFile);

		// Check if everything is ok
		assertEquals(3, merged.getNumberOfClasses());
		assertNotNull(merged.getClassData("test.First"));
		assertNotNull(merged.getClassData("test.Second"));
		assertNotNull(merged.getClassData("Seventh"));
		assertNull(merged.getClassData("test.Third"));
	}

	public void testExistingDestinationFile() throws IOException {
		// Create some coverage data
		greenProject.addClassData(firstClass);
		redProject.addClassData(secondClass);

		// Generate filenames for serialized data
		File greenFile = createTempSerFile();
		File dataFile = createTempSerFile();

		// Save coverage data for created data
		CoverageDataFileHandler.saveCoverageData(greenProject, greenFile);
		CoverageDataFileHandler.saveCoverageData(redProject, dataFile);

		// Run merge task
		String[] args = {"--datafile", dataFile.getAbsolutePath(),
				greenFile.getAbsolutePath()};

		Main.main(args);

		// Read merged data
		ProjectData merged = CoverageDataFileHandler.loadCoverageData(dataFile);

		// Check if  everything is ok
		assertEquals(2, merged.getNumberOfClasses());
		assertNotNull(merged.getClassData("test.First"));
		assertNotNull(merged.getClassData("test.Second"));
	}

	public void testBaseDir() throws IOException {
		// Create some coverage data
		greenProject.addClassData(firstClass);
		redProject.addClassData(seventhClass);
		blueProject.addClassData(secondClass);

		// Generate filenames for serialized data
		File greenFile = createTempSerFile();
		File redFile = createTempSerFile();
		File blueFile = createTempSerFile();
		File dataFile = createTempSerFile();

		dataFile.delete();

		// Save coverage data for created data
		CoverageDataFileHandler.saveCoverageData(greenProject, greenFile);
		CoverageDataFileHandler.saveCoverageData(redProject, redFile);
		CoverageDataFileHandler.saveCoverageData(blueProject, blueFile);

		// Run merge task
		String[] args = {"--datafile", dataFile.getAbsolutePath(),
				greenFile.getAbsolutePath(), "--basedir", redFile.getParent(),
				redFile.getName(), blueFile.getName()};

		Main.main(args);

		// Read merged data
		ProjectData merged = CoverageDataFileHandler.loadCoverageData(dataFile);

		// Check if everything is ok
		assertEquals(3, merged.getNumberOfClasses());
		assertNotNull(merged.getClassData("test.First"));
		assertNotNull(merged.getClassData("test.Second"));
		assertNotNull(merged.getClassData("Seventh"));
	}
}
