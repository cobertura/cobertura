package net.sourceforge.cobertura.reporting.xml;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;
import org.apache.log4j.Logger;

public class SummaryXMLReportStrategy implements ReportFormatStrategy {
	private static final Logger log = Logger
			.getLogger(SummaryXMLReportStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new SummaryXMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getComplexity());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.SUMMARY_XML;
	}
}
