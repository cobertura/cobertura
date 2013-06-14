/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Mark Sinke
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

package net.sourceforge.cobertura.merge;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

	public Main(String[] args) {
		File dataFile = CoverageDataFileHandler.getDefaultDataFile();
		File baseDir = null;
		List filesToMerge = new ArrayList();

		// Go through all the parameters
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--datafile"))
				dataFile = new File(args[++i]);
			else if (args[i].equals("--basedir"))
				baseDir = new File(args[++i]);
			else
				filesToMerge.add(new File(baseDir, args[i]));
		}

		// Load coverage data
		ProjectData projectData = null;
		if (dataFile.isFile())
			projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
		if (projectData == null)
			projectData = new ProjectData();

		if (filesToMerge.isEmpty()) {
			System.err.println("Error: No files were specified for merging.");
			System.exit(1);
		}

		// Merge everything
		Iterator iter = filesToMerge.iterator();
		while (iter.hasNext()) {
			File newDataFile = (File) iter.next();
			ProjectData projectDataNew = CoverageDataFileHandler
					.loadCoverageData(newDataFile);
			if (projectDataNew != null)
				projectData.merge(projectDataNew);
		}

		// Save the combined data file
		CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
	}

	public static void main(String[] args) {
		Header.print();

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			System.exit(1);
		}
		new Main(args);
	}
}
