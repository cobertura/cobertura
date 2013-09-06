package net.sourceforge.cobertura.reporting.html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;

public class HTMLReportFormatStrategy implements ReportFormatStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(HTMLReportFormatStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new HTMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getFinder(),
					nativeReport.getComplexity(), nativeReport.getEncoding());
		} catch (Exception e) {
			log.error("Saving HTML report failed.", e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.HTML;
	}
}
