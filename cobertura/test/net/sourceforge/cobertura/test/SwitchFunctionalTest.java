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
import java.util.List;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SwitchFunctionalTest extends AbstractCoberturaTestCase {
	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getCoberturaClassDir());
	
	@Test
	public void noDefaultTest() throws Exception {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has a switch statement with no default.
		 */
		mainSourceFile.getParentFile().mkdirs();
		
		/*
		 * Note that if the code below is changed, the line numbers in the
		 * asserts at the bottom of this method will likely need to be adjusted.
		 */
		FileUtils.write(mainSourceFile,
										"\n package mypackage;" +
										"\n " +
										"\n public class Main {" +
										"\n   public void callNoDefaultSwitch() {" +
										"\n    int i=2;" +
										"\n    switch (i) {" +
										"\n     case 1:" +
										"\n      System.out.println(\"1\");" +
										"\n      break;" +
										"\n     case 2:" +
										"\n      System.out.println(\"2\");" +
										"\n     break;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchFallThrough() {" +
										"\n   switchFallThrough(5);" +
										"\n   switchFallThrough(1);" +
										"\n  }" +
										"\n " +
										"\n  private void switchFallThrough(int i) {" +
										"\n   switch (i) {" +
										"\n    case 2:" +
										"\n     System.out.println(\"2\");" +
										"\n     break;" +
										"\n    case 3:" +
										"\n     break;" +
										"\n    case 1: // fall-through!" +
										"\n    default:" +
										"\n     System.out.println(\"1 or default\");" +
										"\n   }" +
										"\n  }" +
										"\n     " +
										"\n  public void callSwitchWithDefault() {" +
										"\n   switchWithDefault(15);" +
										"\n  }" +
										"\n " +
										"\n  private void switchWithDefault(int i) {" +
										"\n   switch (i) {" +
										"\n    case 15:" +
										"\n     System.out.println(\"1\");" +
										"\n     break;" +
										"\n    case 16:" +
										"\n     System.out.println(\"2\");" +
										"\n     break;" +
										"\n    case 17:" +
										"\n     System.out.println(\"3\");" +
										"\n     break;" +
										"\n    case 18:" +
										"\n     System.out.println(\"4\");" +
										"\n     break;" +
										"\n     // intentionally skip 19 and 20" +
										"\n    case 21:" +
										"\n     System.out.println(\"5\");" +
										"\n     break;" +
										"\n    /*" +
										"\n     * The compiler will add cases for any numbers that are skipped." +
										"\n     * Note the next two cases are commented out, but the compiler" +
										"\n     * adds them to the default case." +
										"\n     */" +
										"\n    //case 19:" +
										"\n    //case 20:" +
										"\n    default:" +
										"\n     System.out.println(\"default\");" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithoutGaps() {" +
										"\n   switchWithoutGaps(1);" +
										"\n  }" +
										"\n " +
										"\n  private void switchWithoutGaps(int i) {" +
										"\n   boolean aBoolean = false;" +
										"\n " +
										"\n   switch (i) {                   // tests assume this is line 76" +
										"\n    // these cases have no gaps - the numbers are sequential" +
										"\n    case 0:" +
										"\n     System.out.println(\"0\");" +
										"\n     break;" +
										"\n    case 1:" +
										"\n     System.out.println(\"1\");" +
										"\n     break;" +
										"\n    case 2:" +
										"\n     System.out.println(\"2\");" +
										"\n     break;" +
										"\n    default:" +
										"\n     System.out.println(\"default\");" +
										"\n     break;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithoutGapsWithFallThrough() {" +
										"\n   switchWithoutGapsWithFallThrough(1);" +
										"\n  }" +
										"\n " +
										"\n  private void switchWithoutGapsWithFallThrough(int i) {" +
										"\n   boolean aBoolean = false;" +
										"\n " +
										"\n   switch (i) {                   // tests assume this is line 100" +
										"\n   // these cases have no gaps - the numbers are sequential" +
										"\n    case 0:" +
										"\n     System.out.println(\"0\");" +
										"\n     break;" +
										"\n    case 1:" +
										"\n     if (aBoolean) {" +
										"\n      System.out.println(\"1\");" +
										"\n      // the break is intentionally put inside this block to cause a fall-through" +
										"\n      break;" +
										"\n     }" +
										"\n    case 2:" +
										"\n     System.out.println(\"2\");" +
										"\n     break;" +
										"\n    default:" +
										"\n     System.out.println(\"default\");" +
										"\n     break;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public enum AnEnumeration {" +
										"\n   FOO, BAR, GONK" +
										"\n  };" +
										"\n " +
										"\n  public void callSwitchBug2075537() {" +
										"\n   switchBug2075537(AnEnumeration.FOO);" +
										"\n   switchBug2075537(AnEnumeration.BAR);" +
										"\n   switchBug2075537(AnEnumeration.GONK);" +
										"\n  }" +
										"\n " +
										"\n  public boolean switchBug2075537(AnEnumeration e) {" +
										"\n   // see bug http://sourceforge.net/tracker/?func=detail&aid=2075537&group_id=130558&atid=720015" +
										"\n   switch (e) {                     // tests assume this is line 132" +
										"\n    case FOO:" +
										"\n    case BAR:" +
										"\n    case GONK:" +
										"\n     return true;" +
										"\n " +
										"\n    default:" +
										"\n     return false;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchBug2075537_2() {" +
										"\n   switchBug2075537_2(AnEnumeration.FOO);" +
										"\n   switchBug2075537_2(AnEnumeration.BAR);" +
										"\n   switchBug2075537_2(AnEnumeration.GONK);" +
										"\n  }" +
										"\n " +
										"\n  public boolean switchBug2075537_2(AnEnumeration e) {" +
										"\n   switch (e) {                     // tests assume this is line 150" +
										"\n    case FOO:" +
										"\n     return true;" +
										"\n " +
										"\n    case BAR:" +
										"\n     return true;" +
										"\n " +
										"\n    case GONK:" +
										"\n     return true;" +
										"\n " +
										"\n    default:" +
										"\n     return false;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchNonDefaultFallThrough() {" +
										"\n   switchNonDefaultFallThrough(AnEnumeration.FOO);" +
										"\n  }" +
										"\n " +
										"\n  public boolean switchNonDefaultFallThrough(AnEnumeration e) {" +
										"\n   switch (e) {                     // tests assume this is line 170" +
										"\n    case FOO:" +
										"\n     System.out.println(\"FOO\");" +
										"\n " +
										"\n    case BAR:" +
										"\n     System.out.println(\"BAR\");" +
										"\n     return true;" +
										"\n " +
										"\n    case GONK:" +
										"\n     return true;" +
										"\n " +
										"\n    default:" +
										"\n     return false;" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithBoolean() {" +
										"\n   switchWithBoolean(1, false);" +
										"\n   switchWithBoolean(3, false);" +
										"\n   switchWithBoolean(7, true);" +
										"\n   switchWithBoolean(7, false);" +
										"\n   switchWithBoolean(8, true);" +
										"\n  }" +
										"\n " +
										"\n  public void switchWithBoolean(int type, boolean aBoolean) {" +
										"\n   switch (type) {                     // tests assume this is line 195" +
										"\n    case 9:{" +
										"\n     System.out.println(\"9\");" +
										"\n     break;" +
										"\n    }" +
										"\n    case 1:{" +
										"\n     try {" +
										"\n      System.out.println(\"1\");" +
										"\n     } catch (Exception e) {}" +
										"\n     break;                     // tests assume this is line 204" +
										"\n    }" +
										"\n    case 5:{" +
										"\n     System.out.println(\"5\");" +
										"\n     break;" +
										"\n    }" +
										"\n    case 4:{" +
										"\n     System.out.println(\"4\");" +
										"\n     break;" +
										"\n    }" +
										"\n    case 3:{" +
										"\n     System.out.println(\"3\");" +
										"\n     break;" +
										"\n    }" +
										"\n    case 7:{" +
										"\n     if (aBoolean) {" +
										"\n      System.out.println(\"7\");" +
										"\n      break;" +
										"\n     }" +
										"\n     // if aBoolean == false, this will fall through to the next case." +
										"\n    }" +
										"\n    case 8:{" +
										"\n     if (aBoolean) {" +
										"\n      System.out.println(\"8\");" +
										"\n      break;" +
										"\n     }" +
										"\n    }" +
										"\n   }" +
										"\n  }" +
										"\n " +
										"\n enum ABCDEnum { A,B,C,D;};" +
										"\n " +
										"\n  public void switchWithEnum(ABCDEnum value) {" +
										"\n   switch (value) {			// tests assume this is line 237" +
										"\n    case B: " +
										"\n     System.out.println(\"B\");" +
										"\n     break;" +
										"\n    case C:" +
										"\n     System.out.println(\"C\");" +
										"\n    case A:" +
										"\n     System.out.println(\"A\");" +
										"\n     break;" +
										"\n    default:" +
										"\n     System.out.println(\"default\");" +
										"\n   }	" +
										"\n  }" +
										"\n " +
										"\n  public void switchWithAllEnumValues(ABCDEnum value) {" +
										"\n   switch (value) {		// tests assume this is line 252" +
										"\n    case B: " +
										"\n     System.out.println(\"B\");" +
										"\n     break;" +
										"\n    case C:" +
										"\n     System.out.println(\"C\");" +
										"\n    case A:" +
										"\n     System.out.println(\"A\");" +
										"\n     break;" +
										"\n    case D:" +
										"\n     System.out.println(\"D\");" +
										"\n     break;" +
										"\n    default:" +
										"\n     // Not reachable, but compiler does not mark it as dead code." +
										"\n     System.out.println(\"default\");	// tests assume this is line 266" +
										"\n   }	" +
										"\n  }" +
										"\n " +
										"\n  public void switchWithAllButDefaultEnumValues(ABCDEnum value) {" +
										"\n   switch (value) {			// tests assume this is line 271		" +
										"\n    case B: " +
										"\n     System.out.println(\"B\");" +
										"\n     break;" +
										"\n    case C:" +
										"\n     System.out.println(\"C\");" +
										"\n    case A:" +
										"\n     System.out.println(\"A\");" +
										"\n     break;" +
										"\n    case D:" +
										"\n     System.out.println(\"D\");" +
										"\n     break;" +
										"\n   }	" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithEnum() {" +
										"\n   switchWithEnum(ABCDEnum.B);" +
										"\n   switchWithEnum(ABCDEnum.C);" +
										"\n   switchWithEnum(ABCDEnum.D);" +
										"\n   switchWithEnum(ABCDEnum.B);" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithAllEnumValues() {" +
										"\n   switchWithAllEnumValues(ABCDEnum.A);" +
										"\n   switchWithAllEnumValues(ABCDEnum.B);" +
										"\n   switchWithAllEnumValues(ABCDEnum.C);" +
										"\n   switchWithAllEnumValues(ABCDEnum.D);" +
										"\n  }" +
										"\n " +
										"\n  public void callSwitchWithAllButDefaultEnumValues() {" +
										"\n   switchWithAllButDefaultEnumValues(ABCDEnum.A);" +
										"\n   switchWithAllButDefaultEnumValues(ABCDEnum.B);" +
										"\n   switchWithAllButDefaultEnumValues(ABCDEnum.C);" +
										"\n   switchWithAllButDefaultEnumValues(ABCDEnum.D);" +
										"\n  }" +
										"\n " +
										"\n " +
										"\n  public static void main(String[] args) {" +
										"\n   Main main = new Main();" +
										"\n   main.callNoDefaultSwitch();" +
										"\n   main.callSwitchFallThrough();" +
										"\n   main.callSwitchWithDefault();" +
										"\n   main.callSwitchWithoutGaps();" +
										"\n   main.callSwitchWithoutGapsWithFallThrough();" +
										"\n   main.callSwitchBug2075537();" +
										"\n   main.callSwitchBug2075537_2();" +
										"\n   main.callSwitchNonDefaultFallThrough();" +
										"\n   main.callSwitchWithBoolean();" +
										"\n   main.callSwitchWithEnum();" +
										"\n   main.callSwitchWithAllEnumValues();" +
										"\n   main.callSwitchWithAllButDefaultEnumValues();" +
										"\n  }	" +
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
		 * Now create a cobertura html report and make sure the files are created.
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
		
		Node dom = TestUtils.getXMLReportDOM(srcDir.getAbsolutePath() + "/coverage.xml");
		
		//switchWithBoolean
		List<Node>lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithBoolean");

		assertConditionCoverage(lines, "50% (4/8)", 195);
		
		/*
		 * A try catch just before a break statement used to cause Cobertura
		 * to report the line with the break as uncovered.  Make sure
		 * this no longer happens.
		 */
		// Unhapilly Java compiler is messing the code too much to support the case.			  
		// def breakInSwitchWithBooleanLine = lines.grep {it.number == '204'}[0]
		// assertEquals(1, breakInSwitchWithBooleanLine.hits)			

		//switchNonDefaultFallThrough
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchNonDefaultFallThrough");
		
		assertConditionCoverage(lines, "33% (1/3)", 170);

		//switchBug2075537
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchBug2075537");
		
		assertConditionCoverage(lines, "50% (1/2)", 132);

		//switchBug2075537_2
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchBug2075537_2");

		assertConditionCoverage(lines, "100% (3/3)", 150);
		
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithoutGaps");
		
		assertConditionCoverage(lines, "25% (1/4)", 76);
		
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithoutGapsWithFallThrough");
		
		assertConditionCoverage(lines, "25% (1/4)", 100);

		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "callNoDefaultSwitch");
		
		assertConditionCoverage(lines, "33% (1/3)", 7);

		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchFallThrough");
		
		assertConditionCoverage(lines, "33% (1/3)", 23);
		
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithDefault");
		
		assertConditionCoverage(lines, "16% (1/6)", 40);

		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithEnum");
		assertConditionCoverage(lines, "75% (3/4)", 237);
		
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithAllEnumValues");
		assertConditionCoverage(lines, "100% (4/4)", 252);

		boolean found = false;
		for(Node node : lines) {
			if (Integer.valueOf((String)node.attribute("number")) == 266) {
				found = true;
				assertEquals((Integer)0, Integer.valueOf((String)node.attribute("hits")) );
			}
		}
		
		assertTrue("Failed to find node on line 266", found);
		
		lines = TestUtils.getLineCounts(dom, "mypackage.Main", "switchWithAllButDefaultEnumValues");
		assertConditionCoverage(lines, "100% (4/4)", 271);
	}
}
