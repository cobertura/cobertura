/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2006 Mark Doliner
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

package com.example.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simple
{

	private static final Logger logger = LoggerFactory.getLogger(Simple.class)

	public int square(int x)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("x: " + x)
		}

		def result = x * x

		if (logger.isDebugEnabled())
		{
			logger.debug("result: " + result)
		}

		return result
	}

	public int f(int x)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("x: " + x)
		}

		if (x < 0)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("negative x")
			}

			return square(x)
		}
		else if ((x >= 0) && (x <= 5))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("0<=x<=5")
			}

			return x + 3
		}
		else
		{
			return 2 * x
		}
	}

	public int sum(Collection c)
	{
		def result = 0;

		for (i in c)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("value: " + i)
			}

			result += i;
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("result: " + result)
		}

		return result
	}
}