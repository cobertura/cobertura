/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Grzegorz Lukasik
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

import net.sourceforge.cobertura.util.CommandLineBuilder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.IOException;

/**
 * Generate a coverage report based on coverage data generated
 * by instrumented classes.
 */
public class ReportTask extends CommonMatchingTask {

	private String dataFile = null;
	private String format = "html";
	private File destDir;
	private String srcDir;
	private String encoding;

	public ReportTask() {
		super("net.sourceforge.cobertura.reporting.Main");
	}

	public void execute() throws BuildException {
		CommandLineBuilder builder = null;
		try {
			builder = new CommandLineBuilder();
			if (dataFile != null)
				builder.addArg("--datafile", dataFile);
			if (destDir != null)
				builder.addArg("--destination", destDir.getAbsolutePath());
			if (format != null)
				builder.addArg("--format", format);
			if (encoding != null)
				builder.addArg("--encoding", encoding);
			if (srcDir != null)
				builder.addArg(srcDir);
			createArgumentsForFilesets(builder);

			builder.saveArgs();
		} catch (IOException ioe) {
			getProject().log("Error creating commands file.", Project.MSG_ERR);
			throw new BuildException("Unable to create the commands file.", ioe);
		}

		// Execute GPL licensed code in separate virtual machine
		getJava().createArg().setValue("--commandsfile");
		getJava().createArg().setValue(builder.getCommandLineFile());
		AntUtil.transferCoberturaDataFileProperty(getJava());
		if (getJava().executeJava() != 0) {
			throw new BuildException(
					"Error running reports. See messages above.");
		}

		builder.dispose();
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public void setDestDir(File destDir) {
		this.destDir = destDir;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setSrcDir(String dir) {
		srcDir = dir;
	}
}
