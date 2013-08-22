package net.sourceforge.cobertura.check;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PackageCoverageTest {

	public static final double DELTA = 0.001;
	public static final double SAMPLE_COVERAGE_COUNT = 0.1;
	public static final int TIMES = 3;
	private PackageCoverage packageCoverage;

	@Before
	public void setUp() throws Exception {
		this.packageCoverage = new PackageCoverage();
	}

	@Test
	public void testGetBranchCount() throws Exception {
		assertEquals(0, packageCoverage.getBranchCount(), DELTA);
	}

	@Test
	public void testAddBranchCount() throws Exception {
		int branchCountIncrement = 1;
		for (int j = 0; j < TIMES; j++) {
			packageCoverage.addBranchCount(branchCountIncrement);
		}

		assertEquals(TIMES * branchCountIncrement, packageCoverage
				.getBranchCount(), DELTA);
	}

	@Test
	public void testGetLineCount() throws Exception {
		assertEquals(0, packageCoverage.getLineCount(), DELTA);
	}

	@Test
	public void testAddLineCount() throws Exception {
		int lineCountIncrement = 1;
		for (int j = 0; j < TIMES; j++) {
			packageCoverage.addLineCount(lineCountIncrement);
		}

		assertEquals(TIMES * lineCountIncrement,
				packageCoverage.getLineCount(), DELTA);
	}

	@Test
	public void testGetBranchCoverage() throws Exception {
		assertEquals(0, packageCoverage.getBranchCoverage(), DELTA);
	}

	@Test
	public void testAddBranchCoverage() throws Exception {
		for (int j = 0; j < TIMES; j++) {
			packageCoverage.addBranchCoverage(SAMPLE_COVERAGE_COUNT);
		}

		assertEquals(TIMES * SAMPLE_COVERAGE_COUNT, packageCoverage
				.getBranchCoverage(), DELTA);
	}

	@Test
	public void testGetLineCoverage() throws Exception {
		assertEquals(0, packageCoverage.getLineCoverage(), DELTA);
	}

	@Test
	public void testAddLineCoverage() throws Exception {
		for (int j = 0; j < TIMES; j++) {
			packageCoverage.addLineCoverage(SAMPLE_COVERAGE_COUNT);
		}

		assertEquals(TIMES * SAMPLE_COVERAGE_COUNT, packageCoverage
				.getLineCoverage(), DELTA);
	}
}
