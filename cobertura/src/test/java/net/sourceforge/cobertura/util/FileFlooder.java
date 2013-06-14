/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Jeremy Thomerson
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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * I used this class to flood aa directory with files to test a fix for a guy
 * that had a ton of files mixed with his code.  You could use it to do similar
 * for fixes / testing / misc.
 *
 * @author Jeremy Thomerson
 */
public class FileFlooder {

	private static final Logger log = Logger.getLogger(FileFlooder.class);

	public static void flood(String directory, String fileName, String fileExt,
			int numOfFiles, int linesPerFile) {
		File dir = new File(directory);
		if (dir.exists() && dir.isDirectory()) {
			for (int i = 1; i <= numOfFiles; i++) {
				FileWriter writer = null;
				try {
					File file = new File(directory + "/" + fileName + i + "."
							+ fileExt);
					log.info("Writing file: " + file.getAbsolutePath());
					writer = new FileWriter(file);
					for (int l = 1; l <= linesPerFile; l++) {
						writer
								.write("This is a test file. blah.... blah.... blah....\n");
					}
				} catch (IOException ioe) {
					log.error("Error while writing file.", ioe);
				} finally {
					if (writer != null)
						try {
							writer.close();
						} catch (IOException e) {
							//Nothing we can do here
						}
				}
			}
		}
	}

	public static void main(String[] args) {
		FileFlooder.flood(".", "file", "txt", 100, 1000);
		log.info("done");
	}

}
