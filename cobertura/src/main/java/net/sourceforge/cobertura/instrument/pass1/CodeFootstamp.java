/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2011 Piotr Tabor
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

package net.sourceforge.cobertura.instrument.pass1;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * <p>Represents a single 'footprint' of some piece of ASM code. Is used to detect if two
 * code pieces are (nearly) the same or different.</p>
 * <p/>
 * <p>During duplicate-detection we create {@link CodeFootstamp} for every block found starting with LINENUMBER directive.<br/>
 * We appends to the {@link CodeFootstamp} all found 'jvm asm' instructions. When we found the end of the block (start of next line) we need to
 * call {@link #finalize()}.  After that we are allowed to use {@link #hashCode()}, {@link #equals(Object)} and {@link #isMeaningful()} methods
 * to compare two blocks and decide if they are duplicates or not.
 * </p>
 * <p/>
 * We find two {@link CodeFootstamp} as duplicates not only when they are completely identical, but also if:
 * <ul>
 * <li> They start with another number of 'LABEL' instructions (see {@link #trimableIfFirst(String)})</li>
 * <li> They differs in last instruction being  'LABEL', 'GOTO', 'RETURN' or 'ATHROW'  (see {@link #trimableIfLast(String)})</li>
 * <li> They use different destination labels for JUMPs and SWITCHES</li>
 * </ul>
 * <p/>
 * <p> You should also use {@link #isMeaningful()} method to avoid comparing two {@link CodeFootstamp} that are two short (for example empty).
 * They would be probably found this snapshot as 'equal', but in fact the snapshoots are too short to proof anything.
 * </p>
 * <p/>
 * <p>At the implementation level we encode all found instructions into list of String {@link #events} and we are comparing those string list.
 * It's not beautiful design - but its simple and works.
 * </p>
 * <p/>
 * <p>This class implements {@link #equals(Object)} and {@link #hashCode()} so might be used as key in maps</p>
 */
public class CodeFootstamp {
	private final LinkedList<String> events = new LinkedList<String>();
	private boolean finalized = false;

	public void visitLabel(Label label) {
		appendIfNotFinal("L");
	}

	private void appendIfNotFinal(String string) {
		assertNotFinal();
		events.addLast(string);
	}

	private void assertNotFinal() {
		if (finalized) {
			throw new IllegalStateException(
					"The signature has bean already finalized");
		}
	}

	public void visitFieldInsn(int access, String name, String description,
			String signature) {
		appendIfNotFinal("F:" + access + ":" + name + ":" + description + ":"
				+ signature);
	}

	public void visitInsn(int opCode) {
		appendIfNotFinal(String.valueOf(opCode));
	}

	void visitIntInsn(int opCode, int variable) {
		appendIfNotFinal(opCode + ":" + variable);
	}

	public void visitIintInsn(int opCode, int variable) {
		appendIfNotFinal(opCode + ":" + variable);
	}

	public void visitLdcInsn(Object obj) {
		appendIfNotFinal("LDC:" + obj.toString());
	}

	public void visitJumpInsn(int opCode, Label label) {
		appendIfNotFinal("JUMP:" + opCode);
	}

	public void visitMethodInsn(int opCode, String className,
			String methodName, String description) {
		appendIfNotFinal("MI:" + opCode + ":" + className + ":" + methodName
				+ ":" + description);
	}

	public void visitMultiANewArrayInsn(String type, int arg1) {
		appendIfNotFinal("MultiArr:" + type + ":" + arg1);

	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		appendIfNotFinal("LSWITCH:" + Arrays.toString(arg1));
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
			Label[] arg3) {
		appendIfNotFinal("TSWITCH:" + arg0 + ":" + arg1);
	}

	public void finalize() {
		while (events.size() > 0 && trimableIfLast(events.getLast())) {
			events.removeLast();
		}
		while (events.size() > 0 && trimableIfFirst(events.getFirst())) {
			events.removeFirst();
		}
		finalized = true;
	}

	private boolean trimableIfFirst(String e) {
		return e.equals("L");//No labels at the begining
	}

	private boolean trimableIfLast(String e) {
		return e.equals("JUMP:" + String.valueOf(Opcodes.GOTO))
				|| e.equals(String.valueOf(Opcodes.RETURN))
				|| e.equals(String.valueOf(Opcodes.ATHROW)) || e.equals("L");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String s : events) {
			sb.append(s).append(';');
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((events == null) ? 0 : events.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CodeFootstamp other = (CodeFootstamp) obj;
		if (events == null) {
			if (other.events != null) {
				return false;
			}
		} else if (!events.equals(other.events)) {
			return false;
		}
		return true;
	}

	/**
	 * Some signatures are to simple (empty) and generates false positive duplicates. To avoid
	 * that we filter here lines that are shorter then 2 jvm asm instruction.
	 *
	 * @return true if the signature is long enough to make sense comparing it
	 */
	public boolean isMeaningful() {
		if (!finalized) {
			throw new IllegalStateException(
					"The signature should been already finalized");
		}
		return events.size() >= 1;
	}

}
