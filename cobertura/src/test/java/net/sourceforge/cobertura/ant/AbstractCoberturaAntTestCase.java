package net.sourceforge.cobertura.ant;

import groovy.util.Node;

import java.io.File;

import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Tutorial on how to add a new Ant Test unit.
 * 
 * In this case we are creating a new argument called IgnoreMethodAnnotations.
 * 
 * 1. Copy src/test/resources/ant/basic and rename the folder. In this case we rename to IgnoreMethodAnnotations.
 * 2. Make a new test unit (call it IgnoreMethodAnnotationAntTest) and extend this class.
 * 3. In test unit, set buildXmlFile = src/test/resources/ant/IgnoreMethodAnnotations/build.xml.
 * 4. Make modification to the build.xml file accordingly.
 * 5. To execute ant, call super.executeAntTarget(TARGET_NAME)
 * 6. To verify execution there are helper methods that can do verification for you.
 *      If you would like to obtain the xml document it is public groovy.util.Node dom.
 * 
 * 
 * @author schristou88
 *
 */
public class AbstractCoberturaAntTestCase {
	public File buildXmlFile;
	@Rule
	public TestName name = new TestName();
	public Node dom;

	/**
	 * NOTE: Must set the baseDir before executing this command.
	 * 
	 * @param target Name of the target
	 * @throws Exception 
	 */
	public void executeAntTarget(String target) throws Exception {
		Exception error = null;
		Project project = new Project();
		BuildLogger buildLogger = new DefaultLogger();
		buildLogger.setErrorPrintStream(System.err);
		buildLogger.setOutputPrintStream(System.out);
		try {
			project.addBuildListener(buildLogger);
			project.fireBuildStarted();
			project.init();
			project.setUserProperty(MagicNames.ANT_FILE, buildXmlFile
					.getAbsolutePath());
			ProjectHelper.configureProject(project, buildXmlFile);
			project.executeTarget(target);
		} catch (Exception e) {
			error = e;
			throw e;
		} finally {
			project.fireBuildFinished(error);
		}

		dom = TestUtils.getXMLReportDOM(new File(buildXmlFile.getParentFile(),
				"reports/cobertura-xml/coverage.xml"));
	}
}