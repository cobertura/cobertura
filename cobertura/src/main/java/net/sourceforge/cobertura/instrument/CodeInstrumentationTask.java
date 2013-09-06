package net.sourceforge.cobertura.instrument;

import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p/>
 * <p>
 * After every line in a class has been "instrumented," Cobertura
 * edits the bytecode for the class one more time and adds
 * "implements net.sourceforge.cobertura.coveragedata.HasBeenInstrumented"
 * This is basically just a flag used internally by Cobertura to
 * determine whether a class has been instrumented or not, so
 * as not to instrument the same class twice.
 * </p>
 */
public class CodeInstrumentationTask {
	private static final LoggerWrapper logger = new LoggerWrapper();
	private CoberturaInstrumenter coberturaInstrumenter;
	private File destinationDirectory;
	private ClassPattern classPattern;

	public CodeInstrumentationTask instrument(Arguments arguments,
			ProjectData projectData) throws Throwable {
		destinationDirectory = arguments.getDestinationDirectory();
		classPattern = new ClassPattern();
		coberturaInstrumenter = new CoberturaInstrumenter();
		coberturaInstrumenter.setIgnoreRegexes(arguments.getIgnoreRegexes());
		// Parse our parameters
		Set<CoberturaFile> filePaths = arguments.getFilesToInstrument();

		File dataFile = arguments.getDataFile();
		destinationDirectory = arguments.getDestinationDirectory();
		coberturaInstrumenter.setDestinationDirectory(destinationDirectory);
		arguments.getIgnoreRegexes();
		classPattern.addExcludeClassesRegex(arguments
				.getClassPatternExcludeClassesRegexes());
		classPattern.addIncludeClassesRegex(arguments
				.getClassPatternIncludeClassesRegexes());
		coberturaInstrumenter.setIgnoreTrivial(arguments.isIgnoreTrivial());
		coberturaInstrumenter.setIgnoreMethodAnnotations(arguments
				.getIgnoreMethodAnnotations());
		coberturaInstrumenter.setThreadsafeRigorous(arguments
				.isThreadsafeRigorous());
		coberturaInstrumenter.setFailOnError(arguments.isFailOnError());
		coberturaInstrumenter.setProjectData(projectData);

		// Instrument classes
		logger.info(String.format("Instrumenting %s %s %s", filePaths.size(),
				(filePaths.size() == 1 ? "file" : "files"),
				(destinationDirectory != null ? " to "
						+ destinationDirectory.getAbsoluteFile() : "")));

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
		return this;
	}

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
						CoberturaInstrumenter.InstrumentationResult res = coberturaInstrumenter
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

	// TODO: Preserved current behaviour, but this code is failing on WARN, not error
	private static class LoggerWrapper {
		private final Logger logger = LoggerFactory.getLogger(Main.class);
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
