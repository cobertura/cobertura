/**
 * 
 */
package net.sourceforge.cobertura.test;

import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;

import com.sun.corba.se.impl.oa.poa.AOMEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author schristou88
 *
 */
public class AbstractCoberturaTestCase {
	public static File tempDir;
	public static File srcDir;
	public static File reportDir;
	public static File instrumentDir;
	File mainSourceFile;
	File datafile;

	@Before
	public void setUp() throws Exception {
		tempDir = TestUtils.getTempDir();

		FileUtils.deleteDirectory(tempDir);

		srcDir = new File(tempDir, "src");
		reportDir = new File(tempDir, "report");
		instrumentDir = new File(tempDir, "instrument");
		mainSourceFile = new File(srcDir, "mypackage/Main.java");
		datafile = new File(srcDir, "cobertura.ser");

		srcDir.mkdirs();
		reportDir.mkdirs();
		instrumentDir.mkdirs();
	}

	@After
	public void tearDown() {
		// Default is do nothing since if we try
		// debugging we can see logs of current failure.
	}

	public static void assertConditionCoverage(List<Node> lines,
			String expectedValue, int lineNumber) {
		boolean found = false;
		for (Node node : lines) {
			if (Integer.valueOf((String) node.attribute("number")) == lineNumber) {
				found = true;
				assertEquals(expectedValue, (String) node
						.attribute("condition-coverage"));
			}
		}
		assertTrue(found);
	}

	public Node createAndExecuteMainMethod(String packageName, String fileName,
			String fileContent, String mainMethod) throws Exception {
		
		FileUtils.write(new File(srcDir, fileName + ".java"), fileContent);
		

		TestUtils.compileSource(TestUtils.antBuilder, srcDir);

		TestUtils.instrumentClasses(TestUtils.antBuilder, srcDir, datafile, instrumentDir);

		/*
		 * Kick off the Main (instrumented) class.
		 */
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname(mainMethod);
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(TestUtils.getCoberturaDefaultClasspath());
		java.execute();
		/*
		 * Now create a cobertura xml file and make sure the correct counts are in it.
		 */
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();

		return TestUtils.getXMLReportDOM(srcDir.getAbsolutePath()
				+ "/coverage.xml");
	}
}
