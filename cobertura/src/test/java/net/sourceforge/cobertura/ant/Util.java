/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
 * 
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License 1.1 (so that it can be used from both the main
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

package net.sourceforge.cobertura.ant;

import java.io.*;

class Util {

	static File createTemporaryTextFile(String prefix) throws IOException {
		File outputFile;
		outputFile = File.createTempFile(prefix, ".txt");
		outputFile.deleteOnExit();
		return outputFile;
	}

	/**
	 * Returns the text of a file as a string.
	 *
	 * @param file The file to read.
	 *
	 * @return A string containing the text of the file
	 */
	static String getText(File file) throws FileNotFoundException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				ps.println(line);
			}
			ps.close();
		} finally {
			ps.close();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println("IOException when closing file "
							+ file.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		return baos.toString();
	}

}
