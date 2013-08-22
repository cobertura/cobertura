package net.sourceforge.cobertura.reporting.xml;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportFormatStrategy;
import org.apache.log4j.Logger;

public class XMLReportFormatStrategy implements ReportFormatStrategy {
	private static final Logger log = Logger
			.getLogger(XMLReportFormatStrategy.class);

	public void save(Report report) {
		NativeReport nativeReport = (NativeReport) report;
		try {
			new XMLReport(nativeReport.getProjectData(), nativeReport
					.getDestinationDir(), nativeReport.getFinder(),
					nativeReport.getComplexity());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public ReportFormat getName() {
		return ReportFormat.XML;
	}
}
