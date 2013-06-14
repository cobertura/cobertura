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

package net.sourceforge.cobertura.reporting.html;

import net.sourceforge.cobertura.coveragedata.SourceFileData;

import java.io.Serializable;
import java.util.Comparator;

public class SourceFileDataBaseNameComparator
		implements
			Comparator,
			Serializable {

	private static final long serialVersionUID = 0L;

	public int compare(Object arg0, Object arg1) {
		SourceFileData sourceFileData0 = (SourceFileData) arg0;
		SourceFileData sourceFileData1 = (SourceFileData) arg1;
		int comparison = sourceFileData0.getBaseName().compareTo(
				sourceFileData1.getBaseName());
		if (comparison != 0)
			return comparison;
		return sourceFileData0.getName().compareTo(sourceFileData1.getName());
	}

}
