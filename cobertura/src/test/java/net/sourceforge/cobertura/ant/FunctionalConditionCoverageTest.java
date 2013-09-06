/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
 * 
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License 1.1 (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.ant;

import junit.framework.TestCase;
import net.sourceforge.cobertura.reporting.JUnitXMLHelper;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;
import org.jdom.*;
import org.jdom.xpath.XPath;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * These tests generally exec ant to run a test.xml file.  A different target is used for
 * each test.  The text.xml file sets up a test, instruments, runs junit, and generates a
 * coverage xml report.  Then the xml report is parsed and checked.
 *
 * @author jwlewi
 */
public class FunctionalConditionCoverageTest extends TestCase {

	private final static File BASEDIR = new File(
			(System.getProperty("basedir") != null) ? System
					.getProperty("basedir") : ".",
			"src/test/resources/examples/functionalconditiontest");

	private final static String CONDITION_MISSING_TRUE = "50%";
	private final static String CONDITION_MISSING_FALSE = "50%";

	private final static Map testInfoMap = new HashMap();

	static {
		ConditionTestInfo[] expectedConditions;
		TestInfo info;

		/*
		 * Load expected information into testInfoMap for each method.
		 */
		expectedConditions = new ConditionTestInfo[1];
		expectedConditions[0] = new ConditionTestInfo("0", "jump",
				CONDITION_MISSING_FALSE);

		info = new TestInfo(ConditionCalls.CALL_CONDITION_LINE_NUMBER,
				"50% (1/2)", expectedConditions);
		info.setIgnoreLineNumber(ConditionCalls.CALL_IGNORE_LINE_NUMBER);

		testInfoMap.put("call", info);

		expectedConditions = new ConditionTestInfo[1];
		expectedConditions[0] = new ConditionTestInfo("0", "switch", "33%");

		info = new TestInfo(ConditionCalls.LOOKUP_SWITCH_LINE_NUMBER,
				"33% (1/3)", expectedConditions);

		testInfoMap.put("callLookupSwitch", info);

		expectedConditions = new ConditionTestInfo[1];
		expectedConditions[0] = new ConditionTestInfo("0", "switch", "10%");

		info = new TestInfo(ConditionCalls.TABLE_SWITCH_LINE_NUMBER,
				"10% (1/10)", expectedConditions);

		testInfoMap.put("callTableSwitch", info);

		expectedConditions = new ConditionTestInfo[3];
		expectedConditions[0] = new ConditionTestInfo("0", "jump",
				CONDITION_MISSING_TRUE);
		expectedConditions[1] = new ConditionTestInfo("1", "jump", "0%");
		expectedConditions[2] = new ConditionTestInfo("2", "jump",
				CONDITION_MISSING_FALSE);

		info = new TestInfo(ConditionCalls.MULTI_CONDITION_LINE_NUMBER,
				"33% (2/6)", expectedConditions);

		testInfoMap.put("callMultiCondition", info);

		expectedConditions = new ConditionTestInfo[3];
		expectedConditions[0] = new ConditionTestInfo("0", "jump",
				CONDITION_MISSING_FALSE);
		expectedConditions[1] = new ConditionTestInfo("1", "jump",
				CONDITION_MISSING_FALSE);
		expectedConditions[2] = new ConditionTestInfo("2", "jump", "0%");

		info = new TestInfo(ConditionCalls.MULTI_CONDITION2_LINE_NUMBER,
				"33% (2/6)", expectedConditions);

		testInfoMap.put("callMultiCondition2", info);
	}

	private static class TestInfo {
		int conditionNumber;
		String expectedLineConditionCoverage;
		ConditionTestInfo[] expectedConditions;
		Integer ignoreLineNumber;

		TestInfo(int conditionNumber, String expectedLineConditionCoverage,
				ConditionTestInfo[] expectedConditions) {
			this.conditionNumber = conditionNumber;
			this.expectedLineConditionCoverage = expectedLineConditionCoverage;
			this.expectedConditions = expectedConditions;
		}

		public void setIgnoreLineNumber(int number) {
			ignoreLineNumber = new Integer(number);
		}
	}

	private static class ConditionTestInfo {
		String number;
		String type;
		String coverage;

		ConditionTestInfo(String number, String type, String coverage) {
			this.number = number;
			this.type = type;
			this.coverage = coverage;
		}
	}

	@Test
	public static void testConditionCoverage() throws Exception {
		runTestAntScript("condition-coverage", "test-condition-coverage");
		verify("condition-coverage");
	}

	private static void verify(String testName) throws Exception {
		verifyXml(testName);
		verifyHtml(testName);
	}

	private static void verifyXml(String testName) throws Exception {
		// Get a list of all classes listed in the XML report
		List classesList = getClassElements();
		assertTrue("Test " + testName
				+ ": Did not find any classes listed in the XML report.",
				classesList.size() > 0);

		boolean conditionCallsClassFound = false;
		for (Iterator iter = classesList.iterator(); iter.hasNext();) {
			Element classElement = (Element) iter.next();
			String className = classElement.getAttributeValue("name");
			if (className.equals("test.condition.ConditionCalls")) {
				conditionCallsClassFound = true;
			} else
				fail("Test "
						+ testName
						+ ": Found a class with the name '"
						+ className
						+ "' in the XML report, but was only expecting 'test.condition.ConditionCalls'.");
			verifyClass(testName, classElement);
		}
		assertTrue(
				"Test "
						+ testName
						+ ": Did not find class 'test.condition.ConditionCalls' in the XML report.",
				conditionCallsClassFound);
	}

	/**
	 * Use XPath to get all &lt;class&gt; elements in the
	 * cobertura.xml file under the given directory.
	 *
	 * @return A list of JDOM Elements.
	 */
	private static List getClassElements() throws IOException, JDOMException {
		File xmlFile = new File(BASEDIR, "reports/cobertura-xml/coverage.xml");
		Document document = JUnitXMLHelper.readXmlFile(xmlFile, true);
		XPath xpath = XPath
				.newInstance("/coverage/packages/package/classes/class");
		List classesList = xpath.selectNodes(document);
		return classesList;
	}

	/**
	 * Verify that the class's condition information is correct.
	 */
	private static void verifyClass(String testName, Element classElement) {
		// Get a list of methods
		Element methodsElement = classElement.getChild("methods");
		List methodList = methodsElement.getChildren("method");
		assertTrue("Test " + testName
				+ ": Did not find any methods listed in the class "
				+ classElement.getAttributeValue("name"), methodList.size() > 0);
		List methodsFound = new ArrayList();
		for (Iterator iter = methodList.iterator(); iter.hasNext();) {
			Element methodElement = (Element) iter.next();
			String methodName = methodElement.getAttributeValue("name");
			TestInfo info = (TestInfo) testInfoMap.get(methodName);
			if (info != null) {
				if (methodsFound.contains(methodName)) {
					fail("Test " + testName
							+ ": Found more than one instance of the method "
							+ methodName + " in the class "
							+ classElement.getAttributeValue("name"));
				}
				methodsFound.add(methodName);

				verifyMethod(info, testName, classElement, methodElement);
			} else if (methodName.equals("<clinit>")
					|| methodName.equals("<init>")
					|| methodName.startsWith("util")
					|| methodName.equals("class$")) {
				// These methods are ok--ignore them.
			} else {
				fail("Test "
						+ testName
						+ ": Found method "
						+ methodName
						+ " in the class "
						+ classElement.getAttributeValue("name")
						+ ", but was only expecting either 'call' or 'dontCall'.");
			}
		}
		/*
		 * now make sure all methods in testInfoMap were found and verified
		 */
		for (Iterator iter = testInfoMap.keySet().iterator(); iter.hasNext();) {
			String methodName = (String) iter.next();
			assertTrue("Test " + testName + ": Did not find method "
					+ methodName + " in the class "
					+ classElement.getAttributeValue("name"), methodsFound
					.contains(methodName));
		}
	}

	private static void verifyMethod(TestInfo info, String testName,
			Element classElement, Element methodElement) {
		Element linesElement = methodElement.getChild("lines");
		List lineList = linesElement.getChildren("line");
		String methodName = methodElement.getAttributeValue("name");
		assertTrue("Test " + testName + ", class "
				+ classElement.getAttributeValue("name")
				+ ": Did not find any lines in the method " + methodName,
				lineList.size() > 0);

		boolean foundCondition = false;
		for (Iterator iter = lineList.iterator(); iter.hasNext();) {
			Element lineElement = (Element) iter.next();
			int number;
			try {
				number = lineElement.getAttribute("number").getIntValue();
				if ((info.ignoreLineNumber != null)
						&& (info.ignoreLineNumber.intValue() == number)) {
					fail("Expected line " + info.ignoreLineNumber
							+ " to be ignored.");
				}
			} catch (DataConversionException e) {
				throw new RuntimeException(e.toString());
			}
			if (number == info.conditionNumber) {
				foundCondition = true;
				verifyLineConditionInfo(lineElement, info.conditionNumber,
						info.expectedLineConditionCoverage,
						info.expectedConditions);
			}
		}
		assertTrue("Expected condition element for line "
				+ info.conditionNumber + " of " + methodName, foundCondition);
	}

	private static void verifyLineConditionInfo(Element lineElement,
			int conditionLineNumber, String expectedLineConditionCoverage,
			ConditionTestInfo[] expectedConditions) {
		String errorMessage = "Line " + conditionLineNumber;
		boolean branch = false;
		try {
			branch = lineElement.getAttribute("branch").getBooleanValue();
		} catch (DataConversionException e) {
			fail(errorMessage + " has missing or wrong branch attribute");
		}
		assertTrue(errorMessage + "Branch attribute should be true", branch);

		String lineCoverageStr = getRequiredAttribute(lineElement,
				"condition-coverage", errorMessage).getValue();
		assertEquals(errorMessage + " has incorrect condition-coverage",
				expectedLineConditionCoverage, lineCoverageStr);

		List conditionList = lineElement.getChildren("conditions");
		assertTrue(errorMessage
				+ " should have one and only one conditions element.",
				conditionList.size() == 1);
		conditionList = ((Element) conditionList.get(0))
				.getChildren("condition");

		assertEquals(errorMessage
				+ " has incorrect number of condition elements.",
				expectedConditions.length, conditionList.size());

		errorMessage = "Condition for " + conditionLineNumber;

		int i = 0;
		for (Iterator iter = conditionList.iterator(); iter.hasNext(); i++) {
			Element element = (Element) iter.next();
			verifyCondition(element, errorMessage, expectedConditions[i]);
		}
	}

	private static void verifyCondition(Element conditionElement,
			String errorMessage, ConditionTestInfo info) {
		String numberStr = getRequiredAttribute(conditionElement, "number",
				errorMessage).getValue();
		assertEquals(errorMessage + " has incorrect number", info.number,
				numberStr);
		String typeStr = getRequiredAttribute(conditionElement, "type",
				errorMessage).getValue();
		assertEquals(errorMessage + " has incorrect type", info.type, typeStr);
		String coverageStr = getRequiredAttribute(conditionElement, "coverage",
				errorMessage).getValue();
		assertEquals(errorMessage + " has incorrect coverage", info.coverage,
				coverageStr);
	}

	private static Attribute getRequiredAttribute(Element element,
			String attribute, String errorMessage) {
		Attribute attr = element.getAttribute(attribute);
		assertNotNull(errorMessage + " has missing " + attribute
				+ " attribute.", attr);
		return attr;
	}

	private static void verifyHtml(String testName) throws Exception {
		File htmlReportDir = new File(BASEDIR, "reports/cobertura-html");

		// Get all files from report directory
		String htmlFiles[] = htmlReportDir.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".html");
			}
		});
		Arrays.sort(htmlFiles);

		assertTrue(htmlFiles.length >= 5);

		// Assert that all required files are there
		String[] requiredFiles = {"index.html", "help.html",
				"frame-packages.html", "frame-summary.html",
				"frame-sourcefiles.html"};

		for (int i = 0; i < requiredFiles.length; i++) {
			if (!containsFile(htmlFiles, requiredFiles[i])) {
				fail("Test " + testName + ": File " + requiredFiles[i]
						+ " not found among report files");
			}
		}

		// Validate selected files
		String previousPrefix = "NONE";
		for (int i = 0; i < htmlFiles.length; i++) {
			// Validate file if has prefix different than previous one, or is required file
			if (containsFile(requiredFiles, htmlFiles[i])
					|| !htmlFiles[i].startsWith(previousPrefix)) {
				JUnitXMLHelper.readXmlFile(
						new File(htmlReportDir, htmlFiles[i]), true);
			}
			if (htmlFiles[i].length() > 7) {
				previousPrefix = htmlFiles[i].substring(0, 7);
			} else {
				previousPrefix = htmlFiles[i];
			}
		}
	}

	private static boolean containsFile(String[] files, String fileName) {
		for (int i = 0; i < files.length; i++) {
			if (files[i].equals(fileName))
				return true;
		}
		return false;
	}

	/**
	 * Use the ant 'java' task to run the test.xml
	 * file and the specified target.
	 */
	private static void runTestAntScript(String testName, String target)
			throws IOException {
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.init();

		// Call ant launcher.  Requires ant-lancher.jar.
		java.setClassname("org.apache.tools.ant.launch.Launcher");
		java.setFork(true);
		AntUtil.transferCoberturaDataFileProperty(java);

		java.createArg().setValue("-f");
		java.createArg().setValue(BASEDIR + "/build.xml");
		java.createArg().setValue(target);

		java.setFailonerror(true);

		// Set output to go to a temp file
		File outputFile = Util.createTemporaryTextFile("cobertura-test");
		java.setOutput(outputFile);

		// Set the classpath to the same classpath as this JVM

		Path classpath = new Path(TestUtils.project);
		PathElement pathElement = classpath.new PathElement();
		pathElement.setPath(System.getProperty("java.class.path"));
		classpath.add(pathElement);
		java.setClasspath(classpath);
		System.out.println(classpath);
		try {
			java.execute();
		} finally {
			if (outputFile.exists()) {
				// Put the contents of the output file in the exception
				System.out.println("\n\n\nOutput from Ant for " + testName
						+ " test:\n----------------------------------------\n"
						+ Util.getText(outputFile)
						+ "----------------------------------------");
				outputFile.delete();
			}
		}
	}

	private class ConditionCalls {
		public static final int CALL_CONDITION_LINE_NUMBER = 18;
		public static final int CALL_IGNORE_LINE_NUMBER = 22;
		public static final int LOOKUP_SWITCH_LINE_NUMBER = 40;
		public static final int TABLE_SWITCH_LINE_NUMBER = 57;
		public static final int MULTI_CONDITION_LINE_NUMBER = 75;
		public static final int MULTI_CONDITION2_LINE_NUMBER = 83;
	}
}
