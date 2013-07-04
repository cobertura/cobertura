# COBERTURA
[![Build Status](https://travis-ci.org/cobertura/cobertura.png)](https://travis-ci.org/cobertura/cobertura)

## ABOUT
Cobertura is a free Java code coverage reporting tool.  It is
based on jcoverage 1.0.5.  See the [Cobertura web page](http://cobertura.sourceforge.net/) for more
details.

Since 2.0.0, Cobertura versions follow the [Semantic versioning](http://semver.org/) guidelines.

## COPYRIGHT
See the included file "LICENSE.txt"

## LICENSE
Cobertura is free software.  Most of it is licensed under the GNU
GPL, and you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option)
any later version.  Please review the file COPYING included in this
distribution for further details.
Parts of Cobertura are licensed under the Apache Software License,
Version 1.1.

## WARRANTY
Cobertura is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

## CONVENTIONS
Before committing
* check all tests pass
* build the project, so that all code gets uniformly indented. A Maven plugin ensures this.

## CONTRIBUTORS
List of all contributors to Cobertura listed alphabetically by last name
* Copyright (C) 2005 Björn Beskow      <bbeskow a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2008 Matt Cordes        <mcordes a.t visa d.o.t com>
* Copyright (C) 2005 Erik Dick          <erdick a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Mark Doliner       <thekingant a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Joakim Erdfelt     <joakim a.t erdfelt d.o.t net>
* Copyright (C) 2008 Scott Frederick    <scottyfred a.t gmail d.o.t com>
* Copyright (C) 2008 Julian Gamble      <juliangamble a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2006 Dan Godfrey        <dgodfrey99 a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2008 Tri Bao Ho         <hotribao a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2006 Naoki Iwami        <naoki_iwami a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2003 jcoverage ltd.
* Copyright (C) 2009 John Lewis         <lewijw a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Grzegorz Lukasik   <hauserx a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2006 Jiri Mares         <jirimares a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2009 Amit Nithianandan  <ANithian a.t gmail d.o.t com>
* Copyright (C) 2005 Olivier Parent     <olivier-parent a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2009 Ed Randall         <ed_randall a.t yahoo d.o.t com>
* Copyright (C) 2005 Alex Ruiz          <alruiz15 a.t users d.o.t yahoo d.o.t com>
* Copyright (C) 2005 James Seigel       <cgul a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Mark Sinke         <marksinke a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2010 Tad Smith          <tsmith a.t lw-lmco d.o.t com>
* Copyright (C) 2009 Charlie Squires    <rockonword a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2010 Piotr Tabor        <piotr.tabor a.t gmail d.o.t com>
* Copyright (C) 2005 Jeremy Thomerson   <jthomerson a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2009 Chris van Es       <cvanes a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2006 Srivathsan Varadarajan <vatsanv a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Nathan Wilson      <ndciwilson a.t users d.o.t sourceforge d.o.t net>
* Copyright (C) 2005 Alexei Yudichev    <sflexus a.t users d.o.t sourceforge d.o.t net>

## MAVEN MIGRATION NOTES
* *How do we get a good blame while all files were moved?*

Use "git blame --follow" nameOfFile.java


## CHANGELOG
Code in the net.sourceforge.cobertura.javancss package is
Copyright (C) 2000 Chr. Clemens Lee   <clemens a.t kclee d.o.t com>

### version 2.0.1:
	* Fix a problem that occurs if class version lower than 49.

### version 2.0.0:
	* New --ignoreTrivial switch that tells Cobertura to ignore the 
	  following in the coverage report: Getter methods that simply 
	  read a class field; Setter methods that set a class field;
	  Constructors that only set class fields and call a super 
	  class constructor.
	  (Patch 3010530 from 1576631) (Scott Frederick/Tad Smith)
	* New --ignoreMethodAnnotation switch used to specify an annotation that, 
	  when present on a method, will cause Cobertura to ignore the 
	  method in the coverage report.
	  (Patch 3010530) (Tad Smith)

### version 1.9.4.1:
	* Fix a problem that occurs in Tomcat.   When TouchCollector 
	  initializes, it calls ProjectData.initialize() which (with 
	  Tomcat only) eventually calls TouchCollector.   However, 
	  TouchCollector's static members have not been initialized.   
	  Added a test that highlights this problem.   (Thanks to Jack Cobb
	  for suggesting the fix).
	* Runs on Java 5.  (Fix for Bug 2962599).

### version 1.9.4:
	* Cobertura is now up to 10x faster. Aggregates changes in
	  temporary TouchCollector class (thread-safe, but lock-free) and after that
	  applies it on real model (ProjectData) as a batch operation. (Piotr Tabor)
	* Fixed "Some packages are included more then once" bug (#2875576) that
	  caused some counts on the HTML report to be incorrect.  (Charlie Squires)
	* Fixed "Inner classes not counted in coverage report total" bug (#2943320)
	  (Charlie Squires)

### version 1.9.3:
	* Update to the latest Javancss (32.53) to fix some complexity calculation
	  problems.  Bug #2824425. (John Lewis)
	* Non-Java source files (like Groovy) no longer show the JavaNCSS warnings
	  during cobertura-report.  Fix of bug #2819844.
	* Support the case where multiple classloaders each load the Cobertura 
	  classes.  (Ed Randall)
	* Fixed bug added with 1.9.2 where a NullPointerException is thrown
	  if ProjectData.saveGlobalProjectData() is called before any instrumented
	  code is executed.

### version 1.9.2:
	* Cobertura is now thread safe.
	* Fix for FileLocker exception when writing coverage data 
	  (java.lang.IllegalStateException: Shutdown in progress) that started
	  to appear with Java 6 update 14. (Chris van Es)
	* Fix for bug "Unix scripts behave oddly due to DOS format - ID: 2788621"

### version 1.9.1.1:
	* Just a copy of 1.9.1 with a corrected Maven POM file (cobertura-runtime.pom).
	  A new version has to be created to get it uploaded to the central Maven repo.

### version 1.9.1:
	* Complexity calculation now works with Java 5 language
	  features such as Annotations. (Tri Bao Ho)
	* Removed the bold font from the source-view for uncovered lines to
	  improve the alignment.  (Jiri Mares)
	* Support Ant <dirset>s. (Matt Cordes, John Lewis)
	* Support the antlib mechanism for defining and importing 
	  ant tasks. (Richard Atkins)
	* Reports now support source encoded in something than UTF-8. (Jiri Mares)
	* Report generation performance improvement. (Ignat Zapolsky)
	* Report generation will look in zip and jar files if the 
	  source java file is not found. (Charlie Squires, John Lewis)
	* cobertura-check with linerate=0, branchrate=0, 
	  packagebranchrate=0, packagelinerate=0, totalbranchrate=0,
	  totallinerate=0 will no longer default all the values to
	  50 as before.   Therefore, cobertura-check will always pass.
	  Note that this still means that <cobertura-check /> (with
	  no attributes) will still default to 50 for all rates. (Charlie
	  Squires) (Bug 2152919)
	* New coberturaFlush.war is created.   Deploy it to a 
	  web server and invoke http://<HOST>:<PORT>/coberturaFlush/flushCobertura
	  for a convenient way of flushing the cobertura data to the
	  datafile without stopping the web server.  (Amit Nithianandan)
	* XML report now shows total lines-covered, lines-valid, 
	  branches-covered, branches-valid, and complexity.  (Julian Gamble)
	* New report option called "summaryXml" will create
	  a small summary XML report that does not have all the
	  details on the classes - just the overall totals.  This
	  is for large projects where the full XML report gets so
	  big it impairs continuous build processes.  (Julian Gamble and Dan Godfrey)
	* Migrated from asm-2.2.1 to asm-3.0. (Jiri Mares)
	* The percentage coverage of 199 out of 200 lines has been 100%. 
	  No more! Now it is 99%.  (Jiri Mares)
	* Spelling error corrected in main.css file - changed magin to
	  margin. (Dennis Lundberg)

### version 1.9:
	* Much improved branch coverage.  Information on whether
	  the true as well as the false of an if statement is
	  collected.  Also, information on the branches of a
	  switch statement (including the default) is collected.
	  (Jiri Mares)
	* Assume Java source files are saved as UTF-8 instead of
	  the computer's default encoding.
	* Write all HTML and XML reports in UTF-8 instead of the
	  computer's default encoding (Naoki Iwami).
	* Fix a bug where the Cobertura ant tasks would not work
	  correctly in Microsoft Windows when Cobertura was
	  installed on a different drive than the drive from which
	  you're running ant (Srivathsan Varadarajan).
	* Added a "maxmemory" attribute to the instrument, merge
	  and report ant tasks (Matt Cordes).
	* Improve support for Maven and similar environments where
	  control over system properties is difficult such as
	  app servers, IoC containers, IDEs, etc.  Setting the
	  datafile location is difficult in these environments.
	  To correct this, a cobertura.properties file 
	  located in the classpath is used to properly set the 
	  net.sourceforge.cobertura.datafile property.  
	  (Joakim Erdfelt)

### version 1.8 (2006-04-10)
	* Ability to have multiple <ignore/> regular expressions
	  in the instrument task (Alexei Yudichev).
	* Ability to specify a minimum branch coverage rate and
	  line coverage rate for each package when using
	  cobertura-check.
	* Show the number of lines and branches covered and the
	  total number of lines and branches in the HTML report.
	* Support for instrumenting classes written in Groovy.
	* Lock the data file before trying to write to it.  This
	  allows multiple JVMs (or multiple class loaders within
	  a single JVM) to write to the same coverage data file
	  with no problems (John Lewis).
	* Ability to instrument classes on a given classpath
	  instead of specifying filesets (John Lewis).
	* Ability to specify which classes will be instrumented
	  using regular expressions (John Lewis).
	* Archives within archives will be instrumented if you
	  specify an includeClassname regular expression (John
	  Lewis).
	* If instrumenting an archive, remove any signatures
	  and checksums, since they will no longer be valid (John
	  Lewis).
	* Removed the Class-Path line from cobertura.jar.  You may
	  need to modify your Cobertura taskdef to include the jars
	  in Cobertura's 'lib' directory.  See our Ant task web
	  page for an example.
	* Reorganized libs into a flatter directory structure--you
	  may need to update your ant scripts.
	* Upgraded from asm 2.1 to asm 2.2.1.  No code changes were
	  needed.
	* Copied portions of classes from JavaNCSS into Cobertura
	  so that we don't need to include the entire JavaNCSS and
	  CCL jars.

### version 1.7 (2005-12-06)
	* log4j is no longer used by the Cobertura classes that are
	  accessed by instrumented Java code.  This means you will
	  not need to add log4j to your project's classpath in order
	  to use Cobertura (but log4j is still required when
	  instrumenting and reporting).
	* Upgraded from asm 2.0 to asm 2.1.  No code changes were
	  needed.
	* Improved the merge task.  It should work correctly now
	  (with help from  Björn Beskow).
	* Fixed the ability to specify a data file in the merge task.
	* Changed the command-line interface to the merge task and
	  added a helper batch/shell script.
	* Added better error checking to the merge task.
	* Fixed a bug where an empty or incomplete coverage data
	  file would be written when you test classes inside Tomcat,
	  and you stop Tomcat using the shutdown.bat or shutdown.sh
	  scripts.  This would result in an EOFException when running
	  cobertura-report.
	* Added support for classes compiled with AspectJ.
	* Cobertura now produces valid XHTML 1.0 reports.

### version 1.6 (2005-08-22)
	* Can now use multiple filesets in the cobertura-instrument
	  task (Thanks to Grzegorz Lukasik).
	* Can now use multiple filesets in the cobertura-report task
	  (Thanks to Jeremy Thomerson, Grzegorz Lukasik and James Seigel).
	* No longer using the Java version of GNU GetOpt
	* Fixed a bug where the total number of classes displayed in
	  the HTML report included anonymous classes when it should
	  not have.

### version 1.5 (2005-08-05)
	* Shortened the header shown when running Cobertura (Thanks
	  to Jarkko Viinamäki).
	* Don't save the data file twice after instrumenting.
	* Print a warning when running cobertura-report with a
	  data file that does not contain information from the
	  instrument step.
	* When instrumenting, you can now specify a zip, jar, war,
	  ear or sar file and Cobertura will instrument any classes
	  inside of the archive.  You must explicity give the name
	  of the archive when instrumenting--giving the name of the
	  directory containing the archive will not work (Thanks to
	  Grzegorz Lukasik).
	* Fixed a bug where the class list in the HTML reports did
	  not show multiple classes with the same name, but in
	  different packages.
	* Add a timestamp and version number to all HTML reports.
	* Add a timestamp and version number to all XML reports.
	* Add the combined line-rate and branch-rate for all
	  packages to all XML reports.
	* Fixed the merge task (Thanks to Mark Sinke).
	* The check task now supports checking against a project's
	  total branch and line coverage rates (Thanks to Nathan
	  Wilson).
	* The check ant task now allows you to fail the ant build,
	  if desired (Thanks to Nathan Wilson).
	* The check task can set an ant property to "true" on
	  failure (Thanks to Alex Ruiz).
	* Changed some of the parameters for the check task.  See
	  the online documentation for usage information.
	* The command line Windows batch scripts work better.

### version 1.4 (2005-05-30)
	* Fixed a bug that sometimes resulted in a
	  StringIndexOutOfBoundsException when running cobertura-report
	  (Thanks to Grzegorz Lukasik).
	* Fixed a bug where classes without coverage data ("N/A") were
	  not always sorted correctly in the HTML report (Thanks to
	  Olivier Parent).
	* Fixed a bug where the code complexity column would not always
	  sort correctly in locales that use a comma to split the decimal
	  part of the number (Thanks to Olivier Parent).
	* Show "N/A" in the branch column of the HTML report for classes
	  and packages that do not have any branches.

### version 1.3 (2005-05-20)
	* Increased speed of HTML reports by filtering the files read
	  in to determine cyclomatic complexity numbers on.
	* In the lower left pane of the HTML reports, classes are now
	  sorted only by their class name (instead of by their package
	  name plus class name).
	* Changed the format of the XML reports to something that
	  is hopefully easier to use and more natural.  This
	  unfortunately breaks backward compatability.
	* We're using a DTD for the XML reports now.  See
	  http://cobertura.sourceforge.net/xml/coverage-01.dtd
	* Added the ability to specify the location of the coverage
	  data files from the ant tasks and the command line.
	* More user-friendly error checking and reporting.

### version 1.2 (2005-03-16)
	* Fix a bug that caused the XML reports to be invalid XML
	  (they were missing the </package> tag).
	* Use Java 1.4 pattern matching and remove Jakarta ORO.

### version 1.1 (2005-03-08)
	* Fix a bug in the syntax highlighting code of the HTML report
	  generation.  Previously, the highlighting for single quotes
	  containing "\\" would not end correctly.
	* Check the third party jars into CVS using the correct CVS
	  substition flag (binary, not ASCII).
	* Temporary files created by the instrument ant task and merge
	  ant task are now deleted after the ant task finishes.
	* Switch the instrumentation classes to use ASM instead of
	  Apache BCEL.  There are three benefits to this:
	  1. BCEL was throwing exceptions with some source code compiled
	     with JDK 1.5--ASM works fine.
	  2. ASM is licensed under the revised BSD license, which
	     is compatable with the GPL, which allows us to remove
	     the questionable exception for BCEL.
	  3. Instrumentation is about 5 times faster with ASM than BCEL.
	* Modify the HTML reports so that classes without line number
	  information will appear as "Not Applicable."  This includes
	  skeleton classes, stub classes, interfaces, or anything not
	  compiled with debug=true.
	* Fix bug #1151777 with a patch from Jeremy Thomerson.
	  Previously we were not escaping some characters correctly in the
	  generated XML coverage report (specificially < and >).
	* Set the class-path in the cobertura.jar manifest file correctly.
	* Fill feature request #1151779 with a patch from Jeremy Thomerson.
	  This changes the structure of the XML report so that <classes> are
	  enclosed inside <packages>.

### version 1.0 (2005-02-12)
	* Forked jcoverage 1.0.5 (although the version in the source says 1.0.4).
	  All original code is copyright 2003 jcoverage ltd.  Kurt Guenther
	  highlighted a bug in the branch coverage, was was fixed.
	* Applied a patch from Joakim Erdfelt to fix a bug where jcoverage
	  would fail to instrument classes if you attempted to instrument a
	  very large number of classes (in the hundreds).
	* Rewrote the HTML reporting and included code complexity in the output.

## RELATED PROJECTS
Here we list projects that spread the use of Cobertura by integrating it to other tools.

### Cobertura Maven Plugin
Adds Maven goals to run Cobertura. Documentation and details can be found [here](http://mojo.codehaus.org/cobertura-maven-plugin/)
### Cobertura Jenkins Plugin
Runs Cobertura on Jenkins CI jobs. Check it [here](https://github.com/jenkinsci/cobertura-plugin)
### eCobertura - Eclipse Plugin for Cobertura
Allows to launch applications or tests in Cobertura covered mode within Eclipse. Displays coverage data on Eclipse IDE code. Check [the website](http://ecobertura.johoop.de/)
### Gradle Cobertura plugins
Gradle Cobertura plugins are listed on Gradle's [wiki](http://wiki.gradle.org/display/GRADLE/Plugins#Plugins-CoberturaPlugin).
