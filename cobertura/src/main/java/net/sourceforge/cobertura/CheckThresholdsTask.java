package net.sourceforge.cobertura;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/*
 * Cobertura - http://cobertura.sourceforge.net/
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
public class CheckThresholdsTask {
	private static final Logger log = LoggerFactory
			.getLogger(CheckThresholdsTask.class);
	private int checkThresholdsExitStatus;

	/**
	 * Checks thresholds.
	 * @param arguments
	 * @param projectData
	 * @return
	 */
	public CheckThresholdsTask checkThresholds(Arguments arguments,
			ProjectData projectData) {
		if (arguments.getTotalBranchThreshold() > projectData
				.getBranchCoverageRate()) {
			log.error("Total branch coverage rate violation");
			checkThresholdsExitStatus = 8;
			return this;
		}
		if (arguments.getTotalLineThreshold() > projectData
				.getLineCoverageRate()) {
			log.error("Total line coverage rate violation");
			checkThresholdsExitStatus = 16;
			return this;
		}

		Iterator packages = projectData.getPackages().iterator();
		PackageData packagedata;
		while (packages.hasNext()) {
			packagedata = (PackageData) packages.next();
			if (arguments.getPackageBranchThreshold() > packagedata
					.getBranchCoverageRate()) {
				log.error("Package branch coverage rate violation");
				checkThresholdsExitStatus = 32;
				break;
			}
			if (arguments.getPackageLineThreshold() > packagedata
					.getLineCoverageRate()) {
				log.error("Package line coverage rate violation");
				checkThresholdsExitStatus = 64;
				break;
			}
			Iterator classes = packagedata.getClasses().iterator();
			ClassData classdata;
			while (classes.hasNext()) {
				classdata = (ClassData) classes.next();
				if (arguments.getClassBranchThreshold() > classdata
						.getBranchCoverageRate()) {
					log.error("Class branch coverage rate violation");
					checkThresholdsExitStatus = 2;
					break;
				}
				if (arguments.getClassLineThreshold() > classdata
						.getLineCoverageRate()) {
					log.error("Class line coverage rate violation");
					checkThresholdsExitStatus = 4;
					break;
				}
			}
		}
		return this;
	}

	/**
	 * Exit status values are:
	 * <ul>
	 *     <li>2- class branch coverage rate violation</li>
	 *     <li>4- class line coverage rate violation</li>
	 *     <li>8- total branch coverage rate violation</li>
	 *     <li>8- total line coverage rate violation</li>
	 *     <li>32- package branch coverage rate violation</li>
	 *     <li>64- package line coverage rate violation</li>
	 * </ul>
	 * @return
	 */
	public int getCheckThresholdsExitStatus() {
		return checkThresholdsExitStatus;
	}
}
