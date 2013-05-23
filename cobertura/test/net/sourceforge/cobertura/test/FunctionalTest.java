/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2010 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package net.sourceforge.cobertura.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionalTest extends AbstractCoberturaTestCase {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());

	Node dom;
	IgnoreUtil ignoreUtil;
	
	@Test
	public void interfaceFunctionalTest() throws Exception {
		/*
		 * Interfaces are not instrumented (yet).   So, instrument an interface and make
		 * sure line/conditional information is not in the report.
		 */
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File reportDir = new File(tempDir, "report");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();

		File interfaceSourceFile = new File(srcDir, "mypackage/MyInterface.java");
		
		FileUtils.write(interfaceSourceFile,
										"\n package mypackage;" +
										"\n " +
										"\n public interface MyInterface {" +
										"\n 	public static final Object MY_CONSTANT = new Object();  /* the test expects this to be line 5 */" +
										"\n }");

		FileUtils.write(mainSourceFile,
										"\n package mypackage;" +
										"\n " +
										"\n public class Main implements MyInterface {" +
										"\n " +
										"\n  public static void main(String[] args) {" +
										"\n   System.out.println(new Main());" +
										"\n   System.out.println(MY_CONSTANT);" +
										"\n  }" +
										"\n }");
		TestUtils.compileSource(ant, srcDir);		
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);
		
		/*
		 * Kick off the Main (instrumented) class.
		 */
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname("mypackage.Main");
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
		
		dom = TestUtils.getXMLReportDOM( srcDir.getAbsolutePath() + "/coverage.xml");
		
		List<Node> lines = TestUtils.getLineCounts(dom, "mypackage.MyInterface", "<clinit>", null);
		
		// When/if interfaces are instrumented, the next line can go away and the
		// lines in this method that have been commented out, can be uncommented.
		assertEquals("Interfaces are being instrumented", lines.size(), 0);
	}

	@Test
	public void conditionalInFinallyFunctionalTest() throws Exception {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File reportDir = new File(tempDir, "report");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();
		
		FileUtils.write(mainSourceFile,
										"\n package mypackage;" +
										"\n " +
										"\n " +
										"\n " +
										"\n public class Main {" +
										"\n  " +
										"\n  private boolean isDisabled() {" +
										"\n   return true;" +
										"\n  }" +
										"\n  " +
										"\n  private void doSomething() {" +
										"\n  }" +
										"\n  " +
										"\n  public void aMethod() {" +
										"\n   boolean disabled = false;" +
										"\n   try {" +
										"\n    disabled = isDisabled();" +
										"\n   } finally {" +
										"\n   if (disabled)" +
										"\n    doSomething();" +
										"\n   }" +
										"\n  }" +
										"\n  " +
										"\n  " +
										"\n }");

		TestUtils.compileSource(ant, srcDir);
		
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);
		
		/*
		* Now create a cobertura xml file and make sure the correct counts are in it.
		*/
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();
		
		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath() + "/coverage.xml");
		
		List<Node> lines = TestUtils.getLineCounts(dom, "mypackage.Main", "aMethod", null);
		Node conditionalLine = null;
		for(Node line : lines) {
			if ("20".equals(line.attribute("number"))) {
				conditionalLine = line;
			}
		}
		assertNotNull(conditionalLine);
		assertEquals("0% (0/2)", conditionalLine.attribute("condition-coverage"));
	}

	@Test
	public void callJunit() throws Exception {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		File tempDir = TestUtils.getTempDir();
		final File srcDir = new File(tempDir, "src");
		
		final File reportDir = new File(tempDir, "report");
		reportDir.mkdirs();
		
		final File instrumentDir = new File(tempDir, "instrument");
		instrumentDir.mkdirs();
		
		File buildDir = new File(tempDir, "build");
		buildDir.mkdirs();
		
		File testSourceFile = new File(srcDir, "mypackage/MyTest.groovy");
		testSourceFile.getParentFile().mkdirs();
		
		FileUtils.write(testSourceFile, 
										"\n package mypackage" +
										"\n " +
										"\n import junit.framework.TestSuite" +
										"\n import junit.framework.Test" +
										"\n " +
										"\n " +
										"\n public class MyTest extends TestSuite {" +
										"\n public MyTest(String arg0) {" +
										"\n 	super(arg0);" +
										"\n }" +
										"\n " +
										"\n public static Test suite() {" +
										"\n 	" +
										"\n 	// do something that will cause Sub's static initializer to run" +
										"\n 	Sub.class" +
										"\n " +
										"\n 	return new TestSuite(\"Empty Suite\")" +
										"\n }" +
										"\n }");
		
		File superSourceFile = new File(srcDir, "mypackage/Super.java");
		File subSourceFile = new File(srcDir, "mypackage/Sub.java");
		File datafile = new File(srcDir, "cobertura.ser");
		
		FileUtils.write(superSourceFile,
										"\n package mypackage;"+
										"\n " +
										"\n public class Super {" +
										"\n  static {" +
										"\n   Sub.aStaticMethod();" +
										"\n  };" +
										"\n }");
		
		FileUtils.write(subSourceFile, 
										"\n package mypackage;" +
										"\n " +
										"\n public class Sub extends Super {" +
										"\n  " +
										"\n  public static void aStaticMethod() {" +
										"\n   System.out.println(\"aStaticMethod called\");" +
										"\n  }" +
										"\n  " +
										"\n }");
		
		// compile to the srcDir
		TestUtils.compileSource(ant, srcDir);
		
		// instrument all but the test class (in place)
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir, new HashMap(){{put("excludeClassesRegexList", "mypackage.MyTest");}});
		
		// run the MyTest
		TestUtils.junit(new HashMap(){{
			put("testClass", "mypackage.MyTest");
			put("ant", ant);
			put("buildDir", srcDir);
			put("instrumentDir", instrumentDir);
			put("reportDir", reportDir);
		}});

		/*
		* Now create a cobertura xml file and make sure the correct counts are in it.
		*/
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();
		
		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath() + "/coverage.xml");
		
		List<Node> lines = TestUtils.getLineCounts(dom, "mypackage.Sub", "aStaticMethod");
		
		Node aStaticMethodLine = null;
		for(Node line : lines) {
			if ("7".equals(line.attribute("number"))) {
				aStaticMethodLine = line;
				break;
			}
		}
		assertEquals(1, Integer.valueOf(aStaticMethodLine.attribute("hits").toString()).intValue());
	}

	@Test
	public void simpleFunctionalTest() throws Exception {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File reportDir = new File(tempDir, "report");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File simpleSourceFile = new File(srcDir, "mypackage/Simple.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();
		
		FileUtils.write(simpleSourceFile, 
										"\n package mypackage;" +
										"\n " +
										"\n public class Simple {" +
										"\n }");
			
		FileUtils.write(mainSourceFile, 
										"\n package mypackage;" +
										"\n " +
										"\n " +
										"\n " +
										"\n public class Main {" +
										"\n " +
										"\n " +
										"\n  public boolean isSimple() {" +
										"\n   return false;" +
										"\n  }" +
										"\n " +
										"\n " +
										"\n  private Object create() {" +
										"\n   if (isSimple()) {" +
										"\n    Object result = new Simple();" +
										"\n   } else {" +
										"\n    Object result = new Main();" +
										"\n   }" +
										"\n   return null;" +
										"\n " +
										"\n  }" +
										"\n }");

		TestUtils.compileSource(ant, srcDir);
		
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);
	}
}