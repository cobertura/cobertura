package net.sourceforge.cobertura.reporting.html;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;
import org.apache.log4j.Logger;

public class HTMLReportFormatStrategy implements ReportFormatStrategy {
	private static final Logger log = Logger
			.getLogger(HTMLReportFormatStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new HTMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getFinder(),
					nativeReport.getComplexity(), nativeReport.getEncoding());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.HTML;
	}
}
