package net.sourceforge.cobertura.webapp.test;

import static org.junit.Assert.*;

import groovy.util.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import net.sourceforge.cobertura.ant.InstrumentTask;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import net.sourceforge.cobertura.webapp.FlushCoberturaServlet;

import org.apache.commons.io.FileUtils;

import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

/**
 * 
 * This test tests the functional FlushCoberturaServlet used for jetty sevlets.
 * 
 * We use the ServletTester API to remove the sensitive tests that were in
 * the previous versions of cobertura.
 * 
 * @author schristou88
 *
 */
public class WebAppFunctionalTest {
	ServletTester tester;
	HttpTester request;
	HttpTester response;
	File tempDir = TestUtils.getTempDir();
	public static final String SIMPLE_SERVLET_CLASSNAME = "com.acme.servlet.SimpleServlet";
	private static final String SAVE_DATA_METHOD_NAME = "doGet";

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(tempDir);
		FileUtils.deleteQuietly(new File("cobertura.ser"));
		FileUtils.deleteQuietly(new File(tempDir.getAbsolutePath(), "coverage.xml"));
	}
	
	@After
	public void tearDown() throws Exception {
		if (tester != null)
			tester.stop();
	}

	@Test
	public void testServletCreation() throws Exception {
		startUpServlet();
		pingServer();
	}

	@Test
	public void testCoberturaServlet() throws Exception {
		startUpServlet();
		pingCoberturaServer();
	}
	
	@Test
	public void testFlushCoberturaData() throws Exception {
		createSimpleWar();

		createCoberturaServlet();

		createCoberturaJar();

		instrumentWar();

		startUpServlet();

		pingServer();

		generateReportFile();

		Node dom = TestUtils.getXMLReportDOM(new File(
				tempDir.getAbsolutePath(), "coverage.xml"));

		int hitCountBefore = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		pingCoberturaServer();

		generateReportFile();

		dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(),
				"coverage.xml"));

		int hitCountAfter = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		tester.stop();
		
		assertEquals("Hit count should have increased by one",
				hitCountBefore + 1, hitCountAfter);
	}
	
	@Test
	public void testFlushCoberturaData2() throws Exception {
		createSimpleWar();
		
		createCoberturaServlet();
		
		createCoberturaJar();
		
		instrumentWar();
		
		startUpServlet();
		
		pingServer();
		
		generateReportFile();
		
		Node dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int hitCountBefore = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(0, hitCountBefore);
		
		pingCoberturaServer();
		
		generateReportFile();
		
		dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int hitCountAfter = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(2, hitCountAfter);
		
		tester.stop();
		
		generateReportFile();
		
		dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int hitCountFinal = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(hitCountAfter, hitCountFinal);
	}
	
	@Test
	public void testFlushCoberturaDataOnly() throws Exception {
		createSimpleWar();
		
		createCoberturaServlet();
		
		createCoberturaJar();
		
		instrumentWar();
		
		startUpServlet();
				
		generateReportFile();
		
		Node dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int hitCountBefore = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(0, hitCountBefore);
		
		generateReportFile();
		
		dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int hitCountAfter = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(0, hitCountAfter);
		
		tester.stop();
		
		generateReportFile();
		
		dom = TestUtils.getXMLReportDOM(new File(tempDir.getAbsolutePath(), "coverage.xml"));
		
		int finalCount = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);
		
		assertEquals(0, finalCount);
		
	}
	
	public void startUpServlet() throws Exception {
		tester = new ServletTester();
		request = new HttpTester();
		response = new HttpTester();

		ClassLoader loader = createClassLoader();
		tester.setClassLoader(loader);
		
		tester.addServlet("com.acme.servlet.SimpleServlet", "/simple/SimpleServlet");
		tester.addServlet(FlushCoberturaServlet.class, "/coberturaFlush/flushCobertura");
		tester.start();

		request.setMethod("GET");
		request.setHeader("host", "tester");
		request.setVersion("HTTP/1.0");
	}
	
	public void pingServer() throws Exception {
		request.setURI("/simple/SimpleServlet");
		response.parse(tester.getResponses(request.generate()));
		assertNull(response.getMethod());
		assertEquals(200, response.getStatus());
		assertEquals("Hi", response.getContent().trim());
	}

	public void pingCoberturaServer() throws Exception {
		request.setURI("/coberturaFlush/flushCobertura");
		response.parse(tester.getResponses(request.generate()));
		assertNull(response.getMethod());
		assertEquals(200, response.getStatus());
		assertNull(response.getContent());
	}

	private void createSimpleWar() throws IOException {
		File webappsDir = new File(tempDir, "webapps");
		File war = new File(webappsDir, "simple.war");

		File classesDir = new File("target/build/simpleWarClasses/");
		if (!classesDir.exists())
			classesDir.mkdirs();

		FileUtils.copyDirectory(new File("target/test-classes/com"), new File(
				classesDir, "com"));

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(new File("src/test/resources/testJetty/web.xml"));

		ZipFileSet classesFileSet = new ZipFileSet();
		classesFileSet.setDir(classesDir);

		antWar.addClasses(classesFileSet);
		antWar.execute();
	}

	private void createCoberturaServlet() {
		File webappsDir = new File(TestUtils.getTempDir(), "webapps");
		File war = new File(webappsDir, "coberturaFlush.war");

		File classesDir = new File("target/build/warClasses");
		if (!classesDir.exists())
			classesDir.mkdirs();
		Javac javac = new Javac();
		javac.setProject(TestUtils.project);
		javac.setSrcdir(new Path(TestUtils.project, "src/main/java"));
		javac.setDestdir(classesDir);
		javac.setDebug(true);

		Path classpath = new Path(TestUtils.project);
		FileSet jettyFileSet = new FileSet();
		jettyFileSet.setDir(new File("src/test/resources/jetty"));
		jettyFileSet.setIncludes("**/*.jar");
		classpath.addFileset(jettyFileSet);

		javac.setIncludes("**/FlushCoberturaServlet.java");

		javac.setClasspath(classpath);
		javac.execute();

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(new File(
				"src/main/java/net/sourceforge/cobertura/webapp/web.xml"));

		ZipFileSet classesFileSet = new ZipFileSet();
		classesFileSet.setDir(classesDir);

		antWar.addClasses(classesFileSet);
		antWar.execute();
	}

	private ClassLoader createClassLoader() throws Exception {
		File simplewar = new File(tempDir, "webapps/simple.war");
		File coberturawar = new File(tempDir, "webapps/coberturaFlush.war");
		File coberturaJar = new File(tempDir, "lib/cobertura.jar");

		@SuppressWarnings("deprecation")
		URL[] urls = new URL[]{
					simplewar.toURL(),
					coberturawar.toURL(),
					coberturaJar.toURL()
				};

		System.out.println(Arrays.toString(urls));

		// Create a new class loader with the directory

		return new URLClassLoader(urls);
	}

	private void createCoberturaJar() {
		File coberturaJar = new File(tempDir, "lib/cobertura.jar");
		File coberturaClassDir = TestUtils.getCoberturaClassDir();

		Zip zip = new Zip();
		zip.setProject(TestUtils.project);
		zip.setDestFile(coberturaJar);

		FileSet fileSet = new FileSet();
		fileSet.setDir(coberturaClassDir);

		zip.addFileset(fileSet);
		zip.execute();
	}

	private void instrumentWar() {
		instrumentWar(new File(tempDir, "webapps/simple.war"));
		// Future: There is an issue with ServetTester api and adding a custom classloader.
		// When specify the instrumented .war file it still prefers to use the
		// target/test-classes/**/*.class files instead. In this situation we instrument the
		// classes directly but a better solution should be provided for this.
		instrumentClasses(new File("target/test-classes/com/acme/servlet/SimpleServlet.class"));
	}
	
	private void instrumentClasses(File classesDir) {
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile("cobertura.ser");
		FileSet fileSet = new FileSet();
		fileSet.setDir(classesDir.getParentFile());
		fileSet.setIncludes("**/*.class");
		
		instrumentTask.addFileset(fileSet);
		instrumentTask.execute();
	}

	private void instrumentWar(File warFile) {
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile("cobertura.ser");
		FileSet fileSet = new FileSet();
		fileSet.setDir(warFile.getParentFile());
		fileSet.setIncludes("**/*.war");

		instrumentTask.addFileset(fileSet);
		instrumentTask.execute();
	}

	public void generateReportFile() {
		File xmlReport = new File(tempDir.getAbsolutePath(), "coverage.xml");

		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile("cobertura.ser");
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(xmlReport.getParent()));
		reportTask.execute();
	}
}
