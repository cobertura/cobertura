package net.sourceforge.cobertura.dsl;

import net.sourceforge.cobertura.check.CoverageThreshold;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.instrument.CoberturaFile;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ArgumentsBuilderTest {

	public static final double SAMPLE_VALID_THRESHOLD = 0.7;
	public static final double DELTA = 0.001;
	public static final double HIGHER_THAN_VALID_THRESHOLD = 1.2;
	public static final int LOWER_THAN_VALID_THRESHOLD = -1;

	@Test
	public void testSetBaseDirectory() throws Exception {
		String baseDir = "somedir";
		assertEquals(baseDir, new ArgumentsBuilder().setBaseDirectory(baseDir)
				.build().getBaseDirectory().getPath());
	}

	@Test
	public void testSetDataFile() throws Exception {
		String dataFile = "somedir";
		assertEquals(dataFile, new ArgumentsBuilder().setDataFile(dataFile)
				.build().getDataFile().getPath());
	}

	@Test
	public void testSetDestinationFile() throws Exception {
		String destinationDir = "somedir";
		assertEquals(destinationDir, new ArgumentsBuilder()
				.setDestinationDirectory(destinationDir).build()
				.getDestinationDirectory().getPath());

	}

	@Test
	public void testSetCommandsFile() throws Exception {
		String commandsFile = "somedir";
		assertEquals(commandsFile, new ArgumentsBuilder().setCommandsFile(
				commandsFile).build().getCommandsFile().getPath());
	}

	@Test
	public void testAddIgnoreRegex() throws Exception {
		String someRegex = "someRegex";
		assertEquals(
				someRegex,
				((org.apache.oro.text.regex.Perl5Pattern) new ArgumentsBuilder()
						.addIgnoreRegex(someRegex).build().getIgnoreRegexes()
						.iterator().next()).getPattern());
	}

	@Test
	public void testAddIgnoreBranchRegex() throws Exception {
		String someRegex = "someRegex";
		assertEquals(someRegex, new ArgumentsBuilder().addIgnoreBranchRegex(
				someRegex).build().getIgnoreBranchesRegexes().iterator().next()
				.getPattern());
	}

	@Test
	public void testAddIgnoreMethodAnnotation() throws Exception {
		String someValue = "someValue";
		assertEquals(someValue, new ArgumentsBuilder()
				.addIgnoreMethodAnnotation(someValue).build()
				.getIgnoreMethodAnnotations().iterator().next());
	}

	@Test
	public void testAddExcludeClassesRegex() throws Exception {
		String someRegex = "someRegex";
		assertEquals(someRegex, new ArgumentsBuilder().addExcludeClassesRegex(
				someRegex).build().getClassPatternExcludeClassesRegexes()
				.iterator().next().getPattern());
	}

	@Test
	public void testAddIncludeClassesRegex() throws Exception {
		String someRegex = "someRegex";
		assertEquals(someRegex, new ArgumentsBuilder().addIncludeClassesRegex(
				someRegex).build().getClassPatternIncludeClassesRegexes()
				.iterator().next().getPattern());
	}

	@Test
	public void testFailOnError() throws Exception {
		boolean failOnError = true;
		assertEquals(failOnError, new ArgumentsBuilder().failOnError(
				failOnError).build().isFailOnError());
	}

	@Test
	public void testIgnoreTrivial() throws Exception {
		boolean ignoreTrivial = true;
		assertEquals(ignoreTrivial, new ArgumentsBuilder().ignoreTrivial(
				ignoreTrivial).build().isIgnoreTrivial());
	}

	@Test
	public void testThreadsafeRigorous() throws Exception {
		boolean threadSafeRigorous = true;
		assertEquals(threadSafeRigorous, new ArgumentsBuilder()
				.threadsafeRigorous(threadSafeRigorous).build()
				.isThreadsafeRigorous());
	}

	@Test
	public void testSetFormat() throws Exception {
		boolean failOnError = true;
		assertEquals(failOnError, new ArgumentsBuilder().failOnError(
				failOnError).build().isFailOnError());
	}

	@Test
	public void testSetEncoding() throws Exception {
		String encoding = "US-ASCII";
		assertEquals(encoding, new ArgumentsBuilder().setEncoding(encoding)
				.build().getEncoding());
	}

	@Test
	public void testAddMinimumCoverageRates() throws Exception {
		String regex = "regex";
		int lineCoverage = 50;
		int branchCoverage = 55;
		CoverageThreshold threshold = new ArgumentsBuilder()
				.addMinimumCoverageRates(regex, branchCoverage, lineCoverage)
				.build().getMinimumCoverageThresholds().iterator().next();
		assertEquals(regex, threshold.getRegex());
		assertEquals(lineCoverage, threshold.getMinLinePercentage());
		assertEquals(branchCoverage, threshold.getMinBranchPercentage());
	}

	@Test
	public void testSetClassBranchCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setClassBranchCoverageThreshold(SAMPLE_VALID_THRESHOLD)
				.build().getClassBranchThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativeClassBranchCoverageThreshold() throws Exception {
		new ArgumentsBuilder()
				.setClassBranchCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidClassBranchCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setClassBranchCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testSetClassLineCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setClassLineCoverageThreshold(SAMPLE_VALID_THRESHOLD).build()
				.getClassLineThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativeClassLineCoverageThreshold() throws Exception {
		new ArgumentsBuilder()
				.setClassLineCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidClassLineCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setClassLineCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testSetPackageBranchCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setPackageBranchCoverageThreshold(SAMPLE_VALID_THRESHOLD)
				.build().getPackageBranchThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativePackageBranchCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setPackageBranchCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidPackageBranchCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setPackageBranchCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testSetPackageLineCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setPackageLineCoverageThreshold(SAMPLE_VALID_THRESHOLD)
				.build().getPackageLineThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativePackageLineCoverageThreshold() throws Exception {
		new ArgumentsBuilder()
				.setPackageLineCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidPackageLineCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setPackageLineCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testSetTotalBranchCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setTotalBranchCoverageThreshold(SAMPLE_VALID_THRESHOLD)
				.build().getTotalBranchThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativeTotalBranchCoverageThreshold() throws Exception {
		new ArgumentsBuilder()
				.setTotalBranchCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidTotalBranchCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setTotalBranchCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testSetTotalLineCoverageThreshold() throws Exception {
		assertEquals(SAMPLE_VALID_THRESHOLD, new ArgumentsBuilder()
				.setTotalLineCoverageThreshold(SAMPLE_VALID_THRESHOLD).build()
				.getTotalLineThreshold(), DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativeTotalLineCoverageThreshold() throws Exception {
		new ArgumentsBuilder()
				.setTotalLineCoverageThreshold(LOWER_THAN_VALID_THRESHOLD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetGreaterThanValidTotalLineCoverageThreshold()
			throws Exception {
		new ArgumentsBuilder()
				.setTotalLineCoverageThreshold(HIGHER_THAN_VALID_THRESHOLD);
	}

	@Test
	public void testAddFileToInstrument() throws Exception {
		String fileToInstrument = "fileToInstrument";
		CoberturaFile file = new ArgumentsBuilder().addFileToInstrument(
				fileToInstrument).build().getFilesToInstrument().iterator()
				.next();
		assertEquals(new File(".", fileToInstrument).getPath(), file.getPath());
	}

	@Test
	public void testAddFileToMerge() throws Exception {
		String fileToMerge = "fileToMerge";
		File file = new ArgumentsBuilder().addFileToMerge(fileToMerge).build()
				.getFilesToMerge().iterator().next();
		assertEquals(fileToMerge, file.getPath());
	}

	@Test
	public void testDefaultValues() throws Exception {
		Arguments defaultArgs = new ArgumentsBuilder().build();

		assertEquals(".", defaultArgs.getBaseDirectory().getPath());
		assertEquals(CoverageDataFileHandler.getDefaultDataFile().getPath(),
				defaultArgs.getDataFile().getPath());

		assertNotNull(defaultArgs.getIgnoreBranchesRegexes());
		assertTrue(defaultArgs.getIgnoreBranchesRegexes().isEmpty());
		assertNotNull(defaultArgs.getIgnoreMethodAnnotations());
		assertTrue(defaultArgs.getIgnoreMethodAnnotations().isEmpty());
		assertNotNull(defaultArgs.getClassPatternExcludeClassesRegexes());
		assertTrue(defaultArgs.getClassPatternExcludeClassesRegexes().isEmpty());
		assertNotNull(defaultArgs.getClassPatternIncludeClassesRegexes());
		assertTrue(defaultArgs.getClassPatternIncludeClassesRegexes().isEmpty());
		assertNotNull(defaultArgs.getFilesToInstrument());
		assertTrue(defaultArgs.getFilesToInstrument().isEmpty());
		assertNotNull(defaultArgs.getFilesToMerge());
		assertTrue(defaultArgs.getFilesToMerge().isEmpty());
		assertNotNull(defaultArgs.getMinimumCoverageThresholds());
		assertTrue(defaultArgs.getMinimumCoverageThresholds().isEmpty());

		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getClassBranchThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getClassLineThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getPackageBranchThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getPackageLineThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getTotalBranchThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_THRESHOLD, defaultArgs
				.getTotalLineThreshold(), DELTA);
		assertEquals(ArgumentsBuilder.DEFAULT_FAIL_ON_ERROR, defaultArgs
				.isFailOnError());
		assertEquals(ArgumentsBuilder.DEFAULT_IGNORE_TRIVIAL, defaultArgs
				.isIgnoreTrivial());
		assertEquals(ArgumentsBuilder.DEFAULT_THREADSAFE_RIGOROUS, defaultArgs
				.isThreadsafeRigorous());
		assertEquals(ArgumentsBuilder.DEFAULT_ENCODING, defaultArgs
				.getEncoding());
	}
}
