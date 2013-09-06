/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2006 Jiri Mares
 * Copyright (C) 2008 Julian Gamble
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

package net.sourceforge.cobertura.reporting.xml;

import net.sourceforge.cobertura.coveragedata.*;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;
import net.sourceforge.cobertura.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLReport {

	private static final Logger logger = LoggerFactory
			.getLogger(XMLReport.class);

	protected final static String coverageDTD = "coverage-04.dtd";

	private final PrintWriter pw;
	private final FileFinder finder;
	private final ComplexityCalculator complexity;
	private int indent = 0;

	public XMLReport(ProjectData projectData, File destinationDir,
			FileFinder finder, ComplexityCalculator complexity)
			throws IOException {
		this.complexity = complexity;
		this.finder = finder;

		File file = new File(destinationDir, "coverage.xml");
		pw = IOUtil.getPrintWriter(file);

		try {
			println("<?xml version=\"1.0\"?>");
			println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/"
					+ coverageDTD + "\">");
			println("");

			double ccn = complexity.getCCNForProject(projectData);
			int numLinesCovered = projectData.getNumberOfCoveredLines();
			int numLinesValid = projectData.getNumberOfValidLines();
			int numBranchesCovered = projectData.getNumberOfCoveredBranches();
			int numBranchesValid = projectData.getNumberOfValidBranches();

			// TODO: Set a schema?
			//println("<coverage " + sourceDirectories.toString() + " xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
			println("<coverage line-rate=\""
					+ projectData.getLineCoverageRate() + "\" branch-rate=\""
					+ projectData.getBranchCoverageRate()
					+ "\" lines-covered=\"" + numLinesCovered
					+ "\" lines-valid=\"" + numLinesValid
					+ "\" branches-covered=\"" + numBranchesCovered
					+ "\" branches-valid=\"" + numBranchesValid

					+ "\" complexity=\"" + ccn

					+ "\" version=\"" + Header.version() + "\" timestamp=\""
					+ new Date().getTime() + "\">");

			increaseIndentation();
			dumpSources();
			dumpPackages(projectData);
			decreaseIndentation();
			println("</coverage>");
		} finally {
			pw.close();
		}
	}

	void increaseIndentation() {
		indent++;
	}

	void decreaseIndentation() {
		if (indent > 0)
			indent--;
	}

	void indent() {
		for (int i = 0; i < indent; i++) {
			pw.print("\t");
		}
	}

	void println(String ln) {
		indent();
		pw.println(ln);
	}

	private void dumpSources() {
		println("<sources>");
		increaseIndentation();
		for (Iterator it = finder.getSourceDirectoryList().iterator(); it
				.hasNext();) {
			String dir = (String) it.next();
			dumpSource(dir);
		}
		decreaseIndentation();
		println("</sources>");
	}

	private void dumpSource(String sourceDirectory) {
		println("<source>" + sourceDirectory + "</source>");
	}

	private void dumpPackages(ProjectData projectData) {
		println("<packages>");
		increaseIndentation();

		Iterator it = projectData.getPackages().iterator();
		while (it.hasNext()) {
			dumpPackage((PackageData) it.next());
		}

		decreaseIndentation();
		println("</packages>");
	}

	private void dumpPackage(PackageData packageData) {
		logger.debug("Dumping package " + packageData.getName());

		println("<package name=\"" + packageData.getName() + "\" line-rate=\""
				+ packageData.getLineCoverageRate() + "\" branch-rate=\""
				+ packageData.getBranchCoverageRate() + "\" complexity=\""
				+ complexity.getCCNForPackage(packageData) + "\"" + ">");
		increaseIndentation();
		dumpSourceFiles(packageData);
		decreaseIndentation();
		println("</package>");
	}

	private void dumpSourceFiles(PackageData packageData) {
		println("<classes>");
		increaseIndentation();

		Iterator it = packageData.getSourceFiles().iterator();
		while (it.hasNext()) {
			dumpClasses((SourceFileData) it.next());
		}

		decreaseIndentation();
		println("</classes>");
	}

	private void dumpClasses(SourceFileData sourceFileData) {
		Iterator it = sourceFileData.getClasses().iterator();
		while (it.hasNext()) {
			dumpClass((ClassData) it.next());
		}
	}

	private void dumpClass(ClassData classData) {
		logger.debug("Dumping class " + classData.getName());

		println("<class name=\"" + classData.getName() + "\" filename=\""
				+ classData.getSourceFileName() + "\" line-rate=\""
				+ classData.getLineCoverageRate() + "\" branch-rate=\""
				+ classData.getBranchCoverageRate() + "\" complexity=\""
				+ complexity.getCCNForClass(classData) + "\"" + ">");
		increaseIndentation();

		dumpMethods(classData);
		dumpLines(classData);

		decreaseIndentation();
		println("</class>");
	}

	private void dumpMethods(ClassData classData) {
		println("<methods>");
		increaseIndentation();

		SortedSet sortedMethods = new TreeSet();
		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
		Iterator iter = sortedMethods.iterator();
		while (iter.hasNext()) {
			dumpMethod(classData, (String) iter.next());
		}

		decreaseIndentation();
		println("</methods>");
	}

	private void dumpMethod(ClassData classData, String nameAndSig) {
		String name = nameAndSig.substring(0, nameAndSig.indexOf('('));
		String signature = nameAndSig.substring(nameAndSig.indexOf('('));
		double lineRate = classData.getLineCoverageRate(nameAndSig);
		double branchRate = classData.getBranchCoverageRate(nameAndSig);

		println("<method name=\"" + xmlEscape(name) + "\" signature=\""
				+ xmlEscape(signature) + "\" line-rate=\"" + lineRate
				+ "\" branch-rate=\"" + branchRate + "\">");
		increaseIndentation();
		dumpLines(classData, nameAndSig);
		decreaseIndentation();
		println("</method>");
	}

	private static String xmlEscape(String str) {
		str = StringUtil.replaceAll(str, "<", "&lt;");
		str = StringUtil.replaceAll(str, ">", "&gt;");
		return str;
	}

	private void dumpLines(ClassData classData) {
		dumpLines(classData.getLines());
	}

	private void dumpLines(ClassData classData, String methodNameAndSig) {
		dumpLines(classData.getLines(methodNameAndSig));
	}

	private void dumpLines(Collection lines) {
		println("<lines>");
		increaseIndentation();

		SortedSet sortedLines = new TreeSet();
		sortedLines.addAll(lines);
		Iterator iter = sortedLines.iterator();
		while (iter.hasNext()) {
			dumpLine((LineData) iter.next());
		}

		decreaseIndentation();
		println("</lines>");
	}

	private void dumpLine(LineData lineData) {
		int lineNumber = lineData.getLineNumber();
		long hitCount = lineData.getHits();
		boolean hasBranch = lineData.hasBranch();
		String conditionCoverage = lineData.getConditionCoverage();

		String lineInfo = "<line number=\"" + lineNumber + "\" hits=\""
				+ hitCount + "\" branch=\"" + hasBranch + "\"";
		if (hasBranch) {
			println(lineInfo + " condition-coverage=\"" + conditionCoverage
					+ "\">");
			dumpConditions(lineData);
			println("</line>");
		} else {
			println(lineInfo + "/>");
		}
	}

	private void dumpConditions(LineData lineData) {
		increaseIndentation();
		println("<conditions>");

		for (int i = 0; i < lineData.getConditionSize(); i++) {
			Object conditionData = lineData.getConditionData(i);
			String coverage = lineData.getConditionCoverage(i);
			dumpCondition(conditionData, coverage);
		}

		println("</conditions>");
		decreaseIndentation();
	}

	private void dumpCondition(Object conditionData, String coverage) {
		increaseIndentation();
		StringBuffer buffer = new StringBuffer("<condition");
		if (conditionData instanceof JumpData) {
			JumpData jumpData = (JumpData) conditionData;
			buffer.append(" number=\"").append(jumpData.getConditionNumber())
					.append("\"");
			buffer.append(" type=\"").append("jump").append("\"");
			buffer.append(" coverage=\"").append(coverage).append("\"");
		} else {
			SwitchData switchData = (SwitchData) conditionData;
			buffer.append(" number=\"").append(switchData.getSwitchNumber())
					.append("\"");
			buffer.append(" type=\"").append("switch").append("\"");
			buffer.append(" coverage=\"").append(coverage).append("\"");
		}
		buffer.append("/>");
		println(buffer.toString());
		decreaseIndentation();
	}

}
