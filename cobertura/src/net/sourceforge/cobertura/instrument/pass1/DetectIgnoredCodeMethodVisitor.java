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

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.instrument.ContextMethodAwareMethodAdapter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class DetectIgnoredCodeMethodVisitor extends ContextMethodAwareMethodAdapter {
	final String superName;
	final Set<Integer> ignoredLineIds;
	final Set<String>  ignoredMethodNamesAndSignatures;
	 
	final Set<String> ignoreMethodAnnotations;
	final boolean ignoreTrivial;

	enum IgnoredStatus {
		POSSIBLE_TRIVIAL_GETTER,
		POSSIBLE_TRIVIAL_SETTER,
		POSSIBLE_TRIVIAL_INIT,
		IGNORED_BY_ANNOTATION,
		NOT_IGNORED;
		
		boolean isTrivial(){
			return (this == POSSIBLE_TRIVIAL_GETTER)
				|| (this == POSSIBLE_TRIVIAL_SETTER)
				|| (this == POSSIBLE_TRIVIAL_INIT);			      
		}
	}
	
	public IgnoredStatus ignoredStatus;
	
	public DetectIgnoredCodeMethodVisitor(MethodVisitor mv,
			Set<Integer> ignoredLineIds, Set<String> ignoredMethodNamesAndSignatures,
			boolean ignoreTrivial, Set<String> ignoreMethodAnnotations,
			String className, String superName, String methodName, String description,
			AtomicInteger lineIdGenerator) {
		super(mv,className, methodName, description, lineIdGenerator);
		this.superName = superName;
		this.ignoredLineIds = ignoredLineIds;
		this.ignoredMethodNamesAndSignatures = ignoredMethodNamesAndSignatures;
		this.ignoreTrivial = ignoreTrivial;
		this.ignoredStatus = checkForTrivialSignature(methodName, description);
		this.ignoreMethodAnnotations = ignoreMethodAnnotations;
	}

	private static IgnoredStatus checkForTrivialSignature(String name, String desc) {
	  Type[] args = Type.getArgumentTypes(desc);
	  Type ret = Type.getReturnType(desc);	  
	  if (name.equals("<init>")) {
      	return IgnoredStatus.POSSIBLE_TRIVIAL_INIT;
	  }
	
	  // a "setter" method must:
	  // - have a name starting with "set"
	  // - take one arguments
	  // - return nothing (void)
	  if (name.startsWith("set") && args.length == 1 && ret.equals(Type.VOID_TYPE)) {
		  return IgnoredStatus.POSSIBLE_TRIVIAL_SETTER;
	  }
	  
	  // a "getter" method must:
	  // - have a name starting with "get", "is", or "has"
	  // - take no arguments
	  // - return a value (non-void)
	  if ((name.startsWith("get") || name.startsWith("is") || name.startsWith("has")) &&
	     args.length == 0 && !ret.equals(Type.VOID_TYPE)) {
		  return IgnoredStatus.POSSIBLE_TRIVIAL_GETTER;
	  }
	  
	  return IgnoredStatus.NOT_IGNORED;
	}
	
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {	 
	   // Check to see if this annotation is one of the ones that we use to 
	   // trigger us to ignore this method
	   String clazz = Type.getObjectType(desc.substring(1).replace(';', ' ').trim()).getClassName();
	   if (ignoreMethodAnnotations.contains(clazz)
		 || desc.equals(Type.getDescriptor(CoverageIgnore.class))){
		   ignoredStatus = IgnoredStatus.IGNORED_BY_ANNOTATION;
	   }	                
	   return super.visitAnnotation(desc, visible);
    }
	
	@Override
	public void visitJumpInsn(int arg0, Label arg1) {		
		markNotTrivial();
		super.visitJumpInsn(arg0, arg1);
	}
	
	public void visitFieldInsn(int opcode, String string, String string1, String string2){
	  super.visitFieldInsn(opcode, string, string1, string2);
	  if (ignoredStatus.isTrivial()) {
	    // trivial opcodes for accessing class fields are:
		// - GETFIELD or PUTFIELD
		if ((ignoredStatus == IgnoredStatus.POSSIBLE_TRIVIAL_GETTER && opcode != Opcodes.GETFIELD) ||
			(ignoredStatus == IgnoredStatus.POSSIBLE_TRIVIAL_SETTER && opcode != Opcodes.PUTFIELD) ||
			(ignoredStatus == IgnoredStatus.POSSIBLE_TRIVIAL_INIT && opcode != Opcodes.PUTFIELD)) {
  		  markNotTrivial();
		}
   	  }
	}
	
	public void visitVarInsn(int opcode, int i1) {
		super.visitVarInsn(opcode, i1);
		if (ignoredStatus.isTrivial() &&
		    opcode != Opcodes.ILOAD &&
		    opcode != Opcodes.LLOAD &&
			opcode != Opcodes.FLOAD &&
  			opcode != Opcodes.DLOAD &&
			opcode != Opcodes.ALOAD) {
          markNotTrivial();
	    }
	 }
	
	@Override
	public void visitTypeInsn(int arg0, String arg1) {
	  super.visitTypeInsn(arg0, arg1);
	  markNotTrivial();
	}
	
	@Override
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		super.visitLookupSwitchInsn(arg0, arg1, arg2);
		markNotTrivial();
	}
	
	@Override
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
			Label[] arg3) {
	  super.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
	  markNotTrivial();
	}
	
	@Override
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		super.visitMultiANewArrayInsn(arg0, arg1);
		markNotTrivial();
	}
	
	@Override
	public void visitIincInsn(int arg0, int arg1) {
		super.visitIincInsn(arg0, arg1);
		markNotTrivial();
	}
	
	@Override
	public void visitLdcInsn(Object arg0) {
		super.visitLdcInsn(arg0);
		markNotTrivial();		
	}
	
	@Override
	public void visitIntInsn(int arg0, int arg1) {
		super.visitIntInsn(arg0, arg1);
		markNotTrivial();
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (ignoredStatus.isTrivial() &&
		  !(ignoredStatus == IgnoredStatus.POSSIBLE_TRIVIAL_INIT
			  && name.equals("<init>") && owner.equals(superName) && opcode == Opcodes.INVOKESPECIAL)) {
			 markNotTrivial();
		}		
		super.visitMethodInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
		if ((ignoredStatus == IgnoredStatus.IGNORED_BY_ANNOTATION)
		    || (ignoreTrivial && ignoredStatus.isTrivial())) {
		  ignoredMethodNamesAndSignatures.add(methodName + methodSignature);
		}		
 	}
	
	public void markNotTrivial(){
		if (ignoredStatus.isTrivial()) {
			ignoredStatus = IgnoredStatus.NOT_IGNORED;
		}
	}
	
}
