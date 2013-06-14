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

package net.sourceforge.cobertura.reporting.xml;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.JUnitXMLHelper;
import net.sourceforge.cobertura.util.FileFinder;

import java.io.File;

public class XMLReportTest extends TestCase {

	private final static String BASEDIR = ".";
	private final static String PATH_TO_TEST_OUTPUT = BASEDIR
			+ "/target/build/test/XMLReportTest";
	private File tmpDir;

	public void setUp() {
		tmpDir = new File(PATH_TO_TEST_OUTPUT);
		tmpDir.mkdirs();
	}

	public void tearDown() {
		tmpDir = new File(PATH_TO_TEST_OUTPUT);
		File files[] = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++)
			files[i].delete();
		tmpDir.delete();
	}

	public void testXMLReportWithNonSourceLines() throws Exception {
		ProjectData projectData = new ProjectData();

		// Adding line to the project data that hasn't been yet marked as source line
		ClassData cd = projectData.getOrCreateClassData(XMLReport.class
				.getName());
		cd.touch(7777, 1);

		File reportDir = File.createTempFile("XMLReportTest", "");
		reportDir.delete();
		reportDir.mkdir();

		FileFinder fileFinder = new FileFinder();
		ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

		new XMLReport(projectData, reportDir, fileFinder, complexity);

		File coverageFile = new File(reportDir, "coverage.xml");
		JUnitXMLHelper.readXmlFile(coverageFile, true);

		coverageFile.delete();
		reportDir.delete();
	}

}
