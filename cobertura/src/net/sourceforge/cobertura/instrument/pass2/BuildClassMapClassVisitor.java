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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.instrument.AbstractFindTouchPointsClassInstrumenter;
import net.sourceforge.cobertura.instrument.FindTouchPointsMethodAdapter;
import net.sourceforge.cobertura.instrument.HistoryMethodAdapter;
import net.sourceforge.cobertura.instrument.pass3.CodeProvider;
import net.sourceforge.cobertura.instrument.tp.ClassMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>Analyzes given class. Builds {@link ClassMap} that represents any touch-points and other important information
 * for instrumentation.</p>
 * 
 *  This instrumenter ({@link ClassVisitor}) does not change the bytecode of the class. It makes only analyzys and fills {@link ClassMap}. 
 * 
 * @author piotr.tabor@gmail.com
 */
public class BuildClassMapClassVisitor extends AbstractFindTouchPointsClassInstrumenter{
	/**
	 * {@link ClassMap} for the currently analyzed class. 
	 */
	private final ClassMap classMap = new ClassMap();
	
	/**
	 * Information about important 'events' (instructions) are sent into the listener that is internally
	 * responsible for modifying the {@link #classMap} content. 
	 */
	private final BuildClassMapTouchPointListener touchPointListener = new BuildClassMapTouchPointListener(classMap);	
	
	/**
	 * It's flag that signals if the class should be instrumented by cobertura. 
	 * After analyzing the class you can check the field using {@link #shouldBeInstrumented()}.
	 */
	private boolean toInstrument = true; 
	
	private final Set<String> ignoredMethods;

	/**
	 * @param cv                 - a listener for code-instrumentation events 
	 * @param ignoreRegexp       - list of patters of method calls that should be ignored from line-coverage-measurement 
	 * @param duplicatedLinesMap - map of found duplicates in the class. You should use {@link DetectDuplicatedCodeClassVisitor} to find the duplicated lines. 
	 */
	public BuildClassMapClassVisitor(ClassVisitor cv, Collection<Pattern> ignoreRegexes,Map<Integer, Map<Integer, Integer>> duplicatedLinesMap,
			Set<String> ignoredMethods) {
		super(cv,ignoreRegexes,duplicatedLinesMap);
		this.ignoredMethods = ignoredMethods;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String name, boolean arg1) {
		if (Type.getDescriptor(CoverageIgnore.class).equals(name)) {
			toInstrument = false;
		}
		return super.visitAnnotation(name, arg1);
	}

	/**
	 * Stores in {@link #classMap} information of className and if the class should be instrumented ({@link #shouldBeInstrumented()}) 
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
			String parent, String[] interfaces) {
		classMap.setClassName(name);
		
		if ((access & Opcodes.ACC_INTERFACE) != 0) {
			toInstrument = false;
		}
		super.visit(version, access, name, signature, parent, interfaces);		
	}
	
	/**
	 * Stores in {@link #classMap} information of source filename
	 */
	@Override
	public void visitSource(String file, String debug) {
		classMap.setSource(file);
		super.visitSource(file, debug);
	}
	
	/**
	 * Analyzes given method and stores  information about all found important places into {@link #classMap} 
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {		
		if (((access & Opcodes.ACC_STATIC) != 0) 
				&& CodeProvider.COBERTURA_INIT_METHOD_NAME.equals(name)) {
		  toInstrument = false; //  The class has bean already instrumented.
		}
		
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,	exceptions);
		if (ignoredMethods.contains(name + desc)) {
			return mv;
		}
		FindTouchPointsMethodAdapter instrumenter = new FindTouchPointsMethodAdapter(
				new HistoryMethodAdapter(mv, 4),
				classMap.getClassName(),name,desc,eventIdGenerator,duplicatedLinesMap,lineIdGenerator);
		instrumenter.setTouchPointListener(touchPointListener);
		instrumenter.setIgnoreRegexp(getIgnoreRegexp());
		return instrumenter;
	}

	/**
	 * Returns classMap build for the analyzed map. The classmap is filled after running the analyzer ({@link ClassReader#accept(ClassVisitor, int)}).
	 * 
	 * @return the classmap.
	 */
	public ClassMap getClassMap(){
		return classMap;
	}

	/**
	 * It's flag that signals if the class should be instrumented by Cobertura.
	 */
	public boolean shouldBeInstrumented() {
		return toInstrument;
	}
}
