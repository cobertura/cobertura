/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
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

import junit.framework.TestCase;

import java.io.*;

/**
 * @author Grzegorz Lukasik
 */
public class IOUtilTest extends TestCase {

	private static final byte[] emptyByteArray = new byte[]{};

	private static final byte[] singletonByteArray = new byte[]{7};

	private static final byte[] smallByteArray = new byte[]{1, 0, 2, -128, 127};

	private File createFileWithData(byte[] data) throws IOException {
		File file = File.createTempFile("IOUtilTest", ".txt");
		file.deleteOnExit();
		OutputStream src = new FileOutputStream(file);
		src.write(data);
		src.close();
		return file;
	}

	public void testMoveFile() throws IOException {
		// Move file if destination does not exist
		File srcFile = createFileWithData(smallByteArray);
		File destFile = createFileWithData(emptyByteArray);
		destFile.delete();
		assertTrue(!destFile.isFile());
		IOUtil.moveFile(srcFile, destFile);
		assertTrue(destFile.isFile());
		InputStream in = new FileInputStream(destFile);
		for (int i = 0; i < smallByteArray.length; i++)
			assertEquals(smallByteArray[i], (byte) in.read());
		assertEquals(-1, in.read());
		in.close();

		// Move file if destination exists
		srcFile = createFileWithData(singletonByteArray);
		destFile = createFileWithData(smallByteArray);
		IOUtil.moveFile(srcFile, destFile);
		assertTrue(destFile.isFile());
		in = new FileInputStream(destFile);
		for (int i = 0; i < singletonByteArray.length; i++)
			assertEquals(singletonByteArray[i], (byte) in.read());
		assertEquals(-1, in.read());
		in.close();

		// Pass null values
		srcFile = createFileWithData(smallByteArray);
		try {
			IOUtil.moveFile(srcFile, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException ex) {
		}

		destFile = createFileWithData(smallByteArray);
		try {
			IOUtil.moveFile(null, destFile);
			fail("Expected NullPointerException");
		} catch (NullPointerException ex) {
		}
	}

	public void testCopyStream() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(smallByteArray);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtil.copyStream(in, out);
		assertEquals(smallByteArray, out.toByteArray());

		in = new ByteArrayInputStream(singletonByteArray);
		out = new ByteArrayOutputStream();
		IOUtil.copyStream(in, out);
		assertEquals(singletonByteArray, out.toByteArray());

		in = new ByteArrayInputStream(emptyByteArray);
		out = new ByteArrayOutputStream();
		IOUtil.copyStream(in, out);
		assertEquals(emptyByteArray, out.toByteArray());

		byte[] bigArray = generateBigByteArray();
		in = new ByteArrayInputStream(bigArray);
		out = new ByteArrayOutputStream();
		IOUtil.copyStream(in, out);
		assertEquals(bigArray, out.toByteArray());

		try {
			IOUtil.copyStream(null, new ByteArrayOutputStream());
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}

		try {
			IOUtil.copyStream(new ByteArrayInputStream(bigArray), null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	public void testFillByteArrayFromInputStream() throws IOException {
		byte[] out = IOUtil
				.createByteArrayFromInputStream(new ByteArrayInputStream(
						smallByteArray));
		assertEquals(smallByteArray, out);

		out = IOUtil.createByteArrayFromInputStream(new ByteArrayInputStream(
				emptyByteArray));
		assertEquals(emptyByteArray, out);

		out = IOUtil.createByteArrayFromInputStream(new ByteArrayInputStream(
				singletonByteArray));
		assertEquals(singletonByteArray, out);

		byte[] bigArray = generateBigByteArray();
		out = IOUtil.createByteArrayFromInputStream(new ByteArrayInputStream(
				bigArray));
		assertEquals(bigArray, out);

		try {
			IOUtil.createByteArrayFromInputStream(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		for (int i = 0; i < first.length; i++) {
			assertEquals(first[i], second[i]);
		}
	}

	private byte[] generateBigByteArray() {
		byte[] bigArray = new byte[1000000];
		for (int i = 0; i < bigArray.length; i++) {
			bigArray[i] = (byte) i;
		}
		return bigArray;
	}

}
