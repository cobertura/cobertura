package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.ReportFormat;

/**
 * Strategy to export Report data into a specific format.
 */
public interface ReportFormatStrategy {
	/**
	 * Exports Report information to a given format
	 * @param report - Report object which contains coverage information.
	 */
	void save(Report report);

	/**
	 * Returns name of report format strategy
	 * @return ReportFormat - some report format value.
	 */
	ReportFormat getName();
}
