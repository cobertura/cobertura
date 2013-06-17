/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 Mark Doliner
 * Copyright (C) 2006 John Lewis
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

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class JUnitXMLHelper {

	/**
	 * This reads the given file into an XML Document.
	 *
	 * @param file     A valid file on the file system.
	 * @param validate Whether to validate the XML or not.
	 *
	 * @return An XML document representing the given XML file.
	 *
	 * @throws FileNotFoundException If the file does not exist.
	 * @throws IOException           If the file could not be open/read.
	 * @throws JDOMException         If the file is not well-formed XML, or
	 *                               if validation is enabled and the document is not
	 *                               valid.
	 */
	public static Document readXmlFile(File file, boolean validate)
			throws FileNotFoundException, IOException, JDOMException {
		System.out.println("Reading " + file.getAbsolutePath());

		// First create an XML document parser
		SAXBuilder saxBuilder = new SAXBuilder();
		saxBuilder.setValidation(validate);
		saxBuilder.setEntityResolver(new JUnitXMLParserEntityResolver(new File(
				"src/test/resources/dtds")));
		saxBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());
		return saxBuilder.build(file);
	}

}
