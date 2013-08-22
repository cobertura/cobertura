/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Jiri Mares
 * Copyright (C) 2008 Scott Frederick
 * Copyright (C) 2010 Tad Smith 
 * Copyright (C) 2010 Piotr Tabor  
 * Contact information for the above is given in the COPYRIGHT file.
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

package net.sourceforge.cobertura.instrument;

import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.util.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * <p>
 * Add coverage instrumentation to existing classes.
 * </p>
 * <p/>
 * <h3>What does that mean, exactly?</h3>
 * <p>
 * It means Cobertura will look at each class you give it.  It
 * loads the bytecode into memory.  For each line of source,
 * Cobertura adds a few extra instructions.  These instructions
 * do the following:
 * </p>
 * <p/>
 * <ol>
 * <li>Get an instance of the ProjectData class.</li>
 * <li>Call a method in this ProjectData class that increments
 * a counter for this line of code.
 * </ol>
 */
public class Main {
	private static final LoggerWrapper logger = new LoggerWrapper();
	public static URLClassLoader urlClassLoader;

	public static void main(String[] args) {
		Header.print(System.out);

		long startTime = System.currentTimeMillis();

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			System.exit(1);
		}
		try {
			new Cobertura(createArgumentsFromCMDParams(args).build())
					.instrumentCode().saveProjectData();
		} catch (Throwable throwable) {
			System.err.println(String.format(
					"Failed while instrumenting code: %s", throwable
							.getMessage()));
			throwable.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		logger.info("Instrument time: " + (stopTime - startTime) + "ms");
	}

	private static ArgumentsBuilder createArgumentsFromCMDParams(String[] args) {
		ArgumentsBuilder builder = new ArgumentsBuilder();

		// Parse parameters
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--basedir")) {
				String baseDir = args[++i];
				builder.setBaseDirectory(baseDir);
			} else if (args[i].equals("--datafile"))
				builder.setDataFile(args[++i]);
			else if (args[i].equals("--destination")) {
				builder.setDestinationDirectory(args[++i]);
			} else if (args[i].equals("--ignore")) {
				builder.addIgnoreRegex(args[++i]);
			} else if (args[i].equals("--ignoreMethodAnnotation")) {
				builder.addIgnoreMethodAnnotation(args[++i]);
			} else if (args[i].equals("--ignoreTrivial")) {
				builder.ignoreTrivial(true);
			} else if (args[i].equals("--includeClasses")) {
				builder.addIncludeClassesRegex(args[++i]);
			} else if (args[i].equals("--excludeClasses")) {
				builder.addExcludeClassesRegex(args[++i]);
			} else if (args[i].equals("--failOnError")) {
				builder.failOnError(true);
				logger.setFailOnError(true);
			} else if (args[i].equals("--threadsafeRigorous")) {
				builder.threadsafeRigorous(true);
			} else if (args[i].equals("--auxClasspath")) {
				addElementsToJVM(args[++i]);
			} else {
				builder.addFileToInstrument(args[i]);
			}
		}
		return builder;
	}

	private static void addElementsToJVM(String classpath) {
		List<URL> urlsArray = new ArrayList<URL>();
		String[] classpathParsed = classpath.split(File.pathSeparator);

		for (String element : classpathParsed) {
			File f = null;
			try {
				f = new File(element);
				urlsArray.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Warning - could not convert file: " + element
						+ " to a URL.", e);
			}
		}
		urlClassLoader = new URLClassLoader(urlsArray.toArray(new URL[urlsArray
				.size()]));
	}

	// TODO: Preserved current behaviour, but this code is failing on WARN, not error
	private static class LoggerWrapper {
		private final Logger logger = Logger.getLogger(Main.class);
		private boolean failOnError = false;

		public void setFailOnError(boolean failOnError) {
			this.failOnError = failOnError;
		}

		public void debug(String message) {
			logger.debug(message);
		}

		public void debug(String message, Throwable t) {
			logger.debug(message, t);
		}

		public void info(String message) {
			logger.debug(message);
		}

		public void warn(String message, Throwable t) {
			logger.warn(message, t);
			if (failOnError) {
				throw new RuntimeException(
						"Warning detected and failOnError is true", t);
			}
		}
	}
}
