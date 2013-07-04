/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2011 Piotr Tabor
 * Copyright (C) 2011 John Lewis
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
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LargeFileTest {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils
			.getCoberturaClassDir());
	Node dom;

	@Test
	public void largeFileTest() throws Exception {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has trivial methods such as .
		 */
		File tempDir = TestUtils.getTempDir();
		FileUtils.deleteDirectory(tempDir);

		File srcDir = new File(tempDir, "src");
		File reportDir = new File(tempDir, "report");
		File instrumentDir = new File(tempDir, "instrument");

		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();

		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			content.append("if (i < 1000) {i++;};\n");
		}

		FileUtils.write(mainSourceFile, "\n package mypackage;" + "\n "
				+ "\n public class Main extends Thread {"
				+ "\n public static void method1(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method2(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method3(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method4(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method5(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method6(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method7(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method8(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n public static void method9(){int i =0; "
				+ content.toString()
				+ " };"
				+ "\n 		"
				+ "\n public static void main(String[] args) {"
				+ "\n method1();"
				+ "\n method2();"
				+ "\n method3();"
				+ "\n method4();"
				+ "\n method5();"
				+ "\n method6();"
				+ "\n method7();"
				+ "\n method8();"
				+ "\n method9();"
				+ "\n 	}"
				+ "\n }");

		TestUtils.compileSource(ant, srcDir);

		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir,
				new HashMap() {
					{
						put("ignoretrivial", true);
					}
				});

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

		/* Now create a cobertura html report and make sure the files are created.
		 */
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("html");
		reportTask.setDestDir(reportDir);
		reportTask.setSrcDir(srcDir.getAbsolutePath());
		reportTask.execute();

		assertTrue(new File(reportDir, "index.html").exists());
		assertTrue(new File(reportDir, "mypackage.Main.html").exists());

		/*
		 * Now create a cobertura xml file and make sure the correct counts are in it.
		 */
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(srcDir);
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath()
				+ "/coverage.xml");

		//look for the last method - method9
		List<Node> lines = TestUtils.getLineCounts(dom, "mypackage.Main",
				"method9");

		// take lines greater than 4500 - any big number will do
		boolean found = false;
		for (Node node : lines) {
			if (Integer.valueOf((String) node.attribute("number")) > 4500
					&& Integer.valueOf((String) node.attribute("number")) < 4513) {
				assertEquals("50% (1/2)", (String) node
						.attribute("condition-coverage"));
				found = true;
			}
		}

		assertTrue("Did not find line number larger than 4500.", found);
	}
}
