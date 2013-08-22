package net.sourceforge.cobertura.check;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoverageRateTest {
	public static final double BRANCH_COVERAGE_RATE = 0.9;
	public static final double LINE_COVERAGE_RATE = 0.9;
	public static final double DELTA = 0.001;
	private CoverageRate coverageRate;

	@Before
	public void setUp() throws Exception {
		this.coverageRate = new CoverageRate(LINE_COVERAGE_RATE,
				BRANCH_COVERAGE_RATE);
	}

	@Test
	public void testGetLineCoverageRate() throws Exception {
		assertEquals(LINE_COVERAGE_RATE, coverageRate.getLineCoverageRate(),
				DELTA);
	}

	@Test
	public void testGetBranchCoverageRate() throws Exception {
		assertEquals(BRANCH_COVERAGE_RATE,
				coverageRate.getBranchCoverageRate(), DELTA);
	}
}
