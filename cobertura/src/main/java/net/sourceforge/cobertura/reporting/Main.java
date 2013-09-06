/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 Dan Godfrey
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static void parseArgumentsAndReport(String[] args) throws Exception {
		ArgumentsBuilder builder = new ArgumentsBuilder();

		String baseDir = null;
		String format = null;
		boolean sourcesParam = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--basedir")) {
				baseDir = args[++i];
				builder.setBaseDirectory(baseDir);
			} else if (args[i].equals("--datafile")) {
				String datafile = args[++i];
				validateDataFile(datafile);
				builder.setDataFile(datafile);
			} else if (args[i].equals("--destination")) {
				String destination = args[++i];
				builder.setDestinationDirectory(destination);
				validateAndCreateDestinationDirectory(destination);
			} else if (args[i].equals("--format")) {
				format = args[++i];
				validateFormat(format);
			} else if (args[i].equals("--encoding")) {
				builder.setEncoding(args[++i]);
			} else {
				builder.addSources(args[i], baseDir == null);
			}
		}

		Arguments arguments = builder.build();

		if (arguments.getDestinationDirectory() == null) {
			System.err.println("Error: destination directory must be set");
			System.exit(1);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("format is %s encoding is %s", format,
					arguments.getEncoding()));
			LOGGER.debug("dataFile is "
					+ arguments.getDataFile().getAbsolutePath());
			LOGGER.debug("destinationDir is "
					+ arguments.getDestinationDirectory().getAbsolutePath());
		}

		new Cobertura(arguments).report().export(
				ReportFormat.getFromString(format));
	}

	private static void validateFormat(String format) {
		if (!format.equalsIgnoreCase("html") && !format.equalsIgnoreCase("xml")
				&& !format.equalsIgnoreCase("summaryXml")) {
			System.err.println("" + "Error: format \"" + format
					+ "\" is invalid. Must be either html, xml or summaryXml");
			System.exit(1);
		}
	}

	private static void validateDataFile(String value) {
		File dataFile = new File(value);
		if (!dataFile.exists()) {
			System.err.println("Error: data file " + dataFile.getAbsolutePath()
					+ " does not exist");
			System.exit(1);
		}
		if (!dataFile.isFile()) {
			System.err.println("Error: data file " + dataFile.getAbsolutePath()
					+ " must be a regular file");
			System.exit(1);
		}
	}

	private static void validateAndCreateDestinationDirectory(String value) {
		File destinationDir = new File(value);
		if (destinationDir.exists() && !destinationDir.isDirectory()) {
			System.err.println("Error: destination directory " + destinationDir
					+ " already exists but is not a directory");
			System.exit(1);
		}
		destinationDir.mkdirs();
	}

	public static void main(String[] args) throws Exception {
		Header.print(System.out);

		long startTime = System.currentTimeMillis();

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			System.exit(1);
		}

		parseArgumentsAndReport(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Report time: " + (stopTime - startTime) + "ms");
	}
}
