/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Nathan Wilson
 * Copyright (C) 2009 Charlie Squires
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

package net.sourceforge.cobertura.check;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.Header;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	final Perl5Matcher pm = new Perl5Matcher();

	final Perl5Compiler pc = new Perl5Compiler();

	/**
	 * The default CoverageRate needed for a class to pass the check.
	 */
	CoverageRate minimumCoverageRate;

	/**
	 * The keys of this map contain regular expression Patterns that
	 * match against classes.  The values of this map contain
	 * CoverageRate objects that specify the minimum coverage rates
	 * needed for a class that matches the pattern.
	 */
	Map minimumCoverageRates = new HashMap();

	/**
	 * The keys of this map contain package names. The values of this
	 * map contain PackageCoverage objects that track the line and
	 * branch coverage values for a package.
	 */
	Map packageCoverageMap = new HashMap();

	double inRangeAndDivideByOneHundred(String coverageRateAsPercentage) {
		return inRangeAndDivideByOneHundred(Integer.valueOf(
				coverageRateAsPercentage).intValue());
	}

	double inRangeAndDivideByOneHundred(int coverageRateAsPercentage) {
		if ((coverageRateAsPercentage >= 0)
				&& (coverageRateAsPercentage <= 100)) {
			return (double) coverageRateAsPercentage / 100;
		}
		throw new IllegalArgumentException("The value "
				+ coverageRateAsPercentage
				+ "% is invalid.  Percentages must be between 0 and 100.");
	}

	void setMinimumCoverageRate(String minimumCoverageRate)
			throws MalformedPatternException {
		StringTokenizer tokenizer = new StringTokenizer(minimumCoverageRate,
				":");
		this.minimumCoverageRates.put(pc.compile(tokenizer.nextToken()),
				new CoverageRate(inRangeAndDivideByOneHundred(tokenizer
						.nextToken()), inRangeAndDivideByOneHundred(tokenizer
						.nextToken())));
	}

	/**
	 * This method returns the CoverageRate object that
	 * applies to the given class.  If checks if there is a
	 * pattern that matches the class name, and returns that
	 * if it finds one.  Otherwise it uses the global minimum
	 * rates that were passed in.
	 */
	CoverageRate findMinimumCoverageRate(String classname) {
		Iterator iter = this.minimumCoverageRates.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();

			if (pm.matches(classname, (Pattern) entry.getKey())) {
				return (CoverageRate) entry.getValue();
			}
		}
		return this.minimumCoverageRate;
	}

	public Main(String[] args) throws MalformedPatternException {
		int exitStatus = 0;

		Header.print(System.out);

		File dataFile = CoverageDataFileHandler.getDefaultDataFile();
		double branchCoverageRate = -1.0;
		double lineCoverageRate = -1.0;
		double packageBranchCoverageRate = -1.0;
		double packageLineCoverageRate = -1.0;
		double totalBranchCoverageRate = -1.0;
		double totalLineCoverageRate = -1.0;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--branch")) {
				branchCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			} else if (args[i].equals("--datafile")) {
				dataFile = new File(args[++i]);
			} else if (args[i].equals("--line")) {
				lineCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			} else if (args[i].equals("--regex")) {
				setMinimumCoverageRate(args[++i]);
			} else if (args[i].equals("--packagebranch")) {
				packageBranchCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			} else if (args[i].equals("--packageline")) {
				packageLineCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			} else if (args[i].equals("--totalbranch")) {
				totalBranchCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			} else if (args[i].equals("--totalline")) {
				totalLineCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			}
		}

		ProjectData projectData = CoverageDataFileHandler
				.loadCoverageData(dataFile);

		if (projectData == null) {
			System.err.println("Error: Unable to read from data file "
					+ dataFile.getAbsolutePath());
			System.exit(1);
		}

		// If they didn't specify any thresholds, then use some defaults
		if ((branchCoverageRate == -1.0) && (lineCoverageRate == -1.0)
				&& (packageLineCoverageRate == -1.0)
				&& (packageBranchCoverageRate == -1.0)
				&& (totalLineCoverageRate == -1.0)
				&& (totalBranchCoverageRate == -1.0)
				&& (this.minimumCoverageRates.size() == 0)) {
			branchCoverageRate = 0.5;
			lineCoverageRate = 0.5;
			packageBranchCoverageRate = 0.5;
			packageLineCoverageRate = 0.5;
			totalBranchCoverageRate = 0.5;
			totalLineCoverageRate = 0.5;
		}
		// If they specified one or more thresholds, default everything else to 0
		else {
			if (branchCoverageRate == -1.0)
				branchCoverageRate = 0.0;
			if (lineCoverageRate == -1.0)
				lineCoverageRate = 0.0;
			if (packageLineCoverageRate == -1.0)
				packageLineCoverageRate = 0.0;
			if (packageBranchCoverageRate == -1.0)
				packageBranchCoverageRate = 0.0;
			if (totalLineCoverageRate == -1.0)
				totalLineCoverageRate = 0.0;
			if (totalBranchCoverageRate == -1.0)
				totalBranchCoverageRate = 0.0;
		}

		this.minimumCoverageRate = new CoverageRate(lineCoverageRate,
				branchCoverageRate);

		double totalLines = 0;
		double totalLinesCovered = 0;
		double totalBranches = 0;
		double totalBranchesCovered = 0;

		Iterator iter = projectData.getClasses().iterator();
		while (iter.hasNext()) {
			ClassData classData = (ClassData) iter.next();
			CoverageRate coverageRate = findMinimumCoverageRate(classData
					.getName());

			if (totalBranchCoverageRate > 0.0) {
				totalBranches += classData.getNumberOfValidBranches();
				totalBranchesCovered += classData.getNumberOfCoveredBranches();
			}

			if (totalLineCoverageRate > 0.0) {
				totalLines += classData.getNumberOfValidLines();
				totalLinesCovered += classData.getNumberOfCoveredLines();
			}

			PackageCoverage packageCoverage = getPackageCoverage(classData
					.getPackageName());
			if (packageBranchCoverageRate > 0.0) {
				packageCoverage.addBranchCount(classData
						.getNumberOfValidBranches());
				packageCoverage.addBranchCoverage(classData
						.getNumberOfCoveredBranches());
			}

			if (packageLineCoverageRate > 0.0) {
				packageCoverage.addLineCount(classData.getNumberOfValidLines());
				packageCoverage.addLineCoverage(classData
						.getNumberOfCoveredLines());
			}

			logger.debug("Class " + classData.getName()
					+ ", line coverage rate: "
					+ percentage(classData.getLineCoverageRate())
					+ "%, branch coverage rate: "
					+ percentage(classData.getBranchCoverageRate()) + "%");

			if (classData.getBranchCoverageRate() < coverageRate
					.getBranchCoverageRate()) {
				System.err.println(classData.getName()
						+ " failed check. Branch coverage rate of "
						+ percentage(classData.getBranchCoverageRate())
						+ "% is below "
						+ percentage(coverageRate.getBranchCoverageRate())
						+ "%");
				exitStatus |= 2;
			}

			if (classData.getLineCoverageRate() < coverageRate
					.getLineCoverageRate()) {
				System.err.println(classData.getName()
						+ " failed check. Line coverage rate of "
						+ percentage(classData.getLineCoverageRate())
						+ "% is below "
						+ percentage(coverageRate.getLineCoverageRate()) + "%");
				exitStatus |= 4;
			}
		}

		exitStatus |= checkPackageCoverageLevels(packageBranchCoverageRate,
				packageLineCoverageRate);

		// Check the rates for the overall project
		if ((totalBranches > 0)
				&& (totalBranchCoverageRate > (totalBranchesCovered / totalBranches))) {
			System.err
					.println("Project failed check. "
							+ "Total branch coverage rate of "
							+ percentage(totalBranchesCovered / totalBranches)
							+ "% is below "
							+ percentage(totalBranchCoverageRate) + "%");
			exitStatus |= 8;
		}

		if ((totalLines > 0)
				&& (totalLineCoverageRate > (totalLinesCovered / totalLines))) {
			System.err.println("Project failed check. "
					+ "Total line coverage rate of "
					+ percentage(totalLinesCovered / totalLines)
					+ "% is below " + percentage(totalLineCoverageRate) + "%");
			exitStatus |= 16;
		}

		System.exit(exitStatus);
	}

	private PackageCoverage getPackageCoverage(String packageName) {
		PackageCoverage packageCoverage = (PackageCoverage) packageCoverageMap
				.get(packageName);
		if (packageCoverage == null) {
			packageCoverage = new PackageCoverage();
			packageCoverageMap.put(packageName, packageCoverage);
		}
		return packageCoverage;
	}

	private int checkPackageCoverageLevels(double packageBranchCoverageRate,
			double packageLineCoverageRate) {
		int exitStatus = 0;
		Iterator iter = packageCoverageMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String packageName = (String) entry.getKey();
			PackageCoverage packageCoverage = (PackageCoverage) entry
					.getValue();

			exitStatus |= checkPackageCoverage(packageBranchCoverageRate,
					packageLineCoverageRate, packageName, packageCoverage);
		}
		return exitStatus;
	}

	private int checkPackageCoverage(double packageBranchCoverageRate,
			double packageLineCoverageRate, String packageName,
			PackageCoverage packageCoverage) {
		int exitStatus = 0;
		double branchCoverage = packageCoverage.getBranchCoverage()
				/ packageCoverage.getBranchCount();
		if ((packageCoverage.getBranchCount() > 0)
				&& (packageBranchCoverageRate > branchCoverage)) {
			System.err.println("Package " + packageName
					+ " failed check. Package branch coverage rate of "
					+ percentage(branchCoverage) + "% is below "
					+ percentage(packageBranchCoverageRate) + "%");
			exitStatus |= 32;
		}

		double lineCoverage = packageCoverage.getLineCoverage()
				/ packageCoverage.getLineCount();
		if ((packageCoverage.getLineCount() > 0)
				&& (packageLineCoverageRate > lineCoverage)) {
			System.err.println("Package " + packageName
					+ " failed check. Package line coverage rate of "
					+ percentage(lineCoverage) + "% is below "
					+ percentage(packageLineCoverageRate) + "%");
			exitStatus |= 64;
		}

		return exitStatus;
	}

	private String percentage(double coverateRate) {
		BigDecimal decimal = new BigDecimal(coverateRate * 100);
		return decimal.setScale(1, BigDecimal.ROUND_DOWN).toString();
	}

	public static void main(String[] args) throws MalformedPatternException {
		new Main(args);
	}
}
