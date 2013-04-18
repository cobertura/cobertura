//COBERTURA REMOVE BEGIN
/*
Copyright (C) 2000 Chr. Clemens Lee <clemens@kclee.com>.

This file is part of JavaNCSS
(http://www.kclee.com/clemens/java/javancss/).

JavaNCSS is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2, or (at your option) any
later version.

JavaNCSS is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with JavaNCSS; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.  */
//COBERTURA REMOVE END

package javancss;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ccl.util.Exitable;
import ccl.util.FileUtil;
import ccl.util.Init;
import ccl.util.Util;

import javancss.parser.JavaParser;
import javancss.parser.JavaParserInterface;
import javancss.parser.JavaParserTokenManager;
import javancss.parser.ParseException;
import javancss.parser.TokenMgrError;
import javancss.parser.debug.JavaParserDebug;
import javancss.parser.java15.JavaParser15;
import javancss.parser.java15.debug.JavaParser15Debug;
import javancss.test.JavancssTest;

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
public class Javancss implements Exitable
{
    private static final String S_INIT__FILE_CONTENT =
        "[Init]\n" +
        "Author=Chr. Clemens Lee\n" +
        "\n" +
        "[Help]\n"+
        "; Please do not edit the Help section\n"+
        "HelpUsage=@srcfiles.txt | *.java | <stdin>\n" +
        "Options=ncss,package,object,function,all,gui,xml,out,recursive,check,encoding,parser15\n" +
        "ncss=b,o,Counts the program NCSS (default).\n" +
        "package=b,o,Assembles a statistic on package level.\n" +
        "object=b,o,Counts the object NCSS.\n" +
        "function=b,o,Counts the function NCSS.\n" +
        "all=b,o,The same as '-function -object -package'.\n" +
        "gui=b,o,Opens a gui to present the '-all' output in tabbed panels.\n" +
        "xml=b,o,Output in xml format.\n" +
        "out=s,o,Output file name. By default output goes to standard out.\n"+
        "recursive=b,o,Recurse to subdirs.\n" +
        "check=b,o,Triggers a javancss self test.\n" +
        "encoding=s,o,Encoding used while reading source files (default: platform encoding).\n" +
        "parser15=b,o,Use new experimental Java 1.5 parser.\n" +
        "\n" +
        "[Colors]\n" +
        "UseSystemColors=true\n";

    private boolean _bExit = false;

    private List/*<File>*/ _vJavaSourceFiles = null;
    private String encoding = null;

    private String _sErrorMessage = null;
    private Throwable _thrwError = null;

    private JavaParserInterface _pJavaParser = null;
    private int _ncss = 0;
    private int _loc = 0;
    private List/*<FunctionMetric>*/ _vFunctionMetrics = new ArrayList();
    private List/*<ObjectMetric>*/ _vObjectMetrics = new ArrayList();
    private List/*<PackageMetric>*/ _vPackageMetrics = null;
    private List _vImports = null;
    private Map/*<String,PackageMetric>*/ _htPackages = null;
    private Object[] _aoPackage = null;

    /**
     * Just used for parseImports.
     */
    private File _sJavaSourceFile = null;

    private Reader createSourceReader( File sSourceFile_ )
    {
        try
        {
            return newReader( sSourceFile_ );
        }
        catch ( IOException pIOException )
        {
            if ( Util.isEmpty( _sErrorMessage ) )
            {
                _sErrorMessage = "";
            }
            else
            {
                _sErrorMessage += "\n";
            }
            _sErrorMessage += "File not found: " + sSourceFile_.getAbsolutePath();
            _thrwError = pIOException;

            return null;
        }
    }

    private void _measureSource( File sSourceFile_ ) throws IOException, Exception, Error
    {
        Reader reader = null;

        // opens the file
        try
        {
            reader = newReader( sSourceFile_ );
        }
        catch ( IOException pIOException )
        {
            if ( Util.isEmpty( _sErrorMessage ) )
            {
                _sErrorMessage = "";
            }
            else
            {
                _sErrorMessage += "\n";
            }
            _sErrorMessage += "File not found: " + sSourceFile_.getAbsolutePath();
            _thrwError = pIOException;

            throw pIOException;
        }

        String sTempErrorMessage = _sErrorMessage;
        try
        {
            // the same method but with a Reader
            _measureSource( reader );
        }
        catch ( Exception pParseException )
        {
            if ( sTempErrorMessage == null )
            {
                sTempErrorMessage = "";
            }
            sTempErrorMessage += "ParseException in " + sSourceFile_.getAbsolutePath() +
                   "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            sTempErrorMessage += pParseException.getMessage() + "\n";

            _sErrorMessage = sTempErrorMessage;
            _thrwError = pParseException;

            throw pParseException;
        }
        catch ( Error pTokenMgrError )
        {
            if ( sTempErrorMessage == null )
            {
                sTempErrorMessage = "";
            }
            sTempErrorMessage += "TokenMgrError in " + sSourceFile_.getAbsolutePath() +
                   "\n" + pTokenMgrError.getMessage() + "\n";
            _sErrorMessage = sTempErrorMessage;
            _thrwError = pTokenMgrError;

            throw pTokenMgrError;
        }
    }

    private void _measureSource( Reader reader ) throws IOException, Exception, Error
    {
        Util.debug( "_measureSource(Reader).ENTER" );
        // Util.debug( "_measureSource(Reader).parser15: -->" + (_pInit.getOptions().get( "parser15" ) + "<--" );
        // Util.panicIf( _pInit == null );
        // Util.panicIf( _pInit.getOptions() == null );
        Util.debug( "_measureSource(Reader).ENTER2" );
        try
        {
            // create a parser object
            boolean parser15 = _pInit != null && _pInit.getOptions() != null && _pInit.getOptions().get( "parser15" ) != null;
            if ( Util.isDebug() )
            {
                if ( parser15 )
                {
                    Util.debug( "creating JavaParser15Debug" );
                    _pJavaParser = new JavaParser15Debug( reader );
                }
                else
                {
                    Util.debug( "creating JavaParserDebug" );
                    _pJavaParser = new JavaParserDebug( reader );
                }
            }
            else
            {
                if ( parser15 )
                {
                    Util.debug( "creating JavaParser15" );
                    _pJavaParser = new JavaParser15( reader );
                }
                else
                {
                    Util.debug( "creating JavaParser" );
                    _pJavaParser = new JavaParser( reader );
                }
            }

            // execute the parser
            _pJavaParser.parse();
            Util.debug( "Javancss._measureSource(DataInputStream).SUCCESSFULLY_PARSED" );

            _ncss += _pJavaParser.getNcss(); // increment the ncss
            _loc += _pJavaParser.getLOC(); // and loc
            // add new data to global vector
            _vFunctionMetrics.addAll( _pJavaParser.getFunction() );
            _vObjectMetrics.addAll( _pJavaParser.getObject() );
            Map htNewPackages = _pJavaParser.getPackage();

            /* List vNewPackages = new Vector(); */
            for ( Iterator ePackages = htNewPackages.entrySet().iterator(); ePackages.hasNext(); )
            {
                String sPackage = (String) ( (Map.Entry) ePackages.next() ).getKey();

                PackageMetric pckmNext = (PackageMetric) htNewPackages.get( sPackage );
                pckmNext.name = sPackage;

                PackageMetric pckmPrevious = (PackageMetric) _htPackages.get( sPackage );
                pckmNext.add( pckmPrevious );

                _htPackages.put( sPackage, pckmNext );
            }
        }
        catch ( Exception pParseException )
        {
            if ( _sErrorMessage == null )
            {
                _sErrorMessage = "";
            }
            _sErrorMessage += "ParseException in STDIN";
            if ( _pJavaParser != null )
            {
                _sErrorMessage += "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            }
            _sErrorMessage += pParseException.getMessage() + "\n";
            _thrwError = pParseException;

            throw pParseException;
        }
        catch ( Error pTokenMgrError )
        {
            if ( _sErrorMessage == null )
            {
                _sErrorMessage = "";
            }
            _sErrorMessage += "TokenMgrError in STDIN\n";
            _sErrorMessage += pTokenMgrError.getMessage() + "\n";
            _thrwError = pTokenMgrError;

            throw pTokenMgrError;
        }
    }

    private void _measureFiles( List/*<File>*/ vJavaSourceFiles_ ) throws IOException, ParseException, TokenMgrError
    {
        // for each file
        for ( Iterator e = vJavaSourceFiles_.iterator(); e.hasNext(); )
        {
            File file = (File) e.next();

            try
            {
                _measureSource( file );
            }
            catch ( Throwable pThrowable )
            {
                // hmm, do nothing? Use getLastError() or so to check for details.
            }
        }
    }

    /**
     * If arguments were provided, they are used, otherwise
     * the input stream is used.
     */
    private void _measureRoot( Reader reader ) throws IOException, Exception, Error
    {
        _htPackages = new HashMap();

        // either there are argument files, or stdin is used
        if ( _vJavaSourceFiles == null )
        {
            _measureSource( reader );
        }
        else
        {
            // the collection of files get measured
            _measureFiles( _vJavaSourceFiles );
        }

        _vPackageMetrics = new ArrayList();
        for ( Iterator ePackages = _htPackages.keySet().iterator(); ePackages.hasNext(); )
        {
            String sPackage = (String) ePackages.next();

            PackageMetric pckmNext = (PackageMetric) _htPackages.get( sPackage );
            _vPackageMetrics.add( pckmNext );
        }
//COBERTURA REMOVE BEGIN
        //cobertura - no need to sort
        Collections.sort( _vPackageMetrics );
//COBERTURA REMOVE END
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
    public List/*<FunctionMetric>*/ getFunctions() {
        return _vFunctionMetrics;
    }
//COBERTURA REMOVE BEGIN
    public String printObjectNcss() {
        return getFormatter().printObjectNcss();
    }

    public String printFunctionNcss() {
        return getFormatter().printFunctionNcss();
    }

    public String printPackageNcss() {
        return getFormatter().printPackageNcss();
    }

    public String printJavaNcss() {
        return getFormatter().printJavaNcss();
    }
//COBERTURA REMOVE END

    public Javancss( List/*<File>*/ vJavaSourceFiles_ )
    {
        _vJavaSourceFiles = vJavaSourceFiles_;
        try {
            _measureRoot(newReader(System.in));
        } catch(Exception e) {
            e.printStackTrace();
        } catch(TokenMgrError pError) {
            pError.printStackTrace();
        }
    }

    public Javancss( File sJavaSourceFile_ )
    {
        Util.debug( "Javancss.<init>(String).sJavaSourceFile_: " + sJavaSourceFile_ );
        _sErrorMessage = null;
        _vJavaSourceFiles = new ArrayList();
        _vJavaSourceFiles.add(sJavaSourceFile_);
        try {
            _measureRoot(newReader(System.in));
        } catch(Exception e) {
        	Util.debug( "Javancss.<init>(String).e: " + e );
            e.printStackTrace();
        } catch(TokenMgrError pError) {
        	Util.debug( "Javancss.<init>(String).pError: " + pError );
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
        Util.debug( "Javancss.<init>(InputStream).sJavaSourceFile_: " + isJavaSource_ );
        _sErrorMessage = null;
        _vJavaSourceFiles = null;

        try {
            _measureRoot(newReader(isJavaSource_));
        } catch(Exception e) {
        	Util.debug( "Javancss.<init>(InputStream).e: " + e );
            e.printStackTrace();
        } catch(TokenMgrError pError) {
        	Util.debug( "Javancss.<init>(InputStream).pError: " + pError );
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
        if ( _sJavaSourceFile == null ) {
        	Util.debug( "Javancss.parseImports().NO_FILE" );

            return true;
        }
        Reader reader = createSourceReader( _sJavaSourceFile );
        if ( reader == null ) {
        	Util.debug( "Javancss.parseImports().NO_DIS" );

            return true;
        }

        try {
            Util.debug( "Javancss.parseImports().START_PARSING" );
            if ( Util.isDebug() == false ) {
              _pJavaParser = (JavaParserInterface)(new JavaParser(reader));
            } else {
              _pJavaParser = (JavaParserInterface)(new JavaParserDebug(reader));
            }
            _pJavaParser.parseImportUnit();
            _vImports = _pJavaParser.getImports();
            _aoPackage = _pJavaParser.getPackageObjects();
            Util.debug( "Javancss.parseImports().END_PARSING" );
        } catch(Exception pParseException) {
        	Util.debug( "Javancss.parseImports().PARSE_EXCEPTION" );
            if (_sErrorMessage == null) {
                _sErrorMessage = "";
            }
            _sErrorMessage += "ParseException in STDIN";
            if (_pJavaParser != null) {
                _sErrorMessage += "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            }
            _sErrorMessage += pParseException.getMessage() + "\n";
            _thrwError = pParseException;

            return true;
        } catch(Error pTokenMgrError) {
        	Util.debug( "Javancss.parseImports().TOKEN_ERROR" );
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

    public void setSourceFile( File javaSourceFile_ ) {
        _sJavaSourceFile = javaSourceFile_;
        _vJavaSourceFiles = new ArrayList();
        _vJavaSourceFiles.add(javaSourceFile_);
    }
//COBERTURA REMOVE BEGIN
    public Javancss(Reader reader) {
        try {
            _measureRoot(reader);
        } catch(Exception e) {
        } catch(TokenMgrError pError) {
        }
    }
//COBERTURA REMOVE END
//COBERTURA REMOVE BEGIN
    /**
     * recursively adds *.java files
     * @param dir the base directory to search
     * @param v the list of file to add found files to
     */
    private static void _addJavaFiles( File dir, List v/*<File>*/ )
    {
        File[] files = dir.listFiles();
        if( files == null || files.length == 0 )
        {
            return;
        }

        for( int i = 0; i < files.length; i++ )
        {
            File newFile = files[i];
            if( newFile.isDirectory() )
            {
                //Recurse!!!
                _addJavaFiles( newFile, v );
            }
            else
            {
                if( newFile.getName().endsWith( ".java" ) )
                {
                    v.add( newFile );
                }
            }
        }
    }
//COBERTURA REMOVE END
//COBERTURA REMOVE BEGIN
    private List/*<File>*/ findFiles( List/*<String>*/ filenames, boolean recursive ) throws IOException
    {
        if ( Util.isDebug() )
        {
            Util.debug( "filenames: " + Util.toString( filenames ) );
        }
        if ( filenames.size() == 0 )
        {
            if ( recursive )
            {
                // If no files then add current directory!
                filenames.add( "." );
            }
            else
            {
                return null;
            }
        }

        Set _processedAtFiles = new HashSet();
        List newFiles = new ArrayList();
        for ( Iterator iter = filenames.iterator(); iter.hasNext(); )
        {
            String filename = (String)iter.next();

            // if the file specifies other files...
            if ( filename.startsWith( "@" ) )
            {
                filename = filename.substring( 1 );
                if ( filename.length() > 1 )
                {
                    filename = FileUtil.normalizeFileName( filename );
                    if ( _processedAtFiles.add( filename ) )
                    {
                        String sJavaSourceFileNames = null;
                        try
                        {
                            sJavaSourceFileNames = FileUtil.readFile( filename );
                        }
                        catch( IOException pIOException )
                        {
                            _sErrorMessage = "File Read Error: " + filename;
                            _thrwError = pIOException;
                            throw pIOException;
                        }
                        List vTheseJavaSourceFiles = Util.stringToLines( sJavaSourceFileNames );
                        for ( Iterator iterator = vTheseJavaSourceFiles.iterator(); iterator.hasNext(); )
                        {
                            newFiles.add( new File( (String)iterator.next() ) );
                        }
                    }
                }
            }
            else
            {
                filename = FileUtil.normalizeFileName( filename );
                File file = new File( filename );
                if ( file.isDirectory() )
                {
                    _addJavaFiles( file, newFiles );
                }
                else
                {
                    newFiles.add( file );
                }
            }
        }

        if ( Util.isDebug() )
        {
            Util.debug( "resolved filenames: " + Util.toString( newFiles ) );
        }

        return newFiles;
    }
//COBERTURA REMOVE END
    private Init _pInit = null;
//COBERTURA REMOVE BEGIN
    /**
     * @deprecated use Javancss(String[]) instead, since the sRcsHeader_ parameter is not useful
     */
    public Javancss(String[] asArgs_, String sRcsHeader_) throws IOException {
        this(asArgs_);
    }

    /**
     * This is the constructor used in the main routine in
     * javancss.Main.
     * Other constructors might be helpful to use Javancss out
     * of other programs.
     */
    public Javancss(String[] asArgs_) throws IOException {
        _pInit = new Init(this, asArgs_, Main.S_RCS_HEADER, S_INIT__FILE_CONTENT);
        if (_bExit) {
            return;
        }
        Map htOptions = _pInit.getOptions();

        setEncoding( (String) htOptions.get( "encoding" ) );

        if ( htOptions.get( "check" ) != null ) {
            new JavancssTest().main( new File( _pInit.getApplicationPath() ) );
            return;
        }

        // the arguments (the files) to be processed
        _vJavaSourceFiles = findFiles( _pInit.getArguments(), htOptions.get( "recursive" ) != null );

        if ( htOptions.get( "gui" ) != null )
        {
            final JavancssFrame pJavancssFrame = new JavancssFrame(_pInit);
            /*final Thread pThread = Thread.currentThread();*/
            pJavancssFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e_) {
                        Util.debug("JavancssAll.run().WindowAdapter.windowClosing().1");
                        pJavancssFrame.setVisible(false);
                        pJavancssFrame.dispose();
                    }
                });
            pJavancssFrame.setVisible(true);

            try {
                _measureRoot(newReader(System.in));
            } catch(Throwable pThrowable) {
                // shouldn't we print something here?
            }

            pJavancssFrame.showJavancss(this);
            pJavancssFrame.setSelectedTab(JavancssFrame.S_PACKAGES);
            pJavancssFrame.run();

            return;
        }

        // this initiates the measurement
        try
        {
            _measureRoot( newReader( System.in ) );
        }
        catch(Throwable pThrowable)
        {
        }
        if ( getLastErrorMessage() != null )
        {
            Util.printlnErr( getLastErrorMessage() + "\n" );
            if ( getNcss() <= 0 )
            {
                return;
            }
        }

        boolean bNoNCSS = false;

        String sOutputFile = (String)htOptions.get( "out" );
        OutputStream out = System.out;
        if (sOutputFile != null)
        {
            try
            {
                out = new FileOutputStream( FileUtil.normalizeFileName( sOutputFile ) );
            } catch ( Exception exception ) {
                Util.printlnErr( "Error opening output file '"
                                 + sOutputFile
                                 + "': " + exception.getMessage() );

                out = System.out;
                sOutputFile = null;
            }
        }
        // TODO: encoding configuration support for result output
        PrintWriter pw = useXML() ? new PrintWriter(new OutputStreamWriter(out, "UTF-8")) : new PrintWriter(out);

        if ( useXML() )
        {
            pw.print( XmlFormatter.printStart() );
        }

        if (htOptions.get( "package" ) != null ||
            htOptions.get( "all" ) != null)
        {
            pw.print( printPackageNcss() );
            bNoNCSS = true;
        }
        if (htOptions.get( "object" ) != null ||
            htOptions.get( "all" ) != null)
        {
            if ( bNoNCSS )
            {
                pw.println();
            }
            pw.print( printObjectNcss() );
            bNoNCSS = true;
        }
        if (htOptions.get( "function" ) != null ||
            htOptions.get( "all" ) != null)
        {
            if ( bNoNCSS )
            {
                pw.println();
            }
            pw.print( printFunctionNcss() );
            bNoNCSS = true;
        }
        if (!bNoNCSS) {
            pw.print( printJavaNcss() );
        }

        if ( useXML() )
        {
            if ( !bNoNCSS )
            {
                pw.print( printJavaNcss() );
            }
            pw.println( "</javancss>" );
        }

        if ( sOutputFile != null )
        {
            pw.close();
        } else
        {
            // stdout is used: don't close but ensure everything is flushed
            pw.flush();
        }
    }
//COBERTURA REMOVE END
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
        return(_vFunctionMetrics);
    }

    public List/*<ObjectMetric>*/ getObjectMetrics() {
        return(_vObjectMetrics);
    }

    /**
     * Returns list of packages in the form
     * PackageMetric objects.
     */
    public List getPackageMetrics() {
        return(_vPackageMetrics);
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

//COBERTURA REMOVE BEGIN
    private boolean _bXML = false;

    public void setXML( boolean bXML )
    {
        _bXML = bXML;
    }

    public boolean useXML()
    {
        return _bXML
               || (_pInit != null && _pInit.getOptions().get( "xml" ) != null );
    }

    public Formatter getFormatter()
    {
        if ( useXML() )
        {
            return new XmlFormatter( this );
        }

        return new AsciiFormatter( this );
    }
//COBERTURA REMOVE END

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    private Reader newReader( InputStream stream ) throws UnsupportedEncodingException
    {
        return ( encoding == null ) ? new InputStreamReader( stream ) : new InputStreamReader( stream, encoding );
    }

    private Reader newReader( File file ) throws FileNotFoundException, UnsupportedEncodingException
    {
        return newReader( new FileInputStream( file ) );
    }
}
