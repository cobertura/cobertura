package net.sourceforge.cobertura.ant;

import static org.junit.Assert.*;

import java.io.File;

import net.sourceforge.cobertura.test.util.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class IgnoreMethodAnnotationAntTest extends AbstractCoberturaAntTestCase {
	@Before
	public void setUp() throws Exception {
		buildXmlFile = new File("src",
				"/test/resources/ant/IgnoreMethodAnnotation/build.xml");

		String target = "all";
		super.executeAntTarget(target);
	}

	@Test
	public void test() {
		String className = "test.condition.IgnoreMe";
		String methodName = "foo";

		assertEquals(0, TestUtils.getTotalHitCount(dom, className, methodName));

		className = "test.condition.IgnoreMeAlso";
		assertEquals(0, TestUtils.getTotalHitCount(dom, className, methodName));

		className = "test.condition.IgnoreMeNot";
		assertTrue(TestUtils.getHitCount(dom, className, methodName) > 0);
	}
}