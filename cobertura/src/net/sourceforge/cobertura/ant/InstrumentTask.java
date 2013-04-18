/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2005 Alexei Yudichev
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Jiri Mares 
 * Copyright (C) 2008 Scott Frederick
 * Copyright (C) 2010 Tad Smith
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.cobertura.util.CommandLineBuilder;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


public class InstrumentTask extends CommonMatchingTask
{

	private String dataFile = null;

	private File toDir = null;

	final List<Ignore> ignoreRegexs = new ArrayList<Ignore>();

	final List<IgnoreBranches> ignoreBranchesRegexs = new ArrayList<IgnoreBranches>();
	
	final List<IgnoreMethodAnnotation> ignoreMethodAnnotations = new ArrayList<IgnoreMethodAnnotation>();

	final List<IncludeClasses> includeClassesRegexs = new ArrayList<IncludeClasses>();

	final List<ExcludeClasses> excludeClassesRegexs = new ArrayList<ExcludeClasses>();

	boolean ignoreTrivial = false;

	private Integer forkedJVMDebugPort;
	
	private Path instrumentationClasspath = null;
	
	boolean threadsafeRigorous = false;

	final private HashMap<String, FileSet> fileSetMap = new HashMap<String, FileSet>();

	public InstrumentTask() {
		super(net.sourceforge.cobertura.instrument.Main.class.getCanonicalName());
	}

	public Ignore createIgnore() {
		Ignore ignoreRegex = new Ignore();
		ignoreRegexs.add(ignoreRegex);
		return ignoreRegex;
	}

	public IgnoreBranches createIgnoreBranches() {
		IgnoreBranches ignoreBranchesRegex = new IgnoreBranches();
		ignoreBranchesRegexs.add(ignoreBranchesRegex);
		return ignoreBranchesRegex;
	}

	
	public IgnoreMethodAnnotation createIgnoreMethodAnnotation() {
		IgnoreMethodAnnotation ignoreAnnotation = new IgnoreMethodAnnotation();
		ignoreMethodAnnotations.add(ignoreAnnotation);
		return ignoreAnnotation;
	}
	
	
	public IncludeClasses createIncludeClasses() {
		IncludeClasses includeClassesRegex = new IncludeClasses();
		includeClassesRegexs.add(includeClassesRegex);
		return includeClassesRegex;
	}

	public ExcludeClasses createExcludeClasses() {
		ExcludeClasses excludeClassesRegex = new ExcludeClasses();
		excludeClassesRegexs.add(excludeClassesRegex);
		return excludeClassesRegex;
	}

	public Path createInstrumentationClasspath() {
		if (instrumentationClasspath == null) {
			instrumentationClasspath = new Path(getProject());
		}
		return instrumentationClasspath.createPath();
	}

	/*
	 * TODO: Is the following method needed to use a classpath ref?  If so,
	 *       test it and uncomment it.
	 */
	/*
	public void setInstrumentationClasspathRef(Reference r)
	{
		createInstrumentationClasspath().setRefid(r);
	}
	*/

	public void execute() throws BuildException {
		CommandLineBuilder builder = null;
		try {
			builder = new CommandLineBuilder();
			if (dataFile != null)
				builder.addArg("--datafile", dataFile);
			if (toDir != null)
				builder.addArg("--destination", toDir.getAbsolutePath());

			for (int i = 0; i < ignoreRegexs.size(); i++) {
				Ignore ignoreRegex = (Ignore)ignoreRegexs.get(i);
				builder.addArg("--ignore", ignoreRegex.getRegex());
			}

			for (int i = 0; i < ignoreBranchesRegexs.size(); i++) {
				IgnoreBranches ignoreBranchesRegex = (IgnoreBranches)ignoreBranchesRegexs.get(i);
				builder.addArg("--ignoreBranches", ignoreBranchesRegex.getRegex());
			}

			for (int i = 0; i < ignoreMethodAnnotations.size(); i++) {
				IgnoreMethodAnnotation ignoreMethodAnn = (IgnoreMethodAnnotation)ignoreMethodAnnotations.get(i);
				builder.addArg("--ignoreMethodAnnotation", ignoreMethodAnn.getAnnotationName());
			}
			
			for (int i = 0; i < includeClassesRegexs.size(); i++) {
				IncludeClasses includeClassesRegex = (IncludeClasses)includeClassesRegexs.get(i);
				builder.addArg("--includeClasses", includeClassesRegex.getRegex());
			}

			for (int i = 0; i < excludeClassesRegexs.size(); i++) {
				ExcludeClasses excludeClassesRegex = (ExcludeClasses)excludeClassesRegexs.get(i);
				builder.addArg("--excludeClasses", excludeClassesRegex.getRegex());
			}

			if (ignoreTrivial)
				builder.addArg("--ignoreTrivial");
			
			if (threadsafeRigorous)
				builder.addArg("--threadsafeRigorous");
			
			if (failOnError)
				builder.addArg("--failOnError");

			if (instrumentationClasspath != null) {
				processInstrumentationClasspath();
			}
			createArgumentsForFilesets(builder);

			builder.saveArgs();
		} catch (IOException ioe) {
			getProject().log("Error creating commands file.", Project.MSG_ERR);
			throw new BuildException("Unable to create the commands file.", ioe);
		}

		// Execute GPL licensed code in separate virtual machine
		getJava().createArg().setValue("--commandsfile");
		getJava().createArg().setValue(builder.getCommandLineFile());
		if (forkedJVMDebugPort != null && forkedJVMDebugPort.intValue() > 0) {
			getJava().createJvmarg().setValue("-Xdebug");
			getJava().createJvmarg().setValue("-Xrunjdwp:transport=dt_socket,address=" + forkedJVMDebugPort + ",server=y,suspend=y");
		}
		AntUtil.transferCoberturaDataFileProperty(getJava());
		if (getJava().executeJava() != 0) {
			throw new BuildException(
					"Error instrumenting classes. See messages above.");
		}

		builder.dispose();
	}

	private void processInstrumentationClasspath() {
		if (includeClassesRegexs.size() == 0)
		{
			throw new BuildException("'includeClasses' is required when 'instrumentationClasspath' is used");
		}

		String[] sources = instrumentationClasspath.list();
		for (int i = 0; i < sources.length; i++) {
			File fileOrDir = new File(sources[i]);
			if (fileOrDir.exists())	{
				if (fileOrDir.isDirectory()) {
					createFilesetForDirectory(fileOrDir);
				} else {
					addFileToFilesets(fileOrDir);
				}
			}
		}
	}

	private void addFileToFilesets(File file) {
		File dir = file.getParentFile();
		String filename = file.getName();
		FileSet fileSet = getFileSet(dir);
		fileSet.createInclude().setName(filename);
	}

	private FileSet getFileSet(File dir) {
		String key = dir.getAbsolutePath();
		FileSet fileSet = (FileSet)fileSetMap.get(key);
		if (fileSet == null) {
	        fileSet = new FileSet();
	        fileSet.setProject(getProject());
	        fileSet.setDir(dir);

	        // Now add the new fileset to the map and to the fileSets list 
	        fileSetMap.put(key, fileSet);
	        addFileset(fileSet);
		}
		return fileSet;
	}

	private void createFilesetForDirectory(File dir) {
		FileSet fileSet = getFileSet(dir);
		fileSet.createInclude().setName("**/*.class");
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public void setToDir(File toDir) {
		this.toDir = toDir;
	}

	public void setIgnoreTrivial(boolean ignoreTrivial) {
		this.ignoreTrivial = ignoreTrivial;
	}
	
	public void setThreadsafeRigorous(boolean threadsafeRigorous) {
		this.threadsafeRigorous = threadsafeRigorous;
	}
	
	public void setForkedJVMDebugPort(Integer forkedJVMDebugPort) {
		this.forkedJVMDebugPort = forkedJVMDebugPort;
	}
}
