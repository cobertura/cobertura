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

package net.sourceforge.cobertura.test

import net.sourceforge.cobertura.test.util.TestUtil

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class LargeFileTest {
	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
	def testUtil = new TestUtil()
	def dom
	def ignoreUtil

	@Test
	void largeFileTest() {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has trivial methods such as .
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def reportDir = new File(tempDir, "report")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			def content = '';
			for (i in 0..500) {
				content <<= "if (i < 1000) {i++;};\n";
			}
			
			mainSourceFile.write """
package mypackage;

public class Main extends Thread {
	public static void method1(){int i =0; ${content} };
	public static void method2(){int i =0; ${content} };
	public static void method3(){int i =0; ${content} };
	public static void method4(){int i =0; ${content} };
	public static void method5(){int i =0; ${content} };
	public static void method6(){int i =0; ${content} };
	public static void method7(){int i =0; ${content} };
	public static void method8(){int i =0; ${content} };
	public static void method9(){int i =0; ${content} };
				
	public static void main(String[] args) {
		method1();
		method2();
		method3();
		method4();
		method5();
		method6();
		method7();
		method8();
		method9();
	}
}
			"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir, [ignoretrivial:true])
			
			/*
			 * Kick off the Main (instrumented) class.
			 */
			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
				classpath {
					dirset(dir:instrumentDir)
					dirset(dir:srcDir)
					dirset(dir:testUtil.coberturaClassDir)
				}
			}

			/* Now create a cobertura html report and make sure the files are created.
			*/
			ant.'cobertura-report'(datafile:datafile, format:'html', destdir:reportDir, srcdir:srcDir)
			assertTrue(new File(reportDir, "index.html").exists())
			assertTrue(new File(reportDir, "mypackage.Main.html").exists())

			/*
			 * Now create a cobertura xml file and make sure the correct counts are in it.
			 */
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
			
			def lines	
			
			//look for the last method - method9
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'method9')
			
			// take lines greater than 4500 - any big number will do
			def bigLineNumberLines = lines.grep {it.number.toInteger() > 4500}
			assertFalse(bigLineNumberLines.isEmpty())
			
			// make sure each line has 50% coverage
			bigLineNumberLines[0..-2].each { line ->
				assertEquals('50% (1/2)', line.conditionCoverage)
			}
		}
	}

}
