/**
 * 
 */
package net.sourceforge.cobertura.test;

import groovy.util.Node;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author schristou88
 *
 */
public class AbstractCoberturaTestCase {
	public static File tempDir;
	public static File srcDir;
	public static File reportDir;
	public static File instrumentDir;
	File mainSourceFile;
	File datafile;

	@Before
	public void setUp() throws IOException {
		tempDir = TestUtils.getTempDir();
		
		FileUtils.deleteDirectory(tempDir);
		
		srcDir = new File(tempDir, "src");
		reportDir = new File(tempDir, "report");
		instrumentDir = new File(tempDir, "instrument");
		mainSourceFile = new File(srcDir, "mypackage/Main.java");
		datafile = new File(srcDir, "cobertura.ser");
		
		srcDir.mkdirs();
		reportDir.mkdirs();
		instrumentDir.mkdirs();
	}
	
	@After
	public void tearDown() {
		// Default is do nothing since if we try
		// debugging we can see logs of current failure.
	}
	
	public static void assertConditionCoverage(List<Node> lines, String expectedValue, int lineNumber) {
		boolean found = false;
		for(Node node : lines) {
			if (Integer.valueOf((String)node.attribute("number")) == lineNumber) {
				found = true;
				assertEquals(expectedValue, (String)node.attribute("condition-coverage"));
			}
		}
		assertTrue(found);
	}
}
