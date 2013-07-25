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

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.CoberturaInstrumenter.InstrumentationResult;
import net.sourceforge.cobertura.util.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

	private File destinationDirectory = null;

	private final ClassPattern classPattern = new ClassPattern();

	private final CoberturaInstrumenter coberturaInstrumenter = new CoberturaInstrumenter();

	public static URLClassLoader urlClassLoader;

	/**
	 * @param entry A zip entry.
	 *
	 * @return True if the specified entry has "class" as its extension,
	 *         false otherwise.
	 */
	private static boolean isClass(ZipEntry entry) {
		return entry.getName().endsWith(".class");
	}

	private boolean addInstrumentationToArchive(CoberturaFile file,
			InputStream archive, OutputStream output) throws Exception {
		ZipInputStream zis = null;
		ZipOutputStream zos = null;

		try {
			zis = new ZipInputStream(archive);
			zos = new ZipOutputStream(output);
			return addInstrumentationToArchive(file, zis, zos);
		} finally {
			zis = (ZipInputStream) IOUtil.closeInputStream(zis);
			zos = (ZipOutputStream) IOUtil.closeOutputStream(zos);
		}
	}

	private void addElementsToJVM(String classpath) {
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

	private boolean addInstrumentationToArchive(CoberturaFile file,
			ZipInputStream archive, ZipOutputStream output) throws Exception {
		/*
		 * "modified" is returned and indicates that something was instrumented.
		 * If nothing is instrumented, the original entry will be used by the
		 * caller of this method.
		 */
		boolean modified = false;
		ZipEntry entry;
		while ((entry = archive.getNextEntry()) != null) {
			try {
				String entryName = entry.getName();

				/*
				 * If this is a signature file then don't copy it,
				 * but don't set modified to true.  If the only
				 * thing we do is strip the signature, just use
				 * the original entry.
				 */
				if (ArchiveUtil.isSignatureFile(entry.getName())) {
					continue;
				}
				ZipEntry outputEntry = new ZipEntry(entry.getName());
				outputEntry.setComment(entry.getComment());
				outputEntry.setExtra(entry.getExtra());
				outputEntry.setTime(entry.getTime());
				output.putNextEntry(outputEntry);

				// Read current entry
				byte[] entryBytes = IOUtil
						.createByteArrayFromInputStream(archive);

				// Instrument embedded archives if a classPattern has been specified
				if ((classPattern.isSpecified())
						&& ArchiveUtil.isArchive(entryName)) {
					Archive archiveObj = new Archive(file, entryBytes);
					addInstrumentationToArchive(archiveObj);
					if (archiveObj.isModified()) {
						modified = true;
						entryBytes = archiveObj.getBytes();
						outputEntry.setTime(System.currentTimeMillis());
					}
				} else if (isClass(entry) && classPattern.matches(entryName)) {
					try {
						InstrumentationResult res = coberturaInstrumenter
								.instrumentClass(new ByteArrayInputStream(
										entryBytes));
						if (res != null) {
							logger.debug("Putting instrumented entry: "
									+ entry.getName());
							entryBytes = res.getContent();
							modified = true;
							outputEntry.setTime(System.currentTimeMillis());
						}
					} catch (Throwable t) {
						if (entry.getName().endsWith("_Stub.class")) {
							//no big deal - it is probably an RMI stub, and they don't need to be instrumented
							logger.debug(
									"Problems instrumenting archive entry: "
											+ entry.getName(), t);
						} else {
							logger.warn(
									"Problems instrumenting archive entry: "
											+ entry.getName(), t);
						}
					}
				}

				// Add entry to the output
				output.write(entryBytes);
				output.closeEntry();
				archive.closeEntry();
			} catch (Exception e) {
				logger.warn("Problems with archive entry: " + entry.getName(),
						e);
			} catch (Throwable t) {
				logger.warn("Problems with archive entry: " + entry.getName(),
						t);
			}
			output.flush();
		}
		return modified;
	}

	private void addInstrumentationToArchive(Archive archive) throws Exception {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = archive.getInputStream();
			out = new ByteArrayOutputStream();
			boolean modified = addInstrumentationToArchive(archive
					.getCoberturaFile(), in, out);

			if (modified) {
				out.flush();
				byte[] bytes = out.toByteArray();
				archive.setModifiedBytes(bytes);
			}
		} finally {
			in = IOUtil.closeInputStream(in);
			out = (ByteArrayOutputStream) IOUtil.closeOutputStream(out);
		}
	}

	private void addInstrumentationToArchive(CoberturaFile archive) {
		logger.debug("Instrumenting archive " + archive.getAbsolutePath());

		File outputFile = null;
		ZipInputStream input = null;
		ZipOutputStream output = null;
		boolean modified = false;
		try {
			// Open archive
			try {
				input = new ZipInputStream(new FileInputStream(archive));
			} catch (FileNotFoundException e) {
				logger.warn("Cannot open archive file: "
						+ archive.getAbsolutePath(), e);
				return;
			}

			// Open output archive
			try {
				// check if destination folder is set
				if (destinationDirectory != null) {
					// if so, create output file in it
					outputFile = new File(destinationDirectory, archive
							.getPathname());
				} else {
					// otherwise create output file in temporary location
					outputFile = File.createTempFile(
							"CoberturaInstrumentedArchive", "jar");
					outputFile.deleteOnExit();
				}
				output = new ZipOutputStream(new FileOutputStream(outputFile));
			} catch (IOException e) {
				logger.warn("Cannot open file for instrumented archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}

			// Instrument classes in archive
			try {
				modified = addInstrumentationToArchive(archive, input, output);
			} catch (Throwable e) {
				logger.warn("Cannot instrument archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}
		} finally {
			input = (ZipInputStream) IOUtil.closeInputStream(input);
			output = (ZipOutputStream) IOUtil.closeOutputStream(output);
		}

		// If destination folder was not set, overwrite orginal archive with
		// instrumented one
		if (modified && (destinationDirectory == null)) {
			try {
				logger.debug("Moving " + outputFile.getAbsolutePath() + " to "
						+ archive.getAbsolutePath());
				IOUtil.moveFile(outputFile, archive);
			} catch (IOException e) {
				logger.warn("Cannot instrument archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}
		}
		if ((destinationDirectory != null) && (!modified)) {
			outputFile.delete();
		}
	}

	private void addInstrumentationToSingleClass(File file) {
		logger.info("Instrumenting: " + file.getAbsolutePath() + " to "
				+ destinationDirectory);
		coberturaInstrumenter.addInstrumentationToSingleClass(file);
	}

	// TODO: Don't attempt to instrument a file if the outputFile already
	//       exists and is newer than the input file, and the output and
	//       input file are in different locations?
	private void addInstrumentation(CoberturaFile coberturaFile) {
		if (coberturaFile.isClass()
				&& classPattern.matches(coberturaFile.getPathname())) {
			addInstrumentationToSingleClass(coberturaFile);
		} else if (coberturaFile.isDirectory()) {
			String[] contents = coberturaFile.list();
			for (int i = 0; i < contents.length; i++) {
				File relativeFile = new File(coberturaFile.getPathname(),
						contents[i]);
				CoberturaFile relativeCoberturaFile = new CoberturaFile(
						coberturaFile.getBaseDir(), relativeFile.toString());
				//recursion!
				addInstrumentation(relativeCoberturaFile);
			}
		}
	}

	private void parseArguments(String[] args) {
		Collection<Pattern> ignoreRegexes = new Vector<Pattern>();
		coberturaInstrumenter.setIgnoreRegexes(ignoreRegexes);

		File dataFile = CoverageDataFileHandler.getDefaultDataFile();

		// Parse our parameters
		List<CoberturaFile> filePaths = new ArrayList<CoberturaFile>();
		String baseDir = null;

		boolean threadsafeRigorous = false;
		boolean ignoreTrivial = false;
		boolean failOnError = false;
		Set<String> ignoreMethodAnnotations = new HashSet<String>();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--basedir"))
				baseDir = args[++i];
			else if (args[i].equals("--datafile"))
				dataFile = new File(args[++i]);
			else if (args[i].equals("--destination")) {
				destinationDirectory = new File(args[++i]);
				coberturaInstrumenter
						.setDestinationDirectory(destinationDirectory);
			} else if (args[i].equals("--ignore")) {
				RegexUtil.addRegex(ignoreRegexes, args[++i]);
			}
			/*else if (args[i].equals("--ignoreBranches"))
			{
				RegexUtil.addRegex(ignoreBranchesRegexes, args[++i]);
			}*/
			else if (args[i].equals("--ignoreMethodAnnotation")) {
				ignoreMethodAnnotations.add(args[++i]);
			} else if (args[i].equals("--ignoreTrivial")) {
				ignoreTrivial = true;
			} else if (args[i].equals("--includeClasses")) {
				classPattern.addIncludeClassesRegex(args[++i]);
			} else if (args[i].equals("--excludeClasses")) {
				classPattern.addExcludeClassesRegex(args[++i]);
			} else if (args[i].equals("--failOnError")) {
				failOnError = true;
				logger.setFailOnError(true);
			} else if (args[i].equals("--threadsafeRigorous")) {
				threadsafeRigorous = true;
			} else if (args[i].equals("--auxClasspath")) {
				addElementsToJVM(args[++i]);
			} else {
				filePaths.add(new CoberturaFile(baseDir, args[i]));
			}
		}

		coberturaInstrumenter.setIgnoreTrivial(ignoreTrivial);
		coberturaInstrumenter
				.setIgnoreMethodAnnotations(ignoreMethodAnnotations);
		coberturaInstrumenter.setThreadsafeRigorous(threadsafeRigorous);
		coberturaInstrumenter.setFailOnError(failOnError);

		ProjectData projectData;

		// Load previous coverage data (if exists)
		projectData = dataFile.isFile() ? CoverageDataFileHandler
				.loadCoverageData(dataFile) : new ProjectData();
		coberturaInstrumenter.setProjectData(projectData);

		// Instrument classes
		logger.info("Instrumenting "
				+ filePaths.size()
				+ " "
				+ (filePaths.size() == 1 ? "file" : "files")
				+ (destinationDirectory != null ? " to "
						+ destinationDirectory.getAbsoluteFile() : ""));

		Iterator<CoberturaFile> iter = filePaths.iterator();
		while (iter.hasNext()) {
			CoberturaFile coberturaFile = iter.next();
			if (coberturaFile.isArchive()) {
				addInstrumentationToArchive(coberturaFile);
			} else {
				addInstrumentation(coberturaFile);
			}
		}

		// Save coverage data (ser file with list of touch points, but not hits registered).
		CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
	}

	public static void main(String[] args) {
		Header.print(System.out);

		long startTime = System.currentTimeMillis();

		Main main = new Main();

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			System.exit(1);
		}
		main.parseArguments(args);

		long stopTime = System.currentTimeMillis();
		logger.info("Instrument time: " + (stopTime - startTime) + "ms");
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
