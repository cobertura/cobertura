package net.sourceforge.cobertura.dsl;

import net.sourceforge.cobertura.check.CoverageThreshold;
import net.sourceforge.cobertura.instrument.CoberturaFile;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.RegexUtil;
import org.apache.oro.text.regex.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArgumentsTest {
	private static final double CLASS_LINE_THRESHOLD = 0.5;
	private static final double CLASS_BRANCH_THRESHOLD = 0.6;
	private static final double PACKAGE_LINE_THRESHOLD = 0.7;
	private static final double PACKAGE_BRANCH_THRESHOLD = 0.8;
	private static final double TOTAL_LINE_THRESHOLD = 0.9;
	private static final double TOTAL_BRANCH_THRESHOLD = 0.95;
	private static final File BASEDIR = new File("basedir");
	private static final File DATA_FILE = new File("dataFile");
	private static final File DESTINATION_DIRECTORY = new File(
			"destinationDirectory");
	private static final File COMMANDS_FILE = new File("commandsFile");
	private static final boolean FAIL_ON_ERROR = false;
	private static final boolean IGNORE_TRIVIAL = true;
	private static final boolean THREADSAFE_RIGOROUS = true;
	private static final String ENCODING = "UTF-8";
	private static final String IGNORE_REGEX_PATTERN = "ignoreRegexPattern";
	private static final String IGNORE_BRANCHES_REGEXES = "ignoreBranchesRegexes";
	private static final String CLASS_PATTERN_INCLUDE_CLASSES_REGEXES = "classPatternIncludeClassesRegexes";
	private static final String CLASS_PATTERN_EXCLUDE_CLASSES_REGEXES = "classPatternExcludeClassesRegexes";
	private static final String COVERAGE_THRESHOLD_REGEX = "coverageThresholdRegex";
	private static final int MIN_BRANCH_PERCENTAGE = 50;
	private static final int MIN_LINE_PERCENTAGE = 50;
	private static final CoberturaFile FILE_TO_INSTRUMENT = new CoberturaFile(
			".", "fileToInstrument");
	private static final File FILE_TO_MERGE = new File("fileToMerge");
	private static final String IGNORE_METHOD_ANNOTATIONS = "ignoreMethodAnnotations";
	private static final FileFinder SOURCES = new FileFinder();
	private static final double DELTA = 0.001;
	private Arguments arguments;

	private Collection ignoreRegexes = new ArrayList();
	private Collection<Pattern> ignoreBranchesRegexes = new ArrayList<Pattern>();
	private Collection<Pattern> classPatternIncludeClassesRegexes = new ArrayList<Pattern>();
	private Collection<Pattern> classPatternExcludeClassesRegexes = new ArrayList<Pattern>();
	private Set<CoverageThreshold> minimumCoverageThresholds = new HashSet<CoverageThreshold>();
	private Set<CoberturaFile> filesToInstrument = new HashSet<CoberturaFile>();
	private Set<File> filesToMerge = new HashSet<File>();
	private Set<String> ignoreMethodAnnotations = new HashSet<String>();

	@Before
	public void setUp() throws Exception {
		ignoreRegexes.add(IGNORE_REGEX_PATTERN);
		RegexUtil.addRegex(ignoreBranchesRegexes, IGNORE_BRANCHES_REGEXES);
		RegexUtil.addRegex(classPatternIncludeClassesRegexes,
				CLASS_PATTERN_INCLUDE_CLASSES_REGEXES);
		RegexUtil.addRegex(classPatternExcludeClassesRegexes,
				CLASS_PATTERN_EXCLUDE_CLASSES_REGEXES);
		minimumCoverageThresholds.add(new CoverageThreshold(
				COVERAGE_THRESHOLD_REGEX, MIN_BRANCH_PERCENTAGE,
				MIN_LINE_PERCENTAGE));
		filesToInstrument.add(FILE_TO_INSTRUMENT);
		filesToMerge.add(FILE_TO_MERGE);
		ignoreMethodAnnotations.add(IGNORE_METHOD_ANNOTATIONS);

		this.arguments = new Arguments(BASEDIR, DATA_FILE,
				DESTINATION_DIRECTORY, COMMANDS_FILE, ignoreRegexes,
				ignoreBranchesRegexes, classPatternIncludeClassesRegexes,
				classPatternExcludeClassesRegexes, FAIL_ON_ERROR,
				IGNORE_TRIVIAL, THREADSAFE_RIGOROUS, ENCODING,
				minimumCoverageThresholds, CLASS_LINE_THRESHOLD,
				CLASS_BRANCH_THRESHOLD, PACKAGE_LINE_THRESHOLD,
				PACKAGE_BRANCH_THRESHOLD, TOTAL_LINE_THRESHOLD,
				TOTAL_BRANCH_THRESHOLD, filesToInstrument, filesToMerge,
				ignoreMethodAnnotations, SOURCES);
	}

	@Test
	public void testGetBaseDirectory() throws Exception {
		assertEquals(BASEDIR, arguments.getBaseDirectory());
	}

	@Test
	public void testGetDataFile() throws Exception {
		assertEquals(DATA_FILE, arguments.getDataFile());
	}

	@Test
	public void testGetDestinationDirectory() throws Exception {
		assertEquals(DESTINATION_DIRECTORY, arguments.getDestinationDirectory());
	}

	@Test
	public void testGetCommandsFile() throws Exception {
		assertEquals(COMMANDS_FILE, arguments.getCommandsFile());
	}

	@Test
	public void testGetIgnoreRegexes() throws Exception {
		assertTrue(arguments.getIgnoreRegexes().contains(IGNORE_REGEX_PATTERN));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetIgnoreBranchesRegexes() throws Exception {
		assertTrue(arguments.getIgnoreBranchesRegexes().iterator().next()
				.getPattern().equals(IGNORE_BRANCHES_REGEXES));
		RegexUtil.addRegex(arguments.getIgnoreBranchesRegexes(), "someElement");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetClassPatternIncludeClassesRegexes() throws Exception {
		assertTrue(arguments.getClassPatternIncludeClassesRegexes().iterator()
				.next().getPattern().equals(
						CLASS_PATTERN_INCLUDE_CLASSES_REGEXES));
		RegexUtil.addRegex(arguments.getClassPatternIncludeClassesRegexes(),
				"someElement");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetClassPatternExcludeClassesRegexes() throws Exception {
		assertTrue(arguments.getClassPatternExcludeClassesRegexes().iterator()
				.next().getPattern().equals(
						CLASS_PATTERN_EXCLUDE_CLASSES_REGEXES));
		RegexUtil.addRegex(arguments.getClassPatternExcludeClassesRegexes(),
				"someElement");
	}

	@Test
	public void testIsFailOnError() throws Exception {
		assertEquals(FAIL_ON_ERROR, arguments.isFailOnError());
	}

	@Test
	public void testIsIgnoreTrivial() throws Exception {
		assertEquals(IGNORE_TRIVIAL, arguments.isIgnoreTrivial());
	}

	@Test
	public void testIsThreadsafeRigorous() throws Exception {
		assertEquals(THREADSAFE_RIGOROUS, arguments.isThreadsafeRigorous());
	}

	@Test
	public void testGetEncoding() throws Exception {
		assertEquals(ENCODING, arguments.getEncoding());
	}

	@Test
	public void testGetMinimumCoverageThresholds() throws Exception {
		CoverageThreshold threshold = arguments.getMinimumCoverageThresholds()
				.iterator().next();
		assertEquals(MIN_LINE_PERCENTAGE, threshold.getMinLinePercentage());
		assertEquals(MIN_BRANCH_PERCENTAGE, threshold.getMinBranchPercentage());
		assertEquals(COVERAGE_THRESHOLD_REGEX, threshold.getRegex());
	}

	@Test
	public void testGetClassLineThreshold() throws Exception {
		assertEquals(CLASS_LINE_THRESHOLD, arguments.getClassLineThreshold(),
				DELTA);
	}

	@Test
	public void testGetClassBranchThreshold() throws Exception {
		assertEquals(CLASS_BRANCH_THRESHOLD, arguments
				.getClassBranchThreshold(), DELTA);
	}

	@Test
	public void testGetPackageLineThreshold() throws Exception {
		assertEquals(PACKAGE_LINE_THRESHOLD, arguments
				.getPackageLineThreshold(), DELTA);
	}

	@Test
	public void testGetPackageBranchThreshold() throws Exception {
		assertEquals(PACKAGE_BRANCH_THRESHOLD, arguments
				.getPackageBranchThreshold(), DELTA);
	}

	@Test
	public void testGetTotalLineThreshold() throws Exception {
		assertEquals(TOTAL_LINE_THRESHOLD, arguments.getTotalLineThreshold(),
				DELTA);
	}

	@Test
	public void testGetTotalBranchThreshold() throws Exception {
		assertEquals(TOTAL_BRANCH_THRESHOLD, arguments
				.getTotalBranchThreshold(), DELTA);
	}

	@Test
	public void testGetFilesToInstrument() throws Exception {
		assertTrue(arguments.getFilesToInstrument()
				.contains(FILE_TO_INSTRUMENT));
	}

	@Test
	public void testGetFilesToMerge() throws Exception {
		assertTrue(arguments.getFilesToMerge().contains(FILE_TO_MERGE));
	}

	@Test
	public void testGetIgnoreMethodAnnotations() throws Exception {
		assertTrue(arguments.getIgnoreMethodAnnotations().contains(
				IGNORE_METHOD_ANNOTATIONS));
	}

	@Test
	public void testGetSources() throws Exception {
		assertEquals(SOURCES, arguments.getSources());
	}
}
