package net.sourceforge.cobertura.dsl;

public enum CMDParams {
	COMMANDS_FILE("--commandsfile"), DATA_FILE("--datafile"), DESTINATION(
			"--destination"), IGNORE("--ignore"), IGNORE_BRANCHES(
			"--ignoreBranches"), IGNORE_METHOD_ANNOTATION(
			"--ignoreMethodAnnotation"), INCLUDE_CLASSES("--includeClasses"), EXCLUDE_CLASSES(
			"--excludeClasses"), IGNORE_TRIVIAL("--ignoreTrivial"), FAIL_ON_ERROR(
			"--failOnError"), FORMAT("--format"), ENCODING("--encoding"), BASEDIR(
			"--basedir"), BRANCH("--branch"), LINE("--line"), REGEX("--regex"), PACKAGE_BRANCH(
			"--packagebranch"), PACKAGE_LINE("--packageline"), TOTAL_BRANCH(
			"--totalbranch"), TOTAL_LINE("--totalline"), FILE_TO_INSTRUMENT(
			"filetoinstrument"), FILE_TO_MERGE("filetomerge"), THREADSAFE_RIGOROUS(
			"--threadsafeRigorous");

	private CMDParams(String command) {
		this.command = command;
	}
	private String command;
	@Override
	public String toString() {
		return command;
	}
}
