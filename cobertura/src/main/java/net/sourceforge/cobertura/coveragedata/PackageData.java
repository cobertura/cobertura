/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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

package net.sourceforge.cobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;

import java.util.*;

@CoverageIgnore
public class PackageData extends CoverageDataContainer
		implements
			Comparable<Object> {

	private static final long serialVersionUID = 7;

	private String name;

	public PackageData(String name) {
		if (name == null)
			throw new IllegalArgumentException(
					"Package name must be specified.");
		this.name = name;
	}

	public void addClassData(ClassData classData) {
		lock.lock();
		try {
			if (children.containsKey(classData.getBaseName()))
				throw new IllegalArgumentException("Package " + this.name
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
		if (!o.getClass().equals(PackageData.class))
			return Integer.MAX_VALUE;
		return this.name.compareTo(((PackageData) o).name);
	}

	public boolean contains(String name) {
		lock.lock();
		try {
			return this.children.containsKey(name);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns true if the given object is an instance of the
	 * PackageData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		PackageData packageData = (PackageData) obj;
		getBothLocks(packageData);
		try {
			return super.equals(obj) && this.name.equals(packageData.name);
		} finally {
			lock.unlock();
			packageData.lock.unlock();
		}
	}

	public SortedSet getClasses() {
		lock.lock();
		try {
			return new TreeSet(this.children.values());
		} finally {
			lock.unlock();
		}
	}

	public String getName() {
		return this.name;
	}

	public String getSourceFileName() {
		return this.name.replace('.', '/');
	}

	public Collection getSourceFiles() {
		SortedMap sourceFileDatas = new TreeMap();

		lock.lock();
		try {
			Iterator iter = this.children.values().iterator();
			while (iter.hasNext()) {
				ClassData classData = (ClassData) iter.next();
				String sourceFileName = classData.getSourceFileName();
				SourceFileData sourceFileData = (SourceFileData) sourceFileDatas
						.get(sourceFileName);
				if (sourceFileData == null) {
					sourceFileData = new SourceFileData(sourceFileName);
					sourceFileDatas.put(sourceFileName, sourceFileData);
				}
				sourceFileData.addClassData(classData);
			}
		} finally {
			lock.unlock();
		}
		return sourceFileDatas.values();
	}

	public int hashCode() {
		return this.name.hashCode();
	}

}
