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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IgnoreMethodAnnotationFunctionalTest {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());
	IgnoreUtil ignoreUtil;
	Node dom;
	
	@Before
	public void setUp() throws IOException {
		FileUtils.deleteQuietly(new File(TestUtils.getTempDir(), "src"));
		FileUtils.deleteQuietly(new File(TestUtils.getTempDir(), "instrument"));
	}
	
	@Test
	public void ignoreMethodAnnotationTest() throws Exception {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has methods with annotations that we want to ignore.
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File instrumentDir = new File(tempDir, "instrument");
		
		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File annotationSourceFile = new File(srcDir, "mypackage/IgnoreAnnotation.java");
		File annotation2SourceFile = new File(srcDir, "mypackage/IgnoreAnnotation2.java");
		
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();
		
		FileUtils.write(annotationSourceFile,
											 "\n package mypackage;"+
											 "\n "+
											 "\n public @interface IgnoreAnnotation {}");
		
		
		FileUtils.write(annotation2SourceFile, 
											  "\n package mypackage;" +
											  "\n " +
											  "\n public @interface IgnoreAnnotation2 {}");
		FileUtils.write(mainSourceFile,
										"\n package mypackage;" +
										"\n " +
										"\n public class Main" +
										"\n {" +
										"\n 	" +		
										"\n public static void main(String[] args) {" +
										"\n Main main = new Main();" +
										"\n 			" +
										"\n 		/*" +
										"\n 		 * Call all methods so they will be considered \"covered\" unless" +
										"\n 		 * they are ignored." +
										"\n 		 *" +
										"\n 		 * These are in no particular order." +
										"\n 		 */" +
										"\n main.doNotIgnore();" +
										"\n main.ignore();" +
										"\n }" +
										"\n " +
										"\n public void doNotIgnore()" +
										"\n {" +
										"\n }" +
										"\n " +
										"\n @IgnoreAnnotation" +
										"\n public void ignore()" +
										"\n {" +
										"\n }" +
										"\n " +
										"\n 	@IgnoreAnnotation2" +
										"\n 	public void ignore2()" +
										"\n 	{" +
										"\n 	}" +
										"\n }");

		TestUtils.compileSource(ant, srcDir);
		
		TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir, 
				new HashMap(){{put("ignoreAnnotationNames", new ArrayList<String>(){{add("mypackage.IgnoreAnnotation");add("mypackage.IgnoreAnnotation2");}});}});
		
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
		
		ignoreUtil = new IgnoreUtil("mypackage.Main", dom);			
		
		assertNotIgnored("doNotIgnore");
		
		assertIgnored("ignore");
		
		assertIgnored("ignore2");
	}
	
	public void assertIgnored(String methodName) {
		ignoreUtil.assertIgnored(methodName, null);
	}

	public void assertNotIgnored(String methodName) {
		ignoreUtil.assertNotIgnored(methodName, null);
	}
}
