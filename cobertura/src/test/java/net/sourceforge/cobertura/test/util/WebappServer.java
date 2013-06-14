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

package net.sourceforge.cobertura.test.util;

import groovy.lang.Closure;
import groovy.util.AntBuilder;
import net.sourceforge.cobertura.ant.InstrumentTask;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class WebappServer {
	public static final String SIMPLE_SERVLET_CLASSNAME = "com.acme.servlet.SimpleServlet";

	private static final String JETTY_DIR = "src/test/resources/jetty";

	public static final String SIMPLE_SERVLET_TEXT = "\n package com.acme.servlet;"
			+ "\n "
			+ "\n import javax.servlet.http.HttpServlet;"
			+ "\n import javax.servlet.http.HttpServletRequest;"
			+ "\n import javax.servlet.http.HttpServletResponse;"
			+ "\n "
			+ "\n import java.io.PrintWriter;"
			+ "\n import java.io.IOException;"
			+ "\n "
			+ "\n public class SimpleServlet extends HttpServlet"
			+ "\n {"
			+ "\n  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException"
			+ "\n  {"
			+ "\n   response.setContentType(\"text/html;charset=UTF-8\");"
			+ "\n   PrintWriter out = response.getWriter();"
			+ "\n   try"
			+ "\n   {"
			+ "\n    out.println(\"Hi\");"
			+ "\n   }"
			+ "\n   finally"
			+ "\n   {"
			+ "\n    if (out != null)"
			+ "\n    {"
			+ "\n     out.close();" + "\n    }" + "\n   }" + "\n  }" + "\n }";

	public static final String SIMPLE_SERVLET_WEB_XML_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "\n <web-app version=\"2.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">"
			+ "\n  <servlet>"
			+ "\n   <servlet-name>SimpleServlet</servlet-name>"
			+ "\n   <servlet-class>com.acme.servlet.SimpleServlet</servlet-class>"
			+ "\n  </servlet>"
			+ "\n  <servlet-mapping>"
			+ "\n   <servlet-name>SimpleServlet</servlet-name>"
			+ "\n   <url-pattern>/SimpleServlet</url-pattern>"
			+ "\n  </servlet-mapping>" + "\n </web-app>";

	private static final AntBuilder ant = TestUtils
			.getCoberturaAntBuilder(TestUtils.getTempDir());
	private static final String LOCALHOST = "127.0.0.1";
	private static final String WEB_XML = "src/main/java/net/sourceforge/cobertura/webapp/web.xml";
	private static final String SRC_DIR = "src/main/java";

	File dir = new File(TestUtils.getTempDir(), "webserver");
	int msecNeededToStop = 10000;
	boolean modifyMainCoberturaDataFile;
	boolean tomcat;

	public WebappServer(File webappServerDir, boolean tomcat) {
		this.tomcat = tomcat;
	}

	/**
	 * Copies a web server installation into dir and deploys a webapp to it.
	 * @throws Exception 
	 * 
	 */
	public void deployApp(Map map) throws Exception {
		modifyMainCoberturaDataFile = (map
				.containsKey("modifyMainCoberturaDataFile")) ? (Boolean) map
				.get("modifyMainCoberturaDataFile") : false;

		File extractedDir = new File(dir, "extracted");
		File webInfDir = new File(extractedDir, "WEB-INF");
		File classesDir = new File(webInfDir, "classes");

		File webInfFile = writeWebInfFile(webInfDir, (String) map
				.get("webInfText"));

		compileSourceFiles((File) map.get("srcDir"), classesDir);

		copyJettyFiles(dir);

		File war = makeWarFile((String) map.get("appName"), webInfFile,
				classesDir);

		Delete delete = new Delete();
		delete.setProject(TestUtils.project);
		delete.setDir(extractedDir);
		delete.execute();

		File coberturaJar = createCoberturaJar();

		AntBuilder coberturaAnt = TestUtils.getCoberturaAntBuilder(TestUtils
				.getCoberturaClassDir());

		if (map.get("instrumentRegEx") != null) {
			instrumentWar(war, (String) map.get("instrumentRegEx"));
		}

		if (map.get("instrumentCobertura") != null) {
			instrumentCoberturaJar(coberturaJar);
		}

		if (map.get("deployCoberturaFlush") != null) {
			deployCoberturaFlush((map.containsKey("instrumentCobertura"))
					? (Boolean) map.get("instrumentCobertura")
					: false);
		}
	}

	private File writeWebInfFile(File webInfDir, String text)
			throws IOException {
		webInfDir.mkdirs();
		File webInfFile = new File(webInfDir, "web.xml");

		FileUtils.writeStringToFile(webInfFile, text);
		return webInfFile;
	}

	private void compileSourceFiles(File srcDir, File classesDir) {
		Mkdir mkdir = new Mkdir();
		mkdir.setProject(TestUtils.project);
		mkdir.setDir(classesDir);
		mkdir.execute();

		Javac javac = new Javac();
		javac.setProject(TestUtils.project);
		javac.setSrcdir(new Path(TestUtils.project, srcDir.getAbsolutePath()));
		javac.setDestdir(classesDir);
		javac.setDebug(true);
		javac.setTarget("1.5");

		Path classpath = new Path(TestUtils.project);
		FileSet fileSet = new FileSet();
		fileSet.setDir(new File(JETTY_DIR));
		fileSet.setIncludes("**/*.jar");
		classpath.addFileset(fileSet);

		javac.setClasspath(classpath);
		javac.execute();
	}

	private void copyJettyFiles(File todir) {
		Copy copy = new Copy();
		copy.setProject(TestUtils.project);
		copy.setTodir(todir);
		FileSet fileSet = new FileSet();
		fileSet.setProject(TestUtils.project);
		fileSet.setDir(new File(JETTY_DIR));
		copy.addFileset(fileSet);
		copy.execute();
	}

	private File makeWarFile(String appName, File webInfFile, File classesDir) {
		File webappsDir = new File(dir, "webapps");
		File war = new File(webappsDir, appName + ".war");

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(webInfFile);
		ZipFileSet zipFileSet = new ZipFileSet();
		zipFileSet.setDir(classesDir);
		antWar.addClasses(zipFileSet);
		antWar.execute();

		return war;
	}

	private void deployCoberturaFlush(boolean instrumentCobertura)
			throws Exception {
		File webappsDir = new File(dir, "webapps");
		File war = new File(webappsDir, "coberturaFlush.war");

		File classesDir = new File("target/build/warClasses");
		classesDir.mkdirs();
		Javac javac = new Javac();
		javac.setProject(TestUtils.project);
		javac.setSrcdir(new Path(TestUtils.project, SRC_DIR));
		javac.setDestdir(classesDir);
		javac.setDebug(true);

		Path classpath = new Path(TestUtils.project);
		//		FileSet libFileSet = new FileSet();
		FileSet jettyFileSet = new FileSet();
		//		libFileSet.setDir(new File("lib"));
		//		libFileSet.setIncludes("**/*.jar");
		jettyFileSet.setDir(new File(JETTY_DIR));
		jettyFileSet.setIncludes("**/*.jar");
		//		classpath.addFileset(libFileSet);
		classpath.addFileset(jettyFileSet);

		javac.setIncludes("**/FlushCoberturaServlet.java");

		javac.setClasspath(classpath);
		javac.execute();

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(new File(WEB_XML));

		ZipFileSet classesFileSet = new ZipFileSet();
		classesFileSet.setDir(classesDir);

		antWar.addClasses(classesFileSet);
		antWar.execute();

		if (instrumentCobertura) {
			instrumentWar(war, "net.sourceforge.cobertura.*");
		}
	}

	private void instrumentWar(File war, String instrumentRegEx) {
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile(dir.getAbsolutePath() + "/cobertura.ser");
		instrumentTask.createIncludeClasses().setRegex(instrumentRegEx);
		instrumentTask.createExcludeClasses().setRegex(".*Test.*");

		FileSet fileSet = new FileSet();
		fileSet.setDir(war.getParentFile());
		fileSet.setIncludes("**/*.war");

		instrumentTask.addFileset(fileSet);
		instrumentTask.execute();
	}

	private void instrumentCoberturaJar(File coberturaJar) {
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile(dir.getAbsolutePath() + "/cobertura.ser");
		instrumentTask.createIncludeClasses().setRegex(
				"net.sourceforge.cobertura.*");
		instrumentTask.createExcludeClasses().setRegex(".*Test.*");

		FileSet fileSet = new FileSet();
		fileSet.setDir(new File(coberturaJar.getParent()));
		fileSet.setIncludes(coberturaJar.getName());

		instrumentTask.addFileset(fileSet);
	}

	/**
	 * Starts the web server and calls the closure.   The web server will be stopped 
	 * when the closure exits (successfully or not).
	 * @throws Exception 
	 * 
	 */
	Map<String, Integer> freePorts = null;

	public Map withRunningServer(Closure closure) throws Exception {
		freePorts = findFreePorts();
		System.out.println("Stop: " + freePorts.get("stop"));
		System.out.println("Webapp: " + freePorts.get("webapp"));
		new File(dir, "logs").mkdirs();

		startWebServer(freePorts);
		Map data = new HashMap();
		try {
			data.put("xmlReport", new File(dir.getAbsolutePath()
					+ "/coverage.xml"));
			data.put("hostname", LOCALHOST);
			data.put("webappPort", freePorts.get("webapp"));
			data.put("coberturaAnt", TestUtils.getCoberturaAntBuilder(TestUtils
					.getTempDir()));

			data.put("datafile", getDatafileToUse());

			/*
			 * add a closure that can be called to flush the cobertura data
			 */

			closure.call(data);
		} finally {
		}

		return data;
	}

	public void killServer() {
		stopWebServer("" + freePorts.get("stop"));
	}

	private File getDatafileToUse() {
		File datafile = new File(dir.getAbsolutePath() + "/cobertura.ser");

		if (modifyMainCoberturaDataFile) {
			/*
			 * modify the cobertura.ser file created by the instrumentation target
			 * of the main build.xml file.   That way, the counts
			 * will end up in the overall coverage report
			 */
			File tempfile = new File("target/build/cobertura.ser");
			if (tempfile.exists()) {
				datafile = tempfile;
			} else {
				/*
				 * It looks like the main build.xml's instrument target has not been run.
				 * So, a coverage report is not being generated.
				 */
			}
		}
		return datafile;
	}

	/**
	 * Find two port numbers that can be used to start a webapp server (one port for
	 * http connections; one port for a stop command).
	 * @throws IOException 
	 */
	private Map findFreePorts() throws IOException {
		Map freePorts = new HashMap<String, Integer>() {
			{
				put("webapp", null);
				put("stop", null);
			}
		};

		ServerSocket webappSocket = null;
		ServerSocket stopSocket = null;

		try {
			webappSocket = new ServerSocket(0, 1, null);
			stopSocket = new ServerSocket(0, 1, null);

			freePorts.put("webapp", webappSocket.getLocalPort());

			freePorts.put("stop", stopSocket.getLocalPort());
		} finally {
			closeSocket(webappSocket);
			closeSocket(stopSocket);
		}
		return freePorts;
	}

	private void closeSocket(ServerSocket socket) {
		try {
			if (socket != null || socket.isClosed()) {
				socket.close();
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	private void startWebServer(final Map<String, Integer> freePorts) {
		Echo echo = new Echo();
		echo.setProject(TestUtils.project);
		echo.setMessage("Starting Jetty webapp server on "
				+ freePorts.get("webapp"));
		echo.execute();

		Thread t = new Thread() {
			@Override
			public void run() {
				Java java = new Java();
				java.setProject(TestUtils.project);
				java.setDir(dir);
				java.setJar(new File(dir, "start.jar"));
				java.setFork(true);

				Variable jettyPort = new Variable();
				jettyPort.setKey("jetty.port");
				jettyPort.setValue(freePorts.get("webapp").toString());
				java.addSysproperty(jettyPort);

				Variable stopPort = new Variable();
				stopPort.setKey("STOP.PORT");
				stopPort.setValue(freePorts.get("stop").toString());
				java.addSysproperty(stopPort);

				Variable stopKey = new Variable();
				stopKey.setKey("STOP.KEY");
				stopKey.setValue("cobertura");
				java.addSysproperty(stopKey);

				if (tomcat) {
					Variable catalinaHome = new Variable();
					catalinaHome.setKey("catalina.home");
					catalinaHome.setValue(dir.getAbsolutePath());
					java.addSysproperty(catalinaHome);
				}

				if (modifyMainCoberturaDataFile) {
					Variable dataFile = new Variable();
					dataFile.setKey("net.sourceforge.cobertura.datafile");
					dataFile.setValue(getDatafileToUse().getAbsolutePath());
					java.addSysproperty(dataFile);
				}

				java.execute();
			}
		};
		t.start();

		try {
			Thread.sleep(10 * 1000); // Sleep for 10 seconds. The servlet needs a few seconds to boot up.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void stopWebServer(final String stopPort) {
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setJar(new File("/tmp/cobertura/webserver/start.jar"));
		java.setDir(dir);
		java.setFork(true);
		java.setArgs("--stop");
		Variable stopPortVariable = new Variable();
		Variable stopKeyVariable = new Variable();

		stopPortVariable.setKey("STOP.PORT");
		stopPortVariable.setValue(stopPort);

		stopKeyVariable.setKey("STOP.KEY");
		stopKeyVariable.setValue("cobertura");

		java.addSysproperty(stopPortVariable);
		java.addSysproperty(stopKeyVariable);

		java.execute();
	}

	private File createCoberturaJar() {
		File coberturaJar = new File(dir, "lib/cobertura.jar");
		File coberturaClassDir = TestUtils.getCoberturaClassDir();

		Zip zip = new Zip();
		zip.setProject(TestUtils.project);
		zip.setDestFile(coberturaJar);

		FileSet fileSet = new FileSet();
		fileSet.setDir(coberturaClassDir);

		zip.addFileset(fileSet);
		zip.execute();

		return coberturaJar;
	}

	public static void writeSimpleServletSource(File srcDir) throws IOException {
		File servletSourceFile = new File(srcDir,
				"com/acme/servlet/SimpleServlet.java");
		servletSourceFile.getParentFile().mkdirs();

		FileUtils.write(servletSourceFile, SIMPLE_SERVLET_TEXT);
	}
}