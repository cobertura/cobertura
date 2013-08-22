package net.sourceforge.cobertura.ant;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.ShutdownHooks;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

import java.io.OutputStream;

/**
 * This formatter is used to apply shutdown hooks previously bundled to
 * a static statement on TouchCollector class, so that a shutdown hook
 * and some classes would be loaded when the TouchCollector class would be loaded.
 */
public class CoberturaJUnitResultFormatter implements JUnitResultFormatter {

	public void startTestSuite(JUnitTest jUnitTest) throws BuildException {
		//dummy, nothing here
	}

	public void endTestSuite(JUnitTest jUnitTest) throws BuildException {
		System.out
				.println("Running CoberturaJUnitResultFormatter: applying shutdown hooks...");
		ShutdownHooks.saveGlobalProjectDataWithTomcatInits();
	}

	public void setOutput(OutputStream outputStream) {
		//dummy, nothing here
	}

	public void setSystemOutput(String s) {
		//dummy, nothing here
	}

	public void setSystemError(String s) {
		//dummy, nothing here
	}

	public void addFailure(Test test, AssertionFailedError assertionFailedError) {
		//dummy, nothing here
	}

	public void endTest(Test test) {
		//dummy, nothing here
	}

	public void startTest(Test test) {
		//dummy, nothing here
	}

	public void addError(Test test, Throwable throwable) {
		//dummy, nothing here
	}
}
