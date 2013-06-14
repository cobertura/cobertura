/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class CopyFilesTest extends TestCase {
	private final static String basedir = (System.getProperty("basedir") != null)
			? System.getProperty("basedir")
			: "./";
	private final static File tmpDir = new File(basedir,
			"/target/build/test/tmp");

	public void setUp() {
		tmpDir.mkdirs();
	}

	private final static void removeDir(File dir) {
		File files[] = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				removeDir(files[i]);
			else
				files[i].delete();
		}
		dir.delete();
	}

	public void tearDown() {
		removeDir(tmpDir);
	}

	public static void testCopy() throws IOException {
		CopyFiles.copy(tmpDir);
		assertTrue(new File(tmpDir, "help.html").isFile());
		assertTrue(new File(tmpDir, "index.html").isFile());

		File cssDir = new File(tmpDir, "css");
		assertTrue(cssDir.isDirectory());
		assertTrue(new File(cssDir, "help.css").isFile());
		assertTrue(new File(cssDir, "main.css").isFile());
		assertTrue(new File(cssDir, "sortabletable.css").isFile());
		assertTrue(new File(cssDir, "source-viewer.css").isFile());
		assertTrue(new File(cssDir, "tooltip.css").isFile());

		File imagesDir = new File(tmpDir, "images");
		assertTrue(imagesDir.isDirectory());
		assertTrue(new File(imagesDir, "blank.png").isFile());
		assertTrue(new File(imagesDir, "downsimple.png").isFile());
		assertTrue(new File(imagesDir, "upsimple.png").isFile());

		File jsDir = new File(tmpDir, "js");
		assertTrue(jsDir.isDirectory());
		assertTrue(new File(jsDir, "customsorttypes.js").isFile());
		assertTrue(new File(jsDir, "popup.js").isFile());
		assertTrue(new File(jsDir, "sortabletable.js").isFile());
		assertTrue(new File(jsDir, "stringbuilder.js").isFile());
	}
}