package net.sourceforge.cobertura.bugs;

import static org.junit.Assert.*;

import groovy.util.Node;

import org.junit.Test;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;

public class GithubIssue30 extends AbstractCoberturaTestCase {

	Node dom;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		dom = super.createAndExecuteMainMethod(packageName, fileName, file1,
				mainMethod);
	}

	@Test
	public void testIssue() {
		assertTrue(TestUtils.getTotalHitCount(dom,
				"mypackage.InstrumentationFailsOnFirstNewClassInTryBlock",
				"saveToDatabase") > 0);
		assertEquals(3, TestUtils.getTotalHitCount(dom,
				"mypackage.InstrumentationFailsOnFirstNewClassInTryBlock",
				"main"));
		assertEquals(2, TestUtils.getTotalHitCount(dom, "mypackage.DataAccess",
				"<init>"));
	}

	static final String packageName = "mypackage";
	static final String fileName = "InstrumentationFailsOnFirstNewClassInTryBlock";
	static final String mainMethod = "mypackage.InstrumentationFailsOnFirstNewClassInTryBlock";
	static final String file1 = "package mypackage;"
			+ "\n public class InstrumentationFailsOnFirstNewClassInTryBlock {"
			+ "\n "
			+ "\n "
			+ "\n  public void saveToDatabase() {"
			+ "\n    try {"
			+ "\n      // boolean b=false;"
			+ "\n      // if (b) {"
			+ "\n      //   System.out.println(\"no action\");"
			+ "\n      // }"
			+ "\n      DataAccess da = new DataAccess();"
			+ "\n      System.out.println(\"nothing\");"
			+ "\n "
			+ "\n    } catch (Exception e) {"
			+ "\n "
			+ "\n "
			+ "\n    }"
			+ "\n   }"
			+ "\n   public static void main(String[] args) {"
			+ "\n     InstrumentationFailsOnFirstNewClassInTryBlock ifofncitb = new InstrumentationFailsOnFirstNewClassInTryBlock();"
			+ "\n     ifofncitb.saveToDatabase();"
			+ "\n   }"
			+ "\n }"
			+ "\n class DataAccess {"
			+ "\n   public DataAccess() {"
			+ "\n     //To change body of created methods use File | Settings | File Templates."
			+ "\n   }" + "\n }";
}