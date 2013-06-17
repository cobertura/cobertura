/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
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

package net.sourceforge.cobertura.reporting;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * <p>
 * This is a very simple XML EntityResolver.  If
 * you are parsing an XML document using a DocumentBuilder,
 * and you set the DocumentBuilder's EntityResolver to an
 * instance of this class, then we never attempt to resolve
 * XML documents on the Internet.  Instead we use a local
 * copy of the DTD.
 * </p>
 * <p/>
 * <p>
 * This is done so that the XMLReportTest.java JUnit test will
 * not fail when the test is run on a non-networked machine,
 * or when webpages must be accessed through a proxy server.
 * </p>
 */
public class JUnitXMLParserEntityResolver extends DefaultHandler {

	private final File DTD_DIRECTORY;

	public JUnitXMLParserEntityResolver(File dtdDirectory) {
		this.DTD_DIRECTORY = dtdDirectory;
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException {
		System.out.println("systemId=" + systemId);
		String systemIdBasename = systemId.substring(systemId.lastIndexOf('/'));
		File localDtd = new File(this.DTD_DIRECTORY, systemIdBasename);
		try {
			return new InputSource(new FileInputStream(localDtd));
		} catch (FileNotFoundException e) {
			System.out.println("Unable to open local DTD file "
					+ localDtd.getAbsolutePath() + ", using " + systemId
					+ " instead.");
		}

		InputSource source = null;

		try {
			super.resolveEntity(publicId, systemId);
		} catch (Exception exception) {
			// apparently 1.5 throws an IOException here, but we can't catch it specifically if
			//	we're not on 1.5 (docs on both kind of say that they throw it)
			//	actual code on 1.4.2 has it remmed out so that it only throws SAXException
			throw new SAXException(exception);
		}

		return source;
	}

}
