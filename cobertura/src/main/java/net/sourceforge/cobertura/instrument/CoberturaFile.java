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

import net.sourceforge.cobertura.util.ArchiveUtil;

import java.io.File;

/**
 * This represents a regular File, but unlike java.io.File, the baseDir and
 * relative pathname used to create it are saved for later use.
 *
 * @author John Lewis
 */
public class CoberturaFile extends File {

	private static final long serialVersionUID = 0L;

	private String baseDir;
	private String pathname;

	public CoberturaFile(String baseDir, String pathname) {
		super(baseDir, pathname);
		this.baseDir = baseDir;
		this.pathname = pathname;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public String getPathname() {
		return pathname;
	}

	/**
	 * @return True if file has an extension that matches one of the
	 *         standard java archives, false otherwise.
	 */
	boolean isArchive() {
		if (!isFile()) {
			return false;
		}
		return ArchiveUtil.isArchive(pathname);
	}

	/**
	 * @return True if file has "class" as its extension,
	 *         false otherwise.
	 */
	boolean isClass() {
		return isFile() && pathname.endsWith(".class");
	}

	public String toString() {
		return "pathname=" + pathname + " and baseDir=" + baseDir;
	}

}
