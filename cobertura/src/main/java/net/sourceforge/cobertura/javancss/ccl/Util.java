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

import java.util.Vector;

/**
 * A general purpose class with a variety of support and convenience methods.
 * <p/>
 * <p> There are different groups of methods in this class:
 * <br>
 * <br><a href="#print(char)">print methods</a> - convenience methods for System.out.print etc. that additionally make sure output is gets flushed immediately.
 * <br><a href="#isEmpty(java.lang.String)">string methods</a>
 * <br><a href="#concat(java.util.Vector)">string/vector converter methods</a>
 * <br><a href="#getConstantObject()">miscellaneous methods</a>
 * <p/>
 * <p/>
 * Some basic but none the less the most used methods by myself are:<br>
 * - {@link #isEmpty(java.lang.String) isEmpty}<br>
 * - {@link #stringToLines(java.lang.String) stringToLines}<br>
 * - {@link #sleep(int) sleep}<br>
 * <p/>
 *
 * @author <a href="http://www.kclee.com/clemens/">
 *         Chr. Clemens Lee</a>
 *         &lt;<a href="mailto:clemens@kclee.com">
 *         clemens@kclee.com
 *         </a>>
 */

/*
 * cobertura - this file was moved from net.sourceforge.cobertura.javancss package.
 * 
 * Mark Doliner apparently got the source from somewhere, but it is not available now.
 */

public class Util {

	public static final Object CONSTANT_OBJECT = new Object();

	/**
	 * This is an utility class, there is (should be) no need
	 * for an instance of this class.
	 */
	private Util() {
		super();
	}

	// -----------------------------------------------------
	// debug methods and assertion stuff
	// -----------------------------------------------------

	/**
	 * panicIf <=> not assert. Throws ApplicationException if true.
	 * It's not necessary to catch this exception.
	 */
	public static void panicIf(boolean bPanic_) {
		if (bPanic_) {
			throw (new RuntimeException());
		}
	}

	/**
	 * panicIf <=> not assert. Throws ApplicationException if true.
	 * It's not necessary to catch this exception.
	 *
	 * @param sMessage_ The error message for the Exception.
	 */
	public static void panicIf(boolean bPanic_, String sMessage_) {
		if (bPanic_) {
			throw (new RuntimeException(sMessage_));
		}
	}

	/**
	 * Tests, if a given String equals null or "".
	 */
	public static boolean isEmpty(String sTest_) {
		if (sTest_ == null || sTest_.equals("")) {
			return true;
		}

		return false;
	}

	/**
	 * This function takes a String and separates it into different
	 * lines. The last line does not need to have a separator character.
	 *
	 * @param lines_   The number of lines that should be extracted.
	 *                 Zero if maximum number of lines is requested.
	 * @param cCutter_ The character that separates pString_ into
	 *                 different lines
	 *
	 * @return The single lines do not contain the cCutter_
	 *         character at the end.
	 */
	private static Vector stringToLines(int lines_, String pString_,
			char cCutter_) {
		int maxLines = Integer.MAX_VALUE;
		if (lines_ > 0) {
			maxLines = lines_;
		}

		Vector vRetVal = new Vector();
		if (pString_ == null) {
			return vRetVal;
		}

		int startIndex = 0;
		for (; maxLines > 0; maxLines--) {
			int endIndex = pString_.indexOf(cCutter_, startIndex);
			if (endIndex == -1) {
				if (startIndex < pString_.length()) {
					endIndex = pString_.length();
				} else {
					break;
				}
			}
			String sLine = pString_.substring(startIndex, endIndex);
			vRetVal.addElement(sLine);
			startIndex = endIndex + 1;
		}

		return vRetVal;
	}

	/**
	 * This function takes a String and separates it into different
	 * lines. The last line does not need to have a separator character.
	 *
	 * @param cCutter_ The character that separates pString_ into
	 *                 different lines
	 *
	 * @return The single lines do not contain the cCutter_ character
	 *         at the end.
	 */
	public static Vector stringToLines(String pString_, char cCutter_) {
		return stringToLines(0, pString_, cCutter_);
	}

	/**
	 * This function takes a String and separates it into different
	 * lines. The last line does not need to have a '\n'. The function
	 * can't handle dos carriage returns.
	 *
	 * @return The single lines do not contain the '\n' character
	 *         at the end.
	 */
	public static Vector stringToLines(String pString_) {
		return stringToLines(pString_, '\n');
	}

	/**
	 * Current thread sleeps in seconds.
	 */
	private static void sleep(int seconds_) {
		try {
			Thread.sleep(seconds_ * 1000);
		} catch (Exception pException) {
		}
	}

	public static Vector concat(Vector vFirst_, Vector vSecond_) {
		//cobertura Mark Doliner appears to have simplified this.
		vFirst_.addAll(vSecond_);
		return vFirst_;
	}

	public static Vector<Object> sort(Vector<Object> input) {
		//cobertura - apparently Mark Doliner didn't think we need to sort.
		return input;
	}

	// cobertura - gotten from decompile of ccl.jar.
	public static void debug(Object oMessage_) {
		if (_bDebug)
			printlnErr(oMessage_.toString());
	}

	public static Object getConstantObject() {
		return CONSTANT_OBJECT;
	}

	// cobertura - gotten from decompile of ccl.jar.
	public static void setDebug(boolean bDebug_) {
		_bDebug = bDebug_;
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static boolean isDebug() {
		return _bDebug;
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static void println(String pString_) {
		System.out.println(pString_);
		System.out.flush();
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static void printErr(char c_) {
		System.err.print(c_);
		System.err.flush();
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static void printErr(String pString_) {
		System.err.print(pString_);
		System.err.flush();
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static void printlnErr() {
		printErr('\n');
	}

	// cobertura = gotten from decompile of ccl.jar.
	public static void printlnErr(String sMessage_) {
		printErr(sMessage_);
		printlnErr();
	}

	// cobertura = gotten from decompile of ccl.jar.
	private static boolean _bDebug = false;

}
