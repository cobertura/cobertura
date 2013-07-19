package net.sourceforge.cobertura.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.cobertura.test.IgnoreUtil;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class IgnoreTrivialAntTest extends AbstractCoberturaAntTestCase {
	IgnoreUtil ignoreUtil;
	@Before
	public void setUp() throws Exception {
		buildXmlFile = new File("src",
				"/test/resources/ant/IgnoreTrivial/build.xml");

		String target = "all";
		super.executeAntTarget(target);
	}

	@Test
	public void test() throws ParserConfigurationException, SAXException,
			IOException {
		dom = TestUtils.getXMLReportDOM(new File(buildXmlFile.getParentFile(),
				"reports/cobertura-xml/coverage.xml"));

		ignoreUtil = new IgnoreUtil("mypackage.Main", dom);

		// trivial empty constructor
		assertIgnored("<init>", "()V");

		// trivial constructor Main(Thread, String) that just calls super()
		assertIgnored("<init>", "(Ljava/lang/Thread;Ljava/lang/String;)V");

		// trivial getter
		assertIgnored("getterTrivial");

		// isBool is trivial
		assertIgnored("isBool");

		// hasBool is trivial
		assertIgnored("hasBool");

		// setInt is trivial
		assertIgnored("setInt");

		// Main(int) has non-trivial switch
		assertNotIgnored("<init>", "(I)V");

		// Main(boolean) has non-trivial conditional
		assertNotIgnored("<init>", "(Z)V");

		// "empty" does not start with "get", "is", "has", or "set".
		assertNotIgnored("empty");

		// gets with no return are considered non-trivial
		assertNotIgnored("getVoid");

		// gets that have parameters are considered non-trivial
		assertNotIgnored("getIntWithIntParm");

		// sets that have no parameters are considered non-trivial
		assertNotIgnored("set");

		// sets that have more than one parameters are considered non-trivial
		assertNotIgnored("setIntWithTwoParms");

		// don't ignore methods with multi-dimensional array creates
		assertNotIgnored("getMultiDimArray");

		// don't ignore methods with increment instructions for local variables
		assertNotIgnored("setIncrement");

		// don't ignore methods with LDC instructions (that use constants from the runtime pool)
		assertNotIgnored("setConst");
		assertNotIgnored("<init>", "(Ljava/lang/Thread;I)V"); // Main(Thread, int)

		// don't ignore methods with a single int operand (like creating an array).
		assertNotIgnored("getArray");

		// don't ignore methods with type instructions (like creating an object).
		assertNotIgnored("getObject");

		// don't ignore methods that use statics.
		assertNotIgnored("getStatic");
		assertNotIgnored("setStatic");
		assertNotIgnored("<init>", "(Ljava/lang/String;)V");

		// non-trivial local variable instructions (causes visitVarInsn call)
		assertNotIgnored("setISTORE");
		assertNotIgnored("setLSTORE");
		assertNotIgnored("setFSTORE");
		assertNotIgnored("setDSTORE");
		assertNotIgnored("setASTORE");

		// non-trivial method calls
		assertNotIgnored("getINVOKEVIRTUAL");
		assertNotIgnored("getINVOKESPECIAL");
		assertNotIgnored("getINVOKESTATIC");
		assertNotIgnored("setINVOKEINTERFACE");
		assertNotIgnored("<init>", "(Ljava/lang/String;Ljava/lang/String;)V"); // Main(String, String)
		assertNotIgnored("<init>", "(Ljava/lang/String;I)V"); // Main(String, int)
		assertNotIgnored("<init>", "(Ljava/lang/String;Z)V"); // Main(String, boolean)
	}

	public void assertIgnored(String methodName, String signature) {
		ignoreUtil.assertIgnored(methodName, signature);
	}

	public void assertIgnored(String methodName) {
		assertIgnored(methodName, null);
	}

	public void assertNotIgnored(String methodName, String signature) {
		ignoreUtil.assertNotIgnored(methodName, signature);
	}

	public void assertNotIgnored(String methodName) {
		assertNotIgnored(methodName, null);
	}
}