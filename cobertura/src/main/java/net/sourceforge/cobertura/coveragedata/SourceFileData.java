/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2006 Jiri Mares
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

package net.sourceforge.cobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.util.StringUtil;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

@CoverageIgnore
public class SourceFileData extends CoverageDataContainer
		implements
			Comparable<Object> {

	private static final long serialVersionUID = 3;

	private String name;

	/**
	 * @param name In the format, "net/sourceforge/cobertura/coveragedata/SourceFileData.java"
	 */
	public SourceFileData(String name) {
		if (name == null)
			throw new IllegalArgumentException(
					"Source file name must be specified.");
		this.name = name;
	}

	public void addClassData(ClassData classData) {
		lock.lock();
		try {
			if (children.containsKey(classData.getBaseName()))
				throw new IllegalArgumentException("Source file " + this.name
						+ " already contains a class with the name "
						+ classData.getBaseName());

			// Each key is a class basename, stored as an String object.
			// Each value is information about the class, stored as a ClassData object.
			children.put(classData.getBaseName(), classData);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This is required because we implement Comparable.
	 */
	public int compareTo(Object o) {
		if (!o.getClass().equals(SourceFileData.class))
			return Integer.MAX_VALUE;
		return this.name.compareTo(((SourceFileData) o).name);
	}

	public boolean contains(String name) {
		lock.lock();
		try {
			return this.children.containsKey(name);
		} finally {
			lock.unlock();
		}
	}

	public boolean containsInstrumentationInfo() {
		lock.lock();
		try {
			// Return false if any of our child ClassData's does not
			// contain instrumentation info
			Iterator iter = this.children.values().iterator();
			while (iter.hasNext()) {
				ClassData classData = (ClassData) iter.next();
				if (!classData.containsInstrumentationInfo())
					return false;
			}
		} finally {
			lock.unlock();
		}
		return true;
	}

	/**
	 * Returns true if the given object is an instance of the
	 * SourceFileData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		SourceFileData sourceFileData = (SourceFileData) obj;
		getBothLocks(sourceFileData);
		try {
			return super.equals(obj) && this.name.equals(sourceFileData.name);
		} finally {
			lock.unlock();
			sourceFileData.lock.unlock();
		}
	}

	public String getBaseName() {
		String fullNameWithoutExtension;
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1) {
			fullNameWithoutExtension = this.name;
		} else {
			fullNameWithoutExtension = this.name.substring(0, lastDot);
		}

		int lastSlash = fullNameWithoutExtension.lastIndexOf('/');
		if (lastSlash == -1) {
			return fullNameWithoutExtension;
		}
		return fullNameWithoutExtension.substring(lastSlash + 1);
	}

	public SortedSet getClasses() {
		lock.lock();
		try {
			return new TreeSet(this.children.values());
		} finally {
			lock.unlock();
		}
	}

	public LineData getLineCoverage(int lineNumber) {
		lock.lock();
		try {
			Iterator iter = this.children.values().iterator();
			while (iter.hasNext()) {
				ClassData classData = (ClassData) iter.next();
				if (classData.isValidSourceLineNumber(lineNumber))
					return classData.getLineCoverage(lineNumber);
			}
		} finally {
			lock.unlock();
		}
		return null;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * @return The name of this source file without the file extension
	 *         in the format
	 *         "net.sourceforge.cobertura.coveragedata.SourceFileData"
	 */
	public String getNormalizedName() {
		String fullNameWithoutExtension;
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1) {
			fullNameWithoutExtension = this.name;
		} else {
			fullNameWithoutExtension = this.name.substring(0, lastDot);
		}

		return StringUtil.replaceAll(fullNameWithoutExtension, "/", ".");
	}

	/**
	 * @return The name of the package that this source file is in.
	 *         In the format "net.sourceforge.cobertura.coveragedata"
	 */
	public String getPackageName() {
		int lastSlash = this.name.lastIndexOf('/');
		if (lastSlash == -1) {
			return null;
		}
		return StringUtil.replaceAll(this.name.substring(0, lastSlash), "/",
				".");
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	public boolean isValidSourceLineNumber(int lineNumber) {
		lock.lock();
		try {
			Iterator iter = this.children.values().iterator();
			while (iter.hasNext()) {
				ClassData classData = (ClassData) iter.next();
				if (classData.isValidSourceLineNumber(lineNumber))
					return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

}
