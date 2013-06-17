/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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

package net.sourceforge.cobertura.reporting.html.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class CopyFiles {
	
	public static void copy(File destinationDir) throws IOException {
		File cssOutputDir = new File(destinationDir, "css");
		File imagesOutputDir = new File(destinationDir, "images");
		File jsOutputDir = new File(destinationDir, "js");

		destinationDir.mkdirs();
		cssOutputDir.mkdir();
		imagesOutputDir.mkdir();
		jsOutputDir.mkdir();

		copyResourceFromJar("help.css", cssOutputDir);
		copyResourceFromJar("main.css", cssOutputDir);
		copyResourceFromJar("sortabletable.css", cssOutputDir);
		copyResourceFromJar("source-viewer.css", cssOutputDir);
		copyResourceFromJar("tooltip.css", cssOutputDir);

		copyResourceFromJar("blank.png", imagesOutputDir);
		copyResourceFromJar("downsimple.png", imagesOutputDir);
		copyResourceFromJar("upsimple.png", imagesOutputDir);

		copyResourceFromJar("customsorttypes.js", jsOutputDir);
		copyResourceFromJar("popup.js", jsOutputDir);
		copyResourceFromJar("sortabletable.js", jsOutputDir);
		copyResourceFromJar("stringbuilder.js", jsOutputDir);

		copyResourceFromJar("help.html", destinationDir);
		copyResourceFromJar("index.html", destinationDir);
	}

	/**
	 * Copy a file from the jar to a directory on the local machine.
	 *
	 * @param resourceName The name of the file in the jar.  This file
	 *                     must exist the same package as this method.
	 * @param directory    The directory to copy the jar to.
	 *
	 * @throws IOException If the file could not be read from the
	 *                     jar or written to the disk.
	 */
	private static void copyResourceFromJar(String resourceName, File directory)
			throws IOException {
		int n;
		byte[] buf = new byte[1024];

		InputStream in = null;
		FileOutputStream out = null;
		directory.mkdirs();
		try {
			in = CopyFiles.class.getResourceAsStream(resourceName);
			if (in == null) {
				throw new IllegalArgumentException("Resource " + resourceName
						+ " does not exist in this package.");
			}
			out = new FileOutputStream(new File(directory, resourceName));
			while ((n = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, n);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
