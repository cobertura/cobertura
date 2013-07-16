package net.sourceforge.cobertura.bugs;

import java.io.IOException;

import org.junit.Test;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;

public class GithubIssue42 extends AbstractCoberturaTestCase {

	@Test
	public void testPlusEqualsParsing() throws IOException {
		String imports = "";
		String method = "\n public void foo() {" + "\n   int yyn = 0;"
				+ "\n   int yychar = 1;" + "\n   if ((yyn += yychar) >= 0)"
				+ "\n   {} " + "\n   if ((yyn -= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn *= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn /= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn &= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn |= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn ^= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn %= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn <<= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn >>= yychar) >= 0)" + "\n   {} "
				+ "\n   if ((yyn >>>= yychar) >= 0)" + "\n   {} " + "\n }";

		super.parseIssueTester(imports, method);
	}
}