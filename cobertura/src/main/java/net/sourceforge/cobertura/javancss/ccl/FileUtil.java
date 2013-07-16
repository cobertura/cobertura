/**
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the reusable ccl java library
 * (http://www.kclee.com/clemens/java/ccl/).
 *
 * The Initial Developer of the Original Code is
 * Chr. Clemens Lee.
 * Portions created by Chr. Clemens Lee are Copyright (C) 2002
 * Chr. Clemens Lee. All Rights Reserved.
 *
 * Contributor(s): Chr. Clemens Lee <clemens@kclee.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 **/

package net.sourceforge.cobertura.javancss.ccl;

import java.io.*;

/**
 * Utility class for file operations.<p>
 * <p/>
 * Simple but most commonly used methods of this class are:<br>
 * - {@link #readFile(java.lang.String) readFile}<br>
 * - {@link #concatPath(java.lang.String, java.lang.String) concatPath}<br>
 * <p/>
 * Other less frequently used but still handy methods are:<br>
 * - {@link #normalizeFileName(java.lang.String) normalizeFileName} to take the current user directory into account via the 'user.dir' system property<br>
 *
 * @author <a href="http://www.kclee.com/clemens/">
 *         Chr. Clemens Lee</a>
 *         &lt;<a href="mailto:clemens@kclee.com">
 *         clemens@kclee.com
 *         </a>>
 * @version $Id: FileUtil.java 384 2006-03-17 20:10:49Z thekingant $
 */
/*
 * cobertura - this file was moved from net.sourceforge.cobertura.javancss package.
 * Mark Doliner apparently got the source from somewhere, but it is not available now.
 */
public class FileUtil {

	/**
	 * Utility class which should never instanciate itself.
	 */
	private FileUtil() {
		super();
	}

	/**
	 * Concatenates a file path with the file name. If
	 * necessary it adds a File.separator between the path
	 * and file name. For example "/home" or "/home/" and "clemens" both
	 * become "/home/clemens".<p>
	 * <p/>
	 * This method is inspired from the FrIJDE project out
	 * of the gCollins.File.FileTools class.<p>
	 * <p/>
	 * FrIJDE Homepage:
	 * http://amber.wpi.edu/~thethe/Document/Besiex/Java/FrIJDE/
	 *
	 * @param sPath_ a directory path. Is not allowed to be null.
	 * @param sFile_ the base name of a file.
	 *
	 * @return sPath_ if sFile_ is empty.
	 */
	public static String concatPath(String sPath_, String sFile_) {
		Util.panicIf(sPath_ == null);
		//System.out.println("ccl.util.FileUtil.concatPath(..).sPath_: --->" + sPath_ + "<---");
		//System.out.println("ccl.util.FileUtil.concatPath(..).sFile_: " + sFile_);

		String sRetVal = sPath_;

		if (!Util.isEmpty(sFile_)) {
			if (sPath_.length() > 0 && !sPath_.endsWith(File.separator)) {
				sRetVal += File.separator;
			}

			sRetVal += sFile_;
		}

		return sRetVal;
	}

	/**
	 * Reads a File and returns the content in a String.
	 * CRLF -> LF conversion takes place. This is a convenience method so you don't
	 * need to bother creating a file reader object and closing it after it has
	 * been used.
	 *
	 * @param sFileName_ the name of the file to read.
	 *
	 * @return a string with the content of the file but without
	 *         any CR characters.
	 *
	 * @throws FileNotFoundException if file does not exist.
	 * @throws IOException           if any file operation fails.
	 */
	public static String readFile(String sFileName_) throws IOException,
			FileNotFoundException {
		StringBuffer sFileContent = new StringBuffer(100000);

		try {
			FileReader frIni = new FileReader(sFileName_);
			if (frIni != null) {
				BufferedReader brIni = new BufferedReader(frIni);
				if (brIni != null) {
					while (brIni.ready()) {
						String sLine = brIni.readLine();
						if (sLine == null) {
							break;
						}
						sFileContent.append(sLine).append('\n');
					}
					brIni.close();
				}
				frIni.close();
			}
		} catch (FileNotFoundException fileNotFoundException) {
			throw new FileNotFoundException("No such file: '" + sFileName_
					+ "'");
		}

		return sFileContent.toString();
	}

	/**
	 * @return It's the canonical path of sFileName_.
	 */
	public static String getAbsoluteFileName(String sFileName_) {
		String sRetVal = null;

		try {
			File pFile = new File(sFileName_);
			sRetVal = pFile.getCanonicalPath();
		} catch (Exception e) {
			return null;
		}

		return sRetVal;
	}

	/**
	 * This method returns an absolute (canonical)
	 * file name. The difference to getAbsoluteFileName
	 * is that this method uses the system property
	 * "user.dir" instead of the native system's current
	 * directory. This way you get a chance of changing
	 * the current directory inside Java and let your
	 * program reflect that change.
	 */
	public static String normalizeFileName(String sFile) {
		return normalizeFileName(sFile, (String) System.getProperties().get(
				"user.dir"));
	}

	/**
	 * This method returns an absolute (canonical)
	 * file name. The difference to getAbsoluteFileName
	 * is that this method uses the system property
	 * sUserDir instead of the native system's current
	 * directory. This way you get a chance of changing
	 * the current directory inside Java and let your
	 * program reflect that change.
	 */
	private static String normalizeFileName(String sFile, String sUserDir) {
		sFile = sFile.trim();
		if (Util.isEmpty(sFile) || sFile.equals(".")) {
			sFile = sUserDir;
		} else if (!FileUtil.isAbsolute(sFile)) {
			sFile = FileUtil.concatPath(sUserDir, sFile);
		}
		sFile = FileUtil.getAbsoluteFileName(sFile);

		return sFile;
	}

	/**
	 * Tests if the file represented by this File object is an absolute
	 * pathname. The definition of an absolute pathname is system
	 * dependent. For example, on UNIX, a pathname is absolute if its first
	 * character is the separator character. On Windows
	 * platforms, a pathname is absolute if its first character is an
	 * ASCII '\' or '/', or if it begins with a letter followed by a colon.
	 */
	private static boolean isAbsolute(String sFileName_) {
		return new File(sFileName_).isAbsolute();
	}

}
