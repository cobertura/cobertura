package net.sourceforge.cobertura.reporting.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;

public class SummaryXMLReportStrategy implements ReportFormatStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(SummaryXMLReportStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new SummaryXMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getComplexity());
		} catch (Exception e) {
			log.error("Saving summary report failed.", e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.SUMMARY_XML;
	}
}
