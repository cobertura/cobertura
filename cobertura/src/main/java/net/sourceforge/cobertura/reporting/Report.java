package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.ReportFormat;

/**
 * Report that contains metrics calculated on data.
 */
public interface Report {

	/**
	 * Uses the given ReportFormatStrategy to export report data.
	 * @param reportFormat - a report format value,
	 *                             never <code>null</code>
	 */
	void export(ReportFormat reportFormat);

}
