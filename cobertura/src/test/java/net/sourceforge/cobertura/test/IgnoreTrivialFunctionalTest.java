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

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class IgnoreTrivialFunctionalTest extends AbstractCoberturaTestCase {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils
			.getCoberturaClassDir());
	TestUtils testUtil = new TestUtils();
	Node dom;
	IgnoreUtil ignoreUtil;

	@Test
	public void ignoreTrivialTest() throws Exception {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has trivial methods such as .
		 */
		File tempDir = TestUtils.getTempDir();
		File srcDir = new File(tempDir, "src");
		File reportDir = new File(tempDir, "report");
		File instrumentDir = new File(tempDir, "instrument");
		instrumentDir.mkdirs();

		File mainSourceFile = new File(srcDir, "mypackage/Main.java");
		File interfaceSourceFile = new File(srcDir,
				"mypackage/MyInterface.java");
		File datafile = new File(srcDir, "cobertura.ser");
		mainSourceFile.getParentFile().mkdirs();

		FileUtils.write(interfaceSourceFile, "\n package mypackage;" + "\n "
				+ "\n public interface MyInterface {"
				+ "\n 	public void myInterfaceMethod();" + "\n }");

		FileUtils
				.write(
						mainSourceFile,
						"\n package mypackage;"
								+ "\n "
								+ "\n public class Main extends Thread {"
								+ "\n 			"
								+ "\n 	public static class MyObject implements MyInterface"
								+ "\n 	{"
								+ "\n 		public void myInterfaceMethod()"
								+ "\n 		{"
								+ "\n 		}"
								+ "\n 	}"
								+ "\n "
								+ "\n 	public static void main(String[] args) {"
								+ "\n 		Main main = new Main();"
								+ "\n 			"
								+ "\n 		/*"
								+ "\n 		 * Call all methods so they will be considered \"covered\" unless"
								+ "\n 		 * they are ignored as trivial."
								+ "\n 		 *"
								+ "\n 		 * These are in no particular order."
								+ "\n 		 */"
								+ "\n 		main.getterTrivial();"
								+ "\n 		main.empty();"
								+ "\n 		main.getVoid();"
								+ "\n 		main.getIntWithIntParm(0);"
								+ "\n 		main.isBool();"
								+ "\n 		main.hasBool();"
								+ "\n 		main.set();"
								+ "\n 		main.setInt(1);"
								+ "\n 		main.setIntWithTwoParms(1, 2);"
								+ "\n 		main.getMultiDimArray();"
								+ "\n 		main.setIncrement(1);"
								+ "\n 		main.setConst(\"\");"
								+ "\n 		main.getArray();"
								+ "\n 		main.getObject();"
								+ "\n 		main.getStatic();"
								+ "\n 		main.setStatic(1);"
								+ "\n 		main.setISTORE(1);"
								+ "\n 		main.setLSTORE(1);"
								+ "\n 		main.setFSTORE((float)1.0);"
								+ "\n 		main.setDSTORE(1.0);"
								+ "\n 		main.setASTORE(null);"
								+ "\n 		main.getINVOKEVIRTUAL();"
								+ "\n 		main.getINVOKESPECIAL();"
								+ "\n 		main.getINVOKESTATIC();"
								+ "\n 		main.setINVOKEINTERFACE(new MyObject());"
								+ "\n 			"
								+ "\n 		// call constructors in no particular order"
								+ "\n 		new Main(1);"
								+ "\n 		new Main(true);"
								+ "\n 		new Main(\"str\");"
								+ "\n 		new Main(\"\", \"\");"
								+ "\n 		new Main(\"\", 0);"
								+ "\n 		new Main(\"\", true);"
								+ "\n 		new Main((Thread) null, \"string\");"
								+ "\n 		new Main((Thread) null, 0);"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// Be careful when initializing members.  If you instantiate an"
								+ "\n 	// object, then trivial constructors will become non-trivial."
								+ "\n 	// Ex. Integer myInteger = new Integer(1); will cause Main() to be non-trivial."
								+ "\n 	int myint;"
								+ "\n 	boolean mybool;"
								+ "\n 	private static int mystatic;"
								+ "\n 			"
								+ "\n 	// trivial constructor"
								+ "\n 	public Main() {"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// constructors that just call super() are trivial"
								+ "\n 	public Main(Thread t, String str)"
								+ "\n 	{"
								+ "\n 		super(str);"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	// constructors that just call super() are usually trivial, but"
								+ "\n 	// this constructor uses a constant, so it is considered non-trivial."
								+ "\n 	public Main(Thread t, int i)"
								+ "\n 	{"
								+ "\n 		super(\"string\");"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Main(boolean bool) {"
								+ "\n 		// non-trivial conditional"
								+ "\n 		myint = bool ? 0 : 1;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Main(int num) {"
								+ "\n 		// non-trivial switch"
								+ "\n 		switch(num) {"
								+ "\n 			default:"
								+ "\n 		}"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Main(String str) {"
								+ "\n 		// setting of statics is non-trivial"
								+ "\n 		mystatic = 2;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Main(String str1, String str2)"
								+ "\n 	{"
								+ "\n 		// non-trivial method call"
								+ "\n 		privateMethod();"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	public Main(String str1, int i)"
								+ "\n 	{"
								+ "\n 		// non-trivial object construction"
								+ "\n 		new StringBuffer();"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Main(String str1, boolean bool)"
								+ "\n 	{"
								+ "\n 		// non-trivial this() call"
								+ "\n 		this(str1, 0);"
								+ "\n 	}"
								+ "\n 	"
								+ "\n "
								+ "\n 	// trivial getter"
								+ "\n 	public int getterTrivial() {"
								+ "\n 		return myint;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// trivial getter"
								+ "\n 	public boolean isBool() {"
								+ "\n 		return mybool;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// trivial getter"
								+ "\n 	public boolean hasBool() {"
								+ "\n 		return mybool;"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	// trivial setter"
								+ "\n 	public void setInt(int i) {"
								+ "\n 		myint = i;"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	// this would be trivial, but it is a getter that with no return value"
								+ "\n 	public void getVoid() {"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// \"empty\" does not start with \"get\", \"is\", \"has\", or \"set\", so"
								+ "\n 	// it is considered non-trivial."
								+ "\n 	private int empty() {"
								+ "\n 		return 0;"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	// this is a getter that takes a parameter, so it is non-trivial."
								+ "\n 	public int getIntWithIntParm(int i) {"
								+ "\n 		return 0;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// this would be a trivial setter, but it does not have a parameter."
								+ "\n 	public void set() {"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	// this would be a trivial setter, but it has more than one parameter."
								+ "\n 	public void setIntWithTwoParms(int i, int j) {"
								+ "\n 		myint = i;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	public int[][] getMultiDimArray() {"
								+ "\n 		// non-trivial construction of a multi-dimensional array"
								+ "\n 		return new int[1][1];"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public void setIncrement(int i) {"
								+ "\n 		// non-trivial increment of local variable"
								+ "\n 		i++;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public void setConst(String str) {"
								+ "\n 		/*"
								+ "\n 		 * cause visitLdcInsn to be called because \"str\" is in the"
								+ "\n 		 * runtime constant pool.  An LDC operation is performed"
								+ "\n 		 * which is considered non-trivial."
								+ "\n 		 */"
								+ "\n 		System.out.println(\"str\");"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public int[] getArray() {"
								+ "\n 		// causes visitIntInsn to be called.  Creating an array is a \"single int operand\"."
								+ "\n 		// non-trivial."
								+ "\n 		return new int[1];"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public Object getObject() {"
								+ "\n 		// causes visitTypeInsn to be called.  Creating an object is a type instruction."
								+ "\n 		// non-trivial."
								+ "\n 		return new Object();"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public int getStatic() {"
								+ "\n 		// getting a static is non-trivial."
								+ "\n 		return mystatic;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	public void setStatic(int i) {"
								+ "\n 		// setting a static is non-trivial."
								+ "\n 		mystatic = i;"
								+ "\n 	}"
								+ "\n 		"
								+ "\n 	// non-trivial local variable instruction (causes visitVarInsn(ISTORE)) (int store to local var)"
								+ "\n 	public void setISTORE(int i) {"
								+ "\n 		i = 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial local variable instruction (causes visitVarInsn(LSTORE)) (long store to local var)"
								+ "\n 	public void setLSTORE(long l) {"
								+ "\n 		l = 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial local variable instruction (causes visitVarInsn(FSTORE)) (floating store to local var)"
								+ "\n 	public void setFSTORE(float f) {"
								+ "\n 		f = 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial local variable instruction (causes visitVarInsn(DSTORE)) (double store to local var)"
								+ "\n 	public void setDSTORE(double d) {"
								+ "\n 		d = 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial local variable instruction (causes visitVarInsn(ASTORE)) (object store to local var)"
								+ "\n 	public void setASTORE(Object obj) {"
								+ "\n 		obj = null;"
								+ "\n 	}"
								+ "\n 			"
								+ "\n 	public void publicMethod() {"
								+ "\n 	}"
								+ "\n 	private void privateMethod() {"
								+ "\n 	}"
								+ "\n 	public static void staticMethod() {"
								+ "\n 	}"
								+ "\n 	"
								+ "\n 	// non-trivial public method call (causes visitMethodInsn(INVOKEVIRTUAL))"
								+ "\n 	public int getINVOKEVIRTUAL() {"
								+ "\n 		publicMethod();"
								+ "\n 		return 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial private method call (causes visitMethodInsn(INVOKESPECIAL)) "
								+ "\n 	public int getINVOKESPECIAL() {"
								+ "\n 		privateMethod();"
								+ "\n 		return 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial static method call (causes visitMethodInsn(INVOKESTATIC)) "
								+ "\n 	public int getINVOKESTATIC() {"
								+ "\n 		staticMethod();"
								+ "\n 		return 0;"
								+ "\n 	}"
								+ "\n "
								+ "\n 	// non-trivial interface method call (causes visitMethodInsn(INVOKEINTERFACE))"
								+ "\n 	public void setINVOKEINTERFACE(MyInterface obj) {"
								+ "\n 		obj.myInterfaceMethod();" + "\n 	}"
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
		Path classpath = new Path(TestUtils.project);
		DirSet dirSetInstrumentDir = new DirSet();
		DirSet dirSetSrcDir = new DirSet();
		dirSetInstrumentDir.setDir(instrumentDir);
		dirSetSrcDir.setDir(srcDir);
		classpath.addDirset(dirSetInstrumentDir);
		classpath.addDirset(dirSetSrcDir);
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

		dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath()
				+ "/coverage.xml");

		ignoreUtil = new IgnoreUtil("mypackage.Main", dom);

		// trivial empty constructor
		assertIgnored("<init>", "()V");

		// trivial constructor Main(Thread, String) that just calls super()
		assertIgnored("<init>", "(Ljava/lang/Thread;Ljava/lang/String;)V");

		// trivial getter
		assertIgnored("getterTrivial");

		// isBool is trivial
		assertIgnored("isBool");

		// hasBool is trivial
		assertIgnored("hasBool");

		// setInt is trivial
		assertIgnored("setInt");

		// Main(int) has non-trivial switch
		assertNotIgnored("<init>", "(I)V");

		// Main(boolean) has non-trivial conditional
		assertNotIgnored("<init>", "(Z)V");

		// "empty" does not start with "get", "is", "has", or "set".
		assertNotIgnored("empty");

		// gets with no return are considered non-trivial
		assertNotIgnored("getVoid");

		// gets that have parameters are considered non-trivial
		assertNotIgnored("getIntWithIntParm");

		// sets that have no parameters are considered non-trivial
		assertNotIgnored("set");

		// sets that have more than one parameters are considered non-trivial
		assertNotIgnored("setIntWithTwoParms");

		// don't ignore methods with multi-dimensional array creates
		assertNotIgnored("getMultiDimArray");

		// don't ignore methods with increment instructions for local variables
		assertNotIgnored("setIncrement");

		// don't ignore methods with LDC instructions (that use constants from the runtime pool)
		assertNotIgnored("setConst");
		assertNotIgnored("<init>", "(Ljava/lang/Thread;I)V"); // Main(Thread, int)

		// don't ignore methods with a single int operand (like creating an array).
		assertNotIgnored("getArray");

		// don't ignore methods with type instructions (like creating an object).
		assertNotIgnored("getObject");

		// don't ignore methods that use statics.
		assertNotIgnored("getStatic");
		assertNotIgnored("setStatic");
		assertNotIgnored("<init>", "(Ljava/lang/String;)V");

		// non-trivial local variable instructions (causes visitVarInsn call)
		assertNotIgnored("setISTORE");
		assertNotIgnored("setLSTORE");
		assertNotIgnored("setFSTORE");
		assertNotIgnored("setDSTORE");
		assertNotIgnored("setASTORE");

		// non-trivial method calls
		assertNotIgnored("getINVOKEVIRTUAL");
		assertNotIgnored("getINVOKESPECIAL");
		assertNotIgnored("getINVOKESTATIC");
		assertNotIgnored("setINVOKEINTERFACE");
		assertNotIgnored("<init>", "(Ljava/lang/String;Ljava/lang/String;)V"); // Main(String, String)
		assertNotIgnored("<init>", "(Ljava/lang/String;I)V"); // Main(String, int)
		assertNotIgnored("<init>", "(Ljava/lang/String;Z)V"); // Main(String, boolean)

		/*
		 * Now create a cobertura html report and make sure the files are created.
		 */
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("html");
		reportTask.setDestDir(reportDir);
		reportTask.setSrcDir(srcDir.getAbsolutePath());
		reportTask.execute();

		assertTrue(new File(reportDir, "index.html").exists());
		assertTrue(new File(reportDir, "mypackage.Main.html").exists());
		assertTrue(new File(reportDir, "mypackage.MyInterface.html").exists());

		File frameSummaryFile = new File(reportDir, "frame-summary.html");
		assertTrue(frameSummaryFile.exists());

		TestUtils.checkFrameSummaryHtmlFile(frameSummaryFile);
	}

	public void assertIgnored(String methodName, String signature) {
		ignoreUtil.assertIgnored(methodName, signature);
	}

	public void assertIgnored(String methodName) {
		assertIgnored(methodName, null);
	}

	public void assertNotIgnored(String methodName, String signature) {
		ignoreUtil.assertNotIgnored(methodName, signature);
	}

	public void assertNotIgnored(String methodName) {
		assertNotIgnored(methodName, null);
	}
}
