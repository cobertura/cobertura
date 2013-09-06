/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2008 Tri Bao Ho
 * Copyright (C) 2009 John Lewis
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
package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.javancss.FunctionMetric;
import net.sourceforge.cobertura.javancss.Javancss;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows complexity computing for source files, packages and a whole project. Average
 * McCabe's number for methods contained in the specified entity is returned. This class
 * depends on FileFinder which is used to map source file names to existing files.
 * <p/>
 * <p>One instance of this class should be used for the same set of source files - an
 * object of this class can cache computed results.</p>
 *
 * @author Grzegorz Lukasik
 */
public class ComplexityCalculator {
	private static final Logger logger = LoggerFactory
			.getLogger(ComplexityCalculator.class);

	public static final Complexity ZERO_COMPLEXITY = new Complexity();

	// Finder used to map source file names to existing files
	private final FileFinder finder;

	// Contains pairs (String sourceFileName, Complexity complexity)
	private Map sourceFileCNNCache = new HashMap();

	// Contains pairs (String packageName, Complexity complexity)
	private Map packageCNNCache = new HashMap();

	/**
	 * Creates new calculator. Passed {@link FileFinder} will be used to
	 * map source file names to existing files when needed.
	 *
	 * @param finder {@link FileFinder} that allows to find source files
	 *
	 * @throws NullPointerException if finder is null
	 */
	public ComplexityCalculator(FileFinder finder) {
		if (finder == null)
			throw new NullPointerException();
		this.finder = finder;
	}

	/**
	 * Calculates the code complexity number for an input stream.
	 * "CCN" stands for "code complexity number."  This is
	 * sometimes referred to as McCabe's number.  This method
	 * calculates the average cyclomatic code complexity of all
	 * methods of all classes in a given directory.
	 *
	 * @param file The input stream for which you want to calculate
	 *             the complexity
	 *
	 * @return average complexity for the specified input stream
	 */
	private Complexity getAccumlatedCCNForSource(String sourceFileName,
			Source source) {
		if (source == null) {
			return ZERO_COMPLEXITY;
		}
		if (!sourceFileName.endsWith(".java")) {
			return ZERO_COMPLEXITY;
		}
		Javancss javancss = new Javancss(source.getInputStream());

		if (javancss.getLastErrorMessage() != null) {
			//there is an error while parsing the java file. log it
			logger.warn("JavaNCSS got an error while parsing the java "
					+ source.getOriginDesc() + "\n"
					+ javancss.getLastErrorMessage());
		}

		List methodMetrics = javancss.getFunctionMetrics();
		int classCcn = 0;
		for (Iterator method = methodMetrics.iterator(); method.hasNext();) {
			FunctionMetric singleMethodMetrics = (FunctionMetric) method.next();
			classCcn += singleMethodMetrics.ccn;
		}

		return new Complexity(classCcn, methodMetrics.size());
	}

	/**
	 * Calculates the code complexity number for single source file.
	 * "CCN" stands for "code complexity number."  This is
	 * sometimes referred to as McCabe's number.  This method
	 * calculates the average cyclomatic code complexity of all
	 * methods of all classes in a given directory.
	 *
	 * @param sourceFileName
	 * @param file           The source file for which you want to calculate
	 *                       the complexity
	 *
	 * @return average complexity for the specified source file
	 *
	 * @throws IOException
	 */
	private Complexity getAccumlatedCCNForSingleFile(String sourceFileName)
			throws IOException {
		Source source = finder.getSource(sourceFileName);
		try {
			return getAccumlatedCCNForSource(sourceFileName, source);
		} finally {
			if (source != null) {
				source.close();
			}
		}
	}

	/**
	 * Computes CCN for all sources contained in the project.
	 * CCN for whole project is an average CCN for source files.
	 * All source files for which CCN cannot be computed are ignored.
	 *
	 * @param projectData project to compute CCN for
	 *
	 * @return CCN for project or 0 if no source files were found
	 *
	 * @throws NullPointerException if projectData is null
	 */
	public double getCCNForProject(ProjectData projectData) {
		// Sum complexity for all packages
		Complexity act = new Complexity();

		for (Object pkg : projectData.getPackages()) {
			PackageData packageData = (PackageData) pkg;
			act.add(getCCNForPackageInternal(packageData));
		}

		// Return average CCN for source files
		return act.averageCCN();
	}

	/**
	 * Computes CCN for all sources contained in the specified package.
	 * All source files that cannot be mapped to existing files are ignored.
	 *
	 * @param packageData package to compute CCN for
	 *
	 * @return CCN for the specified package or 0 if no source files were found
	 *
	 * @throws NullPointerException if <code>packageData</code> is <code>null</code>
	 */
	public double getCCNForPackage(PackageData packageData) {
		return getCCNForPackageInternal(packageData).averageCCN();
	}

	private Complexity getCCNForPackageInternal(PackageData packageData) {
		// Return CCN if computed earlier
		Complexity cachedCCN = (Complexity) packageCNNCache.get(packageData
				.getName());
		if (cachedCCN != null) {
			return cachedCCN;
		}

		// Compute CCN for all source files inside package
		Complexity act = new Complexity();
		for (Iterator it = packageData.getSourceFiles().iterator(); it
				.hasNext();) {
			SourceFileData sourceData = (SourceFileData) it.next();
			act.add(getCCNForSourceFileNameInternal(sourceData.getName()));
		}

		// Cache result and return it
		packageCNNCache.put(packageData.getName(), act);
		return act;
	}

	/**
	 * Computes CCN for single source file.
	 *
	 * @param sourceFile source file to compute CCN for
	 *
	 * @return CCN for the specified source file, 0 if cannot map <code>sourceFile</code> to existing file
	 *
	 * @throws NullPointerException if <code>sourceFile</code> is <code>null</code>
	 */
	public double getCCNForSourceFile(SourceFileData sourceFile) {
		return getCCNForSourceFileNameInternal(sourceFile.getName())
				.averageCCN();
	}

	private Complexity getCCNForSourceFileNameInternal(String sourceFileName) {
		// Return CCN if computed earlier
		Complexity cachedCCN = (Complexity) sourceFileCNNCache
				.get(sourceFileName);
		if (cachedCCN != null) {
			return cachedCCN;
		}

		// Compute CCN and cache it for further use
		Complexity result = ZERO_COMPLEXITY;
		try {
			result = getAccumlatedCCNForSingleFile(sourceFileName);
		} catch (IOException ex) {
			logger
					.info("Cannot find source file during CCN computation, source=["
							+ sourceFileName + "]");
		}
		sourceFileCNNCache.put(sourceFileName, result);
		return result;
	}

	/**
	 * Computes CCN for source file the specified class belongs to.
	 *
	 * @param classData package to compute CCN for
	 *
	 * @return CCN for source file the specified class belongs to
	 *
	 * @throws NullPointerException if <code>classData</code> is <code>null</code>
	 */
	public double getCCNForClass(ClassData classData) {
		return getCCNForSourceFileNameInternal(classData.getSourceFileName())
				.averageCCN();
	}

	/**
	 * Represents complexity of source file, package or project. Stores the number of
	 * methods inside entity and accumlated complexity for these methods.
	 */
	private static class Complexity {
		private double accumlatedCCN;
		private int methodsNum;

		public Complexity(double accumlatedCCN, int methodsNum) {
			this.accumlatedCCN = accumlatedCCN;
			this.methodsNum = methodsNum;
		}

		public Complexity() {
			this(0, 0);
		}

		public double averageCCN() {
			if (methodsNum == 0) {
				return 0;
			}
			return accumlatedCCN / methodsNum;
		}

		public void add(Complexity second) {
			accumlatedCCN += second.accumlatedCCN;
			methodsNum += second.methodsNum;
		}
	}
}
