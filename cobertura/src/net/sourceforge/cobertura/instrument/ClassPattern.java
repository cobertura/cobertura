/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.instrument;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.cobertura.util.RegexUtil;

/**
 * This class represents a collection of regular expressions that will be used to see
 * if a classname matches them.
 * 
 * Regular expressions are specified by calling add methods.  If no add methods are
 * called, this class will match any classname.
 * 
 * @author John Lewis
 *
 */
public class ClassPattern
{

	private Set<String> includeClassesRegexes = new HashSet<String>();

	private Set<String> excludeClassesRegexes = new HashSet<String>();

	private static final String WEBINF_CLASSES = "WEB-INF/classes/";
	
	/**
	 * Returns true if any regular expressions have been specified by calling the
	 * add methods.  If none are specified, this class matches anything.
	 * 
	 * @return true if any regular expressions have been specified
	 */
	boolean isSpecified()
	{
		return includeClassesRegexes.size() > 0;
	}

	/**
	 * Check to see if a class matches this ClassPattern
	 * 
	 * If a pattern has not been specified, this matches anything.
	 * 
	 * This method also looks for "WEB-INF/classes" at the beginning of the
	 * classname.  It is removed before checking for a match.
	 * 
	 * @param filename Either a full classname or a full class filename
	 * @return true if the classname matches this ClassPattern or if this ClassPattern
	 * has not been specified.
	 */
	boolean matches(String filename)
	{
		boolean matches = true;

		if (isSpecified())
		{
			matches = false;
			// Remove .class extension if it exists
			if (filename.endsWith(".class"))
			{
				filename = filename.substring(0, filename.length() - 6);
			}
			filename = filename.replace('\\', '/');

			filename = removeAnyWebInfClassesString(filename);

			filename = filename.replace('/', '.');
			if (RegexUtil.matches(includeClassesRegexes, filename))
			{
				matches = true;
			}
			if (matches && RegexUtil.matches(excludeClassesRegexes, filename))
			{
				matches = false;
			}
		}
		return matches;
	}

	private String removeAnyWebInfClassesString(String filename)
	{
		if (filename.startsWith(WEBINF_CLASSES))
		{
			filename = filename.substring(WEBINF_CLASSES.length());
		}
		return filename;
	}

	/**
	 * Add a regex to the list of class regexes to include.
	 * 
	 * @param regex A regular expression to add.
	 */
	void addIncludeClassesRegex(String regex)
	{
		RegexUtil.addRegex(includeClassesRegexes, regex);
	}

	/**
	 * Add a regex to the list of class regexes to exclude.
	 * 
	 * @param regex
	 */
	void addExcludeClassesRegex(String regex)
	{
		RegexUtil.addRegex(excludeClassesRegexes, regex);
	}

}
