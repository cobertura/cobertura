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

package net.sourceforge.cobertura.thread.test

import net.sourceforge.cobertura.test.util.TestUtil

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class ThreadedFunctionalTest {
	
	/*
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

	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())

	static numberOfCalls = 300 //this number can't be too high or you will get a stack overflow (due to the inject below)
	static numberOfThreads = 2
	static numberOfRetries = 10
	
	def branchOrSwitchCode
	
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
	static SWITCH_CODE = """
			switch(counter)
			{
				${(0..numberOfCalls).inject('') { cases, num -> "${cases}\ncase ${num}: counter++; break;"}}
				default: counter = 0;
			}
"""

	/*
	 * A big if statement with number of conditionals equal to numberOfCalls:
	 * 
	 * if (counter==0||counter==1||counter==2 ... )
	 * {
	 *    counter++;
	 * }
	 */
	static IF_STATEMENT_CODE = """
			if (${(1..numberOfCalls).inject('counter==0') { conditionals, num -> "${conditionals}||counter==${num}"}})
			{
				counter++;
			}
"""

	def getThreadedCode(branchOrSwitchCode)
	{
		/*
		 * This code will kick off a number of threads equal to numberOfThreads.
		 * Each thread will do a number of calls equal to numberOfCalls.
		 * Each call will be a call to the method acall().   The body of this
		 * method is passed in as branchOrSwithcCode and is either a switch
		 * statement with a large number of cases or an if statement with
		 * a large number of conditionals.
		 */
		return """
package mypackage;

import java.util.ArrayList;

class MyThreads extends Thread
{
		int counter = 0;

		void acall()
		{
			${branchOrSwitchCode}
		}

		public void run()
		{
			try
			{
				for (int i=0; i<${numberOfCalls}; i++)
				{
					yield();
					acall();
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				System.exit(1);
			}
		}

		public static void main(String[] args)
		{
			ArrayList threads = new ArrayList();
			for (int i=0; i<${numberOfThreads}; i++)
			{
				threads.add(new MyThreads());
			}
			for (int i=0; i<${numberOfThreads}; i++)
			{
				((Thread) threads.get(i)).start();
			}
		}
}
"""
	}

	@Before
	void setUp() {
	}
	
	@Test
	void simpleThreadTest() {
		ant.echo(message:"Running threaded test with switch statement.")
		runTest(SWITCH_CODE)
	}
		
	@Test
	void simpleThreadTestWithIfStatement() {
		ant.echo(message:"Running threaded test with if statement.")
		runTest(IF_STATEMENT_CODE)
	}

	private void runTest(code)
	{
		/*
		 * Use a temporary directory and create a MyThreads.java source file
		 * that creates multiple threads which do repetitive calls into
		 * a method.   The method contains either a switch with a large number
		 * of cases or an if statement with a large number of conditionals depending
		 * on the code passed into this method.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			
			def sourceFile = new File(srcDir, "mypackage/MyThreads.java")
			def datafile = new File(srcDir, "cobertura.ser")
			sourceFile.parentFile.mkdirs()
			
			sourceFile.write(getThreadedCode(code))
			
			compileSource(srcDir)
			
			instrumentClasses(srcDir, datafile)
			
			/*
			 * This test does not seem to fail all the time, so we will need to try several times.
			 */
			numberOfRetries.times { index ->
				ant.echo(message:"Try $index")
				ant.java(classname:'mypackage.MyThreads', dir:srcDir, fork:true, failonerror:true) {
					classpath {
						dirset(dir:srcDir)
						fileset(dir:'antLibrary/common/groovy') {
							include(name:'*.jar')
						}
						dirset(dir:TestUtil.getCoberturaClassDir())
					}
				}
			}
					
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
				
			def hitCount = TestUtil.getHitCount(dom, 'mypackage.MyThreads', 'acall')
			
			assertEquals("hit count incorrect", numberOfRetries * numberOfThreads * numberOfCalls, hitCount)
		}
	}
	
	def compileSource(srcDir)
	{
		ant.groovyc(srcdir:srcDir, destDir:srcDir) {
			javac(debug:'true')
		}
	}
	
	def instrumentClasses(srcDir, datafile)
	{
		ant.'cobertura-instrument'(datafile:datafile, threadsafeRigorous:true) {
			includeClasses(regex:'mypackage.*')
			fileset(dir:srcDir) {
				include(name:'**/*.class')
			}
		}
	}
}
