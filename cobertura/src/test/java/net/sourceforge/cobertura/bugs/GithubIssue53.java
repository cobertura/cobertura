/**
 * 
 */
package net.sourceforge.cobertura.bugs;

import java.io.IOException;

import org.junit.Test;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;

/**
 * @author schristou88
 *
 */
public class GithubIssue53 extends AbstractCoberturaTestCase {
	@Test
	public void testInnersParsing() throws IOException {
		String imports = "";
		String method = "\n public class Outer {" + "\n   private Inner inner;"
				+ "\n   public Outer() {" + "\n     inner = this.new Inner() {"
				+ "\n       @Override" + "\n       public int getI() {"
				+ "\n         return 15;" + "\n       }" + "\n     };"
				+ "\n   }" + "\n   private class Inner {"
				+ "\n     public int getI() {" + "\n       return 1;"
				+ "\n     }" + "\n   }" + "\n }";

		super.parseIssueTester(imports, method);
	}
}
