/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2007 Ignat Zapolsky
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

import java.io.*;

/**
 * Helper class with useful I/O operations.
 *
 * @author Grzegorz Lukasik
 */
public abstract class IOUtil {

	/**
	 * Copies bytes from input stream into the output stream.  Stops
	 * when the input stream read method returns -1.  Does not close
	 * the streams.
	 *
	 * @throws IOException          If either passed stream will throw IOException.
	 * @throws NullPointerException If either passed stream is null.
	 */
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException {
		// NullPointerException is explicity thrown to guarantee expected behaviour
		if (in == null || out == null)
			throw new NullPointerException();

		int el;
		byte[] buffer = new byte[1 << 15];
		while ((el = in.read(buffer)) != -1) {
			out.write(buffer, 0, el);
		}
	}

	/**
	 * Returns an array that contains values read from the
	 * given input stream.
	 *
	 * @throws NullPointerException If null stream is passed.
	 */
	public static byte[] createByteArrayFromInputStream(InputStream in)
			throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		copyStream(in, byteArray);
		return byteArray.toByteArray();
	}

	/**
	 * Moves a file from one location to other.
	 *
	 * @throws IOException          If IO exception occur during moving.
	 * @throws NullPointerException If either passed file is null.
	 */
	public static void moveFile(File sourceFile, File destinationFile)
			throws IOException {
		if (destinationFile.exists()) {
			destinationFile.delete();
		}

		// Move file using File method if possible
		boolean succesfulMove = sourceFile.renameTo(destinationFile);
		if (succesfulMove)
			return;

		// Copy file from source to destination
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(destinationFile);
			copyStream(in, out);
		} finally {
			in = closeInputStream(in);
			out = closeOutputStream(out);
		}

		// Remove source file
		sourceFile.delete();
	}

	/**
	 * Closes an input stream.
	 *
	 * @param in The stream to close.
	 *
	 * @return null unless an exception was thrown while closing, else
	 *         returns the stream
	 */
	public static InputStream closeInputStream(InputStream in) {
		if (in != null) {
			try {
				in.close();
				in = null;
			} catch (IOException e) {
				System.err.println("Cobertura: Error closing input stream.");
				e.printStackTrace();
			}
		}
		return in;
	}

	/**
	 * Closes an output stream.
	 *
	 * @param out The stream to close.
	 *
	 * @return null unless an exception was thrown while closing, else
	 *         returns the stream.
	 */
	public static OutputStream closeOutputStream(OutputStream out) {
		if (out != null) {
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				System.err.println("Cobertura: Error closing output stream.");
				e.printStackTrace();
			}
		}
		return out;
	}

	public static PrintWriter getPrintWriter(File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		Writer osWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8"), 16384);
		PrintWriter pw = new PrintWriter(osWriter, false);
		return pw;
	}

}
