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

package net.sourceforge.cobertura.javancss;

import net.sourceforge.cobertura.javancss.ccl.Exitable;
import net.sourceforge.cobertura.javancss.ccl.Init;
import net.sourceforge.cobertura.javancss.ccl.Util;
import net.sourceforge.cobertura.javancss.parser.*;
import net.sourceforge.cobertura.javancss.parser.debug.JavaParserDebug;
import net.sourceforge.cobertura.javancss.parser.java15.JavaParser15;
import net.sourceforge.cobertura.javancss.parser.java15.debug.JavaParser15Debug;

import java.io.*;
import java.util.*;

/**
 * While the Java parser class might be the heart of JavaNCSS,
 * this class is the brain. This class controls input and output and
 * invokes the Java parser.
 *
 * @author    Chr. Clemens Lee <clemens@kclee.com>
 *            , recursive feature by Pääkö Hannu
 *            , additional javadoc metrics by Emilio Gongora <emilio@sms.nl>
 *            , and Guillermo Rodriguez <guille@sms.nl>.
 * @version   $Id: Javancss.java 198 2009-09-07 21:43:19Z hboutemy $
 */
public class Javancss implements Exitable {
	private static final String S_INIT__FILE_CONTENT = "[Init]\n"
			+ "Author=Chr. Clemens Lee\n"
			+ "\n"
			+ "[Help]\n"
			+ "; Please do not edit the Help section\n"
			+ "HelpUsage=@srcfiles.txt | *.java | <stdin>\n"
			+ "Options=ncss,package,object,function,all,gui,xml,out,recursive,check,encoding,parser15\n"
			+ "ncss=b,o,Counts the program NCSS (default).\n"
			+ "package=b,o,Assembles a statistic on package level.\n"
			+ "object=b,o,Counts the object NCSS.\n"
			+ "function=b,o,Counts the function NCSS.\n"
			+ "all=b,o,The same as '-function -object -package'.\n"
			+ "gui=b,o,Opens a gui to present the '-all' output in tabbed panels.\n"
			+ "xml=b,o,Output in xml format.\n"
			+ "out=s,o,Output file name. By default output goes to standard out.\n"
			+ "recursive=b,o,Recurse to subdirs.\n"
			+ "check=b,o,Triggers a javancss self test.\n"
			+ "encoding=s,o,Encoding used while reading source files (default: platform encoding).\n"
			+ "parser15=b,o,Use new experimental Java 1.5 parser.\n" + "\n"
			+ "[Colors]\n" + "UseSystemColors=true\n";

	private boolean _bExit = false;

	private List/*<File>*/_vJavaSourceFiles = null;
	private String encoding = null;

	private String _sErrorMessage = null;
	private Throwable _thrwError = null;

	private JavaParserInterface _pJavaParser = null;
	private int _ncss = 0;
	private int _loc = 0;
	private List/*<FunctionMetric>*/_vFunctionMetrics = new ArrayList();
	private List/*<ObjectMetric>*/_vObjectMetrics = new ArrayList();
	private List/*<PackageMetric>*/_vPackageMetrics = null;
	private List _vImports = null;
	private Map/*<String,PackageMetric>*/_htPackages = null;
	private Object[] _aoPackage = null;

	/**
	 * Just used for parseImports.
	 */
	private File _sJavaSourceFile = null;

	private Reader createSourceReader(File sSourceFile_) {
		try {
			return newReader(sSourceFile_);
		} catch (IOException pIOException) {
			if (Util.isEmpty(_sErrorMessage)) {
				_sErrorMessage = "";
			} else {
				_sErrorMessage += "\n";
			}
			_sErrorMessage += "File not found: "
					+ sSourceFile_.getAbsolutePath();
			_thrwError = pIOException;

			return null;
		}
	}

	private void _measureSource(File sSourceFile_) throws IOException,
			Exception, Error {
		Reader reader = null;

		// opens the file
		try {
			reader = newReader(sSourceFile_);
		} catch (IOException pIOException) {
			if (Util.isEmpty(_sErrorMessage)) {
				_sErrorMessage = "";
			} else {
				_sErrorMessage += "\n";
			}
			_sErrorMessage += "File not found: "
					+ sSourceFile_.getAbsolutePath();
			_thrwError = pIOException;

			throw pIOException;
		}

		String sTempErrorMessage = _sErrorMessage;
		try {
			// the same method but with a Reader
			_measureSource(reader);
		} catch (Exception pParseException) {
			if (sTempErrorMessage == null) {
				sTempErrorMessage = "";
			}
			sTempErrorMessage += "ParseException in "
					+ sSourceFile_.getAbsolutePath()
					+ "\nLast useful checkpoint: \""
					+ _pJavaParser.getLastFunction() + "\"\n";
			sTempErrorMessage += pParseException.getMessage() + "\n";

			_sErrorMessage = sTempErrorMessage;
			_thrwError = pParseException;

			throw pParseException;
		} catch (Error pTokenMgrError) {
			if (sTempErrorMessage == null) {
				sTempErrorMessage = "";
			}
			sTempErrorMessage += "TokenMgrError in "
					+ sSourceFile_.getAbsolutePath() + "\n"
					+ pTokenMgrError.getMessage() + "\n";
			_sErrorMessage = sTempErrorMessage;
			_thrwError = pTokenMgrError;

			throw pTokenMgrError;
		}
	}

	private void _measureSource(Reader reader) throws IOException, Exception,
			Error {
		Util.debug("_measureSource(Reader).ENTER");
		// Util.debug( "_measureSource(Reader).parser15: -->" + (_pInit.getOptions().get( "parser15" ) + "<--" );
		// Util.panicIf( _pInit == null );
		// Util.panicIf( _pInit.getOptions() == null );
		Util.debug("_measureSource(Reader).ENTER2");
		try {
			// create a parser object
			boolean parser15 = _pInit != null && _pInit.getOptions() != null
					&& _pInit.getOptions().get("parser15") != null;
			if (Util.isDebug()) {
				if (parser15) {
					Util.debug("creating JavaParser15Debug");
					_pJavaParser = new JavaParser15Debug(reader);
				} else {
					Util.debug("creating JavaParserDebug");
					_pJavaParser = new JavaParserDebug(reader);
				}
			} else {
				if (parser15) {
					Util.debug("creating JavaParser15");
					_pJavaParser = new JavaParser15(reader);
				} else {
					Util.debug("creating JavaParser");
					_pJavaParser = new JavaParser(reader);
				}
			}

			// execute the parser
			_pJavaParser.parse();
			Util
					.debug("Javancss._measureSource(DataInputStream).SUCCESSFULLY_PARSED");

			_ncss += _pJavaParser.getNcss(); // increment the ncss
			_loc += _pJavaParser.getLOC(); // and loc
			// add new data to global vector
			_vFunctionMetrics.addAll(_pJavaParser.getFunction());
			_vObjectMetrics.addAll(_pJavaParser.getObject());
			Map htNewPackages = _pJavaParser.getPackage();

			/* List vNewPackages = new Vector(); */
			for (Iterator ePackages = htNewPackages.entrySet().iterator(); ePackages
					.hasNext();) {
				String sPackage = (String) ((Map.Entry) ePackages.next())
						.getKey();

				PackageMetric pckmNext = (PackageMetric) htNewPackages
						.get(sPackage);
				pckmNext.name = sPackage;

				PackageMetric pckmPrevious = (PackageMetric) _htPackages
						.get(sPackage);
				pckmNext.add(pckmPrevious);

				_htPackages.put(sPackage, pckmNext);
			}
		} catch (Exception pParseException) {
			if (_sErrorMessage == null) {
				_sErrorMessage = "";
			}
			_sErrorMessage += "ParseException in STDIN";
			if (_pJavaParser != null) {
				_sErrorMessage += "\nLast useful checkpoint: \""
						+ _pJavaParser.getLastFunction() + "\"\n";
			}
			_sErrorMessage += pParseException.getMessage() + "\n";
			_thrwError = pParseException;

			throw pParseException;
		} catch (Error pTokenMgrError) {
			if (_sErrorMessage == null) {
				_sErrorMessage = "";
			}
			_sErrorMessage += "TokenMgrError in STDIN\n";
			_sErrorMessage += pTokenMgrError.getMessage() + "\n";
			_thrwError = pTokenMgrError;

			throw pTokenMgrError;
		}
	}

	private void _measureFiles(List/*<File>*/vJavaSourceFiles_)
			throws IOException, ParseException, TokenMgrError {
		// for each file
		for (Iterator e = vJavaSourceFiles_.iterator(); e.hasNext();) {
			File file = (File) e.next();

			try {
				_measureSource(file);
			} catch (Throwable pThrowable) {
				// hmm, do nothing? Use getLastError() or so to check for details.
			}
		}
	}

	/**
	 * If arguments were provided, they are used, otherwise
	 * the input stream is used.
	 */
	private void _measureRoot(Reader reader) throws IOException, Exception,
			Error {
		_htPackages = new HashMap();

		// either there are argument files, or stdin is used
		if (_vJavaSourceFiles == null) {
			_measureSource(reader);
		} else {
			// the collection of files get measured
			_measureFiles(_vJavaSourceFiles);
		}

		_vPackageMetrics = new ArrayList();
		for (Iterator ePackages = _htPackages.keySet().iterator(); ePackages
				.hasNext();) {
			String sPackage = (String) ePackages.next();

			PackageMetric pckmNext = (PackageMetric) _htPackages.get(sPackage);
			_vPackageMetrics.add(pckmNext);
		}
	}

	public List getImports() {
		return _vImports;
	}

	/**
	 * Return info about package statement.
	 * First element has name of package,
	 * then begin of line, etc.
	 */
	public Object[] getPackage() {
		return _aoPackage;
	}

	/**
	 * The same as getFunctionMetrics?!
	 */
	public List/*<FunctionMetric>*/getFunctions() {
		return _vFunctionMetrics;
	}

	public Javancss(List/*<File>*/vJavaSourceFiles_) {
		_vJavaSourceFiles = vJavaSourceFiles_;
		try {
			_measureRoot(newReader(System.in));
		} catch (Exception e) {
			e.printStackTrace();
		} catch (TokenMgrError pError) {
			pError.printStackTrace();
		}
	}

	public Javancss(File sJavaSourceFile_) {
		Util.debug("Javancss.<init>(String).sJavaSourceFile_: "
				+ sJavaSourceFile_);
		_sErrorMessage = null;
		_vJavaSourceFiles = new ArrayList();
		_vJavaSourceFiles.add(sJavaSourceFile_);
		try {
			_measureRoot(newReader(System.in));
		} catch (Exception e) {
			Util.debug("Javancss.<init>(String).e: " + e);
			e.printStackTrace();
		} catch (TokenMgrError pError) {
			Util.debug("Javancss.<init>(String).pError: " + pError);
			pError.printStackTrace();
		}
	}

	/*
	 * cobertura:  add this next constructor so any input stream can be used.
	 * 
	 * It should (more or less) be a copy of the Javancss(File) constructor, but just
	 * make sure _vJavaSourceFiles is null.   _measureRoot will
	 * use the input stream if it is null.
	 */
	public Javancss(InputStream isJavaSource_) {
		Util.debug("Javancss.<init>(InputStream).sJavaSourceFile_: "
				+ isJavaSource_);
		_sErrorMessage = null;
		_vJavaSourceFiles = null;

		try {
			_measureRoot(newReader(isJavaSource_));
		} catch (Exception e) {
			Util.debug("Javancss.<init>(InputStream).e: " + e);
			e.printStackTrace();
		} catch (TokenMgrError pError) {
			Util.debug("Javancss.<init>(InputStream).pError: " + pError);
			pError.printStackTrace();
		}
	}

	/**
	 * Only way to create object that does not immediately
	 * start to parse.
	 */
	public Javancss() {
		super();

		_sErrorMessage = null;
		_thrwError = null;
	}

	public boolean parseImports() {
		if (_sJavaSourceFile == null) {
			Util.debug("Javancss.parseImports().NO_FILE");

			return true;
		}
		Reader reader = createSourceReader(_sJavaSourceFile);
		if (reader == null) {
			Util.debug("Javancss.parseImports().NO_DIS");

			return true;
		}

		try {
			Util.debug("Javancss.parseImports().START_PARSING");
			if (Util.isDebug() == false) {
				_pJavaParser = (JavaParserInterface) (new JavaParser(reader));
			} else {
				_pJavaParser = (JavaParserInterface) (new JavaParserDebug(
						reader));
			}
			_pJavaParser.parseImportUnit();
			_vImports = _pJavaParser.getImports();
			_aoPackage = _pJavaParser.getPackageObjects();
			Util.debug("Javancss.parseImports().END_PARSING");
		} catch (Exception pParseException) {
			Util.debug("Javancss.parseImports().PARSE_EXCEPTION");
			if (_sErrorMessage == null) {
				_sErrorMessage = "";
			}
			_sErrorMessage += "ParseException in STDIN";
			if (_pJavaParser != null) {
				_sErrorMessage += "\nLast useful checkpoint: \""
						+ _pJavaParser.getLastFunction() + "\"\n";
			}
			_sErrorMessage += pParseException.getMessage() + "\n";
			_thrwError = pParseException;

			return true;
		} catch (Error pTokenMgrError) {
			Util.debug("Javancss.parseImports().TOKEN_ERROR");
			if (_sErrorMessage == null) {
				_sErrorMessage = "";
			}
			_sErrorMessage += "TokenMgrError in STDIN\n";
			_sErrorMessage += pTokenMgrError.getMessage() + "\n";
			_thrwError = pTokenMgrError;

			return true;
		}

		return false;
	}

	public void setSourceFile(File javaSourceFile_) {
		_sJavaSourceFile = javaSourceFile_;
		_vJavaSourceFiles = new ArrayList();
		_vJavaSourceFiles.add(javaSourceFile_);
	}
	private Init _pInit = null;
	public int getNcss() {
		return _ncss;
	}

	public int getLOC() {
		return _loc;
	}

	// added by SMS
	public int getJvdc() {
		return _pJavaParser.getJvdc();
	}

	/**
	 * JDCL stands for javadoc comment lines (while jvdc stands
	 * for number of javadoc comments).
	 */
	public int getJdcl() {
		return JavaParserTokenManager._iFormalComments;
	}

	public int getSl() {
		return JavaParserTokenManager._iSingleComments;
	}

	public int getMl() {
		return JavaParserTokenManager._iMultiComments;
	}
	//

	public List getFunctionMetrics() {
		return (_vFunctionMetrics);
	}

	public List/*<ObjectMetric>*/getObjectMetrics() {
		return (_vObjectMetrics);
	}

	/**
	 * Returns list of packages in the form
	 * PackageMetric objects.
	 */
	public List getPackageMetrics() {
		return (_vPackageMetrics);
	}

	public String getLastErrorMessage() {
		if (_sErrorMessage == null) {
			return null;
		}
		return _sErrorMessage;
	}

	public Throwable getLastError() {
		return _thrwError;
	}

	public void setExit() {
		_bExit = true;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private Reader newReader(InputStream stream)
			throws UnsupportedEncodingException {
		return (encoding == null)
				? new InputStreamReader(stream)
				: new InputStreamReader(stream, encoding);
	}

	private Reader newReader(File file) throws FileNotFoundException,
			UnsupportedEncodingException {
		return newReader(new FileInputStream(file));
	}
}
