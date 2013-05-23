/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2009 John Lewis
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

package net.sourceforge.cobertura.thread.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.bcel.util.ClassPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.*; 

public class MultipleClassloaderFunctionalTest {
	
	/*
	 * This tests the case where there are multiple classloaders that each
	 * load the Cobertura classes.   Cobertura uses static fields to
	 * hold the data.   When there are multiple classloaders, each classloader
	 * will keep track of the line counts for the classes that it loads.  
	 * 
	 * The static initializers for the Cobertura classes are also called for
	 * each classloader.   So, there is one shutdown hook for each classloader.
	 * So, when the JVM exits, each shutdown hook will try to write the
	 * data it has kept to the datafile.   They will do this at the same
	 * time.   Before Java 6, this seemed to work fine, but with Java 6, there
	 * seems to have been a change with how file locks are implemented.   So,
	 * care has to be taken to make sure only one thread locks a file at a time.
	 * 
	 */

	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());
	TestUtils testUtil = new TestUtils();

	private static String CALLED_CODE =
										"\n package mypackage;" +
										"\n " +
										"\n public class Called {" +
										"\n	 public static void callThis()" +
										"\n  {" +
										"\n 	}" +
										"\n }";

	@Before
	public void setUp() throws IOException {
		FileUtils.deleteDirectory(new File("/tmp/src"));
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File("/tmp/src"));
	}
	
	@Test
	public void multipleClassloadersTest() throws Exception {
		Echo echo = new Echo();
		echo.setMessage("Running multiple classloader test.");
		echo.execute();
		runTest();
	}
	
	/*
	 * This code creates a Java class that has a main method that will create two
	 * classloaders.   Each classloader will then load the mypackage.Called class
	 * defined above.  Then, the mypackage.Called.callThis() method is called once.
	 */
	private String getMainCode(File instrumentDir) {
		return
				"\n package mypackage;" +
				"\n " +
				"\n import java.net.URLClassLoader;" +
				"\n import java.net.URL;" +
				"\n import java.io.File;" +
				"\n import java.lang.reflect.Method;" +
				"\n " +
				"\n public class Main {" +
				"\n 	public static void main(String[] args) throws Throwable" +
				"\n 	{" +
				"\n 		createClassloaderAndCallMethod();" +
				"\n  		createClassloaderAndCallMethod();" +
				"\n 	}" +
				"\n " +
				"\n 	/*" +
				"\n 	 * Create a classloader that loads the instrumented code and the cobertura classes." +
				"\n 	 * Then, call the mypackage.Called.callThis() static method." +
				"\n 	 */" +
				"\n 	public static void createClassloaderAndCallMethod() throws Throwable" +
				"\n 	{" +
				"\n 		File instrumentDir = new File(\"" + instrumentDir.getAbsolutePath() + "\");" +
				"\n 		File coberturaClassDir = new File(\"" + TestUtils.getCoberturaClassDir().getAbsolutePath() + "\");" +
				"\n " +
				"\n 		/*" +
				"\n 		 * Create a classloader with a null parent classloader to ensure that this" +
				"\n 		 * classloader loads the Cobertura classes itself." +
				"\n 		 */" +
				"\n          URLClassLoader loader = new URLClassLoader(" +
				"\n         		new URL[] {instrumentDir.toURL(), coberturaClassDir.toURL()}, null);" +
				"\n " +
				"\n 		// use reflection to call mypackage.Called.callThis() once." +
				"\n         Class calledClass = loader.loadClass(\"mypackage.Called\");" +
				"\n         Method method = calledClass.getMethod(\"callThis\", null);" +
				"\n " +
				"\n         method.invoke(null, null);" +
				"\n 	}" +
				"\n }";
	}

	private void runTest() throws Exception
	{
		/*
		 * Use a temporary directory and create a Main.java source file
		 * that creates multiple classloaders.   Also, create a Called.java
		 * file that defines the mypackage.Called.callThis() method that
		 * will be called by mypackage.Main.main().
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(mainSourceFile));
			bw.write(getMainCode(instrumentDir));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			IOUtils.closeQuietly(bw);
		}
		
		File calledSourceFile = new File(srcDir, "mypackage/Called.java");
		
		try {
			bw = new BufferedWriter(new FileWriter(calledSourceFile));
			bw.write(CALLED_CODE);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			IOUtils.closeQuietly(bw);
		}
		
		TestUtils.compileSource(ant, srcDir);
		
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);
		
		/*
		 * Kick off the Main class.   I'll use the non-instrumented classes, but
		 * I think you could use the instrumented ones as well.
		 */
		DirSet dirSet = new DirSet();
		dirSet.setDir(srcDir);
		dirSet.setProject(TestUtils.project);
		Path classpath = new Path(TestUtils.project);
		classpath.addDirset(dirSet);
		classpath.addDirset(TestUtils.getCoberturaClassDirSet());

		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname("mypackage.Main");
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(classpath);
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
		
		
		Node dom = TestUtils.getXMLReportDOM( srcDir.getAbsolutePath() + "/coverage.xml");
			
		List<Node> lines = TestUtils.getLineCounts(dom, "mypackage.Called", "callThis", null);
		
		// the callThis() method is empty, so the only line is the ending brace.
		assertEquals(1, lines.size());
		
		for(int i = 0; i < lines.size(); i++) {
			assertEquals("hit count incorrect", "2", lines.get(i).attribute("hits"));
		}
	}
}
