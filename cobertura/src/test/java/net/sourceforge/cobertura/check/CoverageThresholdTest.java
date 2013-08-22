package net.sourceforge.cobertura.check;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoverageThresholdTest {

	public static final int MIN_LINE_PERCENTAGE = 50;
	public static final int MIN_BRANCH_PERCENTAGE = 50;
	public static final String REGEX = "*";
	private CoverageThreshold coverageThreshold;

	@Before
	public void setUp() throws Exception {
		this.coverageThreshold = new CoverageThreshold(REGEX,
				MIN_BRANCH_PERCENTAGE, MIN_LINE_PERCENTAGE);
	}

	@Test
	public void testGetRegex() throws Exception {
		assertEquals(REGEX, coverageThreshold.getRegex());
	}

	@Test
	public void testGetMinBranchPercentage() throws Exception {
		assertEquals(MIN_BRANCH_PERCENTAGE, coverageThreshold
				.getMinBranchPercentage());
	}

	@Test
	public void testGetMinLinePercentage() throws Exception {
		assertEquals(MIN_LINE_PERCENTAGE, coverageThreshold
				.getMinLinePercentage());
	}
}
