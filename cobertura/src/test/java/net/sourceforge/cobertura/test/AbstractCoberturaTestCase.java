/**
 * 
 */
package net.sourceforge.cobertura.test;

import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.After;
import org.junit.Before;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

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
	public static File datafile;

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

		TestUtils.instrumentClasses(TestUtils.antBuilder, srcDir, datafile,
				instrumentDir);

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

	/**
	 * 
	 * @param method method with the parsing issue
	 * @throws IOException 
	 */
	public static void parseIssueTester(String imports, String method)
			throws IOException {
		String wrapper = "\n package mypackage;" + "\n " + imports
				+ "\n public class FooMain {" + method + "\n }";

		FileUtils.write(new File(srcDir, "mypackage/FooMain.java"), wrapper);

		TestUtils.compileSource(TestUtils.antBuilder, srcDir);

		TestUtils.instrumentClasses(TestUtils.antBuilder, srcDir, datafile,
				instrumentDir);

		DefaultLogger fileLogger = new DefaultLogger();
		fileLogger.setErrorPrintStream(new PrintStream(new File(reportDir,
				"error.log")));
		fileLogger.setOutputPrintStream(new PrintStream(new File(reportDir,
				"std.log")));
		fileLogger.setMessageOutputLevel(Project.MSG_INFO);
		TestUtils.project.addBuildListener(fileLogger);

		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setSrcDir(srcDir.getAbsolutePath());
		reportTask.setDestDir(reportDir);
		reportTask.setFailonerror(true);
		reportTask.execute();

		TestUtils.project.removeBuildListener(fileLogger);

		if (FileUtils.readFileToString(new File(reportDir, "error.log"))
				.contains("JavaNCSS got an error while parsing the java file"))
			fail("JavaNCSS Error, see console output or file: "
					+ new File(reportDir, "error.log").getAbsolutePath());

		if (FileUtils.readFileToString(new File(reportDir, "std.log"))
				.contains("JavaNCSS got an error while parsing the java file"))
			fail("JavaNCSS Error, see console output or file: "
					+ new File(reportDir, "std.log").getAbsolutePath());
	}
}
