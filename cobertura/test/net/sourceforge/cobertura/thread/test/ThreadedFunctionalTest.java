package net.sourceforge.cobertura.thread.test;

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

import net.sourceforge.cobertura.ant.InstrumentTask;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.instrument.CoberturaInstrumenter;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.AbstractClasspathResource;
import org.apache.tools.ant.types.resources.JavaResource;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.xerces.parsers.DOMParser;
import org.codehaus.groovy.ant.Groovyc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.bcel.internal.util.ClassPath;

import groovy.util.AntBuilder;
import groovy.util.Node;

import java.io.*;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * This tests the thread safety of Cobertura.
 * 
 * Multiple threads are kicked off that will execute the same switch or
 * if statement.   The switch statement would like like this:
 * 
 * switch(counter)
 * {
 *    case 0: counter++; break;
 *    case 1: counter++; break;
 *    etc.
 *    default: counter = 0;
 * }
 * The if statement looks like this:
 * if (counter==0||counter==1||counter==2 ... )
 * {
 *    counter++;
 * }
 * 
 * Notice that the counter is incremented so the cases or conditionals are
 * hit in order with each call.
 * 
 * Multiple threads will process the switch or if statement at the same time.
 * Since the case and conditional information is handled with arrays that grow 
 * dynamically larger as each case or conditional is hit, you will get index 
 * out of bounds exceptions if Cobertura is not thread safe.   One thread
 * will increase the array while another is trying to do the same.  Only one
 * thread's change is saved, and the other thread is likely to throw an
 * out of bounds exception.
 */
public class ThreadedFunctionalTest {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());
	
	private static int numberOfCalls = 300; //this number can't be too high or you will get a stack overflow (due to the inject below)
	private static int numberOfThreads = 2;
	private static int numberOfRetries = 10;
	
	boolean branchOrSwitchCode;
	
	/*
	 * A big switch statement with number of cases equal to numberOfCalls:
	 * 
	 * switch(counter)
	 * {
	 *    case 0: counter++; break;
	 *    case 1: counter++; break;
	 *    etc.
	 *    default: counter = 0;
	 * }
	 */
	private static String SWITCH_CODE;

	/*
	 * A big if statement with number of conditionals equal to numberOfCalls:
	 * 
	 * if (counter==0||counter==1||counter==2 ... )
	 * {
	 *    counter++;
	 * }
	 */
	private static String IF_STATEMENT_CODE;
	
	static{
		StringBuilder tempBuilder = new StringBuilder();
		tempBuilder.append("\nswitch(counter)");
		tempBuilder.append("\n{");
		for (int i = 0; i < numberOfCalls; i++) {
			tempBuilder.append("\ncase " + i + ": counter++; break;");
		}
		tempBuilder.append("\ndefault: counter = 0; break;}");
		
		SWITCH_CODE = tempBuilder.toString();
		
		tempBuilder = new StringBuilder();
		tempBuilder.append("if (");
		for (int i = 0; i < numberOfCalls; i++) {
			tempBuilder.append("counter==" + i);
			if (i < numberOfCalls - 1) { // want to add || to everything except the last one
				tempBuilder.append("||");
			}
		}
		tempBuilder.append(") {\n counter++;\n }");
		
		IF_STATEMENT_CODE = tempBuilder.toString();
		
		
	}
	

	private String getThreadedCode(String branchOrSwitchCode)
	{
		/*
		 * This code will kick off a number of threads equal to numberOfThreads.
		 * Each thread will do a number of calls equal to numberOfCalls.
		 * Each call will be a call to the method acall().   The body of this
		 * method is passed in as branchOrSwithcCode and is either a switch
		 * statement with a large number of cases or an if statement with
		 * a large number of conditionals.
		 */
		return  "\npackage mypackage;" +
                "\n" +
                "\nimport java.util.ArrayList;" +
                "\n" +
                "\nclass MyThreads extends Thread" +
                "\n{" +
                "\n		int counter = 0;" +
                "\n" +
                "\n		void acall()" +
                "\n		{" +
                "\n			" + branchOrSwitchCode +
                "\n		}" +
                "\n" +
                "\n		public void run()" +
                "\n		{" +
                "\n			try" +
                "\n			{" +
                "\n				for (int i=0; i< " + numberOfCalls + "; i++)" +
                "\n				{" +
                "\n					yield();" +
                "\n					acall();" +
                "\n				}" +
                "\n			}" +
                "\n			catch (Throwable t)" +
                "\n			{" +
                "\n				t.printStackTrace();" +
                "\n				System.exit(1);" +
                "\n			}" +
                "\n		}" +
                "\n" +
                "\n		public static void main(String[] args)" +
                "\n		{" +
                "\n			ArrayList threads = new ArrayList();" +
                "\n			for (int i=0; i<"+ numberOfThreads +"; i++)" +
                "\n			{" +
                "\n				threads.add(new MyThreads());" +
                "\n			}" +
                "\n			for (int i=0; i<" + numberOfThreads + "; i++)" +
                "\n			{" +
                "\n				((Thread) threads.get(i)).start();" +
                "\n			}" +
                "\n		}" +
                "\n}";
	}
	
	@Test
	public void simpleThreadTest() throws Exception {
		Echo echo = new Echo();
		echo.setMessage("Running threaded test with switch statement.");
		echo.execute();

		runTest(SWITCH_CODE);
	}
		
	@Test
	public void simpleThreadTestWithIfStatement() throws Exception {
		Echo echo = new Echo();
		echo.setMessage("Running threaded test with if statement.");
		echo.execute();
		
		runTest(IF_STATEMENT_CODE);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File("/tmp/src"));
	}

	@Before
	public void setUp() throws IOException {
		FileUtils.deleteDirectory(new File("/tmp/src"));
	}

	private void runTest(String code) throws Exception
	{
		/*
		 * Use a temporary directory and create a MyThreads.java source file
		 * that creates multiple threads which do repetitive calls into
		 * a method.   The method contains either a switch with a large number
		 * of cases or an if statement with a large number of conditionals depending
		 * on the code passed into this method.
		 */
		
		File tempDir = TestUtils.getTempDir();
		final File srcDir = new File(tempDir, "src");
			
		File sourceFile = new File(srcDir, "mypackage/MyThreads.java");
		final File datafile = new File(srcDir, "cobertura.ser");
		sourceFile.getParentFile().mkdirs();
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(sourceFile));
			bw.write(getThreadedCode(code));
		} finally {
			bw.close();
		}

		compileSource(srcDir);
		
		instrumentClasses(srcDir, datafile);
		
		/*
		 * This test does not seem to fail all the time, so we will need to try several times.
		 */
		
		Path p = new Path(TestUtils.project);
		DirSet dirSet = new DirSet();
		FileSet fileSet = new FileSet();
		
		dirSet.setDir(srcDir);
		fileSet.setDir(new File("antLibrary/common/groovy"));
		fileSet.setIncludes("*.jar");
		
		p.addFileset(fileSet);
		p.addDirset(dirSet);
		p.setProject(TestUtils.project);
		p.addDirset(TestUtils.getCoberturaClassDirSet());
		
		for (int i = 0; i < numberOfRetries; i++) {
			System.out.println("Executing build: " + i);
			Java java = new Java();
			java.setClassname("mypackage.MyThreads");
			java.setDir(srcDir);
			java.setFork(true);
			java.setProject(TestUtils.project);
			java.setFailonerror(true);
			java.setClasspath(p);
			java.execute();
		}
			
		System.out.println("Starting reporting task.");
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();
		System.out.println("Finish reporting task.");
		
		
		Node dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath() + "/coverage.xml");
				
		int hitCount = TestUtils.getHitCount(dom, "mypackage.MyThreads", "acall");
			
		assertEquals("hit count incorrect", numberOfRetries * numberOfThreads * numberOfCalls, hitCount);
	}
	
	
	public void compileSource(final File srcDir)
	{
		System.out.println("Invoking groovyC command on " + srcDir.getAbsolutePath());
		Javac javac = new Javac();
		javac.setDebug(true);
		javac.setProject(TestUtils.project);
		
		Groovyc groovyc = new Groovyc();
		groovyc.setProject(TestUtils.project);
		groovyc.setSrcdir(new Path(TestUtils.project, srcDir.getAbsolutePath()));
		groovyc.setDestdir(srcDir);
		groovyc.addConfiguredJavac(javac);
		groovyc.execute();
		
		System.out.println("Finish invoking groovyC command.");
	}
	
	public void instrumentClasses(File srcDir, File datafile)
	{
		System.out.println("Start instrumenting classes.");
		FileSet fileset = new FileSet();
		fileset.setDir(srcDir);
		fileset.setIncludes("**/*.class");
		
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile(datafile.getAbsolutePath());
		instrumentTask.setThreadsafeRigorous(true);
		instrumentTask.addFileset(fileset);
		instrumentTask.execute();
		System.out.println("Finish instrumenting classes.");
	}
}