/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package net.sourceforge.cobertura.util;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author Grzegorz Lukasik
 */
public class CommandLineBuilderTest extends TestCase {

	private String[] testArguments(String[] args) throws Exception {
		CommandLineBuilder builder = new CommandLineBuilder();
		for (int i = 0; i < args.length; i++)
			builder.addArg(args[i]);
		builder.saveArgs();

		File cmdFile = new File(builder.getCommandLineFile());
		assertTrue(cmdFile.isAbsolute());
		assertTrue(cmdFile.isFile());

		String[] result = CommandLineBuilder
				.preprocessCommandLineArguments(new String[]{"--commandsfile",
						builder.getCommandLineFile()});
		builder.dispose();

		return result;
	}

	public void testExample() throws Exception {
		CommandLineBuilder builder = new CommandLineBuilder();
		builder.addArg("--someoption");
		builder.addArg("optionValue");
		builder.saveArgs();

		String[] args = CommandLineBuilder
				.preprocessCommandLineArguments(new String[]{"--commandsfile",
						builder.getCommandLineFile()});

		assertEquals("--someoption", args[0]);
		assertEquals("optionValue", args[1]);

		builder.dispose();
	}

	public void testExample_2() throws Exception {
		CommandLineBuilder builder = new CommandLineBuilder();
		builder.addArg("--someoption", "optionValue");
		builder.saveArgs();

		String[] args = CommandLineBuilder
				.preprocessCommandLineArguments(new String[]{"--commandsfile",
						builder.getCommandLineFile()});

		assertEquals("--someoption", args[0]);
		assertEquals("optionValue", args[1]);

		builder.dispose();
	}

	private void assertEquals(String[] first, String[] second) {
		assertEquals(first.length, second.length);
		for (int i = 0; i < first.length; i++) {
			assertEquals(first[i], second[i]);
		}
	}

	public void testManyOptions() throws Exception {
		String[] options = new String[100000];
		for (int i = 0; i < options.length; i++) {
			options[i] = "myOption" + i;
		}

		String[] args = testArguments(options);
		assertEquals(options, args);
	}

	public void testVariousOptions() throws Exception {
		String[] options = {
				"hello",
				" one",
				"two ",
				"  three , ",
				"\"'xx",
				" ",
				"file .java",
				"f.java",
				"#@()39340*(@0$#&%^@#&4098353856_*(_@735/896_udsknbfdvzxvkasd DSFWBXfqw']][.,=---3\\]];",
				"null", "!@#$%^&*()_+-={}|[]\\:\";'<>?,./'"};
		String[] args = testArguments(options);
		assertEquals(options, args);
	}

	public void testEmptyOptions() throws Exception {
		String[] args = testArguments(new String[0]);
		assertEquals(new String[0], args);
	}

	public void testInvalidArguments() throws Exception {
		CommandLineBuilder builder = new CommandLineBuilder();
		try {
			builder.addArg(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
		try {
			builder.addArg("someArgument", null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
		try {
			builder.addArg(null, "someValue");
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
		try {
			CommandLineBuilder.preprocessCommandLineArguments(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}

		try {
			CommandLineBuilder.preprocessCommandLineArguments(new String[]{
					"Hello", null});
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}

		try {
			CommandLineBuilder.preprocessCommandLineArguments(new String[]{
					"--commandsfile", "hello", null});
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
		}
	}

	public void testCommandsFileOption() throws Exception {
		String[] args = {"Hello", "world"};
		String[] result = CommandLineBuilder
				.preprocessCommandLineArguments(args);
		assertSame(args, result);

		try {
			args = new String[]{"Hello", "--commandsfile"};
			CommandLineBuilder.preprocessCommandLineArguments(args);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ex) {
		}

		try {
			args = new String[]{"Hello", "--commandsfile", "hello.cmd"};
			CommandLineBuilder.preprocessCommandLineArguments(args);
			fail("IO Exception expected");
		} catch (IOException ex) {
		}
	}

}
