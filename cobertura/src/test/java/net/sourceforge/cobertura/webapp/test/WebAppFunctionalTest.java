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

package net.sourceforge.cobertura.webapp.test;

import groovy.lang.Closure;
import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;
import net.sourceforge.cobertura.test.util.WebappServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class WebAppFunctionalTest extends AbstractCoberturaTestCase {

	private static final String SRC_DIR = "src/main/java";

	/*
	 * For the next two constants, it would be preferable to use saveGlobalProjectData
	 * in class net.sourceforge.cobertura.coveragedata.ProjectData, but ProjectData is
	 * not instrumented since it is annotated with CoberturaIgnore.   So, the best we can
	 * do is make sure the doGet method in FlushCoberturaServlet is called.
	 */
	private static final String SAVE_DATA_CLASSNAME = "net.sourceforge.cobertura.webapp.FlushCoberturaServlet";
	private static final String SAVE_DATA_METHOD_NAME = "doGet";
	private static final boolean TOMCAT = true;

	AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils.getTempDir());

	@Before
	public void setUp() throws IOException {
		FileUtils.deleteDirectory(TestUtils.getTempDir());
	}

	@Test
	public void basicStartAndStopOfWebApp() throws Exception {
		basicStartAndStopOfWebApp(!TOMCAT);
	}

	@Test
	public void basicStartAndStopOfWebAppInTomcat() throws Exception {
		basicStartAndStopOfWebApp(TOMCAT);
	}

	public void basicStartAndStopOfWebApp(boolean tomcat) throws Exception {
		File tempDir = TestUtils.getTempDir();
		File webappServerDir = new File(tempDir, "webserver");

		final File srcDir = new File(tempDir, SRC_DIR);

		WebappServer.writeSimpleServletSource(srcDir);

		final String appName = "simple";

		WebappServer webappServer = new WebappServer(webappServerDir, tomcat);

		webappServer.deployApp(new HashMap() {
			{
				put("webInfText", WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT);
				put("srcDir", srcDir);
				put("appName", appName);
				put("instrumentRegEx", "com.acme.*");
			}
		});

		Map data = webappServer.withRunningServer(new Closure(null) {
			public void doCall(HashMap values) {
			}
		});

		//do a HTTP get so the doGet method will be hit
		String webappResponse = IOUtils
				.toString(new URL("http://" + data.get("hostname") + ":"
						+ data.get("webappPort") + "/" + appName
						+ "/SimpleServlet").openConnection().getInputStream());

		assertEquals("Webapp response was incorrect", "Hi", webappResponse
				.trim());

		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		Node dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		//make sure the report shows the doGet method has not been hit yet - the data is not flushed until the server stops
		assertFalse(TestUtils.isMethodHit(dom,
				"com.acme.servlet.SimpleServlet", "doGet"));

		webappServer.killServer();
		Thread.sleep(5 * 1000);
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		//now that the server has stopped, make sure the report shows it has been hit
		assertTrue("doGet has hits=0 in cobertura report", TestUtils
				.isMethodHit(dom, "com.acme.servlet.SimpleServlet", "doGet"));
	}

	@Test
	public void flushCoberturaData() throws Exception {
		File tempDir = TestUtils.getTempDir();
		File webappServerDir = new File(tempDir, "webserver");
		final File srcDir = new File(tempDir, SRC_DIR);

		WebappServer.writeSimpleServletSource(srcDir);

		final String appName = "simple";

		WebappServer webappServer = new WebappServer(webappServerDir, false);

		webappServer.deployApp(new HashMap() {
			{
				put("webInfText", WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT);
				put("srcDir", srcDir);
				put("appName", appName);
				put("instrumentCobertura", true);
				put("deployCoberturaFlush", true);
				put("modifyMainCoberturaDataFile", true);
			}
		});

		Map data = webappServer.withRunningServer(new Closure(null) {
			public void doCall(HashMap values) {
			}
		});

		/*
		 * do a HTTP get of the simple webapp - this is just to get somewhat close to a 
		 * real world scenario
		 */
		String webappResponse = IOUtils
				.toString(new java.net.URL("http://" + data.get("hostname")
						+ ":" + data.get("webappPort") + "/" + appName
						+ "/SimpleServlet").openConnection().getInputStream());
		assertEquals("Webapp response was incorrect", "Hi", webappResponse
				.trim());

		//Do a coverage report of the main cobertura.ser file at the root of the project
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		Node dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountBefore = TestUtils.getHitCount(dom, SAVE_DATA_CLASSNAME,
				SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		//flush the cobertura data by doing an HTTP get

		String flushing = IOUtils.toString(new java.net.URL("http://"
				+ data.get("hostname") + ":" + data.get("webappPort")
				+ "/coberturaFlush/flushCobertura").openConnection()
				.getInputStream());

		assertEquals("", flushing);

		Thread.sleep(10 * 1000);

		//run the report again

		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountAfter = TestUtils.getHitCount(dom, SAVE_DATA_CLASSNAME,
				SAVE_DATA_METHOD_NAME);

		webappServer.killServer();

		assertEquals("hit count should have increased by one",
				hitCountBefore + 1, hitCountAfter);
	}

	@Test
	public void flushCoberturaData2() throws Exception {
		File tempDir = TestUtils.getTempDir();
		File webappServerDir = new File(tempDir, "webserver");
		final File srcDir = new File(tempDir, SRC_DIR);

		WebappServer.writeSimpleServletSource(srcDir);

		final String appName = "simple";

		WebappServer webappServer = new WebappServer(webappServerDir, false);
		webappServer.deployApp(new HashMap() {
			{
				put("webInfText", WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT);
				put("srcDir", srcDir);
				put("appName", appName);
				put("deployCoberturaFlush", true);
				put("instrumentRegEx", "com.acme.*");
			}
		});

		Map data = webappServer.withRunningServer(new Closure(null) {
			public void doCall(HashMap values) {
			}
		});

		/*
		 * do a HTTP get of the simple webapp
		 */
		String webappResponse = IOUtils.toString(new URL("http://"
				+ data.get("hostname") + ":" + data.get("webappPort") + "/"
				+ appName + "/SimpleServlet"));

		assertEquals("Webapp response was incorrect", "Hi", webappResponse
				.trim());

		//Do a coverage report of the main cobertura.ser file at the root of the project
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		Node dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountBefore = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		//flush the cobertura data by doing an HTTP get

		assertEquals("", IOUtils.toString(
				new java.net.URL("http://" + data.get("hostname") + ":"
						+ data.get("webappPort")
						+ "/coberturaFlush/flushCobertura").openConnection()
						.getInputStream()).trim());

		//run the report again
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountAfter = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(1, hitCountAfter);

		webappServer.killServer();
		Thread.sleep(5 * 1000); // Give server time to shutdown.

		//run the report again
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountFinal = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(1, hitCountFinal);
	}

	@Test
	public void flushCoberturaDataOnly() throws Exception {
		/*
		 * Test case where a flush is done before any instrumented code is executed.
		 */
		File tempDir = TestUtils.getTempDir();
		File webappServerDir = new File(tempDir, "webserver");
		final File srcDir = new File(tempDir, SRC_DIR);

		WebappServer.writeSimpleServletSource(srcDir);

		final String appName = "simple";

		WebappServer webappServer = new WebappServer(webappServerDir, false);

		webappServer.deployApp(new HashMap() {
			{
				put("webInfText", WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT);
				put("srcDir", srcDir);
				put("appName", appName);
				put("deployCoberturaFlush", true);
				put("instrumentRegEx", "com.acme.*");
			}
		});

		Map data = webappServer.withRunningServer(new Closure(null) {
			public void doCall(HashMap values) {
			}
		});
		//Do a coverage report of the main cobertura.ser file at the root of the project
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		Node dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountBefore = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet");
		assertEquals(0, hitCountBefore);
		System.out.println("http://" + data.get("hostname") + ":"
				+ data.get("webappPort") + "/coberturaFlush/flushCobertura");
		//flush the cobertura data by doing an HTTP get
		String flushing = IOUtils.toString(new java.net.URL("http://"
				+ data.get("hostname") + ":" + data.get("webappPort")
				+ "/coberturaFlush/flushCobertura").openConnection()
				.getInputStream());

		assertEquals("", flushing);

		Thread.sleep(10 * 1000);

		//run the report again
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int hitCountAfter = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet");

		assertEquals(0, hitCountAfter);

		//run the report again
		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(((File) data.get("datafile")).getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(((File) data.get("xmlReport"))
				.getParent()));
		reportTask.execute();

		dom = TestUtils.getXMLReportDOM(((File) data.get("xmlReport"))
				.getAbsolutePath());

		int finalCount = TestUtils.getHitCount(dom,
				WebappServer.SIMPLE_SERVLET_CLASSNAME, "doGet");

		webappServer.killServer();

		assertEquals(0, finalCount);
	}
}
