package net.sourceforge.cobertura.dsl;

import net.sourceforge.cobertura.check.CoverageThreshold;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.instrument.CoberturaFile;

import java.io.File;
import java.util.*;

import org.apache.oro.text.regex.Pattern;

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
 * Encapsulates arguments;
 */
public class Arguments {

	private File baseDirectory;
	private File dataFile;
	private File destinationDirectory;
	private File commandsFile;
	private FileFinder sources;

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

	Arguments(File baseDirectory, File dataFile, File destinationDirectory,
			File commandsFile, Collection ignoreRegexes,
			Collection<Pattern> ignoreBranchesRegexes,
			Collection<Pattern> classPatternIncludeClassesRegexes,
			Collection<Pattern> classPatternExcludeClassesRegexes,
			boolean failOnError, boolean ignoreTrivial,
			boolean threadsafeRigorous, String encoding,
			Set<CoverageThreshold> minimumCoverageThresholds,
			double classLineThreshold, double classBranchThreshold,
			double packageLineThreshold, double packageBranchThreshold,
			double totalLineThreshold, double totalBranchThreshold,
			Set<CoberturaFile> filesToInstrument, Set<File> filesToMerge,
			Set<String> ignoreMethodAnnotations, FileFinder sources) {
		this.baseDirectory = baseDirectory;
		this.dataFile = dataFile;
		this.destinationDirectory = destinationDirectory;
		this.commandsFile = commandsFile;
		this.ignoreRegexes = ignoreRegexes;
		this.sources = sources;
		this.ignoreBranchesRegexes = Collections
				.unmodifiableCollection(ignoreBranchesRegexes);
		this.classPatternIncludeClassesRegexes = Collections
				.unmodifiableCollection(classPatternIncludeClassesRegexes);
		this.classPatternExcludeClassesRegexes = Collections
				.unmodifiableCollection(classPatternExcludeClassesRegexes);
		this.failOnError = failOnError;
		this.ignoreTrivial = ignoreTrivial;
		this.threadsafeRigorous = threadsafeRigorous;
		this.encoding = encoding;
		this.minimumCoverageThresholds = Collections
				.unmodifiableSet(minimumCoverageThresholds);
		this.classLineThreshold = classLineThreshold;
		this.classBranchThreshold = classBranchThreshold;
		this.packageLineThreshold = packageLineThreshold;
		this.packageBranchThreshold = packageBranchThreshold;
		this.totalLineThreshold = totalLineThreshold;
		this.totalBranchThreshold = totalBranchThreshold;
		this.filesToInstrument = Collections.unmodifiableSet(filesToInstrument);
		this.filesToMerge = Collections.unmodifiableSet(filesToMerge);
		this.ignoreMethodAnnotations = Collections
				.unmodifiableSet(ignoreMethodAnnotations);
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public File getDataFile() {
		return dataFile;
	}

	public File getDestinationDirectory() {
		return destinationDirectory;
	}

	public File getCommandsFile() {
		return commandsFile;
	}

	public Collection getIgnoreRegexes() {
		return ignoreRegexes;
	}

	public Collection<Pattern> getIgnoreBranchesRegexes() {
		return ignoreBranchesRegexes;
	}

	public Collection<Pattern> getClassPatternIncludeClassesRegexes() {
		return classPatternIncludeClassesRegexes;
	}

	public Collection<Pattern> getClassPatternExcludeClassesRegexes() {
		return classPatternExcludeClassesRegexes;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	public boolean isIgnoreTrivial() {
		return ignoreTrivial;
	}

	public boolean isThreadsafeRigorous() {
		return threadsafeRigorous;
	}

	public String getEncoding() {
		return encoding;
	}

	public Set<CoverageThreshold> getMinimumCoverageThresholds() {
		return minimumCoverageThresholds;
	}

	public double getClassLineThreshold() {
		return classLineThreshold;
	}

	public double getClassBranchThreshold() {
		return classBranchThreshold;
	}

	public double getPackageLineThreshold() {
		return packageLineThreshold;
	}

	public double getPackageBranchThreshold() {
		return packageBranchThreshold;
	}

	public double getTotalLineThreshold() {
		return totalLineThreshold;
	}

	public double getTotalBranchThreshold() {
		return totalBranchThreshold;
	}

	public Set<CoberturaFile> getFilesToInstrument() {
		return filesToInstrument;
	}

	public Set<File> getFilesToMerge() {
		return filesToMerge;
	}

	public Set<String> getIgnoreMethodAnnotations() {
		return ignoreMethodAnnotations;
	}

	public FileFinder getSources() {
		return sources;
	}
}
