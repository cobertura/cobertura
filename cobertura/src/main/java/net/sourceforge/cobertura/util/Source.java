/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2009 John Lewis
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

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Source {
	private InputStream is;

	//streamOrigin is either a File or a ZipFile
	private Object streamOrigin;

	private static Logger LOGGER = LoggerFactory.getLogger(Source.class);

	public Source(InputStream is, Object streamOrigin) {
		this.is = is;
		this.streamOrigin = streamOrigin;
	}

	public InputStream getInputStream() {
		return is;
	}

	/**
	 * Close the source input stream and the archive if it came from one.
	 * <p/>
	 * This will not throw anything.   Any throwable is caught and a warning is logged.
	 */
	public void close() {
		try {
			is.close();
		} catch (Throwable t) {
			LOGGER.warn("Failure closing input stream for " + getOriginDesc(),
					t);
		}

		if (streamOrigin instanceof ZipFile) {
			try {
				((ZipFile) streamOrigin).close();
			} catch (Throwable t) {
				LOGGER.warn("Failure closing " + getOriginDesc(), t);
			}
		}
	}

	public String getOriginDesc() {
		String ret = "";

		if (streamOrigin instanceof File) {
			ret = "file " + ((File) streamOrigin).getAbsolutePath();
		} else {
			ret = "archive " + ((ZipFile) streamOrigin).getName();
		}
		return ret;
	}
}
