/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * This file was taken from JavaNCSS
 * http://www.kclee.com/clemens/java/javancss/
 * Copyright (C) 2000 Chr. Clemens Lee <clemens a.t kclee d.o.t com>
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

/*
 *
 * WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING  
 *
 * WARNING TO COBERTURA DEVELOPERS
 *
 * DO NOT MODIFY THIS FILE!
 *
 * MODIFY THE FILES UNDER THE JAVANCSS DIRECTORY LOCATED AT THE ROOT OF THE COBERTURA PROJECT.
 *
 * FOLLOW THE PROCEDURE FOR MERGING THE LATEST JAVANCSS INTO COBERTURA LOCATED AT
 * javancss/coberturaREADME.txt
 *
 * WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   
 */
package net.sourceforge.cobertura.javancss.parser;

import java.util.List;
import java.util.Map;

public interface JavaParserInterface {
	public void parse() throws Exception;
	public void parseImportUnit() throws Exception;

	public int getNcss();

	public int getLOC();

	// added by SMS
	public int getJvdc();

	/*public int getTopLevelClasses() {
	  return _topLevelClasses;
	  }*/

	public List/*<FunctionMetric>*/getFunction();

	/**
	 * @return Top level classes in sorted order
	 */
	public List/*<ObjectMetric>*/getObject();

	/**
	 * @return The empty package consists of the name ".".
	 */
	public Map/*<String,PackageMetric>*/getPackage();

	public List getImports();

	/**
	 * name, beginLine, ...
	 */
	public Object[] getPackageObjects();

	/**
	 * if javancss is used with cat *.java a long
	 * input stream might get generated, so line
	 * number information in case of an parse exception
	 * is not very useful.
	 */
	public String getLastFunction();
}
