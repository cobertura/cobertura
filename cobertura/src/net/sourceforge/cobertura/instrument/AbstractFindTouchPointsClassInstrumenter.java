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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.instrument.pass1.DetectDuplicatedCodeClassVisitor;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;


/**
 * Class extending {@link ClassAdapter} that provides features used by all three passes of instrumentation. 
 * 
 * @author piotr.tabor@gmail.com
 */
public class AbstractFindTouchPointsClassInstrumenter extends ClassAdapter{
	/**
	 * List of patterns to know that we don't want trace lines that are calls to some methods
	 */
	private Collection<Pattern> ignoreRegexp;
	
	/**
	 * We assign 'unique event identifiers' to every asm instruction or directive found in the file. Using the identifiers
	 * we are able to distinguish if the instruction is the same as found in the other pass of instrumentation.
	 * 
	 * We will use this 'generator' to provide this identifiers. Remember to acquire identifiers using {@link AtomicInteger#incrementAndGet()} (not {@link AtomicInteger#getAndIncrement()}!!!)
	 */
	protected AtomicInteger eventIdGenerator=new AtomicInteger(0);
	
	/**
	 * We need to assign a unique lineId to every found 'LINENUMBER' directive in the asm code. 
	 * 
	 * <p>Remember that there can exist such a scenario:
	 * <pre>
	 * LINENUMBER 15 L1  //assigned lineId=33
	 * ...
	 * LINENUMBER 16 L2  //assigned lineId=34
	 * ...
	 * LINENUMBER 15 L3  //assigned lineId=35
	 * </pre>
	 * This is a reason, why we are going to use this lineIds instead of just 'line number' 
	 * </p>
	 * 
	 * <p>We will use this 'generator' to provide this identifiers. Remember to acquire identifiers using {@link AtomicInteger#incrementAndGet()} (not {@link AtomicInteger#getAndIncrement()}!!!)</p> 
	 * 
	 * <p>The {@link #lineIdGenerator} that generates the same identifiers is used by: {@link DetectDuplicatedCodeClassVisitor#lineIdGenerator}</p>
	 */
	protected final AtomicInteger lineIdGenerator=new AtomicInteger(0);

	/**
	 * <p>This is a map of found duplicates of line blocks. It's   (lineNumber -> (duplicate LineId -> orygin lineId))</p>  
	 * 
	 * <p>The duplicatedLinesMap can be created by a single pass of {@link DetectDuplicatedCodeClassVisitor} (read there for reasons of duplicated detection).</p>
	 * 		 
	 * <p>The {@link #duplicatedLinesMap} is used to generate the same events Id  for events that occurs in ASM code as distinc instructions, but are reason of compilation of the same source-code (finally blocks problem).  
	 */
	protected final Map<Integer, Map<Integer, Integer>> duplicatedLinesMap;	
	
	/**
	 * @param cv                 - a listener for code-instrumentation events 
	 * @param ignoreRegexp       - list of patters of method calls that should be ignored from line-coverage-measurement 
	 * @param duplicatedLinesMap - map of found duplicates in the class. You should use {@link DetectDuplicatedCodeClassVisitor} to find the duplicated lines. 
	 */
	public AbstractFindTouchPointsClassInstrumenter(ClassVisitor cv,Collection<Pattern> ignoreRegexp,
			Map<Integer, Map<Integer, Integer>> duplicatedLinesMap) {
		super(cv);
		this.ignoreRegexp=ignoreRegexp;
		this.duplicatedLinesMap=duplicatedLinesMap;
	}
	
		
	/**
	 * Gets list of patterns to know that we don't want trace lines that are calls to some methods
	 */
	public Collection<Pattern> getIgnoreRegexp() {
		return ignoreRegexp;
	}
	
	/**
	 * Sets list of pattern to know that we don't want trace lines that are calls to some methods
	 */
	public void setIgnoreRegexp(Collection<Pattern> ignoreRegexp) {
		this.ignoreRegexp = ignoreRegexp;
	}	
    
}
