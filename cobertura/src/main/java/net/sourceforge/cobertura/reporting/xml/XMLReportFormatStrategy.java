package net.sourceforge.cobertura.reporting.xml;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLReportFormatStrategy implements ReportFormatStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(XMLReportFormatStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new XMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getFinder(),
					nativeReport.getComplexity());
		} catch (Exception e) {
			log.error("Saving XML report failed.", e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.XML;
	}
}
