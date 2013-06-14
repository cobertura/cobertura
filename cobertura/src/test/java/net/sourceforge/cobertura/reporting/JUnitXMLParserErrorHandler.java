/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
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

package net.sourceforge.cobertura.reporting;

import junit.framework.Assert;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * This is a very simple SAX XML parser ErrorHandler.  If
 * you are parsing an XML document using a DocumentBuilder,
 * and you set the DocumentBuilder's ErrorHandler to an
 * instance of this class, and the XML document contains
 * any suspect XML, then this class will throw a JUnit
 * assertion failure.
 */
public class JUnitXMLParserErrorHandler implements ErrorHandler {

	private void createErrorMessage(SAXParseException exception) {
		Assert.fail("Line number: " + exception.getLineNumber() + " column: "
				+ exception.getColumnNumber() + "\n" + exception.toString());
	}

	public void error(SAXParseException exception) {
		createErrorMessage(exception);
	}

	public void fatalError(SAXParseException exception) {
		createErrorMessage(exception);
	}

	public void warning(SAXParseException exception) {
		createErrorMessage(exception);
	}

}