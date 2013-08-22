package net.sourceforge.cobertura.dsl;

import net.sourceforge.cobertura.check.CoverageThreshold;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.instrument.CoberturaFile;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.RegexUtil;
import org.apache.oro.text.regex.Pattern;

import java.io.File;
import java.util.*;

/*
 * Cobertura - http://cobertura.sourceforge.net/
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

/**
 * Arguments builder - provides a DSL to build cobertura Arguments
 */
public class ArgumentsBuilder {
	//Visible for testing
	static final String DEFAULT_ENCODING = "UTF-8";
	static final double DEFAULT_THRESHOLD = 0.;
	static final boolean DEFAULT_FAIL_ON_ERROR = false;
	static final boolean DEFAULT_IGNORE_TRIVIAL = false;
	static final boolean DEFAULT_THREADSAFE_RIGOROUS = false;

	private Arguments arguments;

	private File baseDirectory;
	private File dataFile;
	private File destinationDirectory;
	private File commandsFile;
	private List<CodeSource> sources;

	private Collection ignoreRegexes;
	private Collection<Pattern> ignoreBranchesRegexes;
	private Collection<Pattern> classPatternIncludeClassesRegexes;
	private Collection<Pattern> classPatternExcludeClassesRegexes;
	private boolean failOnError;
	private boolean ignoreTrivial;
	private boolean threadsafeRigorous;

	private String encoding;

	private Set<CoverageThreshold> minimumCoverageThresholds;
	private double classLineThreshold;
	private double classBranchThreshold;
	private double packageLineThreshold;
	private double packageBranchThreshold;
	private double totalLineThreshold;
	private double totalBranchThreshold;

	private Set<CoberturaFile> filesToInstrument;
	private Set<File> filesToMerge;
	private Set<String> ignoreMethodAnnotations;

	public ArgumentsBuilder() {
		initVariables();
	}

	public ArgumentsBuilder setBaseDirectory(String baseDir) {
		baseDirectory = new File(baseDir);
		return this;
	}

	public ArgumentsBuilder setDataFile(String dataFile) {
		this.dataFile = new File(dataFile);
		return this;
	}

	public ArgumentsBuilder setDestinationDirectory(String destinationDir) {
		this.destinationDirectory = new File(destinationDir);
		return this;
	}

	public ArgumentsBuilder setCommandsFile(String commandsFile) {
		this.commandsFile = new File(commandsFile);
		return this;
	}

	public ArgumentsBuilder addIgnoreRegex(String regex) {
		RegexUtil.addRegex(ignoreRegexes, regex);;
		return this;
	}

	public ArgumentsBuilder addIgnoreBranchRegex(String regex) {
		RegexUtil.addRegex(ignoreBranchesRegexes, regex);
		return this;
	}

	public ArgumentsBuilder addIgnoreMethodAnnotation(
			String ignoreMethodAnnotation) {
		ignoreMethodAnnotations.add(ignoreMethodAnnotation);
		return this;
	}

	public ArgumentsBuilder addExcludeClassesRegex(String regex) {
		RegexUtil.addRegex(classPatternExcludeClassesRegexes, regex);
		return this;
	}

	public ArgumentsBuilder addIncludeClassesRegex(String regex) {
		RegexUtil.addRegex(classPatternIncludeClassesRegexes, regex);
		return this;
	}

	public ArgumentsBuilder failOnError(boolean failOnError) {
		this.failOnError = failOnError;
		return this;
	}

	public ArgumentsBuilder ignoreTrivial(boolean ignoreTrivial) {
		this.ignoreTrivial = ignoreTrivial;
		return this;
	}

	public ArgumentsBuilder threadsafeRigorous(boolean threadsafeRigorous) {
		this.threadsafeRigorous = threadsafeRigorous;
		return this;
	}

	public ArgumentsBuilder setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}

	public ArgumentsBuilder addMinimumCoverageRates(String regex,
			int branchPercentage, int linePercentage) {
		minimumCoverageThresholds.add(new CoverageThreshold(regex,
				branchPercentage, linePercentage));
		return this;
	}

	public ArgumentsBuilder setClassBranchCoverageThreshold(
			double coverageThreshold) {
		classBranchThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder setClassLineCoverageThreshold(
			double coverageThreshold) {
		classLineThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder setPackageBranchCoverageThreshold(
			double coverageThreshold) {
		packageBranchThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder setPackageLineCoverageThreshold(
			double coverageThreshold) {
		packageLineThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder setTotalBranchCoverageThreshold(
			double coverageThreshold) {
		totalBranchThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder setTotalLineCoverageThreshold(
			double coverageThreshold) {
		totalLineThreshold = inRange(coverageThreshold);
		return this;
	}

	public ArgumentsBuilder addFileToInstrument(String file) {
		String baseDir = getBaseDirectory().getPath();

		file = file.replace(baseDir, "");

		filesToInstrument.add(new CoberturaFile(baseDir, file));
		return this;
	}

	public ArgumentsBuilder addFileToMerge(String file) {
		filesToMerge.add(new File(file));
		return this;
	}

	public ArgumentsBuilder addSources(String sourcePath, boolean isDirectory) {
		if (this.sources == null) {
			this.sources = new ArrayList<CodeSource>();
		}
		this.sources.add(new CodeSource(isDirectory, sourcePath));
		return this;
	}

	public Arguments build() {
		FileFinder sources = new FileFinder();

		if (this.sources != null) {
			for (CodeSource codeSource : this.sources) {
				if (codeSource.isDirectory()) {
					sources.addSourceDirectory(codeSource.getPath());
				} else {
					sources.addSourceFile(getBaseDirectory().getPath(),
							codeSource.getPath());
				}
			}
		}

		return new Arguments(baseDirectory, dataFile, destinationDirectory,
				commandsFile, ignoreRegexes, ignoreBranchesRegexes,
				classPatternIncludeClassesRegexes,
				classPatternExcludeClassesRegexes, failOnError, ignoreTrivial,
				threadsafeRigorous, encoding, minimumCoverageThresholds,
				classLineThreshold, classBranchThreshold, packageLineThreshold,
				packageBranchThreshold, totalLineThreshold,
				totalBranchThreshold, filesToInstrument, filesToMerge,
				ignoreMethodAnnotations, sources);
	}

	private double inRange(double coverageRate) {
		if ((coverageRate >= 0.) && (coverageRate <= 1.)) {
			return coverageRate;
		}
		throw new IllegalArgumentException(String.format(
				"The value %s is invalid.  Rates must be between 0.0 and 1.0",
				coverageRate));
	}

	private void initVariables() {
		dataFile = CoverageDataFileHandler.getDefaultDataFile();
		baseDirectory = new File(".");
		ignoreRegexes = new Vector();
		ignoreBranchesRegexes = new Vector<Pattern>();
		ignoreMethodAnnotations = new HashSet<String>();
		classPatternExcludeClassesRegexes = new HashSet<Pattern>();
		classPatternIncludeClassesRegexes = new HashSet<Pattern>();
		filesToInstrument = new HashSet<CoberturaFile>();
		filesToMerge = new HashSet<File>();
		minimumCoverageThresholds = new HashSet<CoverageThreshold>();

		//previous rule was: default threshold is 0.5 for all
		//if a threshold is specified, the others are defaulted to 0
		classBranchThreshold = DEFAULT_THRESHOLD;
		classLineThreshold = DEFAULT_THRESHOLD;
		packageBranchThreshold = DEFAULT_THRESHOLD;
		packageLineThreshold = DEFAULT_THRESHOLD;
		totalBranchThreshold = DEFAULT_THRESHOLD;
		totalLineThreshold = DEFAULT_THRESHOLD;

		failOnError = DEFAULT_FAIL_ON_ERROR;
		ignoreTrivial = DEFAULT_IGNORE_TRIVIAL;
		threadsafeRigorous = DEFAULT_THREADSAFE_RIGOROUS;
		encoding = DEFAULT_ENCODING;
	}

	private File getBaseDirectory() {
		return baseDirectory;
	}

	private static class CodeSource {
		private boolean directory;
		private String path;

		private CodeSource(boolean directory, String path) {
			this.directory = directory;
			this.path = path;
		}

		public boolean isDirectory() {
			return directory;
		}

		public String getPath() {
			return path;
		}
	}
}
