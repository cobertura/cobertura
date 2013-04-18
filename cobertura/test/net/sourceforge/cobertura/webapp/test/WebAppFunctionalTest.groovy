/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2009 John Lewis
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

package net.sourceforge.cobertura.webapp.test

import net.sourceforge.cobertura.test.util.TestUtil
import net.sourceforge.cobertura.test.util.WebappServer

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class WebAppFunctionalTest {
	
	/*
	 * For the next two constants, it would be preferable to use saveGlobalProjectData
	 * in class net.sourceforge.cobertura.coveragedata.ProjectData, but ProjectData is
	 * not instrumented since it is annotated with CoberturaIgnore.   So, the best we can
	 * do is make sure the doGet method in FlushCoberturaServlet is called.
	 */
	private static final SAVE_DATA_CLASSNAME = "net.sourceforge.cobertura.webapp.FlushCoberturaServlet"
	private static final SAVE_DATA_METHOD_NAME = "doGet"
	private static final boolean TOMCAT = true
		
	def ant = TestUtil.antBuilder
	
	
	@Before
	void setUp() {
	}
	
	@Test
	void basicStartAndStopOfWebApp() {
		basicStartAndStopOfWebApp(!TOMCAT)
	}

	@Test
	void basicStartAndStopOfWebAppInTomcat() {
		basicStartAndStopOfWebApp(TOMCAT)
	}	

	void basicStartAndStopOfWebApp(tomcat) {
		TestUtil.withTempDir { tempDir ->
			def webappServerDir = new File(tempDir, "webserver")
			def srcDir = new File(tempDir, "src")
			
			WebappServer.writeSimpleServletSource(srcDir)
			
			def appName = "simple"

			def webappServer = new WebappServer(dir:webappServerDir, tomcat:tomcat)

			webappServer.deployApp(
					webInfText:WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT,
					srcDir:srcDir,
					appName:appName,
					instrumentRegEx:'com.acme.*')
					
			def data = webappServer.withRunningServer { data ->
			
				//do a HTTP get so the doGet method will be hit
				def webappResponse = (new java.net.URL("http://${data.hostname}:${data.webappPort}/${appName}/SimpleServlet")).text
				assertEquals("Webapp response was incorrect", "Hi", webappResponse.trim())

				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				def dom = TestUtil.getXMLReportDOM(data.xmlReport)
				
				//make sure the report shows the doGet method has not been hit yet - the data is not flushed until the server stops
				assertFalse(TestUtil.isMethodHit(dom, "doGet"))
			}
			
			//now that the server has stopped, make sure the report shows it has been hit
			assertTrue("doGet has hits=0 in cobertura report", TestUtil.isMethodHit(data.dom, "doGet"))
		}
	}

	@Test
	void flushCoberturaData() {
		TestUtil.withTempDir { tempDir ->
			def webappServerDir = new File(tempDir, "webserver")
			def srcDir = new File(tempDir, "src")
			
			WebappServer.writeSimpleServletSource(srcDir)
			
			def appName = "simple"

			def webappServer = new WebappServer(dir:webappServerDir)

			webappServer.deployApp(
					webInfText:WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT,
					srcDir:srcDir,
					appName:appName,
					instrumentCobertura:true,
					deployCoberturaFlush:true,
					modifyMainCoberturaDataFile:true)
				
		
			def data = webappServer.withRunningServer { data ->
			
				/*
				 * do a HTTP get of the simple webapp - this is just to get somewhat close to a 
				 * real world scenario
				 */
				def webappResponse = (new java.net.URL("http://${data.hostname}:${data.webappPort}/${appName}/SimpleServlet")).text
				assertEquals("Webapp response was incorrect", "Hi", webappResponse.trim())

				//Do a coverage report of the main cobertura.ser file at the root of the project
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				def dom = TestUtil.getXMLReportDOM(data.xmlReport)
				
				def hitCountBefore = TestUtil.getHitCount(dom, SAVE_DATA_CLASSNAME, SAVE_DATA_METHOD_NAME)
				
				//flush the cobertura data by doing an HTTP get
				data.flushCobertura()
				
				//run the report again
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				dom = TestUtil.getXMLReportDOM(data.xmlReport)

				def hitCountAfter = TestUtil.getHitCount(dom, SAVE_DATA_CLASSNAME, SAVE_DATA_METHOD_NAME)
				
				assertEquals("hit count should have increased by one", hitCountBefore + 1, hitCountAfter)
			}
		}
	}
	
	@Test
	void flushCoberturaData2() {
		TestUtil.withTempDir { tempDir ->
			def webappServerDir = new File(tempDir, "webserver")
			def srcDir = new File(tempDir, "src")
			
			WebappServer.writeSimpleServletSource(srcDir)
			
			def appName = "simple"

			def webappServer = new WebappServer(dir:webappServerDir)

			webappServer.deployApp(
					webInfText:WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT,
					srcDir:srcDir,
					appName:appName,
					deployCoberturaFlush:true,
					instrumentRegEx:'com.acme.*')
				
	
			def data = webappServer.withRunningServer { data ->
			
				/*
				 * do a HTTP get of the simple webapp
				 */
				def webappResponse = (new java.net.URL("http://${data.hostname}:${data.webappPort}/${appName}/SimpleServlet")).text
				assertEquals("Webapp response was incorrect", "Hi", webappResponse.trim())

				//Do a coverage report of the main cobertura.ser file at the root of the project
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				def dom = TestUtil.getXMLReportDOM(data.xmlReport)
				
				def hitCountBefore = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
				assertEquals(0, hitCountBefore)
				
				//flush the cobertura data by doing an HTTP get
				data.flushCobertura()
				
				//run the report again
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				dom = TestUtil.getXMLReportDOM(data.xmlReport)

				def hitCountAfter = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
				
				assertEquals(1, hitCountAfter)
			}
			
			//run the report again
			data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
			def dom = TestUtil.getXMLReportDOM(data.xmlReport)

			def finalCount = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
			
			assertEquals(1, finalCount)
			
		}
	}

	@Test
	void flushCoberturaDataOnly() {
		/*
		 * Test case where a flush is done before any instrumented code is executed.
		 */
		TestUtil.withTempDir { tempDir ->
			def webappServerDir = new File(tempDir, "webserver")
			def srcDir = new File(tempDir, "src")
			
			WebappServer.writeSimpleServletSource(srcDir)
			
			def appName = "simple"

			def webappServer = new WebappServer(dir:webappServerDir)

			webappServer.deployApp(
					webInfText:WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT,
					srcDir:srcDir,
					appName:appName,
					deployCoberturaFlush:true,
					instrumentRegEx:'com.acme.*')
				
	
			def data = webappServer.withRunningServer { data ->
			
				//Do a coverage report of the main cobertura.ser file at the root of the project
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				def dom = TestUtil.getXMLReportDOM(data.xmlReport)
				
				def hitCountBefore = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
				assertEquals(0, hitCountBefore)
				
				//flush the cobertura data by doing an HTTP get
				data.flushCobertura()
				
				//run the report again
				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				dom = TestUtil.getXMLReportDOM(data.xmlReport)

				def hitCountAfter = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
				
				assertEquals(0, hitCountAfter)
			}
			
			//run the report again
			data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
			def dom = TestUtil.getXMLReportDOM(data.xmlReport)

			def finalCount = TestUtil.getHitCount(dom, WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet")
			
			assertEquals(0, finalCount)
			
		}
	}
}
