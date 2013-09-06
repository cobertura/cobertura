/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2009 Charlie Squires
 * Copyright (C) 2009 John Lewis
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
package net.sourceforge.cobertura.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps source file names to existing files. After adding description
 * of places files can be found in, it can be used to localize
 * the files.
 * <p/>
 * <p/>
 * FileFinder supports two types of source files locations:
 * <ul>
 * <li>source root directory, defines the directory under
 * which source files are located,</li>
 * <li>pair (base directory, file path relative to base directory).</li>
 * </ul>
 * The difference between these two is that in case of the first you add all
 * source files under the specified root directory, and in the second you add
 * exactly one file. In both cases file to be found has to be located under
 * subdirectory that maps to package definition provided with the source file name.
 *
 * @author Jeremy Thomerson
 */
public class FileFinder {

	private static Logger LOGGER = LoggerFactory.getLogger(FileFinder.class);

	// Contains Strings with directory paths
	private Set sourceDirectories = new HashSet();

	// Contains pairs (String directoryRoot, Set fileNamesRelativeToRoot)
	private Map sourceFilesMap = new HashMap();

	/**
	 * Adds directory that is a root of sources. A source file
	 * that is under this directory will be found if relative
	 * path to the file from root matches package name.
	 * <p>
	 * Example:
	 * <pre>
	 * fileFinder.addSourceDirectory( "C:/MyProject/src/main");
	 * fileFinder.addSourceDirectory( "C:/MyProject/src/test");
	 * </pre>
	 * In path both / and \ can be used.
	 * </p>
	 *
	 * @param directory The root of source files
	 *
	 * @throws NullPointerException if <code>directory</code> is <code>null</code>
	 */
	public void addSourceDirectory(String directory) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Adding sourceDirectory=[" + directory + "]");

		// Change \ to / in case of Windows users
		directory = getCorrectedPath(directory);
		sourceDirectories.add(directory);
	}

	/**
	 * Adds file by specifying root directory and relative path to the
	 * file in it. Adds exactly one file, relative path should match
	 * package that the source file is in, otherwise it will be not
	 * found later.
	 * <p>
	 * Example:
	 * <pre>
	 * fileFinder.addSourceFile( "C:/MyProject/src/main", "com/app/MyClass.java");
	 * fileFinder.addSourceFile( "C:/MyProject/src/test", "com/app/MyClassTest.java");
	 * </pre>
	 * In paths both / and \ can be used.
	 * </p>
	 *
	 * @param baseDir sources root directory
	 * @param file    path to source file relative to <code>baseDir</code>
	 *
	 * @throws NullPointerException if either <code>baseDir</code> or <code>file</code> is <code>null</code>
	 */
	public void addSourceFile(String baseDir, String file) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Adding sourceFile baseDir=[" + baseDir + "] file=["
					+ file + "]");

		if (baseDir == null || file == null)
			throw new NullPointerException();

		// Change \ to / in case of Windows users
		file = getCorrectedPath(file);
		baseDir = getCorrectedPath(baseDir);

		// Add file to sourceFilesMap
		Set container = (Set) sourceFilesMap.get(baseDir);
		if (container == null) {
			container = new HashSet();
			sourceFilesMap.put(baseDir, container);
		}
		container.add(file);
	}

	/**
	 * Maps source file name to existing file.
	 * When mapping file name first values that were added with
	 * {@link #addSourceDirectory} and later added with {@link #addSourceFile} are checked.
	 *
	 * @param fileName source file to be mapped
	 *
	 * @return existing file that maps to passed sourceFile
	 *
	 * @throws IOException          if cannot map source file to existing file
	 * @throws NullPointerException if fileName is null
	 */
	public File getFileForSource(String fileName) throws IOException {
		// Correct file name
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Searching for file, name=[" + fileName + "]");
		fileName = getCorrectedPath(fileName);

		// Check inside sourceDirectories
		for (Iterator it = sourceDirectories.iterator(); it.hasNext();) {
			String directory = (String) it.next();
			File file = new File(directory, fileName);
			if (file.isFile()) {
				LOGGER.debug("Found inside sourceDirectories");
				return file;
			}
		}

		// Check inside sourceFilesMap
		for (Iterator it = sourceFilesMap.keySet().iterator(); it.hasNext();) {
			String directory = (String) it.next();
			Set container = (Set) sourceFilesMap.get(directory);
			if (!container.contains(fileName))
				continue;
			File file = new File(directory, fileName);
			if (file.isFile()) {
				LOGGER.debug("Found inside sourceFilesMap");
				return file;
			}
		}

		// Have not found? Throw an error.
		LOGGER.debug("File not found");
		throw new IOException("Cannot find source file, name=[" + fileName
				+ "]");
	}

	/**
	 * Maps source file name to existing file or source archive.
	 * When mapping file name first values that were added with
	 * {@link #addSourceDirectory} and later added with {@link #addSourceFile} are checked.
	 *
	 * @param fileName source file to be mapped
	 *
	 * @return Source that maps to passed sourceFile or null if it can't be found
	 *
	 * @throws NullPointerException if fileName is null
	 */
	public Source getSource(String fileName) {
		File file = null;
		try {
			file = getFileForSource(fileName);
			return new Source(new FileInputStream(file), file);
		} catch (IOException e) {
			//Source file wasn't found. Try searching archives.
			return searchJarsForSource(fileName);
		}

	}

	/**
	 * Gets a BufferedReader for a file within a jar.
	 *
	 * @param fileName source file to get an input stream for
	 *
	 * @return Source for existing file inside a jar that maps to passed sourceFile
	 *         or null if cannot map source file to existing file
	 */
	private Source searchJarsForSource(String fileName) {
		//Check inside jars in sourceDirectories
		for (Iterator it = sourceDirectories.iterator(); it.hasNext();) {
			String directory = (String) it.next();
			File file = new File(directory);
			//Get a list of jars and zips in the directory
			String[] jars = file.list(new JarZipFilter());
			if (jars != null) {
				for (String jar : jars) {
					try {
						LOGGER.debug("Looking for: " + fileName + " in " + jar);
						JarFile jf = new JarFile(directory + "/" + jar);

						//Get a list of files in the jar
						Enumeration<JarEntry> files = jf.entries();
						//See if the jar has the class we need
						while (files.hasMoreElements()) {
							JarEntry entry = files.nextElement();
							if (entry.getName().equals(fileName)) {
								return new Source(jf.getInputStream(entry), jf);
							}
						}
					} catch (Throwable t) {
						LOGGER.warn("Error while reading " + jar, t);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns a list with string for all source directories.
	 * Example: <code>[C:/MyProject/src/main,C:/MyProject/src/test]</code>
	 *
	 * @return list with Strings for all source roots, or empty list if no source roots were specified
	 */
	public List getSourceDirectoryList() {
		// Get names from sourceDirectories
		List result = new ArrayList();
		for (Iterator it = sourceDirectories.iterator(); it.hasNext();) {
			result.add(it.next());
		}

		// Get names from sourceFilesMap
		for (Iterator it = sourceFilesMap.keySet().iterator(); it.hasNext();) {
			result.add(it.next());
		}

		// Return combined names
		return result;
	}

	private String getCorrectedPath(String path) {
		return path.replace('\\', '/');
	}

	/**
	 * Returns string representation of FileFinder.
	 */
	public String toString() {
		return "FileFinder, source directories: "
				+ getSourceDirectoryList().toString();
	}

	/**
	 * A filter that accepts files that end in .jar or .zip
	 */
	private class JarZipFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".jar") || name.endsWith(".zip"));
		}
	}
}
