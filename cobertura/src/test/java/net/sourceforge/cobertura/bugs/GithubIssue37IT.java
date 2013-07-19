package net.sourceforge.cobertura.bugs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class GithubIssue37IT {
	@Test
	public void testVersionCorrect() throws IOException {
		File loc = (File) FileUtils.listFiles(new File("target"),
				new String[]{"jar"}, false).toArray()[0];
		JarFile jar = null;
		try {
			jar = new JarFile(loc);
			Manifest mf = jar.getManifest();
			assertNotNull(mf.getMainAttributes().getValue(
					"Implementation-Version"));
		} finally {
			jar.close();
		}
	}
}
