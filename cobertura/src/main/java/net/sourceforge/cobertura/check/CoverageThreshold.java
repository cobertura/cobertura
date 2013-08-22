package net.sourceforge.cobertura.check;

/**
 * Holds data about coverage thresholds for given regexes
 */
public class CoverageThreshold {
	private String regex;
	private int minBranchPercentage;
	private int minLinePercentage;

	/**
	 * Costructor
	 * @param regex - a regex expression
	 * @param minBranchPercentage -minimum expected branch coverage percentage
	 * @param minLinePercentage -minimum expected line coverage percentage
	 */
	public CoverageThreshold(String regex, int minBranchPercentage,
			int minLinePercentage) {
		this.regex = regex;
		this.minBranchPercentage = minBranchPercentage;
		this.minLinePercentage = minLinePercentage;
	}

	public String getRegex() {
		return regex;
	}

	public int getMinBranchPercentage() {
		return minBranchPercentage;
	}

	public int getMinLinePercentage() {
		return minLinePercentage;
	}
}
