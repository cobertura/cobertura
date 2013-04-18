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

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>The {@link CodeProvider} uses {@link AtomicArrayCodeProvider} to store counters.</p>
 * 
 * This implementation is totally thread-safe, but significantly slower then {@link FastArrayCodeProvider}.
 * 
 * @author piotr.tabor@gmail.com
 */
public class AtomicArrayCodeProvider extends AbstractCodeProvider implements CodeProvider {
	/**
	 * Type of the generated field, that is used to store counters 
	 */
	static final String COBERTURA_COUNTERS_FIELD_TYPE = Type.getType(AtomicIntegerArray.class).toString();
	
	public void generateCountersField(ClassVisitor cv) {
		FieldVisitor fv=cv.visitField(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL|Opcodes.ACC_TRANSIENT, 
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE, null, null);
		fv.visitEnd();		
	}
	
	public void generateCINITmethod(MethodVisitor mv,String className,int counters_cnt){
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, l1);

		mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(AtomicIntegerArray.class));
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(counters_cnt);
	    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
	    		Type.getInternalName(AtomicIntegerArray.class), "<init>", "(I)V");
	    mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
	    		COBERTURA_COUNTERS_FIELD_NAME,
	    		COBERTURA_COUNTERS_FIELD_TYPE);
	    generateRegisterClass(mv, className);
	    mv.visitLabel(l1);
	}
		
	public void generateCodeThatIncrementsCoberturaCounter(MethodVisitor nextMethodVisitor, Integer counterId,String className) {
		/*cobertura_counters.incrementAndGet(i);*/
		/*cobertura_counters.*/nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC,  className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/nextMethodVisitor.visitLdcInsn((int)counterId);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Type.getInternalName(AtomicIntegerArray.class), "incrementAndGet", "(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);
	}
	
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex, String className) {
		/*cobertura_counters.incrementAndGet(value('lastJumpIdVariableIndex'));*/		
		/*cobertura_counters.*/nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Type.getInternalName(AtomicIntegerArray.class), "incrementAndGet", "(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);
	}	
	
	/**
	 * <pre>
	  int[] __cobertura_get_and_reset_counters() {	
	    int[] res = new int[counters.length()];
		for(int i=0; i<counters.length(); i++){
			res[i]=counters.getAndSet(i, 0);
		}
		return res;
	  }
		</pre>
	 */
	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv, String className){
      MethodVisitor mv = cv.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
				COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME, 
				"()[I",
				null,null);
      
      mv.visitCode();
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicIntegerArray", "length", "()I");
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
      mv.visitVarInsn(Opcodes.ASTORE, 0);
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitVarInsn(Opcodes.ISTORE, 1);
      Label l3 = new Label();
      mv.visitJumpInsn(Opcodes.GOTO, l3);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitVarInsn(Opcodes.ILOAD, 1);
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
      mv.visitVarInsn(Opcodes.ILOAD, 1);
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicIntegerArray", "getAndSet", "(II)I");
      mv.visitInsn(Opcodes.IASTORE);
      mv.visitIincInsn(1, 1);
      mv.visitLabel(l3);
      mv.visitVarInsn(Opcodes.ILOAD, 1);
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicIntegerArray", "length", "()I");
      mv.visitJumpInsn(Opcodes.IF_ICMPLT, l4);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitInsn(Opcodes.ARETURN);
      mv.visitMaxs(0, 0);//will be recalculated by writer
	  mv.visitEnd();	
	}	
	
}
