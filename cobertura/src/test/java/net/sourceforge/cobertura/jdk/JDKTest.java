package net.sourceforge.cobertura.jdk;

import static org.junit.Assert.*;

import groovy.util.AntBuilder;
import groovy.util.Node;

import java.io.File;

import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.Test;

/**
 * This test is in here because there are a lot of issues with backward compatibility.
 * We compile with each jdk version to verify that the simplest test will at least produce
 * code coverage.
 * 
 * @author schristou88
 *
 */
public class JDKTest {
	static final AntBuilder ant = TestUtils.antBuilder;
	static Node dom;

	public static void setupAndExecuteTest(String jdkVersion) throws Exception {
		FileUtils.deleteDirectory(TestUtils.getTempDir());

		/*
		 * First create the junit test structure.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File instrumentDir = new File(tempDir, "instrument");
		instrumentDir.mkdirs();
		File mainSourceFile = new File(srcDir, "mypackage/JDKTEST.java");

		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();

		FileUtils.write(mainSourceFile, JDKTEST);

		/*
		 * Next let's compile the test code we just made.
		 */
		TestUtils.compileSource(ant, srcDir, jdkVersion);

		/*
		 * Let's now instrument all the classes. In this case we instrument with default items.
		 */
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);

		/*
		 * Kick off the Main (instrumented) class.
		 */
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname("mypackage.JDKTEST");
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(TestUtils.getCoberturaDefaultClasspath());
		java.setJVMVersion(jdkVersion);
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

		/*
		 * 
		 */
		System.out.println(srcDir.getAbsolutePath());
		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath()
				+ "/coverage.xml");
	}

	@Test
	public void testJDK1() throws Exception {
		setupAndExecuteTest("1.1");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK2() throws Exception {
		setupAndExecuteTest("1.2");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK3() throws Exception {
		setupAndExecuteTest("1.3");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK4() throws Exception {
		setupAndExecuteTest("1.4");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK5() throws Exception {
		setupAndExecuteTest("1.5");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK6() throws Exception {
		setupAndExecuteTest("1.6");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	@Test
	public void testJDK7() throws Exception {
		setupAndExecuteTest("1.7");
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.JDKTEST",
				"main");
		assertEquals(2, hitCount);
	}

	static final String JDKTEST = "\n package mypackage;" + "\n "
			+ "\n public class JDKTEST {"
			+ "\n   public static void main(String[] args) {"
			+ "\n     System.out.println(\"Hello world.\");" + "\n   }"
			+ "\n }";
}