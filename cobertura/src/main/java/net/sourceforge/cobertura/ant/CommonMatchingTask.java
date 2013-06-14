/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 Srivathsan Varadarajan
 * Copyright (C) 2008 Matt Cordes
 * Copyright (C) 2008 John Lewis
 * Copyright (C) 2010 Piotr Tabor
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
import net.sourceforge.cobertura.util.StringUtil;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

public abstract class CommonMatchingTask extends MatchingTask {

	final String className;
	final List<AbstractFileSet> fileSets = new LinkedList<AbstractFileSet>();

	private Java java = null;
	private String maxMemory = null;
	private int forkedJVMDebugPort;
	protected boolean failOnError = false;

	public CommonMatchingTask(String className) {
		this.className = className;
	}

	private String getClassName() {
		return className;
	}

	protected Java getJava() {
		if (java == null) {
			java = (Java) getProject().createTask("java");
			java.setTaskName(getTaskName());
			java.setClassname(getClassName());
			java.setFork(true);
			java.setFailonerror(failOnError);
			java.setDir(getProject().getBaseDir());
			if (maxMemory != null)
				java.setJvmargs("-Xmx" + maxMemory);
			if (forkedJVMDebugPort > 0) {
				java.setJvmargs("-Xdebug");
				java.setJvmargs("-Xrunjdwp:transport=dt_socket,address="
						+ forkedJVMDebugPort + ",server=y,suspend=y");
			}

			/**
			 * We replace %20 with a space character because, for some
			 * reason, when we call Cobertura from within CruiseControl,
			 * the classpath here contains %20's instead of spaces.  I
			 * don't know if this is our problem, or CruiseControl, or
			 * ant, but this seems to fix it.  --Mark
			 */
			if (getClass().getClassLoader() instanceof AntClassLoader) {
				String classpath = ((AntClassLoader) getClass()
						.getClassLoader()).getClasspath();
				createClasspath().setPath(
						StringUtil.replaceAll(classpath, "%20", " "));
			} else if (getClass().getClassLoader() instanceof URLClassLoader) {
				URL[] earls = ((URLClassLoader) getClass().getClassLoader())
						.getURLs();
				for (int i = 0; i < earls.length; i++) {
					String classpath = (new File(earls[i].getFile()))
							.getAbsolutePath();
					createClasspath().setPath(
							StringUtil.replaceAll(classpath, "%20", " "));
				}
			}
		}

		return java;
	}

	protected void createArgumentsForFilesets(CommandLineBuilder builder)
			throws IOException {
		boolean filesetFound = false;
		for (AbstractFileSet fileSet : fileSets) {
			if (fileSet instanceof FileSet) {
				filesetFound = true;
				builder.addArg("--basedir", baseDir(fileSet));
				createArgumentsForFilenames(builder, getFilenames(fileSet));
			} else {
				if (filesetFound) {
					/*
					 * Once --basedir has been used, it cannot be undone without changes to the
					 * Main methods.   So, any dirsets have to come before filesets.
					 */
					throw new BuildException(
							"Dirsets have to come before filesets");
				}
				createArgumentsForFilenames(builder, getDirectoryScanner(
						fileSet).getIncludedDirectories());
			}
		}
	}

	private void createArgumentsForFilenames(CommandLineBuilder builder,
			String[] filenames) throws IOException {
		for (int i = 0; i < filenames.length; i++) {
			getProject().log("Adding " + filenames[i] + " to list",
					Project.MSG_VERBOSE);
			builder.addArg(filenames[i]);
		}
	}

	public Path createClasspath() {
		return getJava().createClasspath().createPath();
	}

	public void setClasspath(Path classpath) {
		createClasspath().append(classpath);
	}

	public void setClasspathRef(Reference r) {
		createClasspath().setRefid(r);
	}

	DirectoryScanner getDirectoryScanner(AbstractFileSet fileSet) {
		return fileSet.getDirectoryScanner(getProject());
	}

	String[] getIncludedFiles(AbstractFileSet fileSet) {
		return getDirectoryScanner(fileSet).getIncludedFiles();
	}

	String[] getExcludedFiles(FileSet fileSet) {
		return getDirectoryScanner(fileSet).getExcludedFiles();
	}

	String[] getFilenames(AbstractFileSet fileSet) {
		String[] filesToReturn = getIncludedFiles(fileSet);

		return filesToReturn;
	}

	String baseDir(AbstractFileSet fileSet) {
		return fileSet.getDirectoryScanner(getProject()).getBasedir()
				.toString();
	}

	public void addDirSet(DirSet dirSet) {
		fileSets.add(dirSet);
	}

	public void addFileset(FileSet fileSet) {
		fileSets.add(fileSet);
	}

	/**
	 * @param maxMemory Assumed to be something along the lines of
	 *                  100M or 50K or 1G.
	 */
	public void setMaxMemory(String maxMemory) {
		this.maxMemory = maxMemory != null ? maxMemory.trim() : null;
	}

	/**
	 * Used to debug the process that is forked to perform the operation.
	 * Setting this to a non-zero number will cause the process to open
	 * a debug port on that port number.   It will suspend until a
	 * remote debugger is attached to the port.
	 *
	 * @param forkedJVMDebugPort
	 */
	public void setForkedJVMDebugPort(int forkedJVMDebugPort) {
		this.forkedJVMDebugPort = forkedJVMDebugPort;
	}

	/**
	 * If true, then fail if the command exits with a
	 * returncode other than zero.
	 *
	 * @param fail if true fail the build when the command exits with a
	 *             nonzero returncode.
	 */
	public void setFailonerror(boolean fail) {
		failOnError = fail;
	}

}
