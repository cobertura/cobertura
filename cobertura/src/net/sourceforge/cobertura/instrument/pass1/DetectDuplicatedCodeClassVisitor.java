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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.cobertura.instrument.AbstractFindTouchPointsClassInstrumenter;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * The same line can cause generation of many byte-code blocks connected to the same line.
 * 
 * This especially occurs in case of 'finally' blocks. For example:
 * 
 * <pre>
 * 173: public void methodWithFinishBlock(FinishReturnTypeEnum f){
 * 174:   try {
 * 175:     switch (f) {
 * 176:       case BY_RETURN:
 * 177:         System.out.println("will return");
 * 178:         return;
 * 179:	      case BY_THROW:
 * 180:	        System.out.println("will throw");
 * 181:	        throw new IllegalStateException("Expected exception");
 * 182:	      default:
 * 183:	        System.out.println("default");
 * 184:     }
 * 185:	  } finally {
 * 186:	    if (f != null) { //This piece of code is generated in ASM 3 times. We should merge it into one block
 * 187:	      System.out.println("Finish with: f="+f);
 * 188:	  }
 * 189:	}
 * 190}
 * </pre>
 * 
 * effects in generation such a JVM code:
 * 
 * <pre>
 * // access flags 1
 *   public methodWithFinishBlock(Ltest/performance/Test1$FinishReturnTypeEnum;)V
 *     TRYCATCHBLOCK L0 L1 L2 
 *     TRYCATCHBLOCK L3 L2 L2 
 *    L0
 *     LINENUMBER 175 L0
 *     INVOKESTATIC test/performance/Test1.$SWITCH_TABLE$test$performance$Test1$FinishReturnTypeEnum()[I
 *     ALOAD 1
 *     INVOKEVIRTUAL test/performance/Test1$FinishReturnTypeEnum.ordinal()I
 *     IALOAD
 *     TABLESWITCH
 *       1: L4
 *       2: L3
 *       default: L5
 *    L4
 *     LINENUMBER 177 L4
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     LDC "will return"
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *    L1
 *     LINENUMBER 186 L1
 *     ALOAD 1
 *     IFNULL L6
 *    L7
 *     LINENUMBER 187 L7
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     NEW java/lang/StringBuilder
 *     DUP
 *     LDC "Finish with: f="
 *     INVOKESPECIAL java/lang/StringBuilder.<init>(Ljava/lang/String;)V
 *     ALOAD 1
 *     INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 *     INVOKEVIRTUAL java/lang/StringBuilder.toString()Ljava/lang/String;
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *    L6
 *     LINENUMBER 178 L6
 *     RETURN
 *    L3
 *     LINENUMBER 180 L3
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     LDC "will throw"
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *    L8
 *     LINENUMBER 181 L8
 *     NEW java/lang/IllegalStateException
 *     DUP
 *     LDC "Expected exception"
 *     INVOKESPECIAL java/lang/IllegalStateException.<init>(Ljava/lang/String;)V
 *     ATHROW
 *    L5
 *     LINENUMBER 183 L5
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     LDC "default"
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *     GOTO L9
 *    L2
 *     LINENUMBER 185 L2
 *     ASTORE 2
 *    L10
 *     LINENUMBER 186 L10
 *     ALOAD 1
 *     IFNULL L11
 *    L12
 *     LINENUMBER 187 L12
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     NEW java/lang/StringBuilder
 *     DUP
 *     LDC "Finish with: f="
 *     INVOKESPECIAL java/lang/StringBuilder.<init>(Ljava/lang/String;)V
 *     ALOAD 1
 *     INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 *     INVOKEVIRTUAL java/lang/StringBuilder.toString()Ljava/lang/String;
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *    L11
 *     LINENUMBER 189 L11
 *     ALOAD 2
 *     ATHROW
 *    L9
 *     LINENUMBER 186 L9
 *     ALOAD 1
 *     IFNULL L13
 *    L14
 *     LINENUMBER 187 L14
 *     GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
 *     NEW java/lang/StringBuilder
 *     DUP
 *     LDC "Finish with: f="
 *     INVOKESPECIAL java/lang/StringBuilder.<init>(Ljava/lang/String;)V
 *     ALOAD 1
 *     INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 *     INVOKEVIRTUAL java/lang/StringBuilder.toString()Ljava/lang/String;
 *     INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
 *    L13
 *     LINENUMBER 190 L13
 *     RETURN
 *    L15
 *     LOCALVARIABLE this Ltest/performance/Test1; L0 L15 0
 *     LOCALVARIABLE f Ltest/performance/Test1$FinishReturnTypeEnum; L0 L15 1
 *     MAXSTACK = 4
 *     MAXLOCALS = 3
 * </pre>
 * 
 * <p>Note that 'LINENUMBER 186' instruction occurs many times and code after that instruction is nearly identical 
 * (see {@link CodeFootstamp} criteria of 'identity').</p>  
 * 
 * <p>On the other hand duplicated 'LINENUMBER 186' instruction could happened for for example for 'for' loop. In this      
 * case the code after this instruction is different.</p>
 * 
 * <p>The goal of this class is to provide {@link #duplicatedLinesCollector} that is map of:
 * (line number -> (duplicated lineId -> origin lineId)).</p>    
 */

public class DetectDuplicatedCodeClassVisitor extends ClassAdapter{
	/**
	 *  map of (line number -> (duplicated lineId -> origin lineId))
	 */ 
	private Map<Integer,Map<Integer,Integer>> duplicatedLinesCollector=new HashMap<Integer, Map<Integer,Integer>>();
	
	/**
	 * Name (internal asm) of currently processed class  
	 **/
	private String className;
	
	/**
	 * Every LINENUMBER instruction will have generated it's lineId. 
	 * 
	 * The generated ids must be the same as those generated by ( {@link AbstractFindTouchPointsClassInstrumenter#lineIdGenerator} ) 
	 */
	private final AtomicInteger lineIdGenerator = new AtomicInteger(0);
		
	public  DetectDuplicatedCodeClassVisitor(ClassVisitor cv) {
		super(cv);
	}
	
	@Override
	public void visit(int version, int access,
			String name, String signature, String superName, String[] interfaces)  {	
		super.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String methodName, String description, 
		    String signature, String[] exceptions) {
		MethodVisitor nestedVisitor=super.visitMethod(access, methodName, description, 
		    signature, exceptions);
		return new DetectDuplicatedCodeMethodVisitor(nestedVisitor, 
		    duplicatedLinesCollector, className, methodName, description, lineIdGenerator);
	}
	
	/**
	 * Returns map of (line number -> (duplicated lineId -> origin lineId))
	 */
	public Map<Integer, Map<Integer, Integer>> getDuplicatesLinesCollector() {
		return duplicatedLinesCollector;
	}
}
