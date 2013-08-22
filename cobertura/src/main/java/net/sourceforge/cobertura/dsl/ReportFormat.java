package net.sourceforge.cobertura.dsl;

public enum ReportFormat {
	XML("xml"), HTML("html"), SUMMARY_XML("summaryXml"), UNKNOWN("unknown");

	private ReportFormat(String format) {
		this.format = format;
	}
	private String format;
	@Override
	public String toString() {
		return format;
	}

	public static ReportFormat getFromString(String format) {
		for (ReportFormat reportFormat : values()) {
			if (reportFormat.toString().equalsIgnoreCase(format)) {
				return reportFormat;
			}
		}
		return UNKNOWN;
	}
}
