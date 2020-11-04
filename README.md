# COBERTURA

[![Build Status](https://travis-ci.org/msiemczyk/cobertura.svg?branch=master)](https://travis-ci.org/msiemczyk/cobertura)

## ABOUT
Cobertura is a free Java code coverage reporting tool.  It is
based on jcoverage 1.0.5.  See the [Cobertura web page](https://cobertura.sourceforge.net/)
and [wiki](https://github.com/cobertura/cobertura/wiki) for more details.

Since 2.0.0, Cobertura versions follow the [Semantic versioning](https://semver.org/) guidelines.

## DOWNLOAD
You can download Cobertura from [The Central Repository](https://repo1.maven.org/maven2/net/sourceforge/cobertura/cobertura/).
That includes the distributions for stand-alone command line usage.

To use it as Maven dependency, you can use the following snippet:

	<dependency>
	    <groupId>net.sourceforge.cobertura</groupId>
	    <artifactId>cobertura</artifactId>
	    <version>2.1.1</version>
	    <scope>test</scope>
	</dependency>


## COPYRIGHT
Code in the net.sourceforge.cobertura.javancss package is
Copyright (C) 2000 Chr. Clemens Lee   <clemens a.t kclee d.o.t com>

See the included file "LICENSE.txt"

## LICENSE
Cobertura is free software.  Most of it is licensed under the GNU
GPL, and you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option)
any later version.  Please review the file LICENSE.txt included in this
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

## MAVEN MIGRATION NOTES
* *How do we get a good blame while all files were moved?*

Use "git blame --follow" nameOfFile.java

Cobertura is built on Travis-CI.
