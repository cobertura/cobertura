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

package net.sourceforge.cobertura.test

import net.sourceforge.cobertura.test.util.TestUtil

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class SwitchFunctionalTest {
	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
	def testUtil = new TestUtil()

	@Test
	void noDefaultTest() {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has a switch statement with no default.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def reportDir = new File(tempDir, "report")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			/*
			 * Note that if the code below is changed, the line numbers in the
			 * asserts at the bottom of this method will likely need to be adjusted.
			 */
			mainSourceFile.write """
package mypackage;

public class Main {
	public void callNoDefaultSwitch() {
        int i=2;
        switch (i) {
         case 1:
             System.out.println("1");
             break;
         case 2:
             System.out.println("2");
             break;
        }
	}

    public void callSwitchFallThrough() {
        switchFallThrough(5);
        switchFallThrough(1);
    }

    private void switchFallThrough(int i) {
        switch (i) {
         case 2:
             System.out.println("2");
          break;
         case 3:
          break;
         case 1: // fall-through!
         default:
             System.out.println("1 or default");
        }
    }
    
	public void callSwitchWithDefault() {
		switchWithDefault(15);
	}

	private void switchWithDefault(int i) {
		switch (i) {
		case 15:
			System.out.println("1");
			break;
		case 16:
			System.out.println("2");
			break;
		case 17:
			System.out.println("3");
			break;
		case 18:
			System.out.println("4");
			break;
		// intentionally skip 19 and 20
		case 21:
			System.out.println("5");
			break;
		/*
		 * The compiler will add cases for any numbers that are skipped.
		 * Note the next two cases are commented out, but the compiler
		 * adds them to the default case.
		 */
		//case 19:
		//case 20:
        default:
			System.out.println("default");
        }
    }

	public void callSwitchWithoutGaps() {
		switchWithoutGaps(1);
	}

	private void switchWithoutGaps(int i) {
		boolean aBoolean = false;
		
		switch (i) {                   // tests assume this is line 76
		// these cases have no gaps - the numbers are sequential
		case 0:
			System.out.println("0");
			break;
		case 1:
			System.out.println("1");
			break;
		case 2:
			System.out.println("2");
			break;
		default:
			System.out.println("default");
			break;
		}
    }

	public void callSwitchWithoutGapsWithFallThrough() {
		switchWithoutGapsWithFallThrough(1);
	}

	private void switchWithoutGapsWithFallThrough(int i) {
		boolean aBoolean = false;
		
		switch (i) {                   // tests assume this is line 100
		// these cases have no gaps - the numbers are sequential
		case 0:
			System.out.println("0");
			break;
		case 1:
			if (aBoolean) {
				System.out.println("1");
				// the break is intentionally put inside this block to cause a fall-through
				break;
			}
		case 2:
			System.out.println("2");
			break;
		default:
			System.out.println("default");
			break;
		}
    }

	public enum AnEnumeration {
		FOO, BAR, GONK
	};

	public void callSwitchBug2075537() {
		switchBug2075537(AnEnumeration.FOO);
		switchBug2075537(AnEnumeration.BAR);
		switchBug2075537(AnEnumeration.GONK);
	}

	public boolean switchBug2075537(AnEnumeration e) {
		// see bug http://sourceforge.net/tracker/?func=detail&aid=2075537&group_id=130558&atid=720015
		switch (e) {                     // tests assume this is line 132
			case FOO:
			case BAR:
			case GONK:
				return true;

			default:
				return false;
		}
	}

	public void callSwitchBug2075537_2() {
		switchBug2075537_2(AnEnumeration.FOO);
		switchBug2075537_2(AnEnumeration.BAR);
		switchBug2075537_2(AnEnumeration.GONK);
	}

	public boolean switchBug2075537_2(AnEnumeration e) {
		switch (e) {                     // tests assume this is line 150
			case FOO:
				return true;

			case BAR:
				return true;

			case GONK:
				return true;

			default:
				return false;
		}
	}

	public void callSwitchNonDefaultFallThrough() {
		switchNonDefaultFallThrough(AnEnumeration.FOO);
	}

	public boolean switchNonDefaultFallThrough(AnEnumeration e) {
		switch (e) {                     // tests assume this is line 170
			case FOO:
				System.out.println("FOO");

			case BAR:
				System.out.println("BAR");
				return true;

			case GONK:
				return true;

			default:
				return false;
		}
	}

	public void callSwitchWithBoolean() {
		switchWithBoolean(1, false);
		switchWithBoolean(3, false);
		switchWithBoolean(7, true);
		switchWithBoolean(7, false);
		switchWithBoolean(8, true);
	}

	public void switchWithBoolean(int type, boolean aBoolean) {
		switch (type) {                     // tests assume this is line 195
			case 9:{
				System.out.println("9");
				break;
			}
			case 1:{
				try {
					System.out.println("1");
				} catch (Exception e) {}
				break;                     // tests assume this is line 204
			}
			case 5:{
				System.out.println("5");
				break;
			}
			case 4:{
				System.out.println("4");
				break;
			}
			case 3:{
				System.out.println("3");
				break;
			}
			case 7:{
				if (aBoolean) {
					System.out.println("7");
					break;
				}
				// if aBoolean == false, this will fall through to the next case.
			}
			case 8:{
				if (aBoolean) {
					System.out.println("8");
					break;
				}
			}
		}
	}
	
	enum ABCDEnum { A,B,C,D;};
	
	public void switchWithEnum(ABCDEnum value) {
		switch (value) {			// tests assume this is line 237
			case B: 
			    System.out.println("B");
			    break;
			case C:
				System.out.println("C");
			case A:
				System.out.println("A");
				break;
			default:
				System.out.println("default");
		}	
	}
	
	public void switchWithAllEnumValues(ABCDEnum value) {
		switch (value) {		// tests assume this is line 252
			case B: 
			    System.out.println("B");
			    break;
			case C:
				System.out.println("C");
			case A:
				System.out.println("A");
				break;
			case D:
				System.out.println("D");
				break;
			default:
				// Not reachable, but compiler does not mark it as dead code.
				System.out.println("default");	// tests assume this is line 266
		}	
	}
	
	public void switchWithAllButDefaultEnumValues(ABCDEnum value) {
		switch (value) {			// tests assume this is line 271		
			case B: 
			    System.out.println("B");
			    break;
			case C:
				System.out.println("C");
			case A:
				System.out.println("A");
				break;
			case D:
				System.out.println("D");
				break;
		}	
	}
	
	public void callSwitchWithEnum() {
		switchWithEnum(ABCDEnum.B);
		switchWithEnum(ABCDEnum.C);
		switchWithEnum(ABCDEnum.D);
		switchWithEnum(ABCDEnum.B);
	}
	
	public void callSwitchWithAllEnumValues() {
		switchWithAllEnumValues(ABCDEnum.A);
		switchWithAllEnumValues(ABCDEnum.B);
		switchWithAllEnumValues(ABCDEnum.C);
		switchWithAllEnumValues(ABCDEnum.D);
	}
	
	public void callSwitchWithAllButDefaultEnumValues() {
		switchWithAllButDefaultEnumValues(ABCDEnum.A);
		switchWithAllButDefaultEnumValues(ABCDEnum.B);
		switchWithAllButDefaultEnumValues(ABCDEnum.C);
		switchWithAllButDefaultEnumValues(ABCDEnum.D);
	}
	

	public static void main(String[] args) {
		Main main = new Main();
		main.callNoDefaultSwitch();
		main.callSwitchFallThrough();
		main.callSwitchWithDefault();
		main.callSwitchWithoutGaps();
		main.callSwitchWithoutGapsWithFallThrough();
		main.callSwitchBug2075537();
		main.callSwitchBug2075537_2();
		main.callSwitchNonDefaultFallThrough();
		main.callSwitchWithBoolean();
		main.callSwitchWithEnum();
		main.callSwitchWithAllEnumValues();
		main.callSwitchWithAllButDefaultEnumValues();
	}	
}
			"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir)
			
			/*
			 * Kick off the Main (instrumented) class.
			 */
			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
				classpath {
					dirset(dir:instrumentDir)
					dirset(dir:testUtil.coberturaClassDir)
				}
			}

			/*
			* Now create a cobertura html report and make sure the files are created.
			*/
			ant.'cobertura-report'(datafile:datafile, format:'html', destdir:reportDir, srcdir:srcDir)
			assertTrue(new File(reportDir, "index.html").exists())
			assertTrue(new File(reportDir, "mypackage.Main.html").exists())

			/*
			 * Now create a cobertura xml file and make sure the correct counts are in it.
			 */
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
			
			def lines	
			
			//switchWithBoolean
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithBoolean')
			
			def switchWithBooleanLine = lines.grep {it.number == '195'}[0]
			assertEquals('50% (4/8)', switchWithBooleanLine.conditionCoverage)

			
			/*
			 * A try catch just before a break statement used to cause Cobertura
			 * to report the line with the break as uncovered.  Make sure
			 * this no longer happens.
			 */
			// Unhapilly Java compiler is messing the code too much to support the case.			  
   			// def breakInSwitchWithBooleanLine = lines.grep {it.number == '204'}[0]
			// assertEquals(1, breakInSwitchWithBooleanLine.hits)			

			//switchNonDefaultFallThrough
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchNonDefaultFallThrough')
			
			def nonDefaultFallThroughSwitchLine = lines.grep {it.number == '170'}[0]
			assertEquals('33% (1/3)', nonDefaultFallThroughSwitchLine.conditionCoverage)			

			//switchBug2075537
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchBug2075537')
			
			def bug2075537SwitchLine = lines.grep {it.number == '132'}[0]
			assertEquals('50% (1/2)', bug2075537SwitchLine.conditionCoverage)

			

			//switchBug2075537_2
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchBug2075537_2')
			
			def bug2075537SwitchLine_2 = lines.grep {it.number == '150'}[0]
			assertEquals('100% (3/3)', bug2075537SwitchLine_2.conditionCoverage)
			

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithoutGaps')
			
			def noGapsSwitchLine = lines.grep {it.number == '76'}[0]
			assertEquals('25% (1/4)', noGapsSwitchLine.conditionCoverage)

			
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithoutGapsWithFallThrough')
			
			def noGapsWithFallThroughSwitchLine = lines.grep {it.number == '100'}[0]
			assertEquals('25% (1/4)', noGapsWithFallThroughSwitchLine.conditionCoverage)

			
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'callNoDefaultSwitch')
			
			def noDefaultSwitchLine = lines.grep {it.number == '7'}[0]
			assertEquals('33% (1/3)', noDefaultSwitchLine.conditionCoverage)

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchFallThrough')
			
			def fallThroughSwitchLine = lines.grep {it.number == '23'}[0]
   			assertEquals('33% (1/3)', fallThroughSwitchLine.conditionCoverage)

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithDefault')
			
			def withDefaultSwitchLine = lines.grep {it.number == '40'}[0]
			assertEquals('16% (1/6)', withDefaultSwitchLine.conditionCoverage)
			
			
			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithEnum')
			def withEnumSwitchLine = lines.grep {it.number == '237'}[0]
			assertEquals('75% (3/4)', withEnumSwitchLine.conditionCoverage)

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithAllEnumValues')
			def withAllEnumValueSwitchLine = lines.grep {it.number == '252'}[0]
			assertEquals('100% (4/4)', withAllEnumValueSwitchLine.conditionCoverage)

			def withAllEnumValuesDefaultLine = lines.grep {it.number == '266'}[0]
			assertEquals(0, withAllEnumValuesDefaultLine.hits)

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchWithAllButDefaultEnumValues')
			def withAllButDefaultEnumValuesSwitchLine = lines.grep {it.number == '271'}[0]
			assertEquals('100% (4/4)', withAllButDefaultEnumValuesSwitchLine.conditionCoverage)			
		}
	}

}
