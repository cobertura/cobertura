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

package net.sourceforge.cobertura.instrument.pass3;

import net.sourceforge.cobertura.instrument.TouchPointListener;
import net.sourceforge.cobertura.instrument.tp.ClassMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Inject code provided by {@link #codeProvider} into the instrumented method's body. Injects code that
 * is responsible for incrementing counters. Mapping of places into counters is provided by {@link #classMap}.
 *
 * @author piotr.tabor@gmail.com
 */
public class InjectCodeTouchPointListener implements TouchPointListener {
	private final static Logger logger = LoggerFactory
			.getLogger(InjectCodeTouchPointListener.class);
	/**
	 * Component that is resposible for generation of the snipets
	 */
	private final CodeProvider codeProvider;

	/**
	 * Source of mapping from place (eventId) into counterId that is incremented if the place is touched
	 */
	private final ClassMap classMap;

	private int lastJumpIdVariableIndex;

	public InjectCodeTouchPointListener(ClassMap classMap,
			CodeProvider codeProvider) {
		this.classMap = classMap;
		this.codeProvider = codeProvider;
	}

	/**
	 * Before jump we will store into 'internal variable' the counterId of a 'true' branch of the JUMP
	 */
	public void beforeJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		Integer jumpTrueCounterId = classMap.getCounterIdForJumpTrue(eventId);
		if (jumpTrueCounterId != null) {
			codeProvider.generateCodeThatSetsJumpCounterIdVariable(
					nextMethodVisitor, jumpTrueCounterId,
					lastJumpIdVariableIndex);
		}
	}

	/**
	 * After jump we will increment counterId for the 'false' branch of the JUMP.
	 * Then we set internal variable to ZERO to avoid fake interpretation (another one incrementation)
	 */
	public void afterJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		logger.debug("After jump:" + currentLine + "(" + eventId + ") to :"
				+ label);
		Integer jumpFalseCounterId = classMap.getCounterIdForJumpFalse(eventId);
		if (jumpFalseCounterId != null) {
			codeProvider.generateCodeThatIncrementsCoberturaCounter(
					nextMethodVisitor, jumpFalseCounterId, classMap
							.getClassName());
			codeProvider.generateCodeThatZeroJumpCounterIdVariable(
					nextMethodVisitor, lastJumpIdVariableIndex);
		}
	}

	/**
	 * Before switch we set the internal variable to a special counterId connected with the switch. This counterId is not
	 * connected with any branch of the switch.
	 */
	public void beforeSwitch(int eventId, Label def, Label[] labels,
			int currentLine, MethodVisitor mv, String conditionType) {
		Integer switchCounterId = classMap.getCounterIdForSwitch(eventId);
		if (switchCounterId != null) {
			codeProvider.generateCodeThatSetsJumpCounterIdVariable(mv,
					switchCounterId, lastJumpIdVariableIndex);
		}
	}

	/**
	 * <p>If the label is JUMP destination, we will increment the counter stored inside the 'internal variable'. This way we are
	 * incrementing the 'true' branch of the condition. </p>
	 * <p/>
	 * <p>If the label is SWITCH destination, we check all switch instructions that have targets in the label we generate
	 * code that checks if the 'internal variable' is equal to id of considered switch and if so increments counterId connected to the switch.
	 */
	public void afterLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
		logger.debug("Looking for jumps going to event(" + eventId + "):"
				+ label + " ");
		if (classMap.isJumpDestinationLabel(eventId)) {
			codeProvider
					.generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
							mv, lastJumpIdVariableIndex, classMap
									.getClassName());
		}

		Map<Integer, Integer> branchTouchPoints = classMap
				.getBranchLabelDescriptorsForLabelEvent(eventId);
		if (branchTouchPoints != null) {
			/*map of counterId of a switch into counterId of the branch of the switch*/
			for (Map.Entry<Integer, Integer> entry : branchTouchPoints
					.entrySet()) {
				codeProvider
						.generateCodeThatIncrementsCoberturaCounterIfVariableEqualsAndCleanVariable(
								mv, entry.getKey(), entry.getValue(),
								lastJumpIdVariableIndex, classMap
										.getClassName());
			}
		}

		if (classMap.isJumpDestinationLabel(eventId)) {
			codeProvider.generateCodeThatZeroJumpCounterIdVariable(mv,
					lastJumpIdVariableIndex);
		}
	}

	/**
	 * After every 'linenumber' instruction we increments counter connected with the line number.
	 */
	public void afterLineNumber(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor, String methodName,
			String methodSignature) {
		Integer lineCounterId = classMap.getCounterIdForLineEventId(eventId);
		if (lineCounterId != null) {
			codeProvider.generateCodeThatIncrementsCoberturaCounter(
					nextMethodVisitor, lineCounterId, classMap.getClassName());
		}
	}

	/**
	 * At the start of every method we initiates the 'internal variable' with zero.
	 */
	public void afterMethodStart(MethodVisitor nextMethodVisitor) {
		codeProvider.generateCodeThatZeroJumpCounterIdVariable(
				nextMethodVisitor, lastJumpIdVariableIndex);
	}

	// ------------------- ignored events -------------------------------	

	public void beforeLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
	}

	public void ignoreLine(int eventId, int currentLine) {
	}

	// ------------------- getters and setters --------------------------	

	/**
	 * Index of 'internal variable'. Should be detected by {@link ShiftVariableMethodAdapter#calculateFirstStackVariable(int, String)}.
	 */
	public void setLastJumpIdVariableIndex(int lastJumpIdVariableIndex) {
		this.lastJumpIdVariableIndex = lastJumpIdVariableIndex;
	}

}
