/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Nathan Wilson
 * Copyright (C) 2005 Alex Ruiz
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package net.sourceforge.cobertura.ant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.BuildException;

/**
 * An ant task that can be used to optionally fail an ant build if
 * the coverage percentage for lines or branches is below a certain,
 * user specifiable threshold.
 */
public class CheckTask extends CommonMatchingTask
{

	private String dataFile = null;

	final Set<Regex> regexes = new HashSet<Regex>();

	private String branchRate = null;

	private String lineRate = null;

	private String packageBranchRate = null;

	private String packageLineRate = null;

	private String totalBranchRate = null;

	private String totalLineRate = null;

	private String failureProperty = null;

	private boolean haltOnFailure = true;

	public CheckTask() {
		super("net.sourceforge.cobertura.check.Main");
	}
	
	public void execute() throws BuildException
	{
		if (dataFile != null) {
			getJava().createArg().setValue("--datafile");
			getJava().createArg().setValue(dataFile);
		}

		if (branchRate != null)	{
			getJava().createArg().setValue("--branch");
			getJava().createArg().setValue(branchRate);
		}

		if (lineRate != null) {
			getJava().createArg().setValue("--line");
			getJava().createArg().setValue(lineRate);
		}

		if (packageBranchRate != null) {
			getJava().createArg().setValue("--packagebranch");
			getJava().createArg().setValue(packageBranchRate);
		}

		if (packageLineRate != null) {
			getJava().createArg().setValue("--packageline");
			getJava().createArg().setValue(packageLineRate);
		}

		if (totalBranchRate != null) {
			getJava().createArg().setValue("--totalbranch");
			getJava().createArg().setValue(totalBranchRate);
		}

		if (totalLineRate != null) {
			getJava().createArg().setValue("--totalline");
			getJava().createArg().setValue(totalLineRate);
		}

		Iterator<Regex> iter = regexes.iterator();
		while (iter.hasNext()) {
			getJava().createArg().setValue("--regex");
			getJava().createArg().setValue(iter.next().toString());
		}

		AntUtil.transferCoberturaDataFileProperty(getJava());
		int returnCode = getJava().executeJava();

		// Check the return code and print a message
		if (returnCode == 0) {
			System.out.println("All checks passed.");
		} else {
			if (haltOnFailure)
				throw new BuildException(
						"Coverage check failed. See messages above.");
			else if (failureProperty != null)
				getProject().setProperty(failureProperty, "true");
			else
				System.err
						.println("Coverage check failed. See messages above.");
		}
	}

	public Regex createRegex()
	{
		Regex regex = new Regex();
		regexes.add(regex);
		return regex;
	}

	public void setDataFile(String dataFile)
	{
		this.dataFile = dataFile;
	}

	public void setBranchRate(String branchRate)
	{
		this.branchRate = branchRate;
	}

	public void setLineRate(String lineRate)
	{
		this.lineRate = lineRate;
	}

	public void setPackageBranchRate(String packageBranchRate)
	{
		this.packageBranchRate = packageBranchRate;
	}

	public void setPackageLineRate(String packageLineRate)
	{
		this.packageLineRate = packageLineRate;
	}

	public void setTotalBranchRate(String totalBranchRate)
	{
		this.totalBranchRate = totalBranchRate;
	}

	public void setTotalLineRate(String totalLineRate)
	{
		this.totalLineRate = totalLineRate;
	}

	public void setFailureProperty(String failureProperty)
	{
		this.failureProperty = failureProperty;
	}

	public void setHaltOnFailure(boolean haltOnFailure)
	{
		this.haltOnFailure = haltOnFailure;
	}

}
