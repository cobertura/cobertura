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

import net.sourceforge.cobertura.coveragedata.LineData;

/**
 * Class representing a touch-point connected to a single line of source-code
 * 
 * <p>A LINE touch-point have assigned only one counter.</p>
 * 
 * <p>We also storing a {@link #methodName} and a {@link #methodSignature} (consider to move this fields into {@link TouchPointDescriptor}).
 * Those fields are needed to properly create instance of {@link LineData}. </p> 
 *    
 * @author piotr.tabor@gmail.com
 */
public class LineTouchPointDescriptor extends TouchPointDescriptor{	
	private Integer counterId;
	
	/**
	 * Name of a method, the line belongs to
	 */
	private String methodName;
	
	/**
	 * Signature (description) of a method, the line belongs to. 
	 */
	private String methodSignature;
	
	public LineTouchPointDescriptor(int eventId, int lineNumber,String methodName,String methodSignature) {
		super(eventId, lineNumber);
		this.methodName=methodName;
		this.methodSignature=methodSignature;
	}

	@Override
	public int assignCounters(AtomicInteger idGenerator) {
		counterId=idGenerator.incrementAndGet();
		return 1;
	}
		
	public Integer getCounterId() {
		return counterId;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getMethodSignature() {
		return methodSignature;
	}
}
