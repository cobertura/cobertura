package net.sourceforge.cobertura.jdk;

import static org.junit.Assert.*;

import groovy.util.AntBuilder;
import groovy.util.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JDK7Test {
	static AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());
	static Node dom;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException, ParserConfigurationException, SAXException {
		Logger.getRootLogger().setLevel(Level.ALL);
		FileUtils.deleteDirectory(TestUtils.getTempDir());
		
		/*
		 * First create the junit test structure.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Java7TestCase.java");
		
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();
		
		FileUtils.write(mainSourceFile, java7TestFile);
		
		
		/*
		 * Next let's compile the test code we just made.
		 */
		TestUtils.compileSource(ant, srcDir);
		
		/*
		 * Let's now instrument all the classes. In this case we instrument with default items.
		 */
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);
		
		/*
		 * Kick off the Main (instrumented) class.
		 */
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname("mypackage.Java7TestCase");
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
		
		/*
		 * 
		 */
		System.out.println(srcDir.getAbsolutePath());
		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath() + "/coverage.xml");
	}
	
	/**
     *    public void try_with_resource() throws Exception {
     *      try (FileOutputStream fos = new FileOutputStream(\"test.txt\");
     *           FileOutputStream fos2 = new FileOutputStream(\"meow.txt\")) {
     *        ....
     *      }
     *    }
	 */
	@Test
	public void testTryWithResource() {
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.Java7TestCase", "try_with_resource");
		assertEquals(5, hitCount);
	}
	
	/**
	 *    public void diamond_operator() {
	 *      Map<String, List<String>> stringMaps = new TreeMap <> ();
	 *    }
	 */
	@Test
	public void testDiamondOperator(){
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.Java7TestCase", "diamond_operator");
		assertEquals(2, hitCount);
	}
	
	/**
     * public void string_in_switch() {
     *   String x = "asdfg";
     *   switch(x) {
     *     case "asdf":
     *       break;
     *     default:
     *       break;
     *   }
     * }
	 */
	@Test
	public void testStringInSwitch(){
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.Java7TestCase", "string_in_switch");
		assertEquals(3, hitCount);
	}
	
	/**
	 *   public void numerical_literals_underscores() {
     *     int thousand = 1_000;
     *     int million  = 1_000_000;
     *   }
	 */
	@Test
	public void testNumericalLteralsUnderscores(){
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.Java7TestCase", "numerical_literals_underscores");
		assertEquals(3, hitCount);
	}
	
	/**
	 *   public void multi_catch() {
	 *     try {
	 *       FileOutputStream fos = new FileOutputStream(\"test.txt\");
	 *       int a = 5/0;
	 *       } catch (IOException | ArithmeticException e){
	 *     }
	 *   }
	 */
	@Test
	public void testMultiCatch() {
		int hitCount = TestUtils.getTotalHitCount(dom, "mypackage.Java7TestCase", "multi_catch");
		assertEquals(4, hitCount);
	}
	
	static final String java7TestFile = 
					"\n package mypackage;" +
					"\n " +
					"\n import java.util.*;" +
					"\n import java.io.*;" +
					"\n " +
					"\n public class Java7TestCase {" +
					"\n " +
					"\n   public static void main (String[] args) throws Exception {" +
					"\n     Java7TestCase t = new Java7TestCase();" +
					"\n     t.try_with_resource();" +
					"\n     t.diamond_operator();" +
					"\n     t.string_in_switch();" +
					"\n     t.numerical_literals_underscores();" +
					"\n     t.multi_catch();" +
					"\n   }" +
					"\n   " +
					"\n   public void try_with_resource() throws Exception {" +
					"\n     try (FileOutputStream fos = new FileOutputStream(\"test.txt\");" +
					"\n          FileOutputStream fos2 = new FileOutputStream(\"meow.txt\")) {" +
					"\n     }" +
					"\n   }" +
					"\n " +
					"\n   public void diamond_operator() {" +
					"\n     Map<String, List<String>> stringMaps = new TreeMap <> ();" +
					"\n   }" +
					"\n " +
					"\n   public void string_in_switch() {" +
					"\n     String x = \"asdfg\";" +
					"\n     switch(x) {" +
					"\n       case \"asdf\":" +
					"\n         break;" +
					"\n       default:" +
					"\n         break;" +
					"\n     }" +
					"\n   }" +
					"\n " +
					"\n   public void numerical_literals_underscores() {" +
					"\n     int thousand = 1_000;" +
					"\n     int million  = 1_000_000;" +
					"\n   }" +
					"\n   " +
					"\n   public void multi_catch() {" +
					"\n     try {" +
					"\n       FileOutputStream fos = new FileOutputStream(\"test.txt\");" +
					"\n       int a = 5/0;" +
					"\n     } catch (IOException | ArithmeticException e){" +
					"\n     }" +
					"\n   }" +
					"\n }";
}
