/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2010 Piotr Tabor
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

import groovy.util.AntBuilder;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class PerformanceTest {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils
			.getCoberturaClassDir());

	@Test
	public void performanceTest() throws Exception {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has trivial methods such as .
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File instrumentDir = new File(tempDir, "instrument");

		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();

		FileUtils
				.write(
						mainSourceFile,
						"\n package mypackage;"
								+ "\n "
								+ "\n public class Main extends Thread {"
								+ "\n 	public static void main(String[] args) {"
								+ "\n 		long start = System.nanoTime();"
								+ "\n 		int j = 0;"
								+ "\n 		for (int i = 0; i < 100000; i++) {"
								+ "\n 		   if (i % 2 == 0) { j+=2; };"
								+ "\n 		   switch (i % 4) {"
								+ "\n 		      case 0 : "
								+ "\n 		      case 1 : j++;"
								+ "\n 		      case 2 : j+=2;"
								+ "\n 		      default: j+=3;"
								+ "\n 		   } "
								+ "\n 		}"
								+ "\n 		long stop = System.nanoTime();"
								+ "\n 		System.out.println(\"Test took:\" + (stop - start)/100000.0 + \" milis\");"
								+ "\n 	}" + "\n }");

		TestUtils.compileSource(ant, srcDir);

		/*
		 * Kick off the Main (instrumented) class.
		 */
		System.out.println("Run without instrumentation:\n");
		Path classpath = new Path(TestUtils.project);
		DirSet dirSetSrcDir = new DirSet();
		dirSetSrcDir.setDir(srcDir);
		classpath.addDirset(dirSetSrcDir);

		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname("mypackage.Main");
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(classpath);
		java.setOutput(new File(tempDir, "PT_uninstrumented.log"));
		java.execute();

		System.out.println(FileUtils.readFileToString(new File(tempDir,
				"PT_uninstrumented.log")));

		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);

		System.out
				.println("Run with instrumentation (not threadsafe-rigorous):\n");

		classpath = new Path(TestUtils.project);
		DirSet dirSetInstrumentDir = new DirSet();
		dirSetSrcDir = new DirSet();
		dirSetInstrumentDir.setDir(instrumentDir);
		dirSetSrcDir.setDir(srcDir);
		classpath.addDirset(dirSetInstrumentDir);
		classpath.addDirset(dirSetSrcDir);
		classpath.addDirset(TestUtils.getCoberturaClassDirSet());

		java = new Java();
		java.setClassname("mypackage.Main");
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(classpath);
		java.setProject(TestUtils.project);
		java.setOutput(new File(tempDir, "PT_instrumentedNonThreadSafe.log"));
		java.execute();

		System.out.println(FileUtils.readFileToString(
				new File(tempDir, "PT_instrumentedNonThreadSafe.log"))
				.toString());

		TestUtils.compileSource(ant, srcDir);

		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir,
				new HashMap() {
					{
						put("threadsafeRigorous", true);
					}
				});

		System.out.println("Run with instrumentation (threadsafe-rigorous):\n");

		java = new Java();
		java.setClassname("mypackage.Main");
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(classpath);
		java.setProject(TestUtils.project);
		java.setOutput(new File(tempDir, "PT_instrumentedThreadSafe.log"));
		java.execute();
		System.out.println(FileUtils.readFileToString(new File(tempDir,
				"PT_instrumentedThreadSafe.log")));
		/*
		 * Now create a cobertura xml file and make sure the correct counts are in it.
		 */
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();
	}
}