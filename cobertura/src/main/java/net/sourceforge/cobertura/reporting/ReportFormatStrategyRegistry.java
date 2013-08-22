package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.html.HTMLReportFormatStrategy;
import net.sourceforge.cobertura.reporting.xml.SummaryXMLReportStrategy;
import net.sourceforge.cobertura.reporting.xml.XMLReportFormatStrategy;

import java.util.HashMap;
import java.util.Map;

public class ReportFormatStrategyRegistry {
	private static ReportFormatStrategyRegistry instance = null;
	private Map<ReportFormat, ReportFormatStrategy> reportFormatsRegistry;

	private ReportFormatStrategyRegistry() {
		reportFormatsRegistry = new HashMap<ReportFormat, ReportFormatStrategy>();
		registerReportFormatStrategies();
	}

	public ReportFormatStrategy getReportFormatStrategy(
			ReportFormat reportFormat) {
		return reportFormatsRegistry.get(reportFormat);
	}

	private void registerReportFormatStrategies() {
		reportFormatsRegistry = new HashMap<ReportFormat, ReportFormatStrategy>();
		registerReportFormatStrategy(new SummaryXMLReportStrategy());
		registerReportFormatStrategy(new XMLReportFormatStrategy());
		registerReportFormatStrategy(new HTMLReportFormatStrategy());
	}

	private void registerReportFormatStrategy(
			ReportFormatStrategy reportFormatStrategy) {
		reportFormatsRegistry.put(reportFormatStrategy.getName(),
				reportFormatStrategy);
	}

	public static ReportFormatStrategyRegistry getInstance() {
		if (instance == null) {
			instance = new ReportFormatStrategyRegistry();
		}
		return instance;
	}
}
