/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
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

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;

import java.io.File;
import java.util.List;

/**
 * Utility methods for working with archives.
 *
 * @author John Lewis
 */
public abstract class ArchiveUtil {

	/**
	 * Return true if the given name ends with .jar, .zip,
	 * .war, .ear, or .sar (case insensitive).
	 *
	 * @param name The file name.
	 *
	 * @return true if the name is an archive.
	 */
	public static boolean isArchive(String name) {
		name = name.toLowerCase();
		return name.endsWith(".jar") || name.endsWith(".zip")
				|| name.endsWith(".war") || name.endsWith(".ear")
				|| name.endsWith(".sar");
	}

	/**
	 * Check to see if the given file name is a signature file
	 * (meta-inf/*.rsa or meta-inf/*.sf).
	 *
	 * @param name The file name.  Commonly a ZipEntry name.
	 *
	 * @return true if the name is a signature file.
	 */
	public static boolean isSignatureFile(String name) {
		name = name.toLowerCase();
		return (name.startsWith("meta-inf/") && (name.endsWith(".rsa") || name
				.endsWith(".sf")));
	}

	public static void getFiles(File baseDir, String validExtension,
			List<File> files) {
		String[] children = baseDir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (String filename : children) {
				File file = new File(baseDir, filename);
				if (filename.endsWith(validExtension)) {
					files.add(file);
				} else {
					if (file.isDirectory()) {
						getFiles(file, validExtension, files);
					}
				}
			}
		}
	}
}
