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

package net.sourceforge.cobertura.instrument.pass2;

import net.sourceforge.cobertura.instrument.TouchPointListener;
import net.sourceforge.cobertura.instrument.tp.ClassMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Analyzes given method and applies information about all found important places into {@link #classmap}.
 * 
 * @author piotr.tabor@gmail.com
 */
public class BuildClassMapTouchPointListener implements TouchPointListener{
	private final ClassMap classmap;
	
	public BuildClassMapTouchPointListener(ClassMap classMap) {
		this.classmap=classMap;
	}
	
	public void beforeJump(int eventId,Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		classmap.registerNewJump(eventId,currentLine,label);		
	}

	public void beforeLabel(int eventId,Label label, int currentLine, MethodVisitor mv) {
		classmap.registerNewLabel(eventId,currentLine, label);		
	}	

	public void afterLineNumber(int eventId,Label label, int currentLine,MethodVisitor nextMethodVisitor,String methodName, String methodSignature) {
		classmap.registerLineNumber(eventId,currentLine,label,methodName,methodSignature);
	}
	
	public void beforeSwitch(int eventId, Label def, Label[] labels, int currentLine, MethodVisitor mv, String conditionType) {
		classmap.registerSwitch(eventId, currentLine, def, labels, conditionType);
	}

	public void ignoreLine(int eventId,int currentLine) {
		classmap.unregisterLine(eventId,currentLine);		
	}
	
// --------------- Not interesting events for analysis ---------------------------
	public void afterJump(int eventId,Label label, int currentLine,	MethodVisitor nextMethodVisitor) {}	
	public void afterLabel(int eventId,Label label, int currentLine, MethodVisitor mv) {}	
	public void afterMethodStart(MethodVisitor nextMethodVisitor) {};
}
