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

package net.sourceforge.cobertura.instrument;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * <p>Interfaces of listener that is called if interesting 'place' is found in instrumented/analyzed code</p>
 * 
 * It is guaranteed that the same 'eventIds' will be used for the same events (in the case of identical source byte-code), 
 * so you can use this ids to identify the same places between different passes of instrumentation.   
 * 
 * @author piotr.tabor@gmail.com
 */
public interface TouchPointListener {
	/**
	 * Event called when a new method have been just started for instrumentation. It is called
	 * after method declaration has been read, but before 'a real' code has been processed.  
	 * 
	 * @param nextMethodVisitor  - sink for instrumented code 
	 */
	public void afterMethodStart(MethodVisitor nextMethodVisitor);
	
	/**
	 * <p>Before code responsible for realizing 'interesting' JUMP </p>
	 * 
	 * <p>JUMP event is not called in case of GOTO and RETURN instruction (not conditional JUMPS)</p>
	 * 
	 * @param eventId     - id of the detected event. 
	 * @param label       - destination label of the jump 
	 * @param currentLine - number of currently visited line
	 * @param nextMethodVisitor - sink for instrumented code
	 */
	public void afterJump(int eventId, Label label,int currentLine, MethodVisitor nextMethodVisitor);
	
	/**
	 * <p>Called after code responsible for realizing 'interesting' JUMP </p>
	 * 
	 * <p>JUMP event is not called in case of GOTO and RETURN instruction (not conditional JUMPS)</p>
	 * 
	 * @param eventId     - id of the detected event. 
	 * @param label       - destination label of the jump 
	 * @param currentLine - number of currently visited line
	 * @param nextMethodVisitor - sink for instrumented code
	 */
	public void beforeJump(int eventId, Label label,int currentLine, MethodVisitor nextMethodVisitor);
	
	/**
	 * after LINENUMBER instruction was processed. 
	 * 
	 * @param eventId            - id of the detected event.
	 * @param label              - label connected to the line 
	 * @param currentLine        - number of currently visited line
	 * @param nextMethodVisitor  - sink for instrumented code
	 * @param methodName         - name  of currently being instrumented method
	 * @param methodSignature    - signature (params and returned value type) of currently being instrumented method 
	 */
	public void afterLineNumber(int eventId,Label label, int currentLine, MethodVisitor nextMethodVisitor,String methodName, String methodSignature);
	
	/**
	 * <p>If we determined that some line should be not 'counted' by cobertura (for example the line might be specified in {@link AbstractFindTouchPointsClassInstrumenter#ignoreRegexp})
	 * we call this method.</p>   
	 * 
	 * It's possible that {@link #afterLineNumber(int, Label, int, MethodVisitor, String, String)} event will be (or has been already) fired.
	 * 
	 * @param eventId            - id of the event connected to the line (the same eventId that might be used for the line in {@link #afterLineNumber(int, Label, int, MethodVisitor, String, String)})  
	 * @param currentLine		 - number of line that should be ignored. 
	 */
	public void ignoreLine(int eventId,int currentLine);
	
	/**
	 * Called befere processing switch statement. 
	 * 
	 * @param eventId     - id of the detected event.
	 * @param def	      - label of 'default' target label that will be used when none of given values match the switch 
	 * @param labels      - table of labels connected to switch 'values' (different switch branches). There might be duplicates in the table (fall-through in switch statement)
	 * @param currentLine - number of line in which the 'switch' keyword has been found (number of line currently being processed)  
	 * @param mv          - sink for instrumented (injected) code
	 * @param conditionType - NULL (if undetected) or signature of variable used as a switch condition.	 * 						 
	 */
	public void beforeSwitch(int eventId,Label def, Label[] labels, int currentLine, MethodVisitor mv, String conditionType);
	
	/**
	 * Called before processing 'label' directive
	 * 
	 * @param eventId     - id of the detected event.
	 * @param label       - internal identifier of label being found (single pass of instrumentation valid only)
	 * @param currentLine - number of line in which the 'switch' keyword has been found (number of line currently being processed)  
	 * @param mv          - sink for instrumented (injected) code
	 */
	public void beforeLabel(int eventId, Label label, int currentLine, MethodVisitor mv);

	/**
	 * Called before processing 'label' directive
	 * 
	 * @param eventId     - id of the detected event.
	 * @param label       - internal identifier of label being found (single pass of instrumentation valid only)
	 * @param currentLine - number of line in which the 'switch' keyword has been found (number of line currently being processed)  
	 * @param mv          - sink for instrumented (injected) code
	 */
	public void afterLabel(int eventId,Label label, int currentLine, MethodVisitor mv);

}
