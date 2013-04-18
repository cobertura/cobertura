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

package net.sourceforge.cobertura.instrument.tp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing a touch-point connected to a JUMP instruction in source-code.
 * 
 * <p>A JUMP touch-point have assigned two counters:
 * <ul>
 *   <li>TRUE - touched in case when jump condition is meet</li>
 *   <li>FALSE - touched when jump condition is not meet</li> 
 * </ul></p>
 *  
 * @author piotr.tabor@gmail.com
 */
public class JumpTouchPointDescriptor extends TouchPointDescriptor{
	private int counterIdForTrue;
	private int counterIdForFalse;
	
	public JumpTouchPointDescriptor(int eventId, int currentLine) {
		super(eventId,currentLine);
	}

	public int getCounterIdForFalse() {
		return counterIdForFalse;
	}
	
	public int getCounterIdForTrue() {
		return counterIdForTrue;
	}
	
	public void setCounterIdForFalse(int counterIdForFalse) {
		this.counterIdForFalse = counterIdForFalse;
	}
	
	public void setCounterIdForTrue(int counterIdForTrue) {
		this.counterIdForTrue = counterIdForTrue;
	}
	
	@Override
	public int assignCounters(AtomicInteger idGenerator) {
		counterIdForFalse=idGenerator.incrementAndGet();
		counterIdForTrue=idGenerator.incrementAndGet();
		return 2;
	}	
	
}
