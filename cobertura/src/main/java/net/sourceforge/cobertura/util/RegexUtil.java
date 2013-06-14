/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 John Lewis
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

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract, not to be instantiated utility class for Regex functions.
 *
 * @author John Lewis (logic copied from MethodInstrumenter)
 */
public abstract class RegexUtil {

	private static final Logger logger = Logger.getLogger(RegexUtil.class);

	private final static Perl5Matcher pm = new Perl5Matcher();

	/**
	 * <p>
	 * Check to see if one of the regular expressions in a collection match
	 * an input string.
	 * </p>
	 *
	 * @param regexs The collection of regular expressions.
	 * @param str    The string to check for a match.
	 *
	 * @return True if a match is found.
	 */
	public static boolean matches(Collection regexs, String str) {
		Iterator iter = regexs.iterator();
		while (iter.hasNext()) {
			Pattern regex = (Pattern) iter.next();
			if (pm.matches(str, regex)) {
				return true;
			}
		}

		return false;
	}

	public static void addRegex(Collection list, String regex) {
		try {
			Perl5Compiler pc = new Perl5Compiler();
			Pattern pattern = pc.compile(regex);
			list.add(pattern);
		} catch (MalformedPatternException e) {
			logger.warn("The regular expression " + regex + " is invalid: "
					+ e.getLocalizedMessage());
		}
	}

}
